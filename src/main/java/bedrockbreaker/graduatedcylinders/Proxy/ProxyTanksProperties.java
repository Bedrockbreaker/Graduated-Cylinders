package bedrockbreaker.graduatedcylinders.Proxy;

import bedrockbreaker.graduatedcylinders.Proxy.ProxyFluidHandler.ProxyType;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTankInfo;
import mekanism.api.gas.IGasItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.aspects.IEssentiaContainerItem;

/**
 * A list of tanks and their properties
 */
public class ProxyTanksProperties {
	
	protected ProxyType type;
	protected ItemStack itemStack;
	protected TileEntity tileEntity;

	protected IFluidTankProperties[] fluidTankProperties;

	protected GasTankInfo[] gasTankProperties;
	protected IGasItem gasItem;

	protected IAspectContainer essentiaTank;
	protected IEssentiaContainerItem essentiaItem;

	public ProxyTanksProperties(IFluidTankProperties[] fluidTankProperties) {
		this.type = ProxyType.FLUID;
		this.fluidTankProperties = fluidTankProperties;
	}

	public ProxyTanksProperties(GasTankInfo[] gasTankProperties) {
		this.type = ProxyType.GAS;
		this.gasTankProperties = gasTankProperties;
	}

	public ProxyTanksProperties(IGasItem gasItem, ItemStack gasItemStack) {
		this.type = ProxyType.GASITEM;
		this.gasItem = gasItem;
		this.itemStack = gasItemStack;
	}

	public ProxyTanksProperties(IAspectContainer essentiaTank, TileEntity tileEntity) {
		this.type = ProxyType.ESSENTIA;
		this.essentiaTank = essentiaTank;
		this.tileEntity = tileEntity;
	}

	public ProxyTanksProperties(IEssentiaContainerItem essentiaItem, ItemStack essentiaItemStack) {
		this.type = ProxyType.ESSENTIAITEM;
		this.essentiaItem = essentiaItem;
		this.itemStack = essentiaItemStack;
	}

	public ProxyTankProperties get(int tankIndex) {
		switch (this.type) {
			case FLUID:
				return new ProxyTankProperties(this.fluidTankProperties[tankIndex]);
			case GAS:
				return new ProxyTankProperties(this.gasTankProperties[tankIndex]);
			case GASITEM:
				return new ProxyTankProperties(this.gasItem, this.itemStack);
			case ESSENTIA: // Duplicate the tile entity but keep only the aspect at the specified tankIndex
				TileEntity tileEntity = TileEntity.create(this.tileEntity.getWorld(), this.tileEntity.getTileData());
				IAspectContainer essentiaTank = (IAspectContainer) tileEntity;
				if (essentiaTank == null) return null; // IDE complaint
				Aspect aspect = this.essentiaTank.getAspects().getAspects()[tankIndex];
				essentiaTank.setAspects(new AspectList().add(aspect, this.essentiaTank.getAspects().getAmount(aspect)));
				return new ProxyTankProperties(essentiaTank, this.tileEntity);
			case ESSENTIAITEM:
				// TODO: essentia
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
			case ESSENTIA:
				return this.essentiaTank.getAspects().size();
			case ESSENTIAITEM:
				return this.essentiaItem.getAspects(this.itemStack).size();
			default:
				return 0;
		}
	}

	/**
	 * Properties about a singular tank
	 */
	public class ProxyTankProperties {

		protected ProxyType type;
		protected ItemStack itemStack;
		protected TileEntity tileEntity;

		protected IFluidTankProperties fluidTankProperties;

		protected GasTankInfo gasTankProperties;
		protected IGasItem gasItem;

		protected IAspectContainer essentiaTank;
		protected IEssentiaContainerItem essentiaItem;

		private ProxyTankProperties(IFluidTankProperties fluidTankProperties) {
			this.type = ProxyType.FLUID;
			this.fluidTankProperties = fluidTankProperties;
		}

		private ProxyTankProperties(GasTankInfo gasTankProperties) {
			this.type = ProxyType.GAS;
			this.gasTankProperties = gasTankProperties;
		}

		private ProxyTankProperties(IGasItem gasItem, ItemStack gasItemStack) {
			this.type = ProxyType.GASITEM;
			this.gasItem = gasItem;
			this.itemStack = gasItemStack;
		}

		private ProxyTankProperties(IAspectContainer essentiaTank, TileEntity tileEntity) {
			this.type = ProxyType.ESSENTIA;
			this.essentiaTank = essentiaTank;
			this.tileEntity = tileEntity;
		}

		private ProxyTankProperties(IEssentiaContainerItem essentiaItem, ItemStack essentiaItemStack) {
			this.type = ProxyType.ESSENTIAITEM;
			this.essentiaItem = essentiaItem;
			this.itemStack = essentiaItemStack;
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
					GasStack gasStack2 = this.gasItem.getGas(this.itemStack);
					return gasStack2 == null ? null : new ProxyFluidStack(gasStack2);
				case ESSENTIA:
					return new ProxyFluidStack(this.essentiaTank.getAspects());
				case ESSENTIAITEM:
					return new ProxyFluidStack(this.essentiaItem.getAspects(this.itemStack));
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
					return this.gasItem.getMaxGas(this.itemStack);
				case ESSENTIA: // Duplicate the tile entity, remove all aspects, and attempt to transfer as much essentia as possible.
					TileEntity tileEntity = TileEntity.create(this.tileEntity.getWorld(), this.tileEntity.getTileData());
					IAspectContainer essentiaTank = (IAspectContainer) tileEntity;
					if (essentiaTank == null) return 0; // IDE complaint
					essentiaTank.setAspects(new AspectList());
					/*
					FIXME: block essentia capacity
					warded jars apparently check if the amount transfered is over 250, and if it is, just skips trying to transfer any.
					brilliant code, azanor.
					could possibly be worked around by attempting to add 1 essentia at a time until #addToContainer returns a non-zero value.
					*/
					// IAspectContainer#addToContainer returns amount of essentia which could *not* be added
					return Integer.MAX_VALUE - essentiaTank.addToContainer(this.essentiaTank.getAspects().getAspects()[0], Integer.MAX_VALUE);
				case ESSENTIAITEM:
					/*
					FIXME: item essentia capacity
					there is no way to grab the capacity of an item.
					and unless I want to harcode everything and break compat with TC addons, essentia transport is sadly a no-go.
					*/
					/*
					ItemStack itemStack = this.itemStack.copy();
					IEssentiaContainerItem essentiaItem = (IEssentiaContainerItem) itemStack.getItem();
					essentiaItem.setAspects(itemStack, new AspectList());
					return Integer.MAX_VALUE - essentiaItem.
					*/
				default:
					return 0;
			}
		}
	}
}
