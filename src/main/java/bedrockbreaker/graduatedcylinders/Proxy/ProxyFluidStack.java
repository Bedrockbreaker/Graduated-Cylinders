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
import thaumcraft.api.aspects.AspectList;

public class ProxyFluidStack {
	
	protected ProxyType type;
	protected FluidStack fluidStack;
	protected GasStack gasStack;
	protected AspectList essentiaStack;

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
			case ESSENTIA:
				AspectList newList = proxyFluidStack.essentiaStack.copy();
				newList.remove(newList.getAspects()[0], newList.getAmount(newList.getAspects()[0]) - 1);
				this.essentiaStack = newList;
				break;
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

	public ProxyFluidStack(AspectList essentiaStack) {
		this.type = ProxyType.ESSENTIA;
		this.essentiaStack = essentiaStack;
		this.amount = essentiaStack.getAmount(essentiaStack.getAspects()[0]);
	}

	public static ProxyFluidStack loadFluidStackFromNBT(NBTTagCompound nbt) {
		if (nbt == null || !nbt.hasKey("ProxyType", Constants.NBT.TAG_INT)) return null;
		switch (nbt.getInteger("ProxyType")) {
			case 0:
				return new ProxyFluidStack(FluidStack.loadFluidStackFromNBT(nbt));
			case 1:
				return new ProxyFluidStack(GasStack.readFromNBT(nbt));
			case 2:
				AspectList essentiaStack = new AspectList();
				essentiaStack.readFromNBT(nbt);
				return new ProxyFluidStack(essentiaStack);
			default:
				return null;
		}
	}

	public boolean isFluidEqual(@Nullable ProxyFluidStack other) {
		if (other == null || this.type != other.type) return false;
		return this.type == ProxyType.FLUID ? this.fluidStack.isFluidEqual(other.fluidStack) : this.gasStack.isGasEqual(other.gasStack);
		// TODO: essentia
	}

	public ItemStack getFilledBucket() {
		FluidStack fluidStack = this.type == ProxyType.GAS && this.gasStack.getGas().hasFluid() ? new FluidStack(this.gasStack.getGas().getFluid(), 1000) : this.fluidStack;
		return fluidStack != null ? FluidUtil.getFilledBucket(fluidStack) : ItemStack.EMPTY;
		// TODO: essentia
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
			case ESSENTIA:
				nbt.setInteger("ProxyType", 2);
				this.essentiaStack.writeToNBT(nbt);
			default:
				nbt.setInteger("ProxyType", -1);
				break;
		}
		return nbt;
	}

	public String toString() {
		String prefix = "[" + this.type + "] (" + this.amount + " mb " + this.getRegistryName() + ") ";
		switch (this.type) {
			case FLUID:
				return prefix + this.fluidStack;
			case GAS:
				return prefix + this.gasStack;
			case ESSENTIA:
				return prefix + this.essentiaStack;
			default:
				return prefix + "ERROR INVALID PROXY FLUID STACK";
		}
	}

	public String getRegistryName() {
		switch (this.type) {
			case FLUID:
				return this.fluidStack.getUnlocalizedName();
			case GAS:
				return this.gasStack.getGas().getTranslationKey();
			case ESSENTIA:
				return this.essentiaStack.getAspects()[0].getName();
			default:
				return "ERROR INVALID PROXY FLUID STACK";
		}
	}

	public ResourceLocation getResourceLocation() {
		switch (this.type) {
			case FLUID:
				return this.fluidStack.getFluid().getStill();
			case GAS:
				return this.gasStack.getGas().getIcon();
			case ESSENTIA:
				return this.essentiaStack.getAspects()[0].getImage();
			default:
				return new ResourceLocation("invalid_proxy_fluid_stack");
		}
	}

	public int getColor() {
		switch (this.type) {
			case FLUID:
				return this.fluidStack.getFluid().getColor();
			case GAS:
				return this.gasStack.getGas().getTint();
			case ESSENTIA:
				return this.essentiaStack.getAspects()[0].getColor();
			default:
				return 0xFFFFFFFF; // Opaque white
		}
	}

	public String getLocalizedName() {
		switch (this.type) {
			case FLUID:
				return this.fluidStack.getLocalizedName();
			case GAS:
				return this.gasStack.getGas().getLocalizedName();
			case ESSENTIA:
				return this.essentiaStack.getAspects()[0].getLocalizedDescription();
			default:
				return "INVALID BAD PROXY FLUID STACK";
		}
	}

	public SoundEvent getFillSound() {
		switch (this.type) {
			case FLUID:
				return this.fluidStack.getFluid().getFillSound(this.fluidStack);
			case GAS:
				return SoundEvents.BLOCK_LAVA_EXTINGUISH; // Mekanism doesn't have any good sounds for gas transfer :/
			case ESSENTIA:
				// FIXME: one of these resource locations has to be right, right? (see getEmptySound below)
				return new SoundEvent(new ResourceLocation("thaumcraft", "sounds/bubble"));
			default:
				return new SoundEvent(new ResourceLocation("invalid_proxy_fluid_stack"));
		}
	}

	public SoundEvent getEmptySound() {
		switch (this.type) {
			case FLUID:
				return this.fluidStack.getFluid().getEmptySound(this.fluidStack);
			case GAS:
				return SoundEvents.BLOCK_LAVA_EXTINGUISH; // Mekanism doesn't have any good sounds for gas transfer :/
			case ESSENTIA:
				return new SoundEvent(new ResourceLocation("assets/thaumcraft/sounds/bubble"));
			default:
				return new SoundEvent(new ResourceLocation("invalid_proxy_fluid_stack"));
		}
	}
}