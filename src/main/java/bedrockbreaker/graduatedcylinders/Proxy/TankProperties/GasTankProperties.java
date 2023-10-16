package bedrockbreaker.graduatedcylinders.proxy.tankproperties;

import bedrockbreaker.graduatedcylinders.api.IProxyTankProperties;
import bedrockbreaker.graduatedcylinders.proxy.stack.GasStackGC;
import mekanism.api.gas.GasTankInfo;

public class GasTankProperties implements IProxyTankProperties {

	protected GasTankInfo gasTankInfo;

	public GasTankProperties(GasTankInfo gasTankInfo) {
		this.gasTankInfo = gasTankInfo;
	}

	public GasStackGC getContents() {
		return this.gasTankInfo.getGas() == null ? null : new GasStackGC(this.gasTankInfo.getGas());
	}

	public int getCapacity() {
		return this.gasTankInfo.getMaxGas();
	}
}
