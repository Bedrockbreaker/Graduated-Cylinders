package bedrockbreaker.graduatedcylinders.proxy.meta;

import bedrockbreaker.graduatedcylinders.api.MetaHandler;
import bedrockbreaker.graduatedcylinders.proxy.handler.GasHandlerItem;
import mekanism.api.gas.IGasItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class MetaGasHandler extends MetaHandler {

	@Override
	public boolean hasHandler(ItemStack itemStack) {
		return !itemStack.isEmpty() && itemStack.getItem() instanceof IGasItem;
	}

	@Override
	public GasHandlerItem getHandler(ItemStack itemStack) {
		if (itemStack.isEmpty()) return null;
		Item item = itemStack.getItem();
		
		return item instanceof IGasItem ? new GasHandlerItem((IGasItem) item, itemStack) : null;
	}
}