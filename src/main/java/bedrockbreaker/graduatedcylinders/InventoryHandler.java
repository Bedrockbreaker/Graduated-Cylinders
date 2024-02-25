package bedrockbreaker.graduatedcylinders;

import org.lwjgl.input.Mouse;

import bedrockbreaker.graduatedcylinders.api.IProxyFluidHandler;
import bedrockbreaker.graduatedcylinders.api.IProxyFluidStack;
import bedrockbreaker.graduatedcylinders.api.MetaHandler;
import bedrockbreaker.graduatedcylinders.network.PacketContainerTransferFluid;
import bedrockbreaker.graduatedcylinders.network.PacketHandler;
import bedrockbreaker.graduatedcylinders.util.ColorCache;
import bedrockbreaker.graduatedcylinders.util.FluidHelper;
import bedrockbreaker.graduatedcylinders.util.TextFormatting;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;
import net.minecraftforge.client.event.GuiScreenEvent;

@SideOnly(Side.CLIENT)
public class InventoryHandler {

	public static boolean clicked = false;

	@SubscribeEvent
	public void onDrawScreen(GuiScreenEvent.DrawScreenEvent.Post event) {
		Minecraft minecraft = Minecraft.getMinecraft();
		if (!(minecraft.currentScreen instanceof GuiContainer)) return;
		GuiContainer container = (GuiContainer) minecraft.currentScreen;

		Slot hoveredSlot = getSlotUnderMouse(container, event.mouseX, event.mouseY);
		if (hoveredSlot == null || !hoveredSlot.canTakeStack(minecraft.thePlayer)) return;

		MetaHandler metaHandler = FluidHelper.getMetaHandler(minecraft.thePlayer.inventory.getItemStack());
		if (metaHandler == null || !metaHandler.hasHandler(hoveredSlot.getStack())) return;
		IProxyFluidHandler heldFluidHandler = metaHandler.getHandler(minecraft.thePlayer.inventory.getItemStack());
		IProxyFluidHandler underFluidHandler = metaHandler.getHandler(hoveredSlot.getStack());

		final int transferAmount = FluidHelper.getTransferAmount(heldFluidHandler, underFluidHandler);
		if (transferAmount == 0) return;

		IProxyFluidStack fluid = heldFluidHandler.getTankProperties(0).getContents();
		if (fluid == null) fluid = underFluidHandler.getTankProperties(0).getContents();
		if (fluid == null) throw new NullPointerException(); // Shouldn't ever throw.

		
		minecraft.fontRenderer.drawStringWithShadow(I18n.format("gc.inventory.rightclick", transferAmount < 0 ? "->" : "<-", ColorCache.getFluidColorCode(fluid, fluid.getColor()) + TextFormatting.BLACK + metaHandler.modes.get(0).formatAmount(Math.abs(transferAmount), false) + TextFormatting.RESET), event.mouseX, event.mouseY, 0xFFFFFF);
	}

	@SubscribeEvent
	public void onRightClick(InputEvent.MouseInputEvent event) {
		Minecraft minecraft = Minecraft.getMinecraft();
		if (!(minecraft.currentScreen instanceof GuiContainer) || Mouse.getEventButton() != 1) return;
		GuiContainer container = (GuiContainer) minecraft.currentScreen;

		if (!Mouse.getEventButtonState()) { // Mouse up
			event.setCanceled(clicked);
			clicked = false;
			return;
		}
		
		Slot hoveredSlot = getSlotUnderMouse(container, Mouse.getX(), Mouse.getY());
		if (hoveredSlot == null || !hoveredSlot.canTakeStack(minecraft.thePlayer)) return;
		
		if (FluidHelper.getTransferAmount(FluidHelper.getProxyFluidHandler(minecraft.thePlayer.inventory.getItemStack()), FluidHelper.getProxyFluidHandler(hoveredSlot.getStack())) == 0) return;
		
		InventoryHandler.clicked = true;
		PacketHandler.INSTANCE.sendToServer(new PacketContainerTransferFluid(hoveredSlot.slotNumber));
		event.setCanceled(true);
	}

	private Slot getSlotUnderMouse(GuiContainer container, int mouseX, int mouseY) {
		for (int i = 0; i < container.inventorySlots.inventorySlots.size(); i++) {
			Slot slot = container.inventorySlots.getSlot(i);
			if (mouseX >= slot.xDisplayPosition && mouseX < slot.xDisplayPosition + 16 && mouseY >= slot.yDisplayPosition && mouseY < slot.yDisplayPosition + 16) return slot;
		}
		return null;
	}
}