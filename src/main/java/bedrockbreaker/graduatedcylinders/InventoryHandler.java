package bedrockbreaker.graduatedcylinders;

import org.lwjgl.input.Mouse;

import bedrockbreaker.graduatedcylinders.api.IProxyFluidHandler;
import bedrockbreaker.graduatedcylinders.api.IProxyFluidStack;
import bedrockbreaker.graduatedcylinders.network.PacketContainerTransferFluid;
import bedrockbreaker.graduatedcylinders.network.PacketHandler;
import bedrockbreaker.graduatedcylinders.util.ColorCache;
import bedrockbreaker.graduatedcylinders.util.FluidHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@EventBusSubscriber(value = Side.CLIENT, modid = GraduatedCylinders.MODID)
public class InventoryHandler {

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public static void onDrawScreen(GuiScreenEvent.DrawScreenEvent.Post event) {
		Minecraft minecraft = Minecraft.getMinecraft();
		GuiScreen screen = minecraft.currentScreen;
		if (!(screen instanceof GuiContainer)) return;

		Slot hoveredSlot = ((GuiContainer) screen).getSlotUnderMouse();
		if (hoveredSlot == null || !hoveredSlot.isEnabled() || !hoveredSlot.canTakeStack(minecraft.player)) return;

		IProxyFluidHandler heldFluidHandler = FluidHelper.getProxyFluidHandler(minecraft.player.inventory.getItemStack());
		IProxyFluidHandler underFluidHandler = FluidHelper.getProxyFluidHandler(hoveredSlot.getStack());

		final int transferAmount = FluidHelper.getTransferAmount(heldFluidHandler, underFluidHandler);
		if (transferAmount == 0) return;

		IProxyFluidStack fluid = heldFluidHandler.getTankProperties(0).getContents();
		if (fluid == null) fluid = underFluidHandler.getTankProperties(0).getContents();
		if (fluid == null) throw new NullPointerException(); // IDE complaint

		screen.drawHoveringText(I18n.format("gc.inventory.rightclick", transferAmount < 0 ? "->" : "<-", ColorCache.getFluidColorCode(fluid, fluid.getColor()) + TextFormatting.BOLD, Math.abs(transferAmount), TextFormatting.RESET), event.getMouseX(), event.getMouseY());
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public static void onRightClick(GuiScreenEvent.MouseInputEvent.Pre event) {
		if (Mouse.getEventButton() != 1 || !Mouse.getEventButtonState()) return;

		Minecraft minecraft = Minecraft.getMinecraft();
		GuiScreen screen = minecraft.currentScreen;
		if (!(screen instanceof GuiContainer)) return;

		Slot hoveredSlot = ((GuiContainer) screen).getSlotUnderMouse();
		if (hoveredSlot == null || !hoveredSlot.isEnabled() || !hoveredSlot.canTakeStack(minecraft.player)) return;

		if (FluidHelper.getTransferAmount(FluidHelper.getProxyFluidHandler(minecraft.player.inventory.getItemStack()), FluidHelper.getProxyFluidHandler(hoveredSlot.getStack())) == 0) return;

		PacketHandler.INSTANCE.sendToServer(new PacketContainerTransferFluid(hoveredSlot.slotNumber));
		// There seems to be a vanilla bug which causes inserting/swapping items with right click to ignore the event cancellation.
		// This means the following line really doesn't do anything, but it should in a perfect world...
		event.setCanceled(true);
	}
}