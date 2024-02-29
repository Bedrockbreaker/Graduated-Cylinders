package bedrockbreaker.graduatedcylinders.proxy.handler;

import javax.annotation.Nullable;

import bedrockbreaker.graduatedcylinders.api.IProxyFluidHandlerItem;
import bedrockbreaker.graduatedcylinders.api.IProxyFluidStack;
import bedrockbreaker.graduatedcylinders.proxy.stack.FluidStackGC;
import bedrockbreaker.graduatedcylinders.proxy.tankproperties.FluidTankPropertiesItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import net.minecraftforge.fluids.IFluidHandler;

public class FluidHandlerItem implements IProxyFluidHandlerItem {

	protected IFluidContainerItem fluidHandlerItem;
	protected ItemStack itemStack;
	protected boolean isSimple = false;

	public FluidHandlerItem(IFluidContainerItem fluidHandlerItem, ItemStack itemStack) {
		this.fluidHandlerItem = fluidHandlerItem;
		this.itemStack = itemStack;
	}

	public FluidHandlerItem(ItemStack itemStack) {
		this.itemStack = itemStack;
		this.isSimple = true;
	}

	public FluidStackGC loadFluidStackFromNBT(NBTTagCompound nbt) {
		FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(nbt);
		return fluidStack == null ? null : new FluidStackGC(fluidStack);
	}

	public boolean isMatchingHandlerType(TileEntity tileEntity, ForgeDirection side) {
		return tileEntity instanceof IFluidHandler;
	}

	public FluidHandler getMatchingHandler(TileEntity tileEntity, ForgeDirection side) {
		return new FluidHandler((IFluidHandler) tileEntity, side);
	}

	public FluidTankPropertiesItem getTankProperties(int tankIndex) {
		return this.isSimple ? new FluidTankPropertiesItem(this.itemStack) : new FluidTankPropertiesItem(this.fluidHandlerItem, this.itemStack);
	}

	public int getNumTanks() {
		return 1;
	}

	public int fill(IProxyFluidStack resource, boolean doFill) {
		if (!(resource instanceof FluidStackGC)) return 0;

		if (!this.isSimple) return this.fluidHandlerItem.fill(this.itemStack, ((FluidStackGC) resource).fluidStack, doFill);
		
		ItemStack filledContainer = FluidContainerRegistry.fillFluidContainer(((FluidStackGC) resource).fluidStack, this.itemStack.copy());
		if (filledContainer != null && doFill) this.itemStack = filledContainer;
		return filledContainer == null ? 0 : FluidContainerRegistry.getContainerCapacity(filledContainer);
	}

	@Nullable
	public FluidStackGC drain(int maxAmount, boolean doDrain) {
		if (!this.isSimple) {
			FluidStack removedFluid = this.fluidHandlerItem.drain(this.itemStack, maxAmount, doDrain);
			return removedFluid == null ? null : new FluidStackGC(removedFluid);
		}

		FluidStack currentFluid = FluidContainerRegistry.getFluidForFilledItem(this.itemStack);
		if (currentFluid == null || currentFluid.amount > maxAmount) return null;
		ItemStack drainedContainer = FluidContainerRegistry.drainFluidContainer(this.itemStack.copy());
		if (drainedContainer != null && doDrain) this.itemStack = drainedContainer;
		return drainedContainer == null ? null : new FluidStackGC(currentFluid);
	}

	@Nullable
	public FluidStackGC drain(IProxyFluidStack resource, boolean doDrain) {
		if (!(resource instanceof FluidStackGC)) return null;
		FluidStackGC fluid = (FluidStackGC) resource;

		if (!this.isSimple) {
			if (!fluid.fluidStack.isFluidEqual(this.fluidHandlerItem.getFluid(this.itemStack))) return null;
			FluidStack removedFluid = this.fluidHandlerItem.drain(this.itemStack, fluid.getAmount(), doDrain);
			return removedFluid == null ? null : new FluidStackGC(removedFluid);
		}

		FluidStack currentFluid = FluidContainerRegistry.getFluidForFilledItem(this.itemStack);
		if (currentFluid == null || !fluid.fluidStack.isFluidEqual(currentFluid) || currentFluid.amount > fluid.getAmount()) return null;
		ItemStack drainedConatiner = FluidContainerRegistry.drainFluidContainer(this.itemStack.copy());
		if (drainedConatiner != null && doDrain) this.itemStack = drainedConatiner;
		return drainedConatiner == null ? null : new FluidStackGC(currentFluid);
	}

	public ItemStack getContainer() {
		return this.itemStack;
	}
}