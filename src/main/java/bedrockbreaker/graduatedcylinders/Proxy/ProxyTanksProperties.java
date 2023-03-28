package bedrockbreaker.graduatedcylinders.Proxy;

import bedrockbreaker.graduatedcylinders.Proxy.ProxyFluidHandler.ProxyType;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTankInfo;
import mekanism.api.gas.IGasItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

public class ProxyTanksProperties {
	
	protected ProxyType type;
	protected IFluidTankProperties[] fluidTankProperties;
	protected GasTankInfo[] gasTankProperties;
	protected IGasItem gasItemProperties;
	protected ItemStack gasItemStack;

	public ProxyTanksProperties(IFluidTankProperties[] fluidTankProperties) {
		this.type = ProxyType.FLUID;
		this.fluidTankProperties = fluidTankProperties;
	}

	public ProxyTanksProperties(GasTankInfo[] gasTankProperties) {
		this.type = ProxyType.GAS;
		this.gasTankProperties = gasTankProperties;
	}

	public ProxyTanksProperties(IGasItem gasItemProperties, ItemStack gasItemStack) {
		this.type = ProxyType.GASITEM;
		this.gasItemProperties = gasItemProperties;
		this.gasItemStack = gasItemStack;
	}

	public ProxyTankProperties get(int tankIndex) {
		switch (this.type) {
			case FLUID:
				return new ProxyTankProperties(this.fluidTankProperties[tankIndex]);
			case GAS:
				return new ProxyTankProperties(this.gasTankProperties[tankIndex]);
			case GASITEM:
				return new ProxyTankProperties(this.gasItemProperties, this.gasItemStack);
			default:
				return null;
		}
	}

	public int getLength() {
		switch (this.type) {
			case FLUID:
				return this.fluidTankProperties.length;
			case GAS:
				return this.gasTankProperties.length;
			case GASITEM:
				return 1;
			default:
				return 0;
			
		}
	}

	public class ProxyTankProperties {

		protected ProxyType type;
		protected IFluidTankProperties fluidTankProperties;
		protected GasTankInfo gasTankProperties;
		protected IGasItem gasItemProperties;
		protected ItemStack gasItemStack;

		private ProxyTankProperties(IFluidTankProperties fluidTankProperties) {
			this.type = ProxyType.FLUID;
			this.fluidTankProperties = fluidTankProperties;
		}

		private ProxyTankProperties(GasTankInfo gasTankProperties) {
			this.type = ProxyType.GAS;
			this.gasTankProperties = gasTankProperties;
		}

		private ProxyTankProperties(IGasItem gasItemProperties, ItemStack gasItemStack) {
			this.type = ProxyType.GASITEM;
			this.gasItemProperties = gasItemProperties;
			this.gasItemStack = gasItemStack;
		}

		public ProxyFluidStack getContents() {
			switch (this.type) {
				case FLUID:
					FluidStack fluidStack = this.fluidTankProperties.getContents();
					return fluidStack == null ? null : new ProxyFluidStack(fluidStack);
				case GAS:
					GasStack gasStack = this.gasTankProperties.getGas();
					return gasStack == null ? null : new ProxyFluidStack(gasStack);
				case GASITEM:
					GasStack gasStack2 = this.gasItemProperties.getGas(this.gasItemStack);
					return gasStack2 == null ? null : new ProxyFluidStack(gasStack2);
				default:
					return null;
			}
		}

		public int getCapacity() {
			switch (this.type) {
				case FLUID:
					return this.fluidTankProperties.getCapacity();
				case GAS:
					return this.gasTankProperties.getMaxGas();
				case GASITEM:
					return this.gasItemProperties.getMaxGas(this.gasItemStack);
				default:
					return 0;
			}
		}
	}
}
