package bedrockbreaker.graduatedcylinders.proxy.tankproperties;

import bedrockbreaker.graduatedcylinders.api.IProxyTankProperties;
import bedrockbreaker.graduatedcylinders.proxy.stack.FluidStackGC;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidTankInfo;

public class FluidTankProperties implements IProxyTankProperties {

	protected FluidTankInfo fluidTankProperties;
	protected EnumFacing side;

	public FluidTankProperties(FluidTankInfo fluidTankProperties, EnumFacing side) {
		this.fluidTankProperties = fluidTankProperties;
	}

	public FluidStackGC getContents() {
		return this.fluidTankProperties.fluid == null ? null : new FluidStackGC(this.fluidTankProperties.fluid);
	}

	public int getCapacity() {
		return this.fluidTankProperties.capacity;
	}
}