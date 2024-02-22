package bedrockbreaker.graduatedcylinders.proxy.handler;

import javax.annotation.Nullable;

import bedrockbreaker.graduatedcylinders.api.IProxyFluidHandler;
import bedrockbreaker.graduatedcylinders.api.IProxyFluidStack;
import bedrockbreaker.graduatedcylinders.api.IProxyTankProperties;
import bedrockbreaker.graduatedcylinders.proxy.stack.FluidStackGC;
import bedrockbreaker.graduatedcylinders.proxy.tankproperties.FluidTankProperties;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;

public class FluidHandler implements IProxyFluidHandler {

	protected IFluidHandler fluidHandler;
	protected EnumFacing side;

	public FluidHandler(IFluidHandler fluidHandler, EnumFacing side) {
		this.fluidHandler = fluidHandler;
		this.side = side;
	}

	public FluidStackGC loadFluidStackFromNBT(NBTTagCompound nbt) {
		FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(nbt);
		return fluidStack == null ? null : new FluidStackGC(fluidStack);
	}

	public boolean isMatchingHandlerType(TileEntity tileEntity, EnumFacing side) {
		return tileEntity instanceof IFluidHandler;
	}

	public IProxyTankProperties getTankProperties(int tankIndex) {
		return new FluidTankProperties(this.fluidHandler.getTankInfo(ForgeDirection.getOrientation(this.side.ordinal()))[tankIndex], this.side);
	}

	public int getNumTanks() {
		return this.fluidHandler.getTankInfo(ForgeDirection.getOrientation(this.side.ordinal())).length;
	}

	public int fill(IProxyFluidStack resource, boolean doFill) {
		if (!(resource instanceof FluidStackGC)) return 0;
		return this.fluidHandler.fill(ForgeDirection.getOrientation(this.side.ordinal()), ((FluidStackGC) resource).fluidStack, doFill);
	}

	@Nullable
	public FluidStackGC drain(int maxAmount, boolean doDrain) {
		FluidStack removedFluid = this.fluidHandler.drain(ForgeDirection.getOrientation(this.side.ordinal()), maxAmount, doDrain);
		return removedFluid == null ? null : new FluidStackGC(removedFluid);
	}

	@Nullable
	public FluidStackGC drain(IProxyFluidStack resource, boolean doDrain) {
		if (!(resource instanceof FluidStackGC)) return null;
		FluidStack removedFluid = this.fluidHandler.drain(ForgeDirection.getOrientation(this.side.ordinal()), ((FluidStackGC) resource).fluidStack, doDrain);
		return removedFluid == null ? null : new FluidStackGC(removedFluid);
	}
}