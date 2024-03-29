package bedrockbreaker.graduatedcylinders.proxy.stack;

import javax.annotation.Nullable;

import bedrockbreaker.graduatedcylinders.api.IProxyFluidStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fluids.FluidStack;

// AKA "FluidStackGraduatedCylinders" to avoid name conflict
public class FluidStackGC implements IProxyFluidStack {

	public FluidStack fluidStack;

	public FluidStackGC(FluidStack fluidStack) {
		this.fluidStack = fluidStack;
	}

	public FluidStackGC copy(IProxyFluidStack fluidStackIn, int amount) {
		return fluidStackIn instanceof FluidStackGC ? new FluidStackGC(new FluidStack(((FluidStackGC) fluidStackIn).fluidStack, amount)) : null;
	}

	public int getAmount() {
		return this.fluidStack.amount;
	}

	public boolean isFluidEqual(@Nullable IProxyFluidStack other) {
		return other instanceof FluidStackGC ? this.fluidStack.isFluidEqual(((FluidStackGC) other).fluidStack) : false;
	}

	public int getColor() {
		return this.fluidStack.getFluid().getColor();
	}

	public SoundEvent getFillSound() {
		return this.fluidStack.getFluid().getFillSound();
	}

	public SoundEvent getEmptySound() {
		return this.fluidStack.getFluid().getEmptySound();
	}

	public String getUnlocalizedName() {
		return this.fluidStack.getUnlocalizedName();
	}

	public String getLocalizedName() {
		return this.fluidStack.getLocalizedName();
	}

	public ResourceLocation getResourceLocation() {
		return this.fluidStack.getFluid().getStill();
	}

	public TextureAtlasSprite getSprite() {
		return Minecraft.getMinecraft().getTextureMapBlocks().getTextureExtry(this.getResourceLocation().toString());
	}

	public FluidStackGC loadFromNBT(NBTTagCompound nbt) {
		return nbt == null ? null : new FluidStackGC(FluidStack.loadFluidStackFromNBT(nbt));
	}

	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		return this.fluidStack.writeToNBT(nbt);
	}

	public String toString() {
		return this.getLocalizedName() + " @ " + this.getAmount();
	}
}
