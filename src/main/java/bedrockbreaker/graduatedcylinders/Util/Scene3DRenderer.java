package bedrockbreaker.graduatedcylinders.Util;

import java.awt.Rectangle;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import bedrockbreaker.graduatedcylinders.GraduatedCylinders;
import bedrockbreaker.graduatedcylinders.Util.FluidHelper.TransferrableFluidResult;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

// Shamelessly modified from EnderIO
// Mostly from https://github.com/SleepyTrousers/EnderIO/blob/release/1.12.2/enderio-base/src/main/java/crazypants/enderio/base/gui/IoConfigRenderer.java
// Used accordingly under its CC0 license
@SideOnly(Side.CLIENT)
@EventBusSubscriber(modid = GraduatedCylinders.MODID, value = Side.CLIENT)
public class Scene3DRenderer {

	// Scene
	private Minecraft mc = Minecraft.getMinecraft();
	private World world = mc.player.world;
	private BlockPos blockOrigin;
	private ArrayList<BlockPos> neighbors = new ArrayList<>();
	private boolean isDragging = false;
	private boolean renderNeighbors = true;
	private long initTime = 0; // Timestamp in miliseconds since scene was initialized
	private Vec3d origin = new Vec3d(); // this.blockOrigin + .5

	// Camera
	private double pitch = 0; // degrees
	private double yaw = 0; // degrees
	private double distance = 5; // blocks from origin to camera
	private Vec3d cameraPosition = new Vec3d();
	private Matrix4d pitchRotation = new Matrix4d();
	private Matrix4d yawRotation = new Matrix4d();
	private Matrix4d viewMatrix;
	private Matrix4d transposeViewMatrix;
	private Matrix4d inverseViewMatrix;
	private Matrix4d projectionMatrix;
	private Matrix4d transposeProjectionMatrix;
	private Matrix4d inverseProjectionMatrix;
	private Rectangle viewport;

	// Misc
	public EnumFacing hoveredFace;
	public EnumFacing selectedFace;
	public ArrayList<TransferrableFluidResult> allowedFaces;
	private static final FloatBuffer MATRIX_BUFFER = GLAllocation.createDirectFloatBuffer(16);
	private static TextureAtlasSprite hoveredFaceSprite;
	private static TextureAtlasSprite selectedFaceSprite;
	private static TextureAtlasSprite blockedFaceSprite;

	public Scene3DRenderer(BlockPos blockOrigin, ArrayList<TransferrableFluidResult> allowedFaces) {
		this.world = mc.player.world;
		this.blockOrigin = blockOrigin;
		this.allowedFaces = allowedFaces;
		for (EnumFacing face : EnumFacing.values()) {
			this.neighbors.add(blockOrigin.offset(face));
		}
		this.origin.set(blockOrigin).add(.5);

		this.pitch = -mc.player.rotationPitch;
		this.yaw = 180 - mc.player.rotationYaw;
		this.pitchRotation.setIdentity();
		this.yawRotation.setIdentity();
	}

	@SubscribeEvent
	public static void registerSprites(TextureStitchEvent.Pre event) {
		Scene3DRenderer.hoveredFaceSprite = event.getMap().registerSprite(new ResourceLocation(GraduatedCylinders.MODID, "overlay/hovered_face"));
		Scene3DRenderer.selectedFaceSprite = event.getMap().registerSprite(new ResourceLocation(GraduatedCylinders.MODID, "overlay/selected_face"));
		Scene3DRenderer.blockedFaceSprite = event.getMap().registerSprite(new ResourceLocation(GraduatedCylinders.MODID, "overlay/blocked_face"));
	}

	public void init() {
		this.initTime = System.currentTimeMillis();
	}

	public void drawScreen(float partialTick, Rectangle viewport) {
		// TODO: make is so the camera rotates around the block origin instead of (0,0,0)
		// Some blocks have a TESR which renders based on the tile entity coordinates, not the given coordinates from the parameters (i.e. Astral Sorcery light well)
		// TODO: Figure out why some blocks cause some of the scene to be dark (i.e. the AS light well causing the overlays and its starlight fluid plane to render darker than normal)
		// In the above example, having an item in the well will cause the fluid plane to render normally, but the overlays still renders too dark
		// This bug also seems somewhat directional. Different relative arrangements of wells and other blocks (with or without this mod's support) will cause the overlays to either be dark or normal.
		// I literally spent a week trying to fix both these bugs, but ultimately gave up.
		if (!this.updateCamera(partialTick, viewport)) return;
		this.applyCamera(partialTick);
		this.renderScene(partialTick);
		this.renderOverlays();
		this.cleanUpGlState();
	}

	public void handleMouseinput() {
		if (Mouse.getEventButton() == 0) this.isDragging = Mouse.getEventButtonState();

		if (this.isDragging) {
			double dx = Mouse.getEventDX() / (double) this.mc.displayWidth;
			double dy = Mouse.getEventDY() / (double) this.mc.displayHeight;
			if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
				this.distance = MathHelper.clamp(this.distance - dy * 15, .01, 200);
			} else {
				this.yaw -= 4 * dx * 180;
				this.pitch = MathHelper.clamp(this.pitch + 2 * dy * 180, -80, 80); // Limit to +-80 degrees, to avoid gimbal lock
			}
		}

		// Mouse Over
		if (this.viewMatrix != null && this.projectionMatrix != null && this.viewport != null) {
			if (this.inverseProjectionMatrix == null) this.inverseProjectionMatrix = new Matrix4d(this.projectionMatrix).invert();
			if (this.inverseViewMatrix == null) this.inverseViewMatrix = new Matrix4d(this.viewMatrix).invert();
			Matrix4d inverseMatrix = Matrix4d.multiply(this.inverseViewMatrix, this.inverseProjectionMatrix);

			// Calculate the mouse's pixel location in screen clip space (width/height range -1 to 1)
			double clipX = (Mouse.getEventX() - this.viewport.getX()) / this.viewport.getWidth() * 2.0 - 1.0;
			double clipY = (Mouse.getEventY() - this.viewport.getY()) / this.viewport.getHeight() * 2.0 - 1.0;

			// Calculate XYZ coords of the clip point on the near and far frustum depth planes
			double nearW = clipX * inverseMatrix.m30 + clipY * inverseMatrix.m31 - inverseMatrix.m32 + inverseMatrix.m33;
			Vec3d nearXYZ = new Vec3d(
				clipX * inverseMatrix.m00 + clipY * inverseMatrix.m01 - inverseMatrix.m02 + inverseMatrix.m03,
				clipX * inverseMatrix.m10 + clipY * inverseMatrix.m11 - inverseMatrix.m12 + inverseMatrix.m13,
				clipX * inverseMatrix.m20 + clipY * inverseMatrix.m21 - inverseMatrix.m22 + inverseMatrix.m23
			).scale(1 / nearW);
			double farW = clipX * inverseMatrix.m30 + clipY * inverseMatrix.m31 + inverseMatrix.m32 + inverseMatrix.m33;
			Vec3d farXYZ = new Vec3d(
				clipX * inverseMatrix.m00 + clipY * inverseMatrix.m01 + inverseMatrix.m02 + inverseMatrix.m03,
				clipX * inverseMatrix.m10 + clipY * inverseMatrix.m11 + inverseMatrix.m12 + inverseMatrix.m13,
				clipX * inverseMatrix.m20 + clipY * inverseMatrix.m21 + inverseMatrix.m22 + inverseMatrix.m23
			).scale(1 / farW);

			// Start and end positions of raytrace
			Vec3d start = this.inverseViewMatrix.getTranslationVector();
			Vec3d end = new Vec3d(farXYZ).sub(nearXYZ).normalize().scale(this.distance * 2).add(start);
			
			this.updateHoveredFace(start, end);
		}

		// Mouse Click
		long timeSinceInit = System.currentTimeMillis() - this.initTime;
		if (Mouse.getEventButton() == -1 || Mouse.getEventButtonState() || timeSinceInit < 500 || this.viewMatrix == null || this.projectionMatrix == null || this.viewport == null) return;
		if (Mouse.getEventButton() == 1) { // right click
			if (this.hoveredFace != null && (this.allowedFaces.get(this.hoveredFace.getIndex()).canExport || this.allowedFaces.get(this.hoveredFace.getIndex()).canImport)) this.selectedFace = this.hoveredFace;
		} else if (Mouse.getEventButton() == 2) { // middle click
			this.renderNeighbors = !this.renderNeighbors;
		}
	}

	public EnumFacing getHoveredFace() {
		return this.hoveredFace;
	}

	private void updateHoveredFace(Vec3d start, Vec3d end) {
		this.hoveredFace = null;
		if (world.isAirBlock(this.blockOrigin)) return;
		start.add(this.origin);
		end.add(this.origin);
		IBlockState blockState = world.getBlockState(blockOrigin);
		RayTraceResult hit = blockState.collisionRayTrace(world, blockOrigin, new net.minecraft.util.math.Vec3d(start.x, start.y, start.z), new net.minecraft.util.math.Vec3d(end.x, end.y, end.z));
		if (hit == null || hit.typeOfHit == RayTraceResult.Type.MISS) return;
		this.hoveredFace = hit.sideHit;
	}

	private boolean updateCamera(float partialTick, Rectangle viewport) {
		if (viewport.width <= 0 || viewport.height <= 0) return false;
		this.viewport = viewport;

		// Compute the projection matrix (view space -> normalized 3D clip space)
		// For magic numbers explanation, see (I did some simplifying of the math):
		// https://github.com/SleepyTrousers/EnderIO/blob/release/1.12.2/enderio-base/src/main/java/crazypants/enderio/base/gui/IoConfigRenderer.java#L539
		// https://github.com/SleepyTrousers/EnderCore/blob/1.12/src/main/java/com/enderio/core/common/vecmath/VecmathUtil.java#L335
		if (this.projectionMatrix == null) this.projectionMatrix = new Matrix4d();
		this.projectionMatrix.set(new double[] {3.7320508075688776d * viewport.height / viewport.width, 0, 0, 0, 0, 3.7320508075688776d, 0, 0, 0, 0, -1.002002002002002d, -0.10010010010010009d, 0, 0, -1, 0});
		this.transposeProjectionMatrix = new Matrix4d(this.projectionMatrix).transpose();
		this.inverseProjectionMatrix = null;

		// Camera position
		this.cameraPosition = new Vec3d(0, 0, this.distance);
		this.pitchRotation.setToRotationX(Math.toRadians(pitch)).transform(this.cameraPosition);
		this.yawRotation.setToRotationY(Math.toRadians(yaw)).transform(this.cameraPosition);

		// Compute the view matrix (world space -> view space)
		Vec3d forward = new Vec3d().sub(this.cameraPosition).normalize();
		Vec3d left = Vec3d.cross(forward, new Vec3d(0, 1, 0)).normalize();
		Vec3d up = Vec3d.cross(left, forward).normalize();
		if (this.viewMatrix == null) this.viewMatrix = new Matrix4d();
		this.viewMatrix.set(new double[] {
			left.x,		left.y,		left.z,		0,
			up.x,		up.y,		up.z,		0,
			-forward.x,	-forward.y,	-forward.z,	0,
			0,			0,			0,			1
		});
		Vec3d cameraPosTemp = new Vec3d(this.cameraPosition);
		this.viewMatrix.transformNormal(cameraPosTemp.scale(-1));
		this.viewMatrix.setTranslation(cameraPosTemp);
		this.transposeViewMatrix = new Matrix4d(this.viewMatrix).transpose();
		this.inverseViewMatrix = null;

		// TODO: this probably always returns true
		return this.viewMatrix != null && this.projectionMatrix != null && this.viewport != null;
	}

	private void applyCamera(float partialTick) {
		GL11.glViewport(this.viewport.x, this.viewport.y, this.viewport.width, this.viewport.height);
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GlStateManager.pushMatrix();
		if (this.transposeProjectionMatrix != null) this.loadMatrix(this.transposeProjectionMatrix);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GlStateManager.pushMatrix();
		if (this.transposeViewMatrix != null) this.loadMatrix(this.transposeViewMatrix);
		GL11.glTranslatef((float) -this.cameraPosition.x, (float) -this.cameraPosition.y, (float) -this.cameraPosition.z);
	}

	private void loadMatrix(Matrix4d matrix) {
		MATRIX_BUFFER.rewind();
		MATRIX_BUFFER.put((float) matrix.m00);
		MATRIX_BUFFER.put((float) matrix.m01);
		MATRIX_BUFFER.put((float) matrix.m02);
		MATRIX_BUFFER.put((float) matrix.m03);
		MATRIX_BUFFER.put((float) matrix.m10);
		MATRIX_BUFFER.put((float) matrix.m11);
		MATRIX_BUFFER.put((float) matrix.m12);
		MATRIX_BUFFER.put((float) matrix.m13);
		MATRIX_BUFFER.put((float) matrix.m20);
		MATRIX_BUFFER.put((float) matrix.m21);
		MATRIX_BUFFER.put((float) matrix.m22);
		MATRIX_BUFFER.put((float) matrix.m23);
		MATRIX_BUFFER.put((float) matrix.m30);
		MATRIX_BUFFER.put((float) matrix.m31);
		MATRIX_BUFFER.put((float) matrix.m32);
		MATRIX_BUFFER.put((float) matrix.m33);
		MATRIX_BUFFER.rewind();
		GL11.glLoadMatrix(MATRIX_BUFFER);
	}

	private void renderScene(float partialTick) {
		GlStateManager.enableCull();
		GlStateManager.enableRescaleNormal();
		RenderHelper.disableStandardItemLighting();
		GlStateManager.disableLighting();
		this.mc.entityRenderer.disableLightmap();
		this.mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		GlStateManager.enableTexture2D();
		GlStateManager.enableAlpha();

		Vec3d translation = new Vec3d(this.cameraPosition).sub(this.origin);
		BlockRenderLayer oldRenderLayer = MinecraftForgeClient.getRenderLayer();
		try {
			for (BlockRenderLayer layer : BlockRenderLayer.values()) {
				ForgeHooksClient.setRenderLayer(layer);
				this.setGlStateForPass(layer, false);
				this.doWorldRenderPass(translation, this.blockOrigin, layer);
			}

			if (renderNeighbors) {
				for (BlockRenderLayer layer : BlockRenderLayer.values()) {
					ForgeHooksClient.setRenderLayer(layer);
					this.setGlStateForPass(layer, true);
					this.doWorldRenderPass(translation, this.neighbors, layer);
				}
			}
		} finally {
			ForgeHooksClient.setRenderLayer(oldRenderLayer);
		}

		RenderHelper.enableStandardItemLighting();
		TileEntityRendererDispatcher.instance.entityX = this.origin.x - this.cameraPosition.x;
		TileEntityRendererDispatcher.instance.entityY = this.origin.y - this.cameraPosition.y;
		TileEntityRendererDispatcher.instance.entityZ = this.origin.z - this.cameraPosition.z;
		TileEntityRendererDispatcher.staticPlayerX = this.origin.x - this.cameraPosition.x;
		TileEntityRendererDispatcher.staticPlayerY = this.origin.y - this.cameraPosition.y;
		TileEntityRendererDispatcher.staticPlayerZ = this.origin.z - this.cameraPosition.z;

		for (int pass = 0; pass < 2; pass++) {
			ForgeHooksClient.setRenderPass(pass);
			this.setGlStateForPass(pass, false);
			this.doTileEntityRenderPass(this.blockOrigin, pass, partialTick);
			if (this.renderNeighbors) {
				this.setGlStateForPass(pass, true);
				this.doTileEntityRenderPass(this.neighbors, pass, partialTick);
			}
		}
		ForgeHooksClient.setRenderPass(-1);
		this.setGlStateForPass(0, false);
	}

	private void setGlStateForPass(BlockRenderLayer layer, boolean isNeighbor) {
		this.setGlStateForPass(layer == BlockRenderLayer.TRANSLUCENT ? 1 : 0, isNeighbor);
	}

	private void setGlStateForPass(int layer, boolean isNeighbor) {
		GlStateManager.color(1, 1, 1, 1);
		if (isNeighbor) {
			GlStateManager.enableDepth();
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_CONSTANT_COLOR);
			GL14.glBlendColor(1, 1, 1, 1);
			return;
		}

		if (layer == 0) {
			GlStateManager.enableDepth();
			GlStateManager.disableBlend();
			GlStateManager.depthMask(true);
		} else {
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GlStateManager.depthMask(false);
		}
	}

	private void doWorldRenderPass(Vec3d translation, ArrayList<BlockPos> blocks, BlockRenderLayer layer) {
		BufferBuilder buffer = Tessellator.getInstance().getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		buffer.setTranslation(translation.x, translation.y, translation.z);

		for (BlockPos pos : blocks) {
			IBlockState blockState = this.world.getBlockState(pos);
			Block block = blockState.getBlock();
			blockState = blockState.getActualState(this.world, pos);
			if (!block.canRenderInLayer(blockState, layer)) continue;
			this.renderBlock(blockState, pos, this.world, buffer);
		}

		Tessellator.getInstance().draw();
		buffer.setTranslation(0, 0, 0);
	}

	private void doWorldRenderPass(Vec3d translation, BlockPos blockPos, BlockRenderLayer layer) {
		ArrayList<BlockPos> list = new ArrayList<BlockPos>();
		list.add(blockPos);
		this.doWorldRenderPass(translation, list, layer);
	}

	private void renderBlock(IBlockState blockState, BlockPos blockPos, IBlockAccess blockAccess, BufferBuilder buffer) {
		try {
			BlockRendererDispatcher blockRendererDispatcher = this.mc.getBlockRendererDispatcher();
			EnumBlockRenderType blockRenderType = blockState.getRenderType();
			if (blockRenderType != EnumBlockRenderType.MODEL) {
				blockRendererDispatcher.renderBlock(blockState, blockPos, blockAccess, buffer);
				return;
			}

			IBakedModel model = blockRendererDispatcher.getModelForState(blockState);
			blockState = blockState.getBlock().getExtendedState(blockState, blockAccess, blockPos);
			blockRendererDispatcher.getBlockModelRenderer().renderModel(blockAccess, model, blockState, blockPos, buffer, false);
		} catch (Throwable error) {
			// Eat the error -- don't care.
		}
	}

	private void doTileEntityRenderPass(ArrayList<BlockPos> blocks, int pass, float partialTick) {
		for (BlockPos pos : blocks) {
			TileEntity tileEntity = this.world.getTileEntity(pos);
			if (tileEntity == null) continue;
			Vec3d renderPos = new Vec3d(this.cameraPosition).add(pos).sub(this.origin);
			if (tileEntity.getClass() == TileEntityChest.class) {
				TileEntityChest chest = (TileEntityChest) tileEntity;
				if (chest.adjacentChestXNeg != null) {
					tileEntity = chest.adjacentChestXNeg;
					renderPos.x--;
				} else if (chest.adjacentChestZNeg != null) {
					tileEntity = chest.adjacentChestZNeg;
					renderPos.z--;
				}
			}
			TileEntityRendererDispatcher.instance.render(tileEntity, renderPos.x, renderPos.y, renderPos.z, partialTick);
		}
	}

	private void doTileEntityRenderPass(BlockPos blockPos, int pass, float partialTick) {
		ArrayList<BlockPos> list = new ArrayList<BlockPos>();
		list.add(blockPos);
		this.doTileEntityRenderPass(list, pass, partialTick);
	}

	private void renderOverlays() {
		if (this.selectedFace == null) return;

		GlStateManager.disableDepth();
		RenderHelper.disableStandardItemLighting();
		GlStateManager.color(1, 1, 1, 1);
		this.mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		
		BufferBuilder buffer = Tessellator.getInstance().getBuffer();
		Vec3d translation = new Vec3d(this.cameraPosition).sub(this.origin);
		buffer.setTranslation(translation.x, translation.y, translation.z);
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		this.drawOverlay(buffer, blockOrigin, this.selectedFace, Scene3DRenderer.selectedFaceSprite, true);
		if (this.hoveredFace != null) this.drawOverlay(buffer, blockOrigin, this.hoveredFace, this.allowedFaces.get(this.hoveredFace.getIndex()).canExport || this.allowedFaces.get(this.hoveredFace.getIndex()).canImport ? Scene3DRenderer.hoveredFaceSprite : Scene3DRenderer.blockedFaceSprite, false);		
		Tessellator.getInstance().draw();
		buffer.setTranslation(0, 0, 0);
	}

	private void drawOverlay(BufferBuilder buffer, BlockPos blockPos, EnumFacing face, TextureAtlasSprite overlay, boolean drawInverse) {
		double[] texelData = this.getPositionsAndUVForFace(blockPos, face, overlay.getMinU(), overlay.getMaxU(), overlay.getMinV(), overlay.getMaxV());
		for (int i = 0; i < 4; i++) {
			buffer.pos(texelData[i*5], texelData[i*5+1], texelData[i*5+2]).tex(texelData[i*5+3], texelData[i*5+4]).endVertex();
		}
		if (drawInverse) { // Render inwards facing selection, seen through blocks in front of it
			for (int i = 3; i >= 0; i--) {
				buffer.pos(texelData[i*5], texelData[i*5+1], texelData[i*5+2]).tex(texelData[i*5+3], texelData[i*5+4]).endVertex();
			}
		}
	}

	// Returns the vertices of the corners for the specified position and face in counter clockwise order.
	private double[] getPositionsAndUVForFace(BlockPos pos, EnumFacing face, float minU, float maxU, float minV, float maxV) {
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();
		switch(face) {
			case NORTH:
				return new double[] {x + 1, y, z, minU, minV, x, y, z, maxU, minV, x, y + 1, z, maxU, maxV, x + 1, y + 1, z, minU, maxV};
			case SOUTH:
				return new double[] {x, y, z + 1, maxU, minV, x + 1, y, z + 1, minU, minV, x + 1, y + 1, z + 1, minU, maxV, x, y + 1, z + 1, maxU, maxV};
			case EAST:
				return new double[] {x + 1, y + 1, z, maxU, maxV, x + 1, y + 1, z + 1, minU, maxV, x + 1, y, z + 1, minU, minV, x + 1, y, z, maxU, minV};
			case WEST:
				return new double[] {x, y, z, maxU, minV, x, y, z + 1, minU, minV, x, y + 1, z + 1, minU, maxV, x, y + 1, z, maxU, maxV};
			case UP:
				return new double[] {x + 1, y + 1, z + 1, minU, minV, x + 1, y + 1, z, minU, maxV, x, y + 1, z, maxU, maxV, x, y + 1, z + 1, maxU, minV};
			case DOWN:
				return new double[] {x, y, z, maxU, maxV, x + 1, y, z, minU, maxV, x + 1, y, z + 1, minU, minV, x, y, z + 1, maxU, minV};
		}
		return null;
	}

	private void cleanUpGlState() {
		RenderHelper.enableStandardItemLighting();
		GL11.glViewport(0, 0, this.mc.displayWidth, this.mc.displayHeight);
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GlStateManager.popMatrix();
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GlStateManager.popMatrix();
	}
}
