package bedrockbreaker.graduatedcylinders.proxy.tankproperties;

import bedrockbreaker.graduatedcylinders.api.IProxyFluidStack;
import bedrockbreaker.graduatedcylinders.api.IProxyTankProperties;
import bedrockbreaker.graduatedcylinders.proxy.stack.FluidStackGC;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;

public class FluidTankPropertiesItem implements IProxyTankProperties {

	protected IFluidContainerItem fluidHandlerItem;
	protected ItemStack itemStack;
	protected boolean isSimple = false;

	public FluidTankPropertiesItem(IFluidContainerItem fluidHandlerItem, ItemStack itemStack) {
		this.fluidHandlerItem = fluidHandlerItem;
		this.itemStack = itemStack;
	}

	public FluidTankPropertiesItem(ItemStack itemStack) {
		this.itemStack = itemStack;
		this.isSimple = true;
	}

	public FluidStackGC getContents() {
		FluidStack fluidStack = this.isSimple ? FluidContainerRegistry.getFluidForFilledItem(this.itemStack) : this.fluidHandlerItem.getFluid(this.itemStack);
		return fluidStack == null ? null : new FluidStackGC(fluidStack);
	}

	public int getCapacity(IProxyFluidStack fluidStack) {
		if (!(fluidStack instanceof FluidStackGC)) return 0;
		return this.isSimple ? FluidContainerRegistry.getContainerCapacity(((FluidStackGC) fluidStack).fluidStack, this.itemStack) : this.fluidHandlerItem.getCapacity(this.itemStack);
	}
}