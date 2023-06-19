package bedrockbreaker.graduatedcylinders.Proxy;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

public class FluidHandlerItem extends FluidHandler implements IProxyFluidHandlerItem {

	protected IFluidHandlerItem fluidHandlerItem;

	public FluidHandlerItem(IFluidHandlerItem fluidHandlerItem) {
		super(fluidHandlerItem);
		this.fluidHandlerItem = fluidHandlerItem;
	}

	public ItemStack getContainer() {
		return this.fluidHandlerItem.getContainer();
	}
}