package bedrockbreaker.graduatedcylinders.proxy.stack;

import javax.annotation.Nullable;

import bedrockbreaker.graduatedcylinders.api.IProxyFluidStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
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

	public String getFillSound() {
		return "item.bucket.fill";
	}

	public String getEmptySound() {
		return "item.bucket.empty";
	}

	public String getUnlocalizedName() {
		return this.fluidStack.getUnlocalizedName();
	}

	public String getLocalizedName() {
		return this.fluidStack.getLocalizedName();
	}

	public TextureAtlasSprite getSprite() {
		return Minecraft.getMinecraft().getTextureMapBlocks().getTextureExtry(this.fluidStack.getFluid().getStillIcon().getIconName());
	}

	public IIcon getIcon() {
		return this.fluidStack.getFluid().getStillIcon();
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
