package bedrockbreaker.graduatedcylinders.Proxy;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

public interface IProxyFluidStack {

	public IProxyFluidStack copy(IProxyFluidStack fluidStack, int amount);

	public int getAmount();

	public boolean isFluidEqual(@Nullable IProxyFluidStack other);

	public ItemStack getFilledBucket();

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