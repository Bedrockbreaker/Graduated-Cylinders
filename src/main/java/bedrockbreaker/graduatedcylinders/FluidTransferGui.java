package bedrockbreaker.graduatedcylinders;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.primitives.Ints;

import bedrockbreaker.graduatedcylinders.Packets.PacketHandler;
import bedrockbreaker.graduatedcylinders.Proxy.IProxyFluidHandler;
import bedrockbreaker.graduatedcylinders.Proxy.IProxyFluidStack;
import bedrockbreaker.graduatedcylinders.Proxy.IProxyTankProperties;
import bedrockbreaker.graduatedcylinders.FluidHelper.FindTransferrableTankResult;
import bedrockbreaker.graduatedcylinders.Packets.PacketBlockTransferFluid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class FluidTransferGui extends GuiScreen {

	private boolean initialized = false;

	private final ItemStack heldItem;
	private final IProxyFluidHandler heldFluidHandler;
	private final World world;
	private final BlockPos pos;
	private final ItemStack blockItem;
	private IProxyFluidStack fluidStack;
	private TextureAtlasSprite fluidSprite;
	private EnumFacing selectedFace;
	private IProxyFluidHandler blockFluidHandler;
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

	public static void open(ItemStack heldItem, BlockPos pos, int side, FindTransferrableTankResult transferResults, IProxyFluidStack heldFluidStack, IProxyFluidStack blockFluidStack) {
		Minecraft.getMinecraft().displayGuiScreen(new FluidTransferGui(heldItem, pos, EnumFacing.getFront(side), transferResults, heldFluidStack, blockFluidStack));
	}

	public FluidTransferGui(ItemStack heldItem, BlockPos pos, EnumFacing side, FindTransferrableTankResult transferResults, IProxyFluidStack heldFluidStack, IProxyFluidStack blockFluidStack) {
		super();

		if (heldItem == null) throw new IllegalArgumentException(); // IDE complaint.

		Minecraft minecraft = Minecraft.getMinecraft();
		
		this.heldItem = heldItem;
		this.heldFluidHandler = FluidHelper.getProxyFluidHandler(heldItem);
		this.heldTankIndex = transferResults.leftTank;
		this.world = minecraft.world;
		this.pos = pos;
		this.blockFluidHandler = FluidHelper.getMatchingProxyFluidHandler(world, pos, side, this.heldFluidHandler);
		this.blockItem = this.pickBlock(pos);
		this.selectedFace = side;
		this.blockTankIndex = transferResults.rightTank;

		IProxyTankProperties heldFluidTank = this.heldFluidHandler.getTankProperties(heldTankIndex);
		IProxyTankProperties blockFluidTank = this.blockFluidHandler.getTankProperties(blockTankIndex);

		// Need to create excess variable because it yells at me for not checking for null.
		IProxyFluidStack fluidStack = heldFluidStack == null ? blockFluidStack : heldFluidStack;
		if (fluidStack == null) throw new NullPointerException();
		this.fluidStack = fluidStack;
		this.fluidSprite = fluidStack.getSprite();

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

		// TODO: allow changing faces and fluidHandlers from gui
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
		final String cmd = Minecraft.IS_RUNNING_ON_MAC ? ".cmd" : "";
		final int leftMargin = this.width - (this.fontRenderer.getStringWidth(I18n.format("gc.gui.100000mb.combo" + cmd)) + this.fontRenderer.getStringWidth(I18n.format("gc.gui.allmb")) + 10);
		final String heldAmount = I18n.format("gc.gui.amount.current", heldFluidHandler.getTankProperties(heldTankIndex).getContents() != null ? heldFluidHandler.getTankProperties(heldTankIndex).getContents().getAmount() : 0);
		final String blockAmount = I18n.format("gc.gui.amount.current", blockFluidHandler.getTankProperties(blockTankIndex).getContents() != null ? blockFluidHandler.getTankProperties(blockTankIndex).getContents().getAmount() : 0);

		this.drawDefaultBackground();
		this.textAmount.drawTextBox();
		super.drawScreen(mouseX, mouseY, partialTicks);

		// Fluid amounts and name above text field
		this.drawCenteredString(this.fontRenderer, heldAmount, centerX - 46 - Math.max(this.fontRenderer.getStringWidth(heldAmount) / 2 - 26, 0), centerY + 43, 0xAAAAAA);
		this.drawCenteredString(this.fontRenderer, blockAmount, centerX + 26 + Math.max(this.fontRenderer.getStringWidth(blockAmount) / 2 - 26, 0), centerY + 43, 0xAAAAAA);
		this.drawCenteredString(this.fontRenderer, ColorCache.getFluidColorCode(fluidStack, fluidStack.getColor()) + TextFormatting.UNDERLINE + this.fluidStack.getLocalizedName() + TextFormatting.RESET, centerX - 10, centerY - 50, 0xFFFFFF);
		this.drawString(this.fontRenderer, I18n.format("gc.gui.amount", this.amount/1000.0F), centerX - 56, centerY - 30, 0xAAAAAA);
		
		// Instructions in top-right corner
		this.drawRightAlignedString(I18n.format("gc.gui.instructions"), this.width - 5, 5, 0xAAAAAA);
		this.drawRightAlignedString(I18n.format("gc.gui.toggle", settings.keyBindJump.getDisplayName()), this.width - 5, 20, 0xAAAAAA);
		this.drawRightAlignedString(I18n.format("gc.gui.accept", settings.keyBindInventory.getDisplayName()), this.width - 5, 35, 0xAAAAAA);
		this.drawRightAlignedString(I18n.format("gc.gui.cancel"), this.width - 5, 50, 0xAAAAAA);
		
		// Combo shortcuts, left-aligned in bottom-right corner
		this.drawString(this.fontRenderer, I18n.format("gc.gui.1mb.combo" + cmd), leftMargin, this.height - 105, 0xAAAAAA);
		this.drawString(this.fontRenderer, I18n.format("gc.gui.10mb.combo" + cmd), leftMargin, this.height - 90, 0xAAAAAA);
		this.drawString(this.fontRenderer, I18n.format("gc.gui.100mb.combo"), leftMargin, this.height - 75, 0xAAAAAA);
		this.drawString(this.fontRenderer, I18n.format("gc.gui.1000mb.combo"), leftMargin, this.height - 60, 0xAAAAAA);
		this.drawString(this.fontRenderer, I18n.format("gc.gui.10000mb.combo"), leftMargin, this.height - 45, 0xAAAAAA);
		this.drawString(this.fontRenderer, I18n.format("gc.gui.100000mb.combo" + cmd), leftMargin, this.height - 30, 0xAAAAAA);
		this.drawString(this.fontRenderer, I18n.format("gc.gui.allmb.combo"), leftMargin, this.height - 15, 0xAAAAAA);
		
		// Combo fluid amounts, right-aligned in bottom-right corner
		this.drawRightAlignedString(I18n.format("gc.gui.1mb"), this.width - 5, this.height - 105, 0xAAAAAA);
		this.drawRightAlignedString(I18n.format("gc.gui.10mb"), this.width - 5, this.height - 90, 0xAAAAAA);
		this.drawRightAlignedString(I18n.format("gc.gui.100mb"), this.width - 5, this.height - 75, 0xAAAAAA);
		this.drawRightAlignedString(I18n.format("gc.gui.1000mb"), this.width - 5, this.height - 60, 0xAAAAAA);
		this.drawRightAlignedString(I18n.format("gc.gui.10000mb"), this.width - 5, this.height - 45, 0xAAAAAA);
		this.drawRightAlignedString(I18n.format("gc.gui.100000mb"), this.width - 5, this.height - 30, 0xAAAAAA);
		this.drawRightAlignedString(I18n.format("gc.gui.allmb"), this.width - 5, this.height - 15, 0xAAAAAA);
	
		// Itemstacks
		this.drawItemStack(this.heldItem, centerX - 61, centerY + 6);
		this.drawItemStack(this.blockItem, centerX + 10, centerY + 6);
		int color = this.fluidStack.getColor();
		GlStateManager.color((color >> 16 & 255) / 255f, (color >> 8 & 255) / 255f, (color & 255) / 255f);
		this.drawTexturedModalRect(centerX - 26, centerY - 92, this.fluidSprite, 32, 32);
		GlStateManager.color(1f, 1f, 1f);
		//if (!this.fluidItem.isEmpty()) this.drawItemStack(this.fluidItem, centerX - 26, centerY - 92);

		// Tooltips
		if (mouseX >= centerX - 62 && mouseX <= centerX - 30 && mouseY >= centerY + 6 && mouseY <= centerY + 38) this.renderToolTip(this.heldItem, mouseX, mouseY);
		if (mouseX >= centerX + 10 && mouseX <= centerX + 42 && mouseY >= centerY + 6 && mouseY <= centerY + 38) this.renderToolTip(this.blockItem, mouseX, mouseY);
		if (this.exportButton.isMouseOver()) this.drawHoveringText(I18n.format(this.forced ? "gc.gui.notoggle" : "gc.gui.yestoggle"), mouseX, mouseY);
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button == this.incFluidButton) {
			this.setAmount(this.amount + delta());
		} else if (button == this.decFluidButton) {
			this.setAmount(this.amount - delta());
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
		this.handleKeyInput(typedChar, keyCode);
	}

	@Override
	public void handleMouseInput() throws IOException {
		if (!this.initialized) return;

		final int scrollAmount = Mouse.getDWheel();
		if (scrollAmount != 0) this.setAmount(this.amount + delta() * MathHelper.clamp(scrollAmount, -1, 1));

		super.handleMouseInput();
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		this.textAmount.mouseClicked(mouseX, mouseY, mouseButton);
		this.handleKeyInput(Character.MIN_VALUE, mouseButton - 100);
	}

	private void handleKeyInput(char typedChar, int keyCode) {
		GameSettings settings = Minecraft.getMinecraft().gameSettings;

		if (keyCode == settings.keyBindInventory.getKeyCode()) {
			Minecraft.getMinecraft().displayGuiScreen(null);
			PacketHandler.INSTANCE.sendToServer(new PacketBlockTransferFluid(this.heldItem, this.heldTankIndex, this.pos, this.selectedFace.getIndex(), this.blockTankIndex, this.amount * (this.export ? -1 : 1)));
		} else if (keyCode == settings.keyBindJump.getKeyCode()) {
			if (this.forced) return;
			this.export = !this.export;
			this.exportButton.displayString = this.export ? "->" : "<-";
		} else if (keyCode == settings.keyBindForward.getKeyCode() || keyCode == 200) {  // Forward key or up arrow
			this.setAmount(this.amount + delta());
		} else if (keyCode == settings.keyBindBack.getKeyCode() || keyCode == 208) { // Back key or down arrow
			this.setAmount(this.amount - delta());
		} else if (keyCode == settings.keyBindLeft.getKeyCode()) {
			this.textAmount.textboxKeyTyped(Character.MIN_VALUE, 203); // Send a left arrow key press to the text box (to move the cursor)
		} else if (keyCode == settings.keyBindRight.getKeyCode()) {
			this.textAmount.textboxKeyTyped(Character.MIN_VALUE, 205); // Send right arrow key ^^
		} else if (NumberUtils.isDigits(Character.toString(typedChar)) || !ChatAllowedCharacters.isAllowedCharacter(typedChar)) { // Digits and non-printable characters (backspace, etc.)
			this.textAmount.textboxKeyTyped(typedChar, keyCode);
			Integer num = Ints.tryParse(StringUtils.defaultIfEmpty(this.textAmount.getText(), "0"));
			if (num == null) num = Integer.MAX_VALUE;
			this.amount = MathHelper.clamp(num, 0, this.max);
			if (num != this.amount) this.textAmount.setText(Integer.toString(this.amount)); // If text representation overflows integer
			
			if (!this.textAmount.getText().startsWith("0") || !NumberUtils.isDigits(Character.toString(typedChar))) return; // If digit is typed and text starts with zeros
			final int cursorPos = this.textAmount.getCursorPosition();
			int leadCount = 0;
			while (leadCount < this.textAmount.getText().length() - 1 && this.textAmount.getText().charAt(leadCount) == '0') leadCount++;
			this.textAmount.setText(this.textAmount.getText().substring(leadCount));
			this.textAmount.setCursorPosition(Math.max(cursorPos - leadCount, 0));
		}
	}

	private void drawRightAlignedString(String text, int x, int y, int color) {
		this.drawString(this.fontRenderer, text, x - this.fontRenderer.getStringWidth(text), y, color);
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

	private void setAmount(int amountIn) {
		this.amount = MathHelper.clamp(amountIn, 0, this.max);
		this.textAmount.setText(Integer.toString(this.amount));
	}

	private int delta() {
		return isShiftKeyDown() ? (isCtrlKeyDown() ? (isAltKeyDown() ? 100000 : 1) : (isAltKeyDown() ? 10000 : 100)) : (isCtrlKeyDown() ? 10 : (isAltKeyDown() ? this.max : 1000));
	}
}
