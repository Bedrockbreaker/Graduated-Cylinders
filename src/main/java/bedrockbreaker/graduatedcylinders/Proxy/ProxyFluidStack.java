package bedrockbreaker.graduatedcylinders.Proxy;

import javax.annotation.Nullable;

import bedrockbreaker.graduatedcylinders.Proxy.ProxyFluidHandler.ProxyType;
import mekanism.api.gas.GasStack;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

public class ProxyFluidStack {
	
	protected ProxyType type;
	protected FluidStack fluidStack;
	protected GasStack gasStack;

	public int amount;

	public ProxyFluidStack(ProxyFluidStack proxyFluidStack, int amount) {
		this.type = proxyFluidStack.type;
		this.amount = amount;
		switch (this.type) {
			case FLUID:
				this.fluidStack = new FluidStack(proxyFluidStack.fluidStack, amount);
				break;
			case GAS:
				this.gasStack = new GasStack(proxyFluidStack.gasStack.getGas(), amount);
				break;
			case GASITEM:
			default:
				throw new IllegalArgumentException(this.type + " is not a valid ProxyFluidStack type");
		}
	}

	public ProxyFluidStack(FluidStack fluidStack) {
		this.type = ProxyType.FLUID;
		this.fluidStack = fluidStack;
		this.amount = fluidStack.amount;
	}

	public ProxyFluidStack(GasStack gasStack) {
		this.type = ProxyType.GAS;
		this.gasStack = gasStack;
		this.amount = gasStack.amount;
	}

	public static ProxyFluidStack loadFluidStackFromNBT(NBTTagCompound nbt) {
		if (nbt == null || !nbt.hasKey("ProxyType", Constants.NBT.TAG_INT)) return null;
		switch (nbt.getInteger("ProxyType")) {
			case 0:
				return new ProxyFluidStack(FluidStack.loadFluidStackFromNBT(nbt));
			case 1:
				return new ProxyFluidStack(GasStack.readFromNBT(nbt));
			case -1:
			default:
				return null;
		}
	}

	public boolean isFluidEqual(@Nullable ProxyFluidStack other) {
		if (other == null || this.type != other.type) return false;
		return this.type == ProxyType.FLUID ? this.fluidStack.isFluidEqual(other.fluidStack) : this.gasStack.isGasEqual(other.gasStack);
	}

	public ItemStack getFilledBucket() {
		FluidStack fluidStack = this.type == ProxyType.GAS && this.gasStack.getGas().hasFluid() ? new FluidStack(this.gasStack.getGas().getFluid(), 1000) : this.fluidStack;
		return fluidStack != null ? FluidUtil.getFilledBucket(fluidStack) : ItemStack.EMPTY;
	}

	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		switch (this.type) {
			case FLUID:
				nbt.setInteger("ProxyType", 0);
				this.fluidStack.writeToNBT(nbt);
				break;
			case GAS:
				nbt.setInteger("ProxyType", 1);
				this.gasStack.write(nbt);
				break;
			case GASITEM:
			default:
				nbt.setInteger("ProxyType", -1);
				break;
		}
		return nbt;
	}

	public String toString() {
		return "[" + this.type + "] (" + this.amount + " mb " + (this.type == ProxyType.FLUID ? this.fluidStack.getUnlocalizedName() + ") " + this.fluidStack : this.gasStack.getGas().getTranslationKey() + ") " +  this.gasStack);
	}

	public String getRegistryName() {
		return this.type == ProxyType.FLUID ? fluidStack.getUnlocalizedName() : gasStack.getGas().getTranslationKey();
	}

	public ResourceLocation getResourceLocation() {
		return this.type == ProxyType.FLUID ? fluidStack.getFluid().getStill() : gasStack.getGas().getIcon();
	}

	public int getColor() {
		return this.type == ProxyType.FLUID ? fluidStack.getFluid().getColor(fluidStack) : gasStack.getGas().getTint();
	}

	public String getLocalizedName() {
		return this.type == ProxyType.FLUID ? fluidStack.getLocalizedName() : gasStack.getGas().getLocalizedName();
	}

	public SoundEvent getFillSound() {
		return this.type == ProxyType.FLUID ? fluidStack.getFluid().getFillSound(this.fluidStack) : SoundEvents.BLOCK_LAVA_EXTINGUISH;
	}

	public SoundEvent getEmptySound() {
		return this.type == ProxyType.FLUID ? fluidStack.getFluid().getEmptySound(this.fluidStack) : SoundEvents.BLOCK_LAVA_EXTINGUISH;
	}
}