package bedrockbreaker.graduatedcylinders.Proxy;

import javax.annotation.Nullable;

import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasItem;
import net.minecraft.item.ItemStack;

public class GasHandlerItem implements IProxyFluidHandlerItem {

	protected IGasItem gasHandlerItem;
	protected ItemStack itemStack;

	public GasHandlerItem(IGasItem gasHandlerItem, ItemStack itemStack) {
		this.gasHandlerItem = gasHandlerItem;
		this.itemStack = itemStack;
	}

	public IProxyTankProperties getTankProperties(int tankIndex) {
		return new GasTankPropertiesItem(this.gasHandlerItem, this.itemStack);
	}

	public int getNumTanks() {
		return 1;
	}

	// Mekanism adds emulated receiveGas/drawGas for tile entity gas handlers, but not one for items??
	// Also, yes, I'm bypassing the rate limiting on gas transfer. No, I don't care.

	public int fill(IProxyFluidStack resource, boolean doFill) {
		if (!(resource instanceof GasStackGC)) return 0;
		GasStack resourceStack = ((GasStackGC) resource).gasStack;
		GasStack storedGas = this.gasHandlerItem.getGas(this.itemStack);
		if (resourceStack == null || resource.getAmount() <= 0 || (storedGas != null && !resourceStack.isGasEqual(storedGas))) return 0;
		int toFill = Math.min(this.gasHandlerItem.getMaxGas(this.itemStack) - (storedGas == null ? 0 : storedGas.amount), resource.getAmount());
		if (toFill > 0 && doFill) this.gasHandlerItem.setGas(this.itemStack, new GasStack(resourceStack.getGas(), toFill + (storedGas == null ? 0 : storedGas.amount)));
		return toFill;
	}

	@Nullable
	public GasStackGC drain(int maxAmount, boolean doDrain) {
		if (this.gasHandlerItem.getGas(this.itemStack) == null || maxAmount <= 0) return null;
		GasStack removedGas = new GasStack(this.gasHandlerItem.getGas(this.itemStack).getGas(), Math.min(this.gasHandlerItem.getGas(this.itemStack).amount, maxAmount));
		if (removedGas.amount <= 0) return null;
		if (doDrain) this.gasHandlerItem.setGas(this.itemStack, new GasStack(removedGas.getGas(), this.gasHandlerItem.getGas(this.itemStack).amount - removedGas.amount));
		return new GasStackGC(removedGas);
	}

	@Nullable
	public GasStackGC drain(IProxyFluidStack resource, boolean doDrain) {
		if (!(resource instanceof GasStackGC)) return null;
		GasStack resourceStack = ((GasStackGC) resource).gasStack;
		if (resourceStack == null || resource.getAmount() <= 0 || !resourceStack.isGasEqual(this.gasHandlerItem.getGas(this.itemStack))) return null;
		GasStack removedGas = new GasStack(this.gasHandlerItem.getGas(this.itemStack).getGas(), Math.min(this.gasHandlerItem.getGas(this.itemStack).amount, resource.getAmount()));
		if (removedGas.amount <= 0) return null;
		if (doDrain) this.gasHandlerItem.setGas(this.itemStack, new GasStack(removedGas.getGas(), this.gasHandlerItem.getGas(this.itemStack).amount - removedGas.amount));
		return new GasStackGC(removedGas);
	}

	public ItemStack getContainer() {
		return this.itemStack;
	}
}