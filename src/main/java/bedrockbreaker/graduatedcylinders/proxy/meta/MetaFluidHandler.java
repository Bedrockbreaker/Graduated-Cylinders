package bedrockbreaker.graduatedcylinders.proxy.meta;

import bedrockbreaker.graduatedcylinders.api.MetaHandler;
import bedrockbreaker.graduatedcylinders.proxy.handler.FluidHandlerItem;
import bedrockbreaker.graduatedcylinders.proxy.mode.IngotMode;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.IFluidContainerItem;

public class MetaFluidHandler extends MetaHandler {

	public MetaFluidHandler() {
		super();
		this.modes.add(IngotMode.INSTANCE);
	}

	@Override
	public boolean hasHandler(ItemStack itemStack) {
		return itemStack != null && (itemStack.getItem() instanceof IFluidContainerItem || FluidContainerRegistry.isContainer(itemStack));
	}

	@Override
	public FluidHandlerItem getHandler(ItemStack itemStack) {
		return itemStack == null || itemStack.getItem() == null || itemStack.stackSize != 1 ? null : (itemStack.getItem() instanceof IFluidContainerItem ? new FluidHandlerItem((IFluidContainerItem) itemStack.getItem(), itemStack) : (FluidContainerRegistry.isContainer(itemStack) ? new FluidHandlerItem(itemStack) : null));
	}
}