package bedrockbreaker.graduatedcylinders.Proxy;

import javax.annotation.Nullable;

import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasHandler;
import mekanism.api.gas.IGasItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class ProxyFluidHandler {

	public static enum ProxyType {
		FLUID,
		GAS,
		GASITEM
	}

	protected ProxyType type;

	protected IFluidHandler fluidHandler;

	protected IGasHandler gasHandler;
	protected EnumFacing side;

	protected IGasItem gasHandlerItem;
	protected ItemStack gasItemStack;

	public ProxyFluidHandler(IFluidHandler fluidHandler) {
		this.type = ProxyType.FLUID;
		this.fluidHandler = fluidHandler;
	}

	public ProxyFluidHandler(IGasHandler gasHandler, EnumFacing side) {
		this.type = ProxyType.GAS;
		this.gasHandler = gasHandler;
		this.side = side;
	}

	public ProxyFluidHandler(IGasItem gasHandlerItem, ItemStack gasItemStack) {
		this.type = ProxyType.GASITEM;
		this.gasHandlerItem = gasHandlerItem;
		this.gasItemStack = gasItemStack;
	}

	public ProxyType getType() {
		return this.type;
	}

	public ProxyTanksProperties getTankProperties() {
		switch (this.type) {
			case FLUID:
				return new ProxyTanksProperties(this.fluidHandler.getTankProperties());
			case GAS:
				return new ProxyTanksProperties(this.gasHandler.getTankInfo());
			case GASITEM:
				return new ProxyTanksProperties(this.gasHandlerItem, this.gasItemStack);
			default:
				return null;
		}
	}

	public int fill(ProxyFluidStack resource, boolean doFill) {
		switch (this.type) {
			case FLUID:
				return this.fluidHandler.fill(resource.fluidStack, doFill);
			case GAS:
				return this.gasHandler.receiveGas(this.side, resource.gasStack, doFill);
			case GASITEM:
				// Mekanism adds emulated receiveGas for tile entity gas handlers, but not one for items??
				// Also, yes, I'm bypassing the rate limiting on gas transfer. No, I don't care.
				GasStack storedGas = this.gasHandlerItem.getGas(this.gasItemStack);
				if (resource == null || resource.gasStack == null || resource.amount <= 0 || (storedGas != null && !resource.gasStack.isGasEqual(storedGas))) return 0;
				int toFill = Math.min(this.gasHandlerItem.getMaxGas(this.gasItemStack) - (storedGas == null ? 0 : storedGas.amount), resource.amount);
				if (toFill > 0 && doFill) this.gasHandlerItem.setGas(this.gasItemStack, new GasStack(resource.gasStack.getGas(), toFill + (storedGas == null ? 0 : storedGas.amount)));
				return toFill;
			default:
				return 0;
		}
	}

	@Nullable
	public ProxyFluidStack drain(int maxAmount, boolean doDrain) {
		switch (this.type) {
			case FLUID:
				FluidStack removedFluid = this.fluidHandler.drain(maxAmount, doDrain);
				return removedFluid == null ? null : new ProxyFluidStack(removedFluid);
			case GAS:
				GasStack removedGas = this.gasHandler.drawGas(this.side, maxAmount, doDrain);
				return removedGas == null ? null : new ProxyFluidStack(removedGas);
			case GASITEM:
				// Mekanism adds emulated drawGas for tile entity gas handlers, but not one for items??
				// Also, yes, I'm bypassing the rate limiting on gas transfer. No, I don't care.
				if (this.gasHandlerItem.getGas(this.gasItemStack) == null || maxAmount <= 0) return null;
				GasStack removedGas2 = new GasStack(this.gasHandlerItem.getGas(this.gasItemStack).getGas(), Math.min(this.gasHandlerItem.getGas(this.gasItemStack).amount, maxAmount));
				if (removedGas2.amount <= 0) return null;
				if (doDrain) this.gasHandlerItem.setGas(this.gasItemStack, new GasStack(removedGas2.getGas(), this.gasHandlerItem.getGas(this.gasItemStack).amount - removedGas2.amount));
				return new ProxyFluidStack(removedGas2);
			default:
				return null;
		}
	}

	@Nullable
	public ProxyFluidStack drain(ProxyFluidStack resource, boolean doDrain) {
		switch (this.type) {
			case FLUID:
				FluidStack removedFluid = this.fluidHandler.drain(resource.fluidStack, doDrain);
				return removedFluid == null ? null : new ProxyFluidStack(removedFluid);
			case GAS:
				GasStack removedGas = this.gasHandler.drawGas(this.side, resource.amount, doDrain);
				return removedGas == null ? null : new ProxyFluidStack(removedGas);
			case GASITEM:
				// Mekanism adds emulated drawGas for tile entity gas handlers, but not one for items??
				// Also, yes, I'm bypassing the rate limiting on gas transfer. No, I don't care.
				if (resource == null || resource.gasStack == null || resource.amount <= 0 || !resource.gasStack.isGasEqual(this.gasHandlerItem.getGas(this.gasItemStack))) return null;
				GasStack removedGas2 = new GasStack(this.gasHandlerItem.getGas(this.gasItemStack).getGas(), Math.min(this.gasHandlerItem.getGas(this.gasItemStack).amount, resource.amount));
				if (removedGas2.amount <= 0) return null;
				if (doDrain) this.gasHandlerItem.setGas(this.gasItemStack, new GasStack(removedGas2.getGas(), this.gasHandlerItem.getGas(this.gasItemStack).amount - removedGas2.amount));
				return new ProxyFluidStack(removedGas2);
			default:
				return null;
		}
	}

	public String toString() {
		String prefix = "[" + this.type + " Handler] ";
		switch (this.type) {
			case FLUID:
				return prefix + this.fluidHandler;
			case GAS:
				return prefix + this.gasHandler;
			case GASITEM:
				return prefix + this.gasHandlerItem;
			default:
				return "[Uh, oh! Broken ProxyFluidHandler!]";
		}
	}
}