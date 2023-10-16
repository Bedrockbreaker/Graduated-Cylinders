package bedrockbreaker.graduatedcylinders.proxy.meta;

import bedrockbreaker.graduatedcylinders.api.MetaHandler;
import bedrockbreaker.graduatedcylinders.proxy.handler.EssentiaHandlerItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import thaumcraft.api.aspects.IEssentiaContainerItem;

public class MetaEssentiaHandler extends MetaHandler {

	@Override
	public boolean hasHandler(ItemStack itemStack) {
		return itemStack != null && itemStack.getItem() instanceof IEssentiaContainerItem;
	}

	@Override
	public EssentiaHandlerItem getHandler(ItemStack itemStack) {
		if (itemStack == null) return null;
		Item item = itemStack.getItem();
		
		return item instanceof IEssentiaContainerItem ? new EssentiaHandlerItem((IEssentiaContainerItem) item, itemStack) : null;
	}
}