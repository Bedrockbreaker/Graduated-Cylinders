package bedrockbreaker.graduatedcylinders.proxy.tankproperties;

import bedrockbreaker.graduatedcylinders.api.IProxyFluidStack;
import bedrockbreaker.graduatedcylinders.api.IProxyTankProperties;
import bedrockbreaker.graduatedcylinders.proxy.stack.FluidStackGC;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidTankInfo;

public class FluidTankProperties implements IProxyTankProperties {

	protected FluidTankInfo fluidTankProperties;
	protected ForgeDirection side;

	public FluidTankProperties(FluidTankInfo fluidTankProperties, ForgeDirection side) {
		this.fluidTankProperties = fluidTankProperties;
	}

	public FluidStackGC getContents() {
		return this.fluidTankProperties.fluid == null ? null : new FluidStackGC(this.fluidTankProperties.fluid);
	}

	public int getCapacity(IProxyFluidStack fluidStack) {
		if (!(fluidStack instanceof FluidStackGC)) return 0;
		return this.fluidTankProperties.capacity;
	}
}