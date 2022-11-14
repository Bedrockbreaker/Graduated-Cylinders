package bedrockbreaker.graduatedcylinders;

import bedrockbreaker.graduatedcylinders.Packets.PacketHandler;
import bedrockbreaker.graduatedcylinders.Packets.PacketUpdateMouseSlot;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

public class ContainerListener implements IContainerListener {

	private EntityPlayer player;
	private int slot;

	public ContainerListener(EntityPlayer player) {
		this.player = player;
	}

	@Override
	@SuppressWarnings("null")
	public void sendSlotContents(Container containerToSend, int slotInd, ItemStack stack) {
		if (!GuiScreen.isCtrlKeyDown()) return;
		// Because we re-swap the contents of the cursor and slot, it fires this event again. This if-statement makes sure we don't end up in an infinite-loop.
		if (this.slot == slotInd) {
			this.slot = -1;
			return;
		}

		final ItemStack mouse = this.player.inventory.getItemStack();
		if (stack.getCount() > 1 || mouse.getCount() > 1) return;

		// The ItemStack Fluid Handler held in the mouse
		IFluidHandlerItem containerHandler = FluidUtil.getFluidHandler(mouse);
		if (containerHandler == null) return;
		// The ItemStack Fluid Handler in the inventory
		IFluidHandlerItem tankHandler = FluidUtil.getFluidHandler(stack);
		if (tankHandler == null) return;
		
		IFluidTankProperties containerProps = containerHandler.getTankProperties()[0];
		IFluidTankProperties tankProps = tankHandler.getTankProperties()[0];
		FluidStack containerContents = containerProps.getContents();
		FluidStack tankContents = tankProps.getContents();
		// Return if at least one of the tanks aren't empty, or they don't have matching fluids.
		if (containerContents != null && tankContents != null && !containerContents.isFluidEqual(tankContents)) return;
		
		int containerAmt = containerContents == null ? 0 : containerContents.amount;
		int tankAmt = tankContents == null ? 0 : tankContents.amount;
		// Return if both tanks are full or empty
		if ((containerAmt == containerProps.getCapacity() && tankAmt == tankProps.getCapacity()) || containerAmt + tankAmt == 0) return;

		final boolean export = tankAmt == 0 || containerAmt == containerProps.getCapacity() ? true : false;
		final int transferAmt = export ? Math.min(tankProps.getCapacity() - tankAmt, containerAmt) : Math.min(containerProps.getCapacity() - containerAmt, tankAmt);

		if (export) {
			if (FluidUtil.tryFluidTransfer(tankHandler, containerHandler, transferAmt, true) != null) {
				this.slot = slotInd;
				this.player.world.playSound(null, this.player.getPosition(), SoundEvents.ITEM_BUCKET_FILL, SoundCategory.PLAYERS, 1.0F, 1.0F);
			}
		} else {
			if (FluidUtil.tryFluidTransfer(containerHandler, tankHandler, transferAmt, true) != null) {
				this.slot = slotInd;
				this.player.world.playSound(null, this.player.getPosition(), SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.PLAYERS, 1.0F, 1.0F);
			}
		}
		// Swap the mouse and inventory items (essentially making them not swap in the first place)
		containerToSend.putStackInSlot(slotInd, FluidUtil.getFluidHandler(mouse).getContainer());
		this.player.inventory.setItemStack(FluidUtil.getFluidHandler(stack).getContainer());
		PacketHandler.INSTANCE.sendTo(new PacketUpdateMouseSlot(player.inventory.getItemStack()), (EntityPlayerMP)this.player);
	}

	@Override
	public void sendAllContents(Container containerToSend, NonNullList<ItemStack> itemsList) {}

	@Override
	public void sendWindowProperty(Container containerIn, int varToUpdate, int newValue) {}

	@Override
	public void sendAllWindowProperties(Container containerIn, IInventory inventory) {}
}
