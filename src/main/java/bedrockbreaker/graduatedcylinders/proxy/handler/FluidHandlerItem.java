package bedrockbreaker.graduatedcylinders.proxy.handler;

import bedrockbreaker.graduatedcylinders.api.IProxyFluidHandlerItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

public class FluidHandlerItem extends FluidHandler implements IProxyFluidHandlerItem {

	protected IFluidHandlerItem fluidHandlerItem;

	public FluidHandlerItem(IFluidHandlerItem fluidHandlerItem) {
		super(fluidHandlerItem);
		this.fluidHandlerItem = fluidHandlerItem;
	}

	public ItemStack getContainer() {
		return this.fluidHandlerItem.getContainer();
	}

	public FluidHandler getMatchingHandler(TileEntity tileEntity, EnumFacing side) {
		return new FluidHandler(FluidUtil.getFluidHandler(tileEntity.getWorld(), tileEntity.getPos(), side));
	}
}