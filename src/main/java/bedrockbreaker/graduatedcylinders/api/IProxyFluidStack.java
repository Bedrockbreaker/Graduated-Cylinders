package bedrockbreaker.graduatedcylinders.api;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IProxyFluidStack {

	/**
	 * Return a copy of the given fluidstack, with a new amount.
	 * Return null if the given fluidstack's meta type doesn't match this fluidstack.
	 */
	public IProxyFluidStack copy(IProxyFluidStack fluidStack, int amount);

	/**
	 * Get the amount of fluid in this stack
	 */
	public int getAmount();

	/**
	 * Check if the given fluid is equal, ignoring amounts
	 */
	public boolean isFluidEqual(@Nullable IProxyFluidStack other);

	/**
	 * Get the color tint of this fluid
	 */
	public int getColor();

	/**
	 * Get the resource location of this fluid
	 */
	public ResourceLocation getResourceLocation();

	/**
	 * Get the container fill sound for this fluid
	 */
	public SoundEvent getFillSound();

	/**
	 * Get the container drain sound for this fluid
	 */
	public SoundEvent getEmptySound();

	/**
	 * Get the unlocalized name of this fluid
	 */
	@SideOnly(Side.CLIENT)
	public String getUnlocalizedName();

	/**
	 * Get the localized name of this fluid
	 */
	@SideOnly(Side.CLIENT)
	public String getLocalizedName();

	/**
	 * Get the TextureAtalsSprite of this fluid
	 */
	@SideOnly(Side.CLIENT)
	public TextureAtlasSprite getSprite();

	/**
	 * Return a new fluid instance from the given nbt, without modifying this existing fluid
	 */
	public IProxyFluidStack loadFromNBT(NBTTagCompound nbt);

	/**
	 * Write this fluid to the given nbt tag
	 */
	public NBTTagCompound writeToNBT(NBTTagCompound nbt);
}