package bedrockbreaker.graduatedcylinders.Proxy.TankProperties;

import bedrockbreaker.graduatedcylinders.Proxy.FluidStacks.EssentiaStack;
import net.minecraft.item.ItemStack;
import thaumcraft.api.aspects.IEssentiaContainerItem;

public class EssentiaTankPropertiesItem implements IProxyTankProperties {

	protected IEssentiaContainerItem essentiaHandlerItem;
	protected ItemStack itemStack;

	public EssentiaTankPropertiesItem(IEssentiaContainerItem essentiaHandlerItem, ItemStack itemStack) {
		this.essentiaHandlerItem = essentiaHandlerItem;
		this.itemStack = itemStack;
	}

	public EssentiaStack getContents() {
		return new EssentiaStack(this.essentiaHandlerItem.getAspects(this.itemStack));
	}

	public int getCapacity() {
		/*
		FIXME: essentia item capacity
		there is physically no way to grab the capacity of an item.
		and unless I want to harcode everything, essentia transport is sadly a no-go.
		Injecting capabilities would probably make this easier, though that still requires manually supporting every TC addon.
		*/
		/*
		ItemStack itemStack = this.itemStack.copy();
		IEssentiaContainerItem essentiaItem = (IEssentiaContainerItem) itemStack.getItem();
		essentiaItem.setAspects(itemStack, new AspectList());
		return Integer.MAX_VALUE - essentiaItem.
		*/
		return 0;
	}
}
