package bedrockbreaker.graduatedcylinders.Proxy;

import javax.annotation.Nullable;

public interface IProxyFluidHandler {

	public IProxyTankProperties getTankProperties(int tankIndex);

	public int getNumTanks();

	public int fill(IProxyFluidStack resource, boolean doFill);

	@Nullable
	public IProxyFluidStack drain(int maxAmount, boolean doDrain);

	@Nullable
	public IProxyFluidStack drain(IProxyFluidStack resource, boolean doDrain);
}