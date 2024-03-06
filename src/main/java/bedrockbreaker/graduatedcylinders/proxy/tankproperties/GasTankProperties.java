package bedrockbreaker.graduatedcylinders.proxy.tankproperties;

import bedrockbreaker.graduatedcylinders.api.IProxyFluidStack;
import bedrockbreaker.graduatedcylinders.api.IProxyTankProperties;
import bedrockbreaker.graduatedcylinders.proxy.stack.GasStackGC;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTankInfo;
import net.minecraftforge.common.util.ForgeDirection;

public class GasTankProperties implements IProxyTankProperties {

	protected GasTankInfo gasTankInfo;
	protected ForgeDirection side;

	public GasTankProperties(GasTankInfo gasTankInfo) {
		this.gasTankInfo = gasTankInfo;
	}

	public GasStackGC getContents() {
		GasStack drawnGas = this.gasTankInfo.getGas();
		return drawnGas == null ? null : new GasStackGC(drawnGas);
	}

	public int getCapacity(IProxyFluidStack fluidStack) {
		if (!(fluidStack instanceof GasStackGC)) return 0;
		return this.gasTankInfo.getMaxGas();
	}
}
