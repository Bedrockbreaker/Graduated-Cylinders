package bedrockbreaker.graduatedcylinders.Proxy.FluidStacks;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import thaumcraft.api.aspects.AspectList;

// An essentia stack represents an AspectList, but with only ever 1 aspect
public class EssentiaStack implements IProxyFluidStack {

	public AspectList essentiaStack;

	public EssentiaStack(AspectList essentiaStack) {
		this.essentiaStack = essentiaStack;
	}

	public EssentiaStack copy(IProxyFluidStack essentiaStack, int amount) {
		return essentiaStack instanceof EssentiaStack ? new EssentiaStack(new AspectList().add(((EssentiaStack) essentiaStack).essentiaStack.getAspects()[0], amount)) : null;
	}

	public int getAmount() {
		return this.essentiaStack.getAmount(this.essentiaStack.getAspects()[0]);
	}

	public boolean isFluidEqual(@Nullable IProxyFluidStack other) {
		return other == null || !(other instanceof EssentiaStack) ? false : this.essentiaStack.getAspects()[0].getTag() == ((EssentiaStack) other).essentiaStack.getAspects()[0].getTag();
	}

	public int getColor() {
		return this.essentiaStack.getAspects()[0].getColor();
	}

	public SoundEvent getFillSound() {
		// FIXME: surely one of these sound events has to be right, right?
		return new SoundEvent(new ResourceLocation("thaumcraft", "sounds/bubble"));
	}

	public SoundEvent getEmptySound() {
		return new SoundEvent(new ResourceLocation("assets/thaumcraft/sounds/bubble"));
	}

	public String getUnlocalizedName() {
		return this.essentiaStack.getAspects()[0].getTag();
	}

	public String getLocalizedName() {
		return this.essentiaStack.getAspects()[0].getName();
	}

	public ResourceLocation getResourceLocation() {
		return this.essentiaStack.getAspects()[0].getImage();
	}

	public TextureAtlasSprite getSprite() {
		// FIXME: this probably doesn't work. The icons probably aren't stored in the block atlas.
		return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(this.getResourceLocation().toString());
	}

	public EssentiaStack loadFromNBT(NBTTagCompound nbt) {
		if (nbt == null) return null;
		AspectList newList = new AspectList();
		newList.readFromNBT(nbt);
		// TODO: I'm not actually sure if #readFromNBT() can return null
		return newList == null ? null : new EssentiaStack(newList);
	}

	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		this.essentiaStack.writeToNBT(nbt); // Why did Azanor decide to mutate the original nbt?
		return nbt;
	}
}