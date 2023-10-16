package bedrockbreaker.graduatedcylinders.proxy.handler;

import javax.annotation.Nullable;

import bedrockbreaker.graduatedcylinders.api.IProxyFluidHandler;
import bedrockbreaker.graduatedcylinders.api.IProxyFluidHandlerItem;
import bedrockbreaker.graduatedcylinders.api.IProxyFluidStack;
import bedrockbreaker.graduatedcylinders.api.IProxyTankProperties;
import bedrockbreaker.graduatedcylinders.proxy.stack.EssentiaStack;
import bedrockbreaker.graduatedcylinders.proxy.tankproperties.EssentiaTankPropertiesItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
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

	@Override
	public IProxyFluidStack loadFluidStackFromNBT(NBTTagCompound nbt) {
		// TODO: load essentia stack from nbt
		throw new UnsupportedOperationException("Unimplemented method 'loadFluidStackFromNBT'");
	}

	@Override
	public boolean isMatchingHandlerType(TileEntity tileEntity, EnumFacing side) {
		// TODO: isMatchingHandlerType for essentia
		throw new UnsupportedOperationException("Unimplemented method 'isMatchingHandlerType'");
	}

	@Override
	public IProxyFluidHandler getMatchingHandler(TileEntity tileEntity, EnumFacing side) {
		// TODO: getMatchingHandler for essentia
		throw new UnsupportedOperationException("Unimplemented method 'getMatchingHandler'");
	}
}
