package bedrockbreaker.graduatedcylinders.api;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

public interface IProxyFluidHandlerItem extends IProxyFluidHandler {

	/**
	 * Get the itemstack container of this handler
	 */
	public ItemStack getContainer();

	/**
	 * Check if the tile entity has a matching fluid handler
	 */
	public boolean isMatchingHandlerType(TileEntity tileEntity, EnumFacing side);

	/**
	 * Get the tile entity's fluid handler whose type matches this fluid handler
	 */
	public IProxyFluidHandler getMatchingHandler(TileEntity tileEntity, EnumFacing side);
}
