package bedrockbreaker.graduatedcylinders;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import bedrockbreaker.graduatedcylinders.Packets.PacketHandler;
import bedrockbreaker.graduatedcylinders.Packets.PacketTransferFluid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class FluidTransferGui extends GuiScreen {

	private static final ArrayList<Integer> allowedChars = new ArrayList<Integer>(Arrays.asList(14, 200, 203, 205, 208)); // Backspace and arrow keys
	private static final List<String> tooltip = new ArrayList<String>(Arrays.asList(
		"Change using the buttons,",
		"arrow keys, or scroll wheel",
		"",
		"Ctrl+Shift:      1 mB",
		"Ctrl:             10 mB",
		"Shift:            100 mB",
		"(None):         1 B",
		"Shift+Alt:       10 B",
		"Ctrl+Shift+Alt: 100 B",
		"Alt:               max/min",
		"",
		"Open/Close Inventory: Accept",
		"Escape: Cancel"
	));
	private static int inventoryKey;

	private final World world;
	private final BlockPos pos;
	private final ItemStack container;
	private final ItemStack tankItem;
	private final boolean forced;
	private final int max;
	private boolean export;
	private int amt;
	private boolean initialized = false;
	private GuiTextField textAmt;
	private GuiButton incFluid;
	private GuiButton decFluid;
	private GuiButton toggleExport;

	public static void open(BlockPos pos, ItemStack container, boolean export, boolean forced, int amt, int max) {
		FluidTransferGui.inventoryKey = Minecraft.getMinecraft().gameSettings.keyBindInventory.getKeyCode();
		Minecraft.getMinecraft().displayGuiScreen(new FluidTransferGui(pos, container, export, forced, amt, max));
	}

	public FluidTransferGui(BlockPos pos, ItemStack container, boolean export, boolean forced, int amt, int max) {
		super();
		this.world = Minecraft.getMinecraft().world;
		this.pos = pos;
		this.container = container;
		this.tankItem = this.pickBlock(pos);
		this.export = export;
		this.forced = forced;
		this.amt = amt;
		this.max = max;
		this.initialized = true;
	}

	@Override
	public void initGui() {
		final int centerX = this.width/2;
		final int centerY = this.height/2;

		this.incFluid = new GuiButton(0, centerX + 50, centerY - 35, 20, 20, "+");
		this.decFluid = new GuiButton(1, centerX + 50, centerY - 5, 20, 20, "-");
		this.toggleExport = new GuiButton(2, centerX - 20, centerY + 12, 20, 20, export ? "->" : "<-");
		if (this.forced) this.toggleExport.enabled = false;
		this.textAmt = new GuiTextField(3, this.fontRenderer, centerX - 60, centerY - 20, 100, 20);
		this.textAmt.setText(Integer.toString(this.amt));
		this.textAmt.setFocused(true);
		this.textAmt.setCanLoseFocus(false);

		this.addButton(this.incFluid);
		this.addButton(this.decFluid);
		this.addButton(this.toggleExport);
	}

	// Called every frame
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		this.textAmt.drawTextBox();
		super.drawScreen(mouseX, mouseY, partialTicks);
		this.drawString(this.fontRenderer, Float.toString(this.amt/1000.0F) + " Buckets", this.width/2 - 56, this.height/2 - 30, 11184810); // Gray (#AAAAAA) (https://minecraft.fandom.com/wiki/Formatting_codes#Color_codes)
		this.drawItemStack(this.container, width/4 - 31, height/4 + 3);
		this.drawItemStack(this.tankItem, width/4 + 5, height/4 + 3);

		if (mouseX >= width/2 + 10 && mouseX <= width/2 + 42 && mouseY >= height/2 + 6 && mouseY <= height/2 + 38) this.renderToolTip(this.tankItem, mouseX, mouseY);
		if (mouseX >= width/2 - 62 && mouseX <= width/2 - 30 && mouseY >= height/2 + 6 && mouseY <= height/2 + 38) this.renderToolTip(this.container, mouseX, mouseY);
		if (this.incFluid.isMouseOver()) this.drawHoveringText(tooltip, mouseX, mouseY);
		if (this.decFluid.isMouseOver()) this.drawHoveringText(tooltip, mouseX, mouseY);
		if (this.toggleExport.isMouseOver()) this.drawHoveringText(forced ? "Can't change fluid transfer direction" : "Toggle fluid transfer direction", mouseX, mouseY);
		// Draw tooltip when hovering over the textbox, except when the mouse is in the center of the screen, such as when the gui is first created
		if (mouseX >= width/2 - 60 && mouseX <= width/2 + 40 && mouseY >= height/2 - 20 && mouseY <= height/2 && (mouseX != width/2 || mouseY != height/2)) this.drawHoveringText(tooltip, mouseX, mouseY);
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		switch (button.id) {
			case 0:
				this.amt = MathHelper.clamp(this.amt + delta(), 0, this.max);
				this.textAmt.setText(Integer.toString(this.amt));
				break;
			case 1:
				this.amt = MathHelper.clamp(this.amt - delta(), 0, this.max);
				this.textAmt.setText(Integer.toString(this.amt));
				break;
			case 2:
				if (this.forced) return;
				this.export = !this.export;
				this.toggleExport.displayString = this.export ? "->" : "<-";
				break;
		}

	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);
		if (keyCode == FluidTransferGui.inventoryKey) {
			Minecraft.getMinecraft().displayGuiScreen(null);
			PacketHandler.INSTANCE.sendToServer(new PacketTransferFluid(this.pos, this.container, this.export, this.amt));
			return;
		}
		if (!NumberUtils.isDigits(Character.toString(typedChar)) && !allowedChars.contains(keyCode)) return;
		if (keyCode == 200 || keyCode == 208) {
			this.amt = MathHelper.clamp(this.amt + (keyCode == 200 ? delta() : -delta()), 0, this.max);
			this.textAmt.setText(Integer.toString(this.amt));
		}
		this.textAmt.textboxKeyTyped(typedChar, keyCode);
	}

	@Override
	public void handleMouseInput() throws IOException {
		if (!this.initialized) return;
		final int scrollAmt = Mouse.getDWheel();
		if (scrollAmt != 0) {
			this.amt = MathHelper.clamp(this.amt + delta() * MathHelper.clamp(scrollAmt, -1, 1), 0, this.max);
			this.textAmt.setText(Integer.toString(this.amt));
		}
		super.handleMouseInput();
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		this.textAmt.mouseClicked(mouseX, mouseY, mouseButton);
	}

	private ItemStack pickBlock(BlockPos pos) {
		final RayTraceResult target = Minecraft.getMinecraft().objectMouseOver;
		ItemStack stack = ItemStack.EMPTY;
		TileEntity tileEntity = null;
		if (target.typeOfHit == RayTraceResult.Type.BLOCK) {
			final IBlockState state = this.world.getBlockState(pos);
			if (state.getBlock().isAir(state, this.world, pos)) return stack;
			if (state.getBlock().hasTileEntity(state)) { // I don't know why it wouldn't..
				tileEntity = this.world.getTileEntity(pos);
				stack = state.getBlock().getPickBlock(state, target, this.world, pos, Minecraft.getMinecraft().player);
			}
		}

		if (stack.isEmpty()) return ItemStack.EMPTY;
		if (tileEntity != null) stack.setTagInfo("BlockEntityTag", tileEntity.writeToNBT(new NBTTagCompound()));

		return stack;
	}

	private void drawItemStack(ItemStack stack, int x, int y) {
		RenderHelper.disableStandardItemLighting();
		RenderHelper.enableGUIStandardItemLighting();
		GL11.glScalef(2.0F, 2.0F, 2.0F);
		this.zLevel = 200.0F;
		this.itemRender.zLevel = 200.0F;
		FontRenderer font = stack.getItem().getFontRenderer(stack);
		if (font == null) font = this.fontRenderer;
		this.itemRender.renderItemAndEffectIntoGUI(stack, x, y);
		this.itemRender.renderItemOverlayIntoGUI(font, stack, x, y, "");
		RenderHelper.enableGUIStandardItemLighting();
		GL11.glScalef(0.5F, 0.5F, 0.5F);
		this.zLevel = 0.0F;
		this.itemRender.zLevel = 0.0F;
	}

	private int delta() {
		return isShiftKeyDown() ? (isCtrlKeyDown() ? (isAltKeyDown() ? 100000 : 1) : (isAltKeyDown() ? 10000 : 100)) : (isCtrlKeyDown() ? 10 : (isAltKeyDown() ? this.max : 1000));
	}
}
