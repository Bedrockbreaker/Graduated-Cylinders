package bedrockbreaker.graduatedcylinders.api;

public interface IProxyTankProperties {

	/**
	 * Get the Fluid Stack stored in this tank
	 */
	public IProxyFluidStack getContents();

	/**
	 * Get the maximum capacity of this tank
	 */
	public int getCapacity();
}