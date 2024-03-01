package bedrockbreaker.graduatedcylinders;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import bedrockbreaker.graduatedcylinders.api.IProxyFluidHandler;
import bedrockbreaker.graduatedcylinders.api.IProxyFluidStack;
import bedrockbreaker.graduatedcylinders.api.MetaHandler;
import bedrockbreaker.graduatedcylinders.util.ColorCache;
import bedrockbreaker.graduatedcylinders.util.FluidHelper;
import bedrockbreaker.graduatedcylinders.util.TextFormatting;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiScreenEvent;

@SideOnly(Side.CLIENT)
public class InventoryHandler {

	private static Method isMouseOverSlot;
	private static boolean triedReflection_isMouseOverSlot = false;
	private static Method drawHoveringText;
	private static boolean triedReflection_drawHoveringText = false;

	public Minecraft minecraft = Minecraft.getMinecraft();
	public boolean clicked = false;

	private static void reflect_isMouseOverSlot() {
		try {
			InventoryHandler.isMouseOverSlot = GuiContainer.class.getDeclaredMethod(GraduatedCylinders.IN_DEV ? "isMouseOverSlot" : "func_146981_a", Slot.class, int.class, int.class);
			InventoryHandler.isMouseOverSlot.setAccessible(true);
		} catch (Exception ignored) {}

		InventoryHandler.triedReflection_isMouseOverSlot = true;
	}

	private static void reflect_drawHoveringText() {
		try {
			// Doesn't have a human-readable name in a dev environment
			InventoryHandler.drawHoveringText = GuiScreen.class.getDeclaredMethod("func_146283_a", List.class, int.class, int.class);
			InventoryHandler.drawHoveringText.setAccessible(true);
		} catch (Exception ignored) {}

		InventoryHandler.triedReflection_drawHoveringText = true;
	}

	@SubscribeEvent
	public void onDrawScreen(GuiScreenEvent.DrawScreenEvent.Post event) {
		if (!(this.minecraft.currentScreen instanceof GuiContainer)) return;
		GuiContainer container = (GuiContainer) minecraft.currentScreen;

		ItemStack heldStack = this.minecraft.thePlayer.inventory.getItemStack();
		if (heldStack == null || heldStack.getItem() == null || heldStack.stackSize != 1) return;

		Slot hoveredSlot = getSlotUnderMouse(container, event.mouseX, event.mouseY);
		if (hoveredSlot == null || !hoveredSlot.getHasStack() || hoveredSlot.getStack().getItem() == null || !hoveredSlot.canTakeStack(minecraft.thePlayer)) return;

		MetaHandler metaHandler = FluidHelper.getMetaHandler(minecraft.thePlayer.inventory.getItemStack());
		if (metaHandler == null || !metaHandler.hasHandler(hoveredSlot.getStack())) return;
		IProxyFluidHandler heldFluidHandler = metaHandler.getHandler(minecraft.thePlayer.inventory.getItemStack());
		IProxyFluidHandler underFluidHandler = metaHandler.getHandler(hoveredSlot.getStack());

		int transferAmount = FluidHelper.getTransferAmount(heldFluidHandler, underFluidHandler);
		if (transferAmount == 0) return;

		IProxyFluidStack fluid = heldFluidHandler.getTankProperties(0).getContents();
		if (fluid == null) fluid = underFluidHandler.getTankProperties(0).getContents();
		if (fluid == null) throw new NullPointerException(); // Shouldn't ever throw.

		this.drawHoveringText(this.minecraft.currentScreen, Arrays.asList(I18n.format("gc.inventory.rightclick", transferAmount < 0 ? "->" : "<-", ColorCache.getFluidColorCode(fluid, fluid.getColor()) + TextFormatting.BOLD + metaHandler.modes.get(0).formatAmount(Math.abs(transferAmount), false) + TextFormatting.RESET)), event.mouseX, event.mouseY);
	}

	private Slot getSlotUnderMouse(GuiContainer container, int mouseX, int mouseY) {
		if (!InventoryHandler.triedReflection_isMouseOverSlot) reflect_isMouseOverSlot();
		if (InventoryHandler.isMouseOverSlot == null) return null;
		
		try {
			for (int i = 0; i < container.inventorySlots.inventorySlots.size(); i++) {
				Slot slot = container.inventorySlots.getSlot(i);
				if ((boolean) isMouseOverSlot.invoke(container, slot, mouseX, mouseY)) return slot;
			}
		} catch (Exception ignored) {}
		return null;
	}

	private void drawHoveringText(GuiScreen screen, List<String> textLines, int x, int y) {
		if (!InventoryHandler.triedReflection_drawHoveringText) reflect_drawHoveringText();
		if (InventoryHandler.drawHoveringText == null) return;

		try {
			InventoryHandler.drawHoveringText.invoke(screen, textLines, x, y);
		} catch (Exception ignored) {}
	}
}