package bedrockbreaker.graduatedcylinders;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import com.google.common.primitives.Ints;

import bedrockbreaker.graduatedcylinders.api.IHandlerMode;
import bedrockbreaker.graduatedcylinders.api.IProxyFluidHandler;
import bedrockbreaker.graduatedcylinders.api.IProxyFluidHandlerItem;
import bedrockbreaker.graduatedcylinders.api.IProxyFluidStack;
import bedrockbreaker.graduatedcylinders.api.MetaHandler;
import bedrockbreaker.graduatedcylinders.network.PacketBlockTransferFluid;
import bedrockbreaker.graduatedcylinders.network.PacketHandler;
import bedrockbreaker.graduatedcylinders.util.BlockPos;
import bedrockbreaker.graduatedcylinders.util.ColorCache;
import bedrockbreaker.graduatedcylinders.util.FluidHelper;
import bedrockbreaker.graduatedcylinders.util.FluidHelper.TransferrableFluidResult;
import bedrockbreaker.graduatedcylinders.util.GuiFluidSprite;
import bedrockbreaker.graduatedcylinders.util.MathHelper;
import bedrockbreaker.graduatedcylinders.util.Scene3DRenderer;
import bedrockbreaker.graduatedcylinders.util.TextFormatting;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

@SideOnly(Side.CLIENT)
public class FluidTransferGui extends GuiScreen {

	public boolean initialized = false;

	private final World world;
	public final BlockPos pos;

	public final MetaHandler metaHandler;
	public final ItemStack heldItem;
	private final IProxyFluidHandlerItem heldFluidHandler; // Based on client knowledge. Inventory desyncs are likely to cause funky issues if client fluidstacks are used
	private IProxyFluidHandler blockFluidHandler; // Based on client knowledge. May not contain any valid fluidstacks (e.g GregTech which doesn't send client updates)
	public ForgeDirection selectedFace;
	public int heldTankIndex;
	public int blockTankIndex;

	// Fluidstacks and transfer results are requested from the server, due to comments above ^^
	private ArrayList<IProxyFluidStack> heldFluidStacks;
	private ArrayList<ArrayList<IProxyFluidStack>> sidedBlockFluidStacks;
	private ArrayList<ArrayList<TransferrableFluidResult>> sidedTransferResults;

	private ArrayList<IProxyFluidStack> blockFluidStacks = new ArrayList<IProxyFluidStack>();
	private HashMap<Pair<Integer, Integer>, TransferrableFluidResult> transferResults = new HashMap<Pair<Integer, Integer>, TransferrableFluidResult>();
	private ArrayList<TransferrableFluidResult> allowedFaces = new ArrayList<TransferrableFluidResult>(); // Array of length 6 (for each face of a block)
	private ArrayList<GuiFluidSprite> heldFluidSprites = new ArrayList<GuiFluidSprite>();
	private ArrayList<GuiFluidSprite> blockFluidSprites = new ArrayList<GuiFluidSprite>();
	private GuiFluidSprite hoveredSprite;
	private int numTransferrableHeldFluids;
	private int numTransferrableBlockFluids;

	public IHandlerMode mode;
	public int[] deltas;
	public ArrayList<String> deltaStrings = new ArrayList<String>();
	
	public int maxAmount;
	public boolean transferDirectionForced;
	public boolean isExporting;
	public int amount = 0;

	private Scene3DRenderer sceneRenderer;
	private int buttonId = 0;
	private GuiTextField textAmount;
	private int instructionsWidth = 0;
	private GuiButton incFluidButton;
	private GuiButton decFluidButton;
	private GuiButton exportButton;
	private GuiButton modeButton;

	public static void open(ItemStack heldItem, BlockPos pos, ArrayList<ArrayList<TransferrableFluidResult>> sidedTransferResults, int heldTankIndex, int side, int blockTankIndex, ArrayList<IProxyFluidStack> heldFluidStacks, ArrayList<ArrayList<IProxyFluidStack>> sidedBlockFluidStacks) {
		Minecraft.getMinecraft().displayGuiScreen(new FluidTransferGui(heldItem, pos, sidedTransferResults, heldTankIndex, side, blockTankIndex, heldFluidStacks, sidedBlockFluidStacks));
	}

	public FluidTransferGui(ItemStack heldItem, BlockPos pos, ArrayList<ArrayList<TransferrableFluidResult>> sidedTransferResults, int heldTankIndex, int side, int blockTankIndex, ArrayList<IProxyFluidStack> heldFluidStacks, ArrayList<ArrayList<IProxyFluidStack>> sidedBlockFluidStacks) {
		super();

		this.world = Minecraft.getMinecraft().theWorld;
		this.pos = pos;
		this.heldItem = heldItem;
		this.metaHandler = FluidHelper.getMetaHandler(heldItem);
		this.heldFluidHandler = this.metaHandler.getHandler(heldItem);
		this.heldFluidStacks = heldFluidStacks;

		this.sidedBlockFluidStacks = sidedBlockFluidStacks;
		this.sidedTransferResults = sidedTransferResults;

		this.updateCaches(heldTankIndex, side, blockTankIndex);
		for (ArrayList<TransferrableFluidResult> transferResults : sidedTransferResults) {
			TransferrableFluidResult indices = new TransferrableFluidResult(0, 0, false, false);
			for (TransferrableFluidResult transferResult : transferResults) {
				if ((!indices.canTransfer() && transferResult.canTransfer()) || ((indices.canExport ^ indices.canImport) && transferResult.canExport && transferResult.canImport)) indices = transferResult;
				if (transferResult.canExport && transferResult.canImport) break;
			}
			this.allowedFaces.add(indices);
		}

		this.sceneRenderer = new Scene3DRenderer(pos, this.allowedFaces);
		this.sceneRenderer.selectedFace = ForgeDirection.getOrientation(side);
		this.initialized = true;
	}

	public void updateCaches(int heldTankIndex, int side, int blockTankIndex) {
		this.selectedFace = ForgeDirection.getOrientation(side);
		this.blockFluidHandler = FluidHelper.getMatchingProxyFluidHandler(world, pos, ForgeDirection.getOrientation(side), this.heldFluidHandler);
		this.heldTankIndex = heldTankIndex;
		this.blockTankIndex = blockTankIndex;
		
		this.transferResults.clear();
		this.blockFluidStacks.clear();
		this.heldFluidSprites.clear();
		this.blockFluidSprites.clear();

		for (IProxyFluidStack fluidStack : this.sidedBlockFluidStacks.get(side)) {
			this.blockFluidStacks.add(fluidStack);
		}

		for (TransferrableFluidResult transferResult : this.sidedTransferResults.get(side)) {
			this.transferResults.put(Pair.of(transferResult.sourceTank, transferResult.destinationTank), transferResult);
		}
		
		int j = 0;
		for (int i = 0; i < this.blockFluidStacks.size(); i++) {
			IProxyFluidStack fluidStack = this.blockFluidStacks.get(i);
			this.blockFluidSprites.add(fluidStack == null ? null : new GuiFluidSprite(fluidStack, 0, 0, 16, 16, i, j++));
		}
		j = 0;
		for (int i = 0; i < this.heldFluidStacks.size(); i++) {
			IProxyFluidStack fluidStack = this.heldFluidStacks.get(i);
			this.heldFluidSprites.add(fluidStack == null ? null : new GuiFluidSprite(fluidStack, 0, 0, 16, 16, i, j++));
		}
	}

	protected boolean changeSelectedFace(ForgeDirection face) {
		int side = face.ordinal();
		if (!this.allowedFaces.get(side).canTransfer()) return false;
		this.updateCaches(this.allowedFaces.get(side).sourceTank, side, this.allowedFaces.get(side).destinationTank);
		this.applyToSprites((GuiFluidSprite sprite, boolean isHeldSprite, boolean isSelectedSprite, int tankIndex) -> {
			sprite.setPos(this.width / 2 - 18, this.height / 2 - (this.isExporting ^ isHeldSprite ? 44 : 76));
			if (this.isExporting ^ isHeldSprite) sprite.setOpacity(0);
		});
		this.initFluidSprites(false);
		this.selectFluid(Pair.of(this.allowedFaces.get(side).sourceTank, this.allowedFaces.get(side).destinationTank), 0, this.isExporting, true);
		return true;
	}

	protected void applyToSprites(SpriteConsumer lambda) {
		for (int i = 0; i < this.heldFluidSprites.size(); i++) {
			GuiFluidSprite sprite = this.heldFluidSprites.get(i);
			if (sprite != null) lambda.apply(sprite, true, i == this.heldTankIndex, i);
		}
		for (int i = 0; i < this.blockFluidSprites.size(); i++) {
			GuiFluidSprite sprite = this.blockFluidSprites.get(i);
			if (sprite != null) lambda.apply(sprite, false, i == this.blockTankIndex, i);
		}
	}

	protected void toggleTransferDirection() {
		if (this.transferDirectionForced) return;
		this.isExporting = !this.isExporting;
		this.exportButton.displayString = this.isExporting ? "->" : "<-";
		
		final int centerX = this.width / 2;
		final int centerY = this.height / 2;
		this.applyToSprites((GuiFluidSprite sprite, boolean isHeldSprite, boolean isSelectedSprite, int tankIndex) -> {
			if (isSelectedSprite && sprite.fluidStack.isFluidEqual(isHeldSprite ? this.blockFluidStacks.get(this.blockTankIndex) : this.heldFluidStacks.get(this.heldTankIndex))) {
				sprite.animateMove(centerX - 26, centerY - 92);
				sprite.setOpacity(this.isExporting ^ isHeldSprite ? 0 : 255);
			} else {
				if (!(this.isExporting ^ isHeldSprite)) sprite.y = centerY - 108;
				sprite.animateMove(centerX + sprite.renderIndex * 24 + (sprite.renderIndex < 0 ? -30 : -6), centerY + (this.isExporting ^ isHeldSprite ? -44 : -76));
				sprite.animateOpacity(this.isExporting ^ isHeldSprite ? 0 : 255);
				if (isSelectedSprite) { // if the selected tanks do not have matching fluids (requires both tanks to be able to hold multiple fluids)
					// TODO: this is probably broken, but I don't know of any multi-tank items to test this with, and I'm too lazy to make my own
					sprite.startY -= 32;
					sprite.endY -= 16;
				}
			}
		});
	}

	protected void cycleMode() {
		this.mode = this.metaHandler.modes.get((this.metaHandler.modes.indexOf(this.mode) + 1) % this.metaHandler.modes.size());
		this.deltas = this.mode.getDeltas(this.amount, this.heldFluidHandler.getTankProperties(this.heldTankIndex).getCapacity(this.getWorkingFluidStack()), this.blockFluidHandler.getTankProperties(this.blockTankIndex).getCapacity(this.getWorkingFluidStack()));
		this.deltaStrings = this.mode.getStringDeltas();

		this.instructionsWidth = 0;
		this.deltaStrings.forEach(s -> this.instructionsWidth = Math.max(this.instructionsWidth, this.fontRendererObj.getStringWidth(s)));
		this.instructionsWidth = this.fontRendererObj.getStringWidth(I18n.format("gc.gui.combo6" + (Minecraft.isRunningOnMac ? ".cmd" : ""))) + this.instructionsWidth + 10;
	}

	protected void selectFluid(Pair<Integer, Integer> tankIndices, int fluidTransferAmount, boolean defaultTransferDirection, boolean doAnimation) {
		// Reset the sprites' relative render indices
		int heldTankRelativeIndexOld = this.heldFluidSprites.get(this.heldTankIndex) == null ? 0 : this.heldFluidSprites.get(this.heldTankIndex).renderIndex;
		int blockTankRelativeIndexOld = this.blockFluidSprites.get(this.blockTankIndex) == null ? 0 : this.blockFluidSprites.get(this.blockTankIndex).renderIndex;
		this.applyToSprites((GuiFluidSprite sprite, boolean isHeldSprite, boolean isSelectedSprite, int tankIndex) -> sprite.renderIndex += isHeldSprite ? heldTankRelativeIndexOld : blockTankRelativeIndexOld);
		
		// Do the stuff
		this.heldTankIndex = tankIndices.getLeft();
		this.blockTankIndex = tankIndices.getRight();
		this.allowedFaces.set(this.selectedFace.ordinal(), this.getTransferCapability());
		this.maxAmount = Math.min(this.heldFluidHandler.getTankProperties(this.heldTankIndex).getCapacity(this.getWorkingFluidStack()), this.blockFluidHandler.getTankProperties(this.blockTankIndex).getCapacity(this.getWorkingFluidStack()));
		this.transferDirectionForced = this.getTransferCapability().canExport ^ this.getTransferCapability().canImport;
		this.isExporting = this.transferDirectionForced ? this.getTransferCapability().canExport : defaultTransferDirection;
		this.exportButton.displayString = this.isExporting ? "->" : "<-";
		this.exportButton.enabled = !this.transferDirectionForced;
		this.setAmount(fluidTransferAmount);
		this.deltas = this.mode.getDeltas(this.amount, this.heldFluidHandler.getTankProperties(this.heldTankIndex).getCapacity(this.getWorkingFluidStack()), this.blockFluidHandler.getTankProperties(this.blockTankIndex).getCapacity(this.getWorkingFluidStack()));
		this.initFluidSprites(doAnimation);
	}

	protected TransferrableFluidResult getTransferCapability() {
		return this.transferResults.get(Pair.of(this.heldTankIndex, this.blockTankIndex));
	}

	protected IProxyFluidStack getWorkingFluidStack() {
		return this.getTransferCapability().canExport ? this.heldFluidStacks.get(this.heldTankIndex) : (this.getTransferCapability().canImport ? this.blockFluidStacks.get(this.blockTankIndex) : null);
	}

	@Override
	public void initGui() {
		final int centerX = this.width / 2;
		final int centerY = this.height / 2;

		this.incFluidButton = new GuiButton(buttonId++, centerX + 50, centerY - 35, 20, 20, "+");
		this.buttonList.add(this.incFluidButton);

		this.decFluidButton = new GuiButton(buttonId++, centerX + 50, centerY - 5, 20, 20, "-");
		this.buttonList.add(this.decFluidButton);

		this.exportButton = new GuiButton(buttonId++, centerX - 72, centerY + 28, 20, 20, this.isExporting ? "->" : "<-");
		this.buttonList.add(this.exportButton);

		this.modeButton = new GuiButton(buttonId++, centerX - 90, centerY - 20, 20, 20, "");
		if (this.metaHandler.modes.size() > 1) this.buttonList.add(this.modeButton);
		this.cycleMode();

		this.textAmount = new GuiTextField(this.fontRendererObj, centerX - 60, centerY - 20, 100, 20);
		this.textAmount.setText(Integer.toString(this.amount));
		this.textAmount.setMaxStringLength(10);
		this.textAmount.setFocused(true);
		this.textAmount.setCanLoseFocus(false);

		this.selectFluid(Pair.of(this.heldTankIndex, this.blockTankIndex), 0, Minecraft.getMinecraft().thePlayer.isSneaking(), false);
		this.sceneRenderer.init();
	}

	private void initFluidSprites(boolean doAnimation) {
		final int centerX = this.width / 2;
		final int centerY = this.height / 2;
		this.numTransferrableHeldFluids = 0;
		this.numTransferrableBlockFluids = 0;
		int heldTankRelativeIndex = this.heldFluidSprites.get(this.heldTankIndex) == null ? 0 : this.heldFluidSprites.get(this.heldTankIndex).renderIndex;
		int blockTankRelativeIndex = this.blockFluidSprites.get(this.blockTankIndex) == null ? 0 : this.blockFluidSprites.get(this.blockTankIndex).renderIndex;
		this.applyToSprites((GuiFluidSprite sprite, boolean isHeldSprite, boolean isSelectedSprite, int tankIndex) -> {
			sprite.renderIndex -= isHeldSprite ? heldTankRelativeIndex : blockTankRelativeIndex;
			if (isSelectedSprite) {
				sprite.animateMove(centerX - 26, centerY - 92);
				sprite.animateScale(32, 32);
				sprite.animateOpacity(this.isExporting ^ isHeldSprite ? 0 : 255);
			} else {
				sprite.animateMove(centerX + sprite.renderIndex * 24 + (sprite.renderIndex < 0 ? -26 : -10), centerY - (this.isExporting ^ isHeldSprite ? 44 : 76));
				sprite.animateScale(16, 16);
				sprite.animateOpacity(this.isExporting ^ isHeldSprite ? 0 : 255);
			}
			if (!doAnimation) sprite.elapsedTime = sprite.transitionLength;
			if (this.isExporting ^ isHeldSprite) return;
			TransferrableFluidResult transferResult = this.transferResults.get(Pair.of(isHeldSprite ? tankIndex : this.heldTankIndex, isHeldSprite ? this.blockTankIndex : tankIndex));
			if (transferResult.canExport) this.numTransferrableHeldFluids++;
			if (transferResult.canImport) this.numTransferrableBlockFluids++;
		});
	}

	// Called every frame (welcome to magic number jigoku)
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		Minecraft minecraft = Minecraft.getMinecraft();
		GameSettings settings = minecraft.gameSettings;
		float scale = new ScaledResolution(minecraft, minecraft.displayWidth, minecraft.displayHeight).getScaleFactor();
		int centerX = this.width / 2;
		int centerY = this.height / 2;
		String cmd = Minecraft.isRunningOnMac ? ".cmd" : "";
		int leftMargin = this.width - this.instructionsWidth;

		this.drawDefaultBackground();
		this.textAmount.drawTextBox();
		super.drawScreen(mouseX, mouseY, partialTicks);

		// Fluid amounts and current fluid name
		String displayHeldAmount = this.mode.formatAmount(this.heldFluidStacks.get(this.heldTankIndex) != null ? this.heldFluidStacks.get(this.heldTankIndex).getAmount() : 0, false);
		this.drawCenteredString(this.fontRendererObj, displayHeldAmount, centerX - 98 - Math.max(this.fontRendererObj.getStringWidth(displayHeldAmount) / 2 - 26, 0), centerY + 75, 0xAAAAAA);
		String displayBlockAmount = this.mode.formatAmount(this.blockFluidStacks.get(this.blockTankIndex) != null ? this.blockFluidStacks.get(this.blockTankIndex).getAmount() : 0, false);
		this.drawCenteredString(this.fontRendererObj, displayBlockAmount, centerX - 10 + Math.max(this.fontRendererObj.getStringWidth(displayBlockAmount) / 2 - 42, 0), centerY + 75, 0xAAAAAA);
		if (this.amount > 0) {
			int heldAmount = this.heldFluidStacks.get(this.heldTankIndex) != null ? this.heldFluidStacks.get(this.heldTankIndex).getAmount() : 0;
			int blockAmount = this.blockFluidStacks.get(this.blockTankIndex) != null ? this.blockFluidStacks.get(this.blockTankIndex).getAmount() : 0;
			int deltaMax = Math.min(this.isExporting
				? Math.min(this.blockFluidHandler.getTankProperties(this.blockTankIndex).getCapacity(this.getWorkingFluidStack()) - blockAmount, heldAmount)
				: Math.min(this.heldFluidHandler.getTankProperties(this.heldTankIndex).getCapacity(this.getWorkingFluidStack()) - heldAmount, blockAmount), this.amount);
			String displayHeldAmountNew = this.mode.formatAmount(heldAmount + (this.isExporting ? -deltaMax : deltaMax), false);
			this.drawCenteredString(this.fontRendererObj, displayHeldAmountNew, centerX - 98 - Math.max(this.fontRendererObj.getStringWidth(displayHeldAmountNew) / 2 - 26, 0), centerY + 90, this.isExporting ? 0xAA0000 : 0x00AA00); // §4 dark_red : §2 dark_green
			String displayTankAmountNew = this.mode.formatAmount(blockAmount + (this.isExporting ? deltaMax : -deltaMax), false);
			this.drawCenteredString(this.fontRendererObj, displayTankAmountNew, centerX - 10 + Math.max(this.fontRendererObj.getStringWidth(displayTankAmountNew) / 2 - 42, 0), centerY + 90, this.isExporting ? 0x00AA00 : 0xAA0000); // §4 dark_green : §2 dark_red
		}
		String displayAmount = this.mode.formatAmount(this.amount, true);
		this.drawString(this.fontRendererObj, displayAmount, centerX - 56 - Math.max(this.fontRendererObj.getStringWidth(displayAmount) - 96, 0), centerY - 30, 0xAAAAAA);
		this.drawCenteredString(this.fontRendererObj, this.getColorizedFluidName(this.getWorkingFluidStack()), centerX - 10, centerY - 50, 0xFFFFFF);

		// Instructions in top-right corner
		int h = -10;
		this.drawRightAlignedString(I18n.format("gc.gui.instructions"), this.width - 5, h += 15, 0xAAAAAA);
		if (!this.transferDirectionForced) this.drawRightAlignedString(I18n.format("gc.gui.toggle", this.getKeyName(settings.keyBindJump)), this.width - 5, h += 15, 0xAAAAAA);
		if (this.numTransferrableHeldFluids > 1 || this.numTransferrableBlockFluids > 1) this.drawRightAlignedString(I18n.format("gc.gui.cycle"), this.width - 5, h += 15, 0xAAAAAA);
		this.drawRightAlignedString(I18n.format("gc.gui.accept", this.getKeyName(settings.keyBindInventory)), this.width - 5, h += 15, 0xAAAAAA);
		this.drawRightAlignedString(I18n.format("gc.gui.cancel"), this.width - 5, h += 15, 0xAAAAAA);

		// Combo shortcuts, left-aligned in bottom-right corner
		this.drawString(this.fontRendererObj, I18n.format("gc.gui.combo1" + cmd), leftMargin, this.height - 105, 0xAAAAAA);
		this.drawString(this.fontRendererObj, I18n.format("gc.gui.combo2" + cmd), leftMargin, this.height - 90, 0xAAAAAA);
		this.drawString(this.fontRendererObj, I18n.format("gc.gui.combo3"), leftMargin, this.height - 75, 0xAAAAAA);
		this.drawString(this.fontRendererObj, I18n.format("gc.gui.combo4"), leftMargin, this.height - 60, 0xAAAAAA);
		this.drawString(this.fontRendererObj, I18n.format("gc.gui.combo5"), leftMargin, this.height - 45, 0xAAAAAA);
		this.drawString(this.fontRendererObj, I18n.format("gc.gui.combo6" + cmd), leftMargin, this.height - 30, 0xAAAAAA);
		this.drawString(this.fontRendererObj, I18n.format("gc.gui.combo7"), leftMargin, this.height - 15, 0xAAAAAA);

		// Combo fluid amounts, right-aligned in bottom-right corner
		this.drawRightAlignedString(this.deltaStrings.get(0), this.width - 5, this.height - 105, 0xAAAAAA);
		this.drawRightAlignedString(this.deltaStrings.get(1), this.width - 5, this.height - 90, 0xAAAAAA);
		this.drawRightAlignedString(this.deltaStrings.get(2), this.width - 5, this.height - 75, 0xAAAAAA);
		this.drawRightAlignedString(this.deltaStrings.get(3), this.width - 5, this.height - 60, 0xAAAAAA);
		this.drawRightAlignedString(this.deltaStrings.get(4), this.width - 5, this.height - 45, 0xAAAAAA);
		this.drawRightAlignedString(this.deltaStrings.get(5), this.width - 5, this.height - 30, 0xAAAAAA);
		this.drawRightAlignedString(this.deltaStrings.get(6), this.width - 5, this.height - 15, 0xAAAAAA);

		// Draw held item stack and mode icon
		this.drawItemStack(this.heldItem, centerX - 114, centerY + 22, 2);
		if (this.metaHandler.modes.size() > 1) this.drawItemStack(this.mode.getModeIcon(), centerX - 88, centerY - 18);

		// Render Sprites
		RenderHelper.disableStandardItemLighting();
		minecraft.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
		this.applyToSprites((GuiFluidSprite sprite, boolean isHeldSprite, boolean isSelectedSprite, int tankIndex) -> sprite.render(partialTicks));

		// Draw hover tooltip on sprites
		GuiFluidSprite previousHoveredSprite = this.hoveredSprite;
		this.hoveredSprite = null;
		this.applyToSprites((GuiFluidSprite sprite, boolean isHeldSprite, boolean isSelectedSprite, int tankIndex) -> {
			if (sprite.isMouseOver(mouseX, mouseY) && !(this.isExporting ^ isHeldSprite)) {
				ArrayList<String> textLines = new ArrayList<String>();
				textLines.add(this.getColorizedFluidName(sprite.fluidStack));
				textLines.add(TextFormatting.GRAY + this.mode.formatAmount(sprite.fluidStack.getAmount(), false));
				if (!isSelectedSprite) {
					this.hoveredSprite = sprite;
					TransferrableFluidResult transferResult = this.transferResults.get(Pair.of(isHeldSprite ? tankIndex : this.heldTankIndex, isHeldSprite ? this.blockTankIndex : tankIndex));
					textLines.add(I18n.format(transferResult.canTransfer() ? "gc.gui.selectfluid" : "gc.gui.fluidblocked", TextFormatting.RED, TextFormatting.GRAY.toString() + TextFormatting.ITALIC));
				} else {
					textLines.add(I18n.format("gc.gui.selected",  TextFormatting.GRAY.toString() + TextFormatting.ITALIC));
				}
				this.drawHoveringText(textLines, mouseX, mouseY, this.fontRendererObj);
			}
		});

		// Scale and move sprites when hovering over them
		if (previousHoveredSprite != this.hoveredSprite) {
			this.applyToSprites((GuiFluidSprite sprite, boolean isHeldSprite, boolean isSelectedSprite, int tankIndex) -> {
				if (this.hoveredSprite == null) {
					if (isSelectedSprite) {
						sprite.animateMove(centerX - 26, centerY - 92);
						sprite.animateScale(32, 32);
					} else {
						sprite.animateMove(centerX + sprite.renderIndex * 24 + (sprite.renderIndex < 0 ? -26 : -10), centerY + (this.isExporting ^ isHeldSprite ? -44 : -76));
						sprite.animateScale(16, 16);
					}
				} else {
					if (sprite == this.hoveredSprite) { // hovered sprite will never be a selected sprite
						sprite.animateMove(centerX + sprite.renderIndex * 24 - 26, centerY + (this.isExporting ^ isHeldSprite ? -60 : -92));
						sprite.animateScale(32, 32);
					} else if (isSelectedSprite) {
						sprite.animateMove(centerX - 26 + (this.hoveredSprite.renderIndex < 0 ? 16 : 0), centerY - 76);
						sprite.animateScale(16, 16);
					} else {
						sprite.animateMove(centerX + sprite.renderIndex * 24 + (sprite.renderIndex < 0 ? -26 : -10) + (this.hoveredSprite.renderIndex < 0 ? (sprite.renderIndex > this.hoveredSprite.renderIndex && sprite.renderIndex < 0 ? 16 : 0) : (sprite.renderIndex < this.hoveredSprite.renderIndex && sprite.renderIndex > 0 ? -16 : 0)), centerY + (this.isExporting ^ isHeldSprite ? -44 : -76));
						sprite.animateScale(16, 16);
					}
				}
			});
		}
		
		RenderHelper.enableGUIStandardItemLighting();

		// 3D scene background (colors mostly pulled from vanilla tooltip rendering)
		final int sceneX = centerX - 42;
		final int sceneY = centerY + 6;
		final int sceneWidth = 64;
		final int sceneHeight = 64;
		Gui.drawRect(sceneX - 1, sceneY - 2, sceneX + sceneWidth + 1, sceneY - 1, 0xF0100010);
		Gui.drawRect(sceneX - 1, sceneY + sceneHeight + 1, sceneX + sceneWidth + 1, sceneY + sceneHeight + 2, 0xF0100010);
		Gui.drawRect(sceneX - 2, sceneY - 1, sceneX - 1, sceneY + sceneHeight + 1, 0xF0100010);
		Gui.drawRect(sceneX + sceneWidth + 1, sceneY - 1, sceneX + sceneWidth + 2, sceneY + sceneHeight + 1, 0xF0100010);
		Gui.drawRect(sceneX - 1, sceneY - 1, sceneX + sceneWidth + 1, sceneY + sceneHeight + 1, 0xFF000000);
		Gui.drawRect(sceneX - 1, sceneY - 1, sceneX + sceneWidth + 1, sceneY, 0x505000FF);
		Gui.drawRect(sceneX - 1, sceneY + sceneHeight, sceneX + sceneWidth + 1, sceneY + sceneHeight + 1, 0x5028007F);
		this.drawGradientRect(sceneX - 1, sceneY, sceneX, sceneY + sceneHeight, 0x505000FF, 0x5028007F);
		this.drawGradientRect(sceneX + sceneWidth, sceneY, sceneX + sceneWidth + 1, sceneY + sceneHeight, 0x505000FF, 0x5028007F);

		// 3D scene render
		this.sceneRenderer.drawScreen(partialTicks, new Rectangle((int) (sceneX * scale), (int) ((this.height - sceneY - sceneHeight) * scale), (int) (sceneWidth * scale), (int) (sceneHeight * scale)));
		
		// Extra tooltips
		if (mouseX >= centerX - 114 && mouseX < centerX -82  && mouseY >= centerY + 22 && mouseY < centerY + 54) this.renderToolTip(this.heldItem, mouseX, mouseY);
		// func_146115_a() returns whether the mouse is over the button
		if (this.exportButton.func_146115_a()) this.drawHoveringText(Arrays.asList(I18n.format(this.transferDirectionForced ? "gc.gui.notoggle" : "gc.gui.yestoggle")), mouseX, mouseY, this.fontRendererObj);
		if (this.modeButton.func_146115_a()) {
			ArrayList<String> lines = new ArrayList<String>();
			lines.add(this.mode.getModeName());
			lines.add(I18n.format("gc.gui.cyclemode", this.getKeyName(settings.keyBindJump)));
			this.drawHoveringText(lines, mouseX, mouseY, this.fontRendererObj);
		}
		if (this.sceneRenderer.hoveredFace != null && !this.allowedFaces.get(this.sceneRenderer.hoveredFace.ordinal()).canTransfer()) this.drawHoveringText(Arrays.asList(I18n.format("gc.gui.sideblocked", TextFormatting.RED, TextFormatting.GRAY.toString() + TextFormatting.ITALIC)), sceneX + sceneWidth, sceneY + sceneHeight / 2 + 5, this.fontRendererObj);
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (button == this.incFluidButton) {
			this.setAmount(this.amount + this.getDelta());
		} else if (button == this.decFluidButton) {
			this.setAmount(this.amount - this.getDelta());
		} else if (button == this.exportButton) {
			this.toggleTransferDirection();
		} else if (button == this.modeButton) {
			this.cycleMode();
		}
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) {
		super.keyTyped(typedChar, keyCode);
		this.handleKeyInput(typedChar, keyCode);
	}

	@Override
	public void handleMouseInput() {
		if (!this.initialized) return;

		int scrollAmount = Mouse.getEventDWheel();
		if (scrollAmount != 0) this.setAmount(this.amount + this.getDelta() * MathHelper.clamp(scrollAmount, -1, 1));

		if ((Mouse.getEventButton() == 0 || Mouse.getEventButton() == 1) && Mouse.getEventButtonState() && this.hoveredSprite != null) {
			Pair<Integer, Integer> tankIndices = Pair.of(this.isExporting ? this.hoveredSprite.tankIndex : this.heldTankIndex, this.isExporting ? this.blockTankIndex : this.hoveredSprite.tankIndex);
			TransferrableFluidResult transferResult = this.transferResults.get(tankIndices);
			if (transferResult.canTransfer()) this.selectFluid(tankIndices, Mouse.getEventButton() == 0 ? this.amount : this.hoveredSprite.fluidStack.getAmount(), this.isExporting, true);
		}

		this.sceneRenderer.handleMouseinput();
		if (this.sceneRenderer.selectedFace != this.selectedFace) this.changeSelectedFace(this.sceneRenderer.selectedFace);

		super.handleMouseInput();
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		this.textAmount.mouseClicked(mouseX, mouseY, mouseButton);
		if (mouseButton == 1 && (this.isExporting ? this.heldFluidSprites.get(this.heldTankIndex) : this.blockFluidSprites.get(this.blockTankIndex)).isMouseOver(mouseX, mouseY)) this.setAmount((this.isExporting ? this.heldFluidStacks.get(this.heldTankIndex) : this.blockFluidStacks.get(this.blockTankIndex)).getAmount());
		this.handleKeyInput(mouseButton - 100, mouseX, mouseY);
	}

	// Handles both keyboard and mouse button input
	private void handleKeyInput(char typedChar, int keyCode, int mouseX, int mouseY) {
		GameSettings settings = Minecraft.getMinecraft().gameSettings;

		if (keyCode == settings.keyBindInventory.getKeyCode()) { // Open/close inventory to confirm transfer
			Minecraft.getMinecraft().displayGuiScreen(null);
			PacketHandler.INSTANCE.sendToServer(new PacketBlockTransferFluid(this.heldItem, this.heldTankIndex, this.pos, this.selectedFace.ordinal(), this.blockTankIndex, this.amount * (this.isExporting ? -1 : 1)));
		} else if (keyCode == settings.keyBindJump.getKeyCode()) { // Jump
			if (GuiScreen.isShiftKeyDown()) { // Shift + Jump to cycle mode
				this.cycleMode();
			} else { // Normal jump to toggle transfer direction
				this.toggleTransferDirection();
			}
		} else if (keyCode == settings.keyBindForward.getKeyCode() || keyCode == 200) {  // Forward key or up arrow
			this.setAmount(this.amount + this.getDelta());
		} else if (keyCode == settings.keyBindBack.getKeyCode() || keyCode == 208) { // Back key or down arrow
			this.setAmount(this.amount - this.getDelta());
		} else if (keyCode == settings.keyBindLeft.getKeyCode()) {
			this.textAmount.textboxKeyTyped(Character.MIN_VALUE, 203); // Send a left arrow key press to the text box (to move the cursor)
		} else if (keyCode == settings.keyBindRight.getKeyCode()) {
			this.textAmount.textboxKeyTyped(Character.MIN_VALUE, 205); // Send right arrow key ^^
		} else if (keyCode == 15) { // (shift+)Tab to select next transferrable fluid
			int numTanks = this.isExporting ? this.heldFluidStacks.size() : this.blockFluidStacks.size();
			int tankIndex = this.isExporting ? this.heldTankIndex : this.blockTankIndex;
			int direction = GuiScreen.isShiftKeyDown() ? -1 : 1;
			for (int i = (tankIndex + direction + numTanks) % numTanks; i != tankIndex; i = (i + direction + numTanks) % numTanks) {
				GuiFluidSprite sprite = this.isExporting ? this.heldFluidSprites.get(i) : this.blockFluidSprites.get(i);
				if (sprite == null) continue;
				Pair<Integer, Integer> tankIndices = Pair.of(this.isExporting ? sprite.tankIndex : this.heldTankIndex, this.isExporting ? this.blockTankIndex : sprite.tankIndex);
				TransferrableFluidResult transferResult = this.transferResults.get(tankIndices);
				if (!transferResult.canTransfer()) continue;
				this.selectFluid(tankIndices, this.amount, this.isExporting, true);
				break;
			}
		} else if (keyCode == -99) { // Clear text box on right click
			if (mouseX < this.textAmount.xPosition || mouseX >= this.textAmount.xPosition + this.textAmount.width || mouseY < this.textAmount.yPosition || mouseY >= this.textAmount.yPosition + this.textAmount.height) return;
			this.setAmount(0);
			this.textAmount.setText("");
		} else if (NumberUtils.isDigits(Character.toString(typedChar)) || !ChatAllowedCharacters.isAllowedCharacter(typedChar)) { // Digits and non-printable characters (backspace, etc.)
			this.textAmount.textboxKeyTyped(typedChar, keyCode);
			Integer num = Ints.tryParse(StringUtils.defaultIfEmpty(this.textAmount.getText(), "0"));
			if (num == null) num = Integer.MAX_VALUE;
			this.amount = MathHelper.clamp(num, 0, this.maxAmount);
			if (num != this.amount) this.textAmount.setText(Integer.toString(this.amount)); // If text representation overflows integer
			
			if (!this.textAmount.getText().startsWith("0") || !NumberUtils.isDigits(Character.toString(typedChar))) return; // If digit is typed and text starts with zeros
			final int cursorPos = this.textAmount.getCursorPosition();
			int leadCount = 0;
			while (leadCount < this.textAmount.getText().length() - 1 && this.textAmount.getText().charAt(leadCount) == '0') leadCount++;
			this.textAmount.setText(this.textAmount.getText().substring(leadCount));
			this.textAmount.setCursorPosition(Math.max(cursorPos - leadCount, 0));
		}
	}

	private void handleKeyInput(char typedChar, int keyCode) {
		this.handleKeyInput(typedChar, keyCode, -1, -1);
	}

	private void handleKeyInput(int keyCode, int mouseX, int mouseY) {
		this.handleKeyInput(Character.MIN_VALUE, keyCode, mouseX, mouseY);
	}

	private String getKeyName(KeyBinding keybind) {
		return Keyboard.getKeyName(keybind.getKeyCode());
	}

	private void drawRightAlignedString(String text, int x, int y, int color) {
		this.drawString(this.fontRendererObj, text, x - this.fontRendererObj.getStringWidth(text), y, color);
	}

	private void drawItemStack(ItemStack stack, int x, int y) {
		this.drawItemStack(stack, x, y, 1);
	}

	private void drawItemStack(ItemStack stack, float x, float y, float scale) {
		x /= scale;
		y /= scale;
		RenderHelper.disableStandardItemLighting();
		RenderHelper.enableGUIStandardItemLighting();
		GL11.glScalef(scale, scale, scale);
		this.zLevel = 200.0F;
		GuiScreen.itemRender.zLevel = 200.0F;
		FontRenderer font = stack.getItem().getFontRenderer(stack);
		if (font == null) font = this.fontRendererObj;
		GuiScreen.itemRender.renderItemAndEffectIntoGUI(font, Minecraft.getMinecraft().getTextureManager(), stack, (int) x, (int) y);
		GuiScreen.itemRender.renderItemOverlayIntoGUI(font, Minecraft.getMinecraft().getTextureManager(), stack, (int) x, (int) y, "");
		RenderHelper.enableGUIStandardItemLighting();
		GL11.glScalef(1/scale, 1/scale, 1/scale);
		this.zLevel = 0.0F;
		GuiScreen.itemRender.zLevel = 0.0F;
	}

	private void setAmount(int amountIn) {
		this.amount = MathHelper.clamp(amountIn, 0, this.maxAmount);
		this.textAmount.setText(Integer.toString(this.amount));
	}

	private int getDelta() {
		/*
		 * Ctrl+Shift:		this.deltas[0]
		 * Ctrl:			this.deltas[1]
		 * Shift:			this.deltas[2]
		 * (None):			this.deltas[3]
		 * Shift+Alt:		this.deltas[4]
		 * Ctrl+Shift+Alt:	this.deltas[5]
		 * Alt:				this.deltas[6]
		 */
		final boolean isShiftDown = GuiScreen.isShiftKeyDown();
		final boolean isCtrlDown = GuiScreen.isCtrlKeyDown();
		final boolean isAltDown = Keyboard.isKeyDown(Keyboard.KEY_LMENU) || Keyboard.isKeyDown(Keyboard.KEY_RMENU) || Keyboard.isKeyDown(Keyboard.KEY_LMETA) || Keyboard.isKeyDown(Keyboard.KEY_RMETA);
		return isShiftDown ? (isCtrlDown ? (isAltDown ? this.deltas[5] : this.deltas[0]) : (isAltDown ? this.deltas[4] : this.deltas[2])) : (isCtrlDown ? this.deltas[1] : (isAltDown ? this.deltas[6] : this.deltas[3]));
	}

	private String getColorizedFluidName(IProxyFluidStack fluidStack) {
		return ColorCache.getFluidColorCode(fluidStack, fluidStack.getColor()) + TextFormatting.UNDERLINE + fluidStack.getLocalizedName() + TextFormatting.RESET;
	}

	@FunctionalInterface
	private interface SpriteConsumer {
		void apply(GuiFluidSprite sprite, boolean isHeldSprite, boolean isSelectedSprite, int tankIndex);
	}
}
