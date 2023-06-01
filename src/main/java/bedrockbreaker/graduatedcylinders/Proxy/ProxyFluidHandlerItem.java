package bedrockbreaker.graduatedcylinders.Proxy;

import mekanism.api.gas.IGasItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import thaumcraft.api.aspects.IEssentiaContainerItem;

public class ProxyFluidHandlerItem extends ProxyFluidHandler {

	protected IFluidHandlerItem fluidHandlerItem;

	public ProxyFluidHandlerItem(IFluidHandlerItem fluidHandlerItem) {
		super(fluidHandlerItem);
		this.fluidHandlerItem = fluidHandlerItem;
	}

	public ProxyFluidHandlerItem(IGasItem gasHandlerItem, ItemStack gasItemStack) {
		super(gasHandlerItem, gasItemStack);
	}

	public ProxyFluidHandlerItem(IEssentiaContainerItem essentiaHandlerItem, ItemStack essentiaItemStack) {
		super(essentiaHandlerItem, essentiaItemStack);
	}

	public ItemStack getContainer() {
		switch (this.type) {
			case FLUID:
				return this.fluidHandlerItem.getContainer();
			case GASITEM:
			case ESSENTIAITEM:
				return itemStack;
			default:
				return null;
		}
	}
}
