package bedrockbreaker.graduatedcylinders.Proxy.FluidHandlers;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

public interface IProxyFluidHandlerItem extends IProxyFluidHandler {

	public ItemStack getContainer();

	public boolean isMatchingHandlerType(TileEntity tileEntity, EnumFacing side);

	public IProxyFluidHandler getMatchingHandler(TileEntity tileEntity, EnumFacing side);
}
