package bedrockbreaker.graduatedcylinders.proxy.tankproperties;

import bedrockbreaker.graduatedcylinders.api.IProxyTankProperties;
import bedrockbreaker.graduatedcylinders.proxy.stack.GasStackGC;
import mekanism.api.gas.IGasItem;
import net.minecraft.item.ItemStack;

public class GasTankPropertiesItem implements IProxyTankProperties {

	protected IGasItem gasHandlerItem;
	protected ItemStack itemStack;

	public GasTankPropertiesItem(IGasItem gasHandlerItem, ItemStack itemStack) {
		this.gasHandlerItem = gasHandlerItem;
		this.itemStack = itemStack;
	}

	public GasStackGC getContents() {
		return this.gasHandlerItem.getGas(this.itemStack) == null ? null : new GasStackGC(this.gasHandlerItem.getGas(this.itemStack));
	}

	public int getCapacity() {
		return this.gasHandlerItem.getMaxGas(this.itemStack);
	}
}
