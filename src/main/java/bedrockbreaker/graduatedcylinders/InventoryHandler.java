package bedrockbreaker.graduatedcylinders;

import org.lwjgl.input.Mouse;

import bedrockbreaker.graduatedcylinders.Packets.PacketContainerTransferFluid;
import bedrockbreaker.graduatedcylinders.Packets.PacketHandler;
import bedrockbreaker.graduatedcylinders.Proxy.ProxyFluidHandler;
import bedrockbreaker.graduatedcylinders.Proxy.ProxyFluidStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@EventBusSubscriber(value = Side.CLIENT, modid = GraduatedCylinders.MODID)
public class InventoryHandler {

	// All text formatting colors, except black and dark blue (for readability purposes)
	private static final int[][] colors = {{0, 170, 0}, {0, 170, 170}, {170, 0, 0}, {170, 0, 170}, {255, 170, 0}, {170, 170, 170}, {85, 85, 85}, {85, 85, 255}, {85, 255, 85}, {85, 255, 255}, {255, 85, 85}, {255, 85, 255}, {255, 255, 85}, {255, 255, 255}};
	private static int fluidColorCache = -1;
	private static String colorCodeCache = "0";

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public static void onDrawScreen(GuiScreenEvent.DrawScreenEvent.Post event) {
		Minecraft minecraft = Minecraft.getMinecraft();
		GuiScreen screen = minecraft.currentScreen;
		if (!(screen instanceof GuiContainer)) return;

		Slot hoveredSlot = ((GuiContainer) screen).getSlotUnderMouse();
		if (hoveredSlot == null) return;

		ProxyFluidHandler heldFluidHandler = FluidHelper.getProxyFluidHandler(minecraft.player.inventory.getItemStack());
		ProxyFluidHandler underFluidHandler = FluidHelper.getProxyFluidHandler(hoveredSlot.getStack());

		final int transferAmount = FluidHelper.getTransferAmount(heldFluidHandler, underFluidHandler);
		if (transferAmount == 0) return;

		ProxyFluidStack fluid = heldFluidHandler.getTankProperties().get(0).getContents();
		if (fluid == null) fluid = underFluidHandler.getTankProperties().get(0).getContents();
		if (fluid == null) throw new NullPointerException(); // IDE complaint

		final int color = fluid.getColor() & 0xFFFFFF; // Remove the alpha channel
		if (color != fluidColorCache) {
			fluidColorCache = color;
			final int red = (color >> 16) & 0xFF;
			final int green = (color >> 8) & 0xFF;
			final int blue = color & 0xFF;

			int minDistance = Integer.MAX_VALUE;
			int chosenColor = -1;
			for (int i = 0; i < colors.length; i++) {
				final int[] c = colors[i];
				final int distanceSquared = (c[0] - red) * (c[0] - red) + (c[1] - green) * (c[1] -green) + (c[2] - blue) * (c[2] - blue);
				if (distanceSquared < minDistance) {
					minDistance = distanceSquared;
					chosenColor = i;
				}
			}

			colorCodeCache = Integer.toHexString(chosenColor + 2); // Range 2-9A-F
		}

		screen.drawHoveringText(I18n.format("gc.inventory.rightclick", transferAmount < 0 ? "->" : "<-", "\u00A7" + colorCodeCache + "\u00A7l", Math.abs(transferAmount), "\u00A7r"), event.getMouseX(), event.getMouseY());
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public static void onRightClick(GuiScreenEvent.MouseInputEvent.Pre event) {
		if (Mouse.getEventButton() != 1 || !Mouse.getEventButtonState()) return;

		Minecraft minecraft = Minecraft.getMinecraft();
		GuiScreen screen = minecraft.currentScreen;
		if (!(screen instanceof GuiContainer)) return;

		Slot hoveredSlot = ((GuiContainer) screen).getSlotUnderMouse();
		if (hoveredSlot == null) return;

		if (FluidHelper.getTransferAmount(FluidHelper.getProxyFluidHandler(minecraft.player.inventory.getItemStack()), FluidHelper.getProxyFluidHandler(hoveredSlot.getStack())) == 0) return;

		event.setCanceled(true);
		PacketHandler.INSTANCE.sendToServer(new PacketContainerTransferFluid(hoveredSlot.slotNumber));
	}
}