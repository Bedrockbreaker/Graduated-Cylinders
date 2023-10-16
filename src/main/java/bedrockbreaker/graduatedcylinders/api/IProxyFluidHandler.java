package bedrockbreaker.graduatedcylinders.api;

import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;

public interface IProxyFluidHandler {

	/**
	 * Load a proxied fluid stack from NBT
	 */
	public IProxyFluidStack loadFluidStackFromNBT(NBTTagCompound nbt);

	/**
	 * Get the properties about a specific tank in this handler
	 */
	public IProxyTankProperties getTankProperties(int tankIndex);

	/**
	 * Get the number of tanks this handler contains
	 */
	public int getNumTanks();

	/**
	 * Fill the handler with the given fluid stack
	 * @param doFill - set to false to do a virtual fill check
	 */
	public int fill(IProxyFluidStack resource, boolean doFill);

	/**
	 * Drain a fluid from the handler
	 * @param doDrain - set to false to do a virtual drain check
	 */
	@Nullable
	public IProxyFluidStack drain(int maxAmount, boolean doDrain);

	/**
	 * Drain a specific fluid from the handler
	 * @param doDrain - set to false to do a virtual drain check
	 */
	@Nullable
	public IProxyFluidStack drain(IProxyFluidStack resource, boolean doDrain);
}