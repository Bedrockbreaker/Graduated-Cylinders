package bedrockbreaker.graduatedcylinders.proxy.handler;

import javax.annotation.Nullable;

import bedrockbreaker.graduatedcylinders.api.IProxyFluidHandlerItem;
import bedrockbreaker.graduatedcylinders.api.IProxyFluidStack;
import bedrockbreaker.graduatedcylinders.proxy.stack.FluidStackGC;
import bedrockbreaker.graduatedcylinders.proxy.tankproperties.FluidTankPropertiesItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import net.minecraftforge.fluids.IFluidHandler;

public class FluidHandlerItem implements IProxyFluidHandlerItem {

	// TODO: add special fix for buckets

	protected IFluidContainerItem fluidHandlerItem;
	protected ItemStack itemStack;

	public FluidHandlerItem(IFluidContainerItem fluidHandlerItem, ItemStack itemStack) {
		this.fluidHandlerItem = fluidHandlerItem;
		this.itemStack = itemStack;
	}

	public FluidStackGC loadFluidStackFromNBT(NBTTagCompound nbt) {
		FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(nbt);
		return fluidStack == null ? null : new FluidStackGC(fluidStack);
	}

	public boolean isMatchingHandlerType(TileEntity tileEntity, EnumFacing side) {
		return tileEntity instanceof IFluidHandler;
	}

	public FluidHandler getMatchingHandler(TileEntity tileEntity, EnumFacing side) {
		return new FluidHandler((IFluidHandler) tileEntity, side);
	}

	public FluidTankPropertiesItem getTankProperties(int tankIndex) {
		return new FluidTankPropertiesItem(this.fluidHandlerItem, this.itemStack);
	}

	public int getNumTanks() {
		return 1;
	}

	public int fill(IProxyFluidStack resource, boolean doFill) {
		if (!(resource instanceof FluidStackGC)) return 0;
		return this.fluidHandlerItem.fill(this.itemStack, ((FluidStackGC) resource).fluidStack, doFill);
	}

	@Nullable
	public FluidStackGC drain(int maxAmount, boolean doDrain) {
		FluidStack removedFluid = this.fluidHandlerItem.drain(this.itemStack, maxAmount, doDrain);
		return removedFluid == null ? null : new FluidStackGC(removedFluid);
	}

	@Nullable
	public FluidStackGC drain(IProxyFluidStack resource, boolean doDrain) {
		if (!(resource instanceof FluidStackGC) || !this.fluidHandlerItem.getFluid(this.itemStack).isFluidEqual(((FluidStackGC) resource).fluidStack)) return null;
		FluidStack removedFluid = this.fluidHandlerItem.drain(this.itemStack, ((FluidStackGC) resource).getAmount(), doDrain);
		return removedFluid == null ? null : new FluidStackGC(removedFluid);
	}

	public ItemStack getContainer() {
		return this.itemStack;
	}
}