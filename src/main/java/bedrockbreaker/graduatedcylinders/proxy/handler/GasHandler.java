package bedrockbreaker.graduatedcylinders.proxy.handler;

import javax.annotation.Nullable;

import bedrockbreaker.graduatedcylinders.api.IProxyFluidHandler;
import bedrockbreaker.graduatedcylinders.api.IProxyFluidStack;
import bedrockbreaker.graduatedcylinders.proxy.stack.GasStackGC;
import bedrockbreaker.graduatedcylinders.proxy.tankproperties.GasTankProperties;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasHandler;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.util.ForgeDirection;

public class GasHandler implements IProxyFluidHandler {

	protected IGasHandler gasHandler;
	protected EnumFacing side;

	public GasHandler(IGasHandler gasHandler, EnumFacing side) {
		this.gasHandler = gasHandler;
		this.side = side;
	}

	public GasStackGC loadFluidStackFromNBT(NBTTagCompound nbt) {
		GasStack gasStack = GasStack.readFromNBT(nbt);
		return gasStack == null ? null : new GasStackGC(gasStack);
	}

	public GasTankProperties getTankProperties(int tankIndex) {
		return new GasTankProperties(this.gasHandler);
	}

	public int getNumTanks() {
		return 1;
	}

	public int fill(IProxyFluidStack resource, boolean doFill) {
		if (!(resource instanceof GasStackGC)) return 0;
		return this.gasHandler.receiveGas(ForgeDirection.getOrientation(this.side.ordinal()), ((GasStackGC) resource).gasStack, doFill);
	}

	@Nullable
	public GasStackGC drain(int maxAmount, boolean doDrain) {
		GasStack removedGas = this.gasHandler.drawGas(ForgeDirection.getOrientation(this.side.ordinal()), maxAmount, doDrain);
		return removedGas == null ? null : new GasStackGC(removedGas);
	}

	@Nullable
	public GasStackGC drain(IProxyFluidStack resource, boolean doDrain) {
		if (!(resource instanceof GasStackGC)) return null;
		GasStack removedGas = this.gasHandler.drawGas(ForgeDirection.getOrientation(this.side.ordinal()), resource.getAmount(), doDrain);
		return removedGas == null ? null : new GasStackGC(removedGas);
	}
}