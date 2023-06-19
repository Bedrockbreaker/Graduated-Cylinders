package bedrockbreaker.graduatedcylinders.Proxy.FluidStacks;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

public interface IProxyFluidStack {

	public IProxyFluidStack copy(IProxyFluidStack fluidStack, int amount);

	public int getAmount();

	public boolean isFluidEqual(@Nullable IProxyFluidStack other);

	public int getColor();

	public SoundEvent getFillSound();

	public SoundEvent getEmptySound();

	public String getUnlocalizedName();

	public String getLocalizedName();

	public ResourceLocation getResourceLocation();

	public TextureAtlasSprite getSprite();

	public IProxyFluidStack loadFromNBT(NBTTagCompound nbt);

	public NBTTagCompound writeToNBT(NBTTagCompound nbt);
}