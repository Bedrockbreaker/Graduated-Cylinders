package bedrockbreaker.graduatedcylinders.proxy.stack;

import javax.annotation.Nullable;

import bedrockbreaker.graduatedcylinders.api.IProxyFluidStack;
import mekanism.api.gas.GasStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.NBTTagCompound;

// AKA "GasStackGraduatedCylinders" to avoid name conflict
public class GasStackGC implements IProxyFluidStack {

	public GasStack gasStack;

	public GasStackGC(GasStack gasStack) {
		this.gasStack = gasStack;
	}

	public GasStackGC copy(IProxyFluidStack gasStack, int amount) {
		return gasStack instanceof GasStackGC ? new GasStackGC(new GasStack(((GasStackGC) gasStack).gasStack.getGas(), amount)) : null;
	}

	public int getAmount() {
		return this.gasStack.amount;
	}

	public boolean isFluidEqual(@Nullable IProxyFluidStack other) {
		return other instanceof GasStackGC ? this.gasStack.isGasEqual(((GasStackGC) other).gasStack) : false;
	}

	public int getColor() {
		return 0xFFFFFF;
	}

	public String getFillSound() {
		return "block.lava.extinguish"; // Mekanism doesn't have any good gas transfer sounds
	}

	public String getEmptySound() {
		return "block.lava.extinguish";
	}

	public String getUnlocalizedName() {
		return this.gasStack.getGas().getUnlocalizedName();
	}

	public String getLocalizedName() {
		return this.gasStack.getGas().getLocalizedName();
	}

	public TextureAtlasSprite getSprite() {
		return Minecraft.getMinecraft().getTextureMapBlocks().getTextureExtry(this.gasStack.getGas().getIcon().getIconName());
	}

	public GasStackGC loadFromNBT(NBTTagCompound nbt) {
		return nbt == null ? null : new GasStackGC(GasStack.readFromNBT(nbt));
	}

	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		return this.gasStack.write(nbt);
	}
}
