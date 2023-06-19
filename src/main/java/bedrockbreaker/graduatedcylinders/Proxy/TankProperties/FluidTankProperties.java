package bedrockbreaker.graduatedcylinders.Proxy.TankProperties;

import bedrockbreaker.graduatedcylinders.Proxy.FluidStacks.FluidStackGC;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

public class FluidTankProperties implements IProxyTankProperties {

	protected IFluidTankProperties fluidTankProperties;

	public FluidTankProperties(IFluidTankProperties fluidTankProperties) {
		this.fluidTankProperties = fluidTankProperties;
	}

	public FluidStackGC getContents() {
		return this.fluidTankProperties.getContents() == null ? null : new FluidStackGC(this.fluidTankProperties.getContents());
	}

	public int getCapacity() {
		return this.fluidTankProperties.getCapacity();
	}
}
