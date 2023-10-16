package bedrockbreaker.graduatedcylinders.proxy.handler;

import javax.annotation.Nullable;

import bedrockbreaker.graduatedcylinders.api.IProxyFluidHandler;
import bedrockbreaker.graduatedcylinders.api.IProxyFluidStack;
import bedrockbreaker.graduatedcylinders.api.IProxyTankProperties;
import bedrockbreaker.graduatedcylinders.proxy.stack.EssentiaStack;
import bedrockbreaker.graduatedcylinders.proxy.tankproperties.EssentiaTankProperties;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import thaumcraft.api.aspects.IAspectContainer;

public class EssentiaHandler implements IProxyFluidHandler {
	
	protected IAspectContainer essentiaHandler;
	protected TileEntity tileEntity;
	protected EnumFacing side;

	public EssentiaHandler(IAspectContainer essentiaHandler, TileEntity tileEntity, EnumFacing side) {
		this.essentiaHandler = essentiaHandler;
		this.tileEntity = tileEntity;
		this.side = side;
	}

	public IProxyTankProperties getTankProperties(int tankIndex) {
		return new EssentiaTankProperties(this.essentiaHandler, this.tileEntity);
	}

	public int getNumTanks() {
		return this.essentiaHandler.getAspects().size();
	}

	public int fill(IProxyFluidStack resource, boolean doFill) {
		// TODO: fill function for essentia tile entity handlers
		return 0;
	}

	@Nullable
	public EssentiaStack drain(int maxAmount, boolean doDrain) {
		// TODO: drain function for essentia tile entity handlers
		return null;
	}

	@Nullable
	public EssentiaStack drain(IProxyFluidStack resource, boolean doDrain) {
		// TODO: drain function for essentia tile entity handlers
		return null;
	}

	@Override
	public IProxyFluidStack loadFluidStackFromNBT(NBTTagCompound nbt) {
		// TODO: load fluid stack from NBT
		throw new UnsupportedOperationException("Unimplemented method 'loadFluidStackFromNBT'");
	}
}
