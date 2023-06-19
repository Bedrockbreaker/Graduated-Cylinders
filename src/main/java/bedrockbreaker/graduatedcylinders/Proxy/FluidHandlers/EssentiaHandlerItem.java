package bedrockbreaker.graduatedcylinders.Proxy.FluidHandlers;

import javax.annotation.Nullable;

import bedrockbreaker.graduatedcylinders.Proxy.FluidStacks.EssentiaStack;
import bedrockbreaker.graduatedcylinders.Proxy.FluidStacks.IProxyFluidStack;
import bedrockbreaker.graduatedcylinders.Proxy.TankProperties.EssentiaTankPropertiesItem;
import bedrockbreaker.graduatedcylinders.Proxy.TankProperties.IProxyTankProperties;
import net.minecraft.item.ItemStack;
import thaumcraft.api.aspects.IEssentiaContainerItem;

public class EssentiaHandlerItem implements IProxyFluidHandlerItem {

	protected IEssentiaContainerItem essentiaHandlerItem;
	protected ItemStack itemStack;

	public EssentiaHandlerItem(IEssentiaContainerItem essentiaHandlerItem, ItemStack itemStack) {
		this.essentiaHandlerItem = essentiaHandlerItem;
		this.itemStack = itemStack;
	}

	public IProxyTankProperties getTankProperties(int tankIndex) {
		return new EssentiaTankPropertiesItem(this.essentiaHandlerItem, this.itemStack);
	}

	public int getNumTanks() {
		return this.essentiaHandlerItem.getAspects(this.itemStack).size();
	}

	public int fill(IProxyFluidStack resource, boolean doFill) {
		// TODO: fill function for essentia item handlers
		return 0;
	}

	@Nullable
	public EssentiaStack drain(int maxAmount, boolean doDrain) {
		// TODO: drain function for essentia item handlers
		return null;
	}

	@Nullable
	public EssentiaStack drain(IProxyFluidStack resource, boolean doDrain) {
		// TODO: drain function for essentia item handlers
		return null;
	}

	public ItemStack getContainer() {
		return this.itemStack;
	}
}