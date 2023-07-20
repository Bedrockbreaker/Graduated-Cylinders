package bedrockbreaker.graduatedcylinders.Proxy.FluidHandlers;

import javax.annotation.Nullable;

import bedrockbreaker.graduatedcylinders.Proxy.FluidStacks.IProxyFluidStack;
import bedrockbreaker.graduatedcylinders.Proxy.TankProperties.IProxyTankProperties;
import net.minecraft.nbt.NBTTagCompound;

public interface IProxyFluidHandler {

	public IProxyFluidStack loadFluidStackFromNBT(NBTTagCompound nbt);

	public IProxyTankProperties getTankProperties(int tankIndex);

	public int getNumTanks();

	public int fill(IProxyFluidStack resource, boolean doFill);

	@Nullable
	public IProxyFluidStack drain(int maxAmount, boolean doDrain);

	@Nullable
	public IProxyFluidStack drain(IProxyFluidStack resource, boolean doDrain);
}