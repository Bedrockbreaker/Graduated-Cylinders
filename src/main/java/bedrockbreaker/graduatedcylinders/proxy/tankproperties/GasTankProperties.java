package bedrockbreaker.graduatedcylinders.proxy.tankproperties;

import bedrockbreaker.graduatedcylinders.api.IProxyTankProperties;
import bedrockbreaker.graduatedcylinders.proxy.stack.GasStackGC;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasHandler;
import net.minecraftforge.common.util.ForgeDirection;

public class GasTankProperties implements IProxyTankProperties {

	protected IGasHandler gasHandler;
	protected ForgeDirection side;

	public GasTankProperties(IGasHandler gasHandler) {
		this.gasHandler = gasHandler;
	}

	public GasStackGC getContents() {
		// FIXME: only works for tile entities which allow draining gas in the first place
		GasStack drawnGas = this.gasHandler.drawGas(this.side, Integer.MAX_VALUE, false);
		return drawnGas == null ? null : new GasStackGC(drawnGas);
	}

	public int getCapacity() {
		// FIXME: get actual capacity of gas tank
		return 0;
	}
}
