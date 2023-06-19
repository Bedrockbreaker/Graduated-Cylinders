package bedrockbreaker.graduatedcylinders.Proxy.FluidHandlers;

import javax.annotation.Nullable;

import bedrockbreaker.graduatedcylinders.Proxy.FluidStacks.FluidStackGC;
import bedrockbreaker.graduatedcylinders.Proxy.FluidStacks.IProxyFluidStack;
import bedrockbreaker.graduatedcylinders.Proxy.TankProperties.FluidTankProperties;
import bedrockbreaker.graduatedcylinders.Proxy.TankProperties.IProxyTankProperties;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class FluidHandler implements IProxyFluidHandler {

	protected IFluidHandler fluidHandler;

	public FluidHandler(IFluidHandler fluidHandler) {
		this.fluidHandler = fluidHandler;
	}

	public IProxyTankProperties getTankProperties(int tankIndex) {
		return new FluidTankProperties(this.fluidHandler.getTankProperties()[tankIndex]);
	}

	public int getNumTanks() {
		return this.fluidHandler.getTankProperties().length;
	}

	public int fill(IProxyFluidStack resource, boolean doFill) {
		if (!(resource instanceof FluidStackGC)) return 0;
		return this.fluidHandler.fill(((FluidStackGC) resource).fluidStack, doFill);
	}

	@Nullable
	public FluidStackGC drain(int maxAmount, boolean doDrain) {
		FluidStack removedFluid = this.fluidHandler.drain(maxAmount, doDrain);
		return removedFluid == null ? null : new FluidStackGC(removedFluid);
	}

	@Nullable
	public FluidStackGC drain(IProxyFluidStack resource, boolean doDrain) {
		if (!(resource instanceof FluidStackGC)) return null;
		FluidStack removedFluid = this.fluidHandler.drain(((FluidStackGC) resource).fluidStack, doDrain);
		return removedFluid == null ? null : new FluidStackGC(removedFluid);
	}
}