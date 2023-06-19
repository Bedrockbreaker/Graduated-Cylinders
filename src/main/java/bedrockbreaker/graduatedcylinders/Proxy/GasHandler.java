package bedrockbreaker.graduatedcylinders.Proxy;

import javax.annotation.Nullable;

import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasHandler;
import net.minecraft.util.EnumFacing;

public class GasHandler implements IProxyFluidHandler {

	protected IGasHandler gasHandler;
	protected EnumFacing side;

	public GasHandler(IGasHandler gasHandler, EnumFacing side) {
		this.gasHandler = gasHandler;
		this.side = side;
	}

	public IProxyTankProperties getTankProperties(int tankIndex) {
		return new GasTankProperties(this.gasHandler.getTankInfo()[tankIndex]);
	}

	public int getNumTanks() {
		return this.gasHandler.getTankInfo().length;
	}

	public int fill(IProxyFluidStack resource, boolean doFill) {
		if (!(resource instanceof GasStackGC)) return 0;
		return this.gasHandler.receiveGas(this.side, ((GasStackGC) resource).gasStack, doFill);
	}

	@Nullable
	public GasStackGC drain(int maxAmount, boolean doDrain) {
		GasStack removedGas = this.gasHandler.drawGas(this.side, maxAmount, doDrain);
		return removedGas == null ? null : new GasStackGC(removedGas);
	}

	@Nullable
	public GasStackGC drain(IProxyFluidStack resource, boolean doDrain) {
		if (!(resource instanceof GasStackGC)) return null;
		GasStack removedGas = this.gasHandler.drawGas(this.side, resource.getAmount(), doDrain);
		return removedGas == null ? null : new GasStackGC(removedGas);
	}
}