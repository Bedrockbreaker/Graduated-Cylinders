package bedrockbreaker.graduatedcylinders.proxy.meta;

import bedrockbreaker.graduatedcylinders.api.MetaHandler;
import bedrockbreaker.graduatedcylinders.proxy.handler.FluidHandlerItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidUtil;

public class MetaFluidHandler extends MetaHandler {

	@Override
	public boolean hasHandler(ItemStack itemStack) {
		return itemStack != null && FluidUtil.getFluidHandler(itemStack) != null;
	}

	@Override
	public FluidHandlerItem getHandler(ItemStack itemStack) {
		return itemStack == null ? null : new FluidHandlerItem(FluidUtil.getFluidHandler(itemStack));
	}
}