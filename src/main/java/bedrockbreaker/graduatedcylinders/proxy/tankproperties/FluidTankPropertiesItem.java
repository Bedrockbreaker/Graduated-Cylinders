package bedrockbreaker.graduatedcylinders.proxy.tankproperties;

import bedrockbreaker.graduatedcylinders.api.IProxyTankProperties;
import bedrockbreaker.graduatedcylinders.proxy.stack.FluidStackGC;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.IFluidContainerItem;

public class FluidTankPropertiesItem implements IProxyTankProperties {

	protected IFluidContainerItem fluidHandlerItem;
	protected ItemStack itemStack;

	public FluidTankPropertiesItem(IFluidContainerItem fluidHandlerItem, ItemStack itemStack) {
		this.fluidHandlerItem = fluidHandlerItem;
		this.itemStack = itemStack;
	}

	public FluidStackGC getContents() {
		return this.fluidHandlerItem.getFluid(this.itemStack) == null ? null : new FluidStackGC(this.fluidHandlerItem.getFluid(this.itemStack));
	}

	public int getCapacity() {
		return this.fluidHandlerItem.getCapacity(this.itemStack);
	}
}