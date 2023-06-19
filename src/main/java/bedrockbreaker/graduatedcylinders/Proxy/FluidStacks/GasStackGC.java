package bedrockbreaker.graduatedcylinders.Proxy.FluidStacks;

import javax.annotation.Nullable;

import mekanism.api.gas.GasStack;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

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
		return other == null || !(other instanceof GasStackGC) ? false : this.gasStack.isGasEqual(((GasStackGC) other).gasStack);
	}

	public int getColor() {
		return this.gasStack.getGas().getTint();
	}

	public SoundEvent getFillSound() {
		return SoundEvents.BLOCK_LAVA_EXTINGUISH; // Mekanism doesn't have any good gas transfer sounds
	}

	public SoundEvent getEmptySound() {
		return SoundEvents.BLOCK_LAVA_EXTINGUISH;
	}

	public String getUnlocalizedName() {
		return this.gasStack.getGas().getTranslationKey();
	}

	public String getLocalizedName() {
		return this.gasStack.getGas().getLocalizedName();
	}

	public ResourceLocation getResourceLocation() {
		return this.gasStack.getGas().getIcon();
	}

	public TextureAtlasSprite getSprite() {
		return this.gasStack.getGas().getSprite();
	}

	public GasStackGC loadFromNBT(NBTTagCompound nbt) {
		return nbt == null ? null : new GasStackGC(GasStack.readFromNBT(nbt));
	}

	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		return this.gasStack.write(nbt);
	}