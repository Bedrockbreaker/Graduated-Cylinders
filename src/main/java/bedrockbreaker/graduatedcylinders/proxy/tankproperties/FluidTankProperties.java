package bedrockbreaker.graduatedcylinders.proxy.tankproperties;

import bedrockbreaker.graduatedcylinders.api.IProxyTankProperties;
import bedrockbreaker.graduatedcylinders.proxy.stack.FluidStackGC;
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
