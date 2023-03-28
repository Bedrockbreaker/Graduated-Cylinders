package bedrockbreaker.graduatedcylinders;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import bedrockbreaker.graduatedcylinders.Packets.PacketHandler;
import bedrockbreaker.graduatedcylinders.Proxy.ProxyFluidHandler;
import bedrockbreaker.graduatedcylinders.Proxy.ProxyFluidStack;
import bedrockbreaker.graduatedcylinders.Proxy.ProxyTanksProperties.ProxyTankProperties;
import bedrockbreaker.graduatedcylinders.FluidHelper.FindTransferrableTankResult;
import bedrockbreaker.graduatedcylinders.Packets.PacketBlockTransferFluid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class FluidTransferGui extends GuiScreen {

	private static final ArrayList<Integer> allowedChars = new ArrayList<Integer>(Arrays.asList(14, 200, 203, 205, 208)); // Backspace and arrow keys
	private boolean initialized = false;

	private final ItemStack heldItem;
	private final ProxyFluidHandler heldFluidHandler;
	private final World world;
	private final BlockPos pos;
	private final ItemStack blockItem;
	private ProxyFluidStack fluidStack;
	private ItemStack fluidItem;
	private EnumFacing selectedFace;
	private ProxyFluidHandler blockFluidHandler;
	private int heldTankIndex;
	private int blockTankIndex;
	private int max;
	private boolean forced;
	private boolean export;
	private int amount = 0;

	private int buttonId = 0;
	private GuiTextField textAmount;
	private GuiButton incFluidButton;
	private GuiButton decFluidButton;
	private GuiButton exportButton;
	private BiMap<EnumFacing, GuiButton> faceButtons = HashBiMap.create(6);
	private List<GuiButton> heldTanksButtons = new ArrayList<GuiButton>();
	private List<GuiButton> blockTanksButtons = new ArrayList<GuiButton>();

	public static void open(ItemStack heldItem, BlockPos pos, int side, FindTransferrableTankResult transferResults, ProxyFluidStack heldFluidStack, ProxyFluidStack blockFluidStack) {
		Minecraft.getMinecraft().displayGuiScreen(new FluidTransferGui(heldItem, pos, EnumFacing.getFront(side), transferResults, heldFluidStack, blockFluidStack));
	}

	public FluidTransferGui(ItemStack heldItem, BlockPos pos, EnumFacing side, FindTransferrableTankResult transferResults, ProxyFluidStack heldFluidStack, ProxyFluidStack blockFluidStack) {
		super();

		if (heldItem == null) throw new IllegalArgumentException(); // IDE complaint.

		Minecraft minecraft = Minecraft.getMinecraft();
		
		this.heldItem = heldItem;
		this.heldFluidHandler = FluidHelper.getProxyFluidHandler(heldItem);
		this.heldTankIndex = transferResults.leftTank;
		this.world = minecraft.world;
		this.pos = pos;
		this.blockFluidHandler = FluidHelper.getProxyFluidHandler(world, pos, side, this.heldFluidHandler.getType());
		this.blockItem = this.pickBlock(pos);
		this.selectedFace = side;
		this.blockTankIndex = transferResults.rightTank;

		ProxyTankProperties heldFluidTank = this.heldFluidHandler.getTankProperties().get(heldTankIndex);
		ProxyTankProperties blockFluidTank = this.blockFluidHandler.getTankProperties().get(blockTankIndex);

		// Need to create excess variable because it yells at me for not checking for null.
		ProxyFluidStack fluidStack = heldFluidStack == null ? blockFluidStack : heldFluidStack;
		if (fluidStack == null) throw new NullPointerException();
		this.fluidStack = fluidStack;
		this.fluidItem = fluidStack.getFilledBucket();

		this.max = Math.min(heldFluidTank.getCapacity(), blockFluidTank.getCapacity());
		this.forced = (transferResults.canExport && !transferResults.canImport) || (!transferResults.canExport && transferResults.canImport);
		this.export = this.forced ? transferResults.canExport : minecraft.player.isSneaking();

		this.initialized = true;
	}

	@Override
	public void initGui() {
		final int centerX = this.width/2;
		final int centerY = this.height/2;

		this.incFluidButton = new GuiButton(buttonId++, centerX + 50, centerY - 35, 20, 20, "+");
		this.addButton(this.incFluidButton);

		this.decFluidButton = new GuiButton(buttonId++, centerX + 50, centerY - 5, 20, 20, "-");
		this.addButton(this.decFluidButton);

		this.exportButton = new GuiButton(buttonId++, centerX - 20, centerY + 12, 20, 20, this.export ? "->" : "<-");
		if (this.forced) this.exportButton.enabled = false;
		this.addButton(this.exportButton);

		this.textAmount = new GuiTextField(0, this.fontRenderer, centerX - 60, centerY - 20, 100, 20);
		this.textAmount.setText(Integer.toString(this.amount));
		this.textAmount.setMaxStringLength(10);
		this.textAmount.setFocused(true);
		this.textAmount.setCanLoseFocus(false);

		/*
		final int blockItemCenterX = this.width / 4 - 31;
		final int blockItemCenterY = this.height / 4 + 3;
		this.faceButtons.put(EnumFacing.NORTH, new GuiButton(buttonId++, blockItemCenterX + 45, blockItemCenterY + 45, 20, 20,"N"));
		this.faceButtons.put(EnumFacing.EAST, new GuiButton(buttonId++, blockItemCenterX + 25, blockItemCenterY + 45, 20, 20, "E"));
		this.faceButtons.put(EnumFacing.SOUTH, new GuiButton(buttonId++, blockItemCenterX + 65, blockItemCenterY + 65, 20, 20, "S"));
		this.faceButtons.put(EnumFacing.WEST, new GuiButton(buttonId++, blockItemCenterX + 65, blockItemCenterY + 45, 20, 20, "W"));
		this.faceButtons.put(EnumFacing.UP, new GuiButton(buttonId++, blockItemCenterX + 45, blockItemCenterY + 25, 20, 20, "U"));
		this.faceButtons.put(EnumFacing.DOWN, new GuiButton(buttonId++, blockItemCenterX + 45, blockItemCenterY + 65, 20, 20, "D"));
		this.faceButtons.get(selectedFace).enabled = false;
		for (GuiButton button : this.faceButtons.values()) {
			this.addButton(button);
		}

		if (heldFluidHandler.getTankProperties().length > 1) {
			for (int i = 0; i < heldFluidHandler.getTankProperties().length; i++) {
				FluidStack contents = heldFluidHandler.getTankProperties()[i].getContents();
				GuiButton tankButton = new GuiButton(buttonId++, this.width / 2 - 31, height / 4 + 20 + 25*i, 20, 20, contents == null ? "Empty" : contents.getLocalizedName());
				if (i == heldTankIndex) tankButton.enabled = false;
				this.heldTanksButtons.add(tankButton);
			}
		}

		if (blockFluidHandler.getTankProperties().length > 1) {
			for (int i = 0; i < blockFluidHandler.getTankProperties().length; i++) {
				FluidStack contents = blockFluidHandler.getTankProperties()[i].getContents();
				GuiButton tankButton = new GuiButton(buttonId++, this.width / 2 + 5, height / 4 + 20 + 25*i, 20, 20, contents == null ? "Empty" : contents.getLocalizedName());
				if (i == blockTankIndex) tankButton.enabled = false;
				this.blockTanksButtons.add(tankButton);
			}
		}
		*/
	}

	// Called every frame
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		GameSettings settings = Minecraft.getMinecraft().gameSettings;
		final int centerX = this.width/2;
		final int centerY = this.height/2;
		final String fluidName = this.fluidStack.getLocalizedName();
		final String cmd = Minecraft.IS_RUNNING_ON_MAC ? ".cmd" : "";
		final int leftMargin = this.width - (this.fontRenderer.getStringWidth(I18n.format("gc.gui.100000mb.combo" + cmd)) + this.fontRenderer.getStringWidth(I18n.format("gc.gui.allmb")) + 10);

		this.drawDefaultBackground();
		this.textAmount.drawTextBox();
		super.drawScreen(mouseX, mouseY, partialTicks);
		
		// Bucket amount above text field
		this.drawString(this.fontRenderer, fluidName, centerX - 10 - this.fontRenderer.getStringWidth(fluidName) / 2, centerY - 50, 16777215); // White (#FFFFFF)
		
		// Instructions in top-right corner
		this.drawString(this.fontRenderer, I18n.format("gc.gui.amount", this.amount/1000.0F), centerX - 56, centerY - 30, 11184810); // Gray (#AAAAAA) (https://minecraft.fandom.com/wiki/Formatting_codes#Color_codes)
		this.drawString(this.fontRenderer, I18n.format("gc.gui.instructions"), this.width - this.fontRenderer.getStringWidth(I18n.format("gc.gui.instructions")) - 5, 5, 11184810);
		this.drawString(this.fontRenderer, I18n.format("gc.gui.toggle", settings.keyBindJump.getDisplayName()), this.width - this.fontRenderer.getStringWidth(I18n.format("gc.gui.toggle", settings.keyBindJump.getDisplayName())) - 5, 20, 11184810);
		this.drawString(this.fontRenderer, I18n.format("gc.gui.accept", settings.keyBindInventory.getDisplayName()), this.width - this.fontRenderer.getStringWidth(I18n.format("gc.gui.accept", settings.keyBindInventory.getDisplayName())) - 5, 35, 11184810);
		this.drawString(this.fontRenderer, I18n.format("gc.gui.cancel"), this.width - this.fontRenderer.getStringWidth(I18n.format("gc.gui.cancel")) - 5, 50, 11184810);
		
		// Combo shortcuts, left-aligned in bottom-right corner
		this.drawString(this.fontRenderer, I18n.format("gc.gui.1mb.combo" + cmd), leftMargin, this.height - 105, 11184810);
		this.drawString(this.fontRenderer, I18n.format("gc.gui.10mb.combo" + cmd), leftMargin, this.height - 90, 11184810);
		this.drawString(this.fontRenderer, I18n.format("gc.gui.100mb.combo"), leftMargin, this.height - 75, 11184810);
		this.drawString(this.fontRenderer, I18n.format("gc.gui.1000mb.combo"), leftMargin, this.height - 60, 11184810);
		this.drawString(this.fontRenderer, I18n.format("gc.gui.10000mb.combo"), leftMargin, this.height - 45, 11184810);
		this.drawString(this.fontRenderer, I18n.format("gc.gui.100000mb.combo" + cmd), leftMargin, this.height - 30, 11184810);
		this.drawString(this.fontRenderer, I18n.format("gc.gui.allmb.combo"), leftMargin, this.height - 15, 11184810);
		
		// Combo fluid amounts, right-aligned in bottom-right corner
		this.drawString(this.fontRenderer, I18n.format("gc.gui.1mb"), this.width - this.fontRenderer.getStringWidth(I18n.format("gc.gui.1mb")) - 5, this.height - 105, 11184810);
		this.drawString(this.fontRenderer, I18n.format("gc.gui.10mb"), this.width - this.fontRenderer.getStringWidth(I18n.format("gc.gui.10mb")) - 5, this.height - 90, 11184810);
		this.drawString(this.fontRenderer, I18n.format("gc.gui.100mb"), this.width - this.fontRenderer.getStringWidth(I18n.format("gc.gui.100mb")) - 5, this.height - 75, 11184810);
		this.drawString(this.fontRenderer, I18n.format("gc.gui.1000mb"), this.width - this.fontRenderer.getStringWidth(I18n.format("gc.gui.1000mb")) - 5, this.height - 60, 11184810);
		this.drawString(this.fontRenderer, I18n.format("gc.gui.10000mb"), this.width - this.fontRenderer.getStringWidth(I18n.format("gc.gui.10000mb")) - 5, this.height - 45, 11184810);
		this.drawString(this.fontRenderer, I18n.format("gc.gui.100000mb"), this.width - this.fontRenderer.getStringWidth(I18n.format("gc.gui.100000mb")) - 5, this.height - 30, 11184810);
		this.drawString(this.fontRenderer, I18n.format("gc.gui.allmb"), this.width - this.fontRenderer.getStringWidth(I18n.format("gc.gui.allmb")) - 5, this.height - 15, 11184810);
		
		this.drawItemStack(this.heldItem, centerX - 61, centerY + 6);
		this.drawItemStack(this.blockItem, centerX + 10, centerY + 6);
		if (!this.fluidItem.isEmpty()) this.drawItemStack(this.fluidItem, centerX - 26, centerY - 92);

		if (mouseX >= centerX - 62 && mouseX <= centerX - 30 && mouseY >= centerY + 6 && mouseY <= centerY + 38) this.renderToolTip(this.heldItem, mouseX, mouseY);
		if (mouseX >= centerX + 10 && mouseX <= centerX + 42 && mouseY >= centerY + 6 && mouseY <= centerY + 38) this.renderToolTip(this.blockItem, mouseX, mouseY);
		//if (this.incFluidButton.isMouseOver() || this.decFluidButton.isMouseOver()) this.drawHoveringText(this.tooltip, mouseX, mouseY);
		if (this.exportButton.isMouseOver()) this.drawHoveringText(I18n.format(this.forced ? "gc.gui.notoggle" : "gc.gui.yestoggle"), mouseX, mouseY);
		// Draw tooltip when hovering over the textbox, except when the mouse is in the center of the screen, such as when the gui is first created
		//if (mouseX >= centerX - 60 && mouseX <= centerX + 40 && mouseY >= centerY - 20 && mouseY <= centerY && (mouseX != centerX || mouseY != centerY)) this.drawHoveringText(this.tooltip, mouseX, mouseY);
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button == this.incFluidButton) {
			this.amount = MathHelper.clamp(this.amount + delta(), 0, this.max);
			this.textAmount.setText(Integer.toString(this.amount));
		} else if (button == this.decFluidButton) {
			this.amount = MathHelper.clamp(this.amount - delta(), 0, this.max);
			this.textAmount.setText(Integer.toString(this.amount));
		} else if (button == this.exportButton) {
			if (this.forced) return;
			this.export = !this.export;
			this.exportButton.displayString = this.export ? "->" : "<-";
		} else if (this.faceButtons.containsValue(button)) {
			System.out.println("Clicked " + this.faceButtons.inverse().get(button));
		} else if (this.heldTanksButtons.contains(button)) {
			System.out.println("Clicked " + button.displayString + " (index " + this.heldTanksButtons.indexOf(button) + ")");
		} else if (this.blockTanksButtons.contains(button)) {
			System.out.println("Clicked " + button.displayString + " (index " + this.blockTanksButtons.indexOf(button) + ")");
		}
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);

		if (keyCode == Minecraft.getMinecraft().gameSettings.keyBindInventory.getKeyCode()) {
			Minecraft.getMinecraft().displayGuiScreen(null);
			PacketHandler.INSTANCE.sendToServer(new PacketBlockTransferFluid(this.heldItem, this.heldTankIndex, this.pos, this.selectedFace.getIndex(), this.blockTankIndex, this.amount * (this.export ? -1 : 1)));
			return;
		} else if (keyCode == Minecraft.getMinecraft().gameSettings.keyBindJump.getKeyCode()) {
			if (this.forced) return;
			this.export = !this.export;
			this.exportButton.displayString = this.export ? "->" : "<-";
			return;
		}
		if (!NumberUtils.isDigits(Character.toString(typedChar)) && !allowedChars.contains(keyCode)) return;

		this.textAmount.textboxKeyTyped(typedChar, keyCode);

		if (keyCode == 200 || keyCode == 208) {
			this.amount = MathHelper.clamp(this.amount + (keyCode == 200 ? delta() : -delta()), 0, this.max);
			this.textAmount.setText(Integer.toString(this.amount));
		} else {
			int num = Integer.parseUnsignedInt(StringUtils.defaultIfEmpty(this.textAmount.getText(), "0"));
			this.amount = MathHelper.clamp(num < 0 ? Integer.MAX_VALUE : num, 0, this.max);
			this.textAmount.setText(Integer.toString(this.amount));
		}
	}

	@Override
	public void handleMouseInput() throws IOException {
		if (!this.initialized) return;

		final int scrollAmount = Mouse.getDWheel();
		if (scrollAmount != 0) {
			this.amount = MathHelper.clamp(this.amount + delta() * MathHelper.clamp(scrollAmount, -1, 1), 0, this.max);
			this.textAmount.setText(Integer.toString(this.amount));
		}

		super.handleMouseInput();
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		this.textAmount.mouseClicked(mouseX, mouseY, mouseButton);
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
		x /= 2;
		y /= 2;
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
