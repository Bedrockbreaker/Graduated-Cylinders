package bedrockbreaker.graduatedcylinders.Proxy.TankProperties;

import bedrockbreaker.graduatedcylinders.Proxy.FluidStacks.IProxyFluidStack;

public interface IProxyTankProperties {

	public IProxyFluidStack getContents();

	public int getCapacity();
}