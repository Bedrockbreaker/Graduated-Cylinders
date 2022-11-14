package bedrockbreaker.graduatedcylinders;

import bedrockbreaker.graduatedcylinders.Packets.PacketHandler;
import bedrockbreaker.graduatedcylinders.Packets.PacketInventoryOpen;
import bedrockbreaker.graduatedcylinders.Packets.PacketOpenFluidGUI;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Events {

	private ContainerListener listener;
	private boolean playerInventoryAttached;

	@SubscribeEvent
	public void onBlockClicked(PlayerInteractEvent.LeftClickBlock event) {
		// Make sure we're running on the logical server and the player isn't in creative mode.
		if (event.getWorld().isRemote || event.getEntityPlayer().isCreative()) return;

		// Grab the fluid handlers from the tank and container, if they exist.
		final IFluidHandler tankHandler = FluidUtil.getFluidHandler(event.getWorld(), event.getPos(), event.getFace());
		if (tankHandler == null) return;
		final IFluidHandler containerHandler = FluidUtil.getFluidHandler(event.getItemStack());
		if (containerHandler == null) return;

		final IFluidTankProperties tankProps = tankHandler.getTankProperties()[0];
		final IFluidTankProperties containerProps = containerHandler.getTankProperties()[0];

		// Return if at least one of the tanks aren't empty, or they don't have matching fluids.
		final FluidStack tankContents = tankProps.getContents();
		final FluidStack containerContents = containerProps.getContents();
		if (tankContents != null && containerContents != null && !tankContents.isFluidEqual(containerContents)) return;

		// Return if both tanks are full or empty
		final int tankAmt = tankContents == null ? 0 : tankContents.amount;
		final int containerAmt = containerContents == null ? 0 : containerContents.amount;
		if ((tankAmt == tankProps.getCapacity() && containerAmt == containerProps.getCapacity()) || (tankAmt + containerAmt == 0)) return;

		// Define the transer direction (export = container -> tank), and whether that direction is forced.
		boolean export = event.getEntityPlayer().isSneaking();
		boolean forced = false;
		if (tankAmt == 0 || containerAmt == containerProps.getCapacity()) {
			export = true;
			forced = true;
		} else if (containerAmt == 0 || tankAmt == tankProps.getCapacity()) {
			export = false;
			forced = true;
		}

		final int max = Math.min(containerProps.getCapacity(), tankProps.getCapacity());
		final int defaultAmt = export ? Math.min(tankProps.getCapacity() - tankAmt, containerAmt) : Math.min(containerProps.getCapacity() - containerAmt, tankAmt);

		PacketHandler.INSTANCE.sendTo(new PacketOpenFluidGUI(event.getPos(), event.getItemStack(), export, forced, defaultAmt, max), (EntityPlayerMP)event.getEntityPlayer());
	}

	@SubscribeEvent
	public void onPlayerInventoryOpen(GuiOpenEvent event) {
		if (!(event.getGui() instanceof GuiInventory) || this.playerInventoryAttached) return;
		this.playerInventoryAttached = true;
		PacketHandler.INSTANCE.sendToServer(new PacketInventoryOpen());
	}

	@SubscribeEvent
	public void onGuiOpen(PlayerContainerEvent.Open event) {
		this.listener = new ContainerListener(event.getEntityPlayer());
		event.getContainer().addListener(this.listener);
	}

	@SubscribeEvent
	public void onGuiClose(PlayerContainerEvent.Close event) {
		if (!(event.getContainer() instanceof ContainerPlayer)) event.getContainer().removeListener(this.listener);
	}
}
