package bedrockbreaker.graduatedcylinders.util;

import javax.annotation.Nullable;

import bedrockbreaker.graduatedcylinders.FluidHandlerRegistry;
import bedrockbreaker.graduatedcylinders.api.IProxyFluidHandler;
import bedrockbreaker.graduatedcylinders.api.IProxyFluidHandlerItem;
import bedrockbreaker.graduatedcylinders.api.IProxyFluidStack;
import bedrockbreaker.graduatedcylinders.api.IProxyTankProperties;
import bedrockbreaker.graduatedcylinders.api.MetaHandler;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class FluidHelper {

	public static MetaHandler getMetaHandler(ItemStack itemStack) {
		if (itemStack == null || itemStack.getItem() == null || itemStack.stackSize != 1) return null;

		for (MetaHandler metaHandler : FluidHandlerRegistry.REGISTRY) {
			if (metaHandler.hasHandler(itemStack)) return metaHandler;
		}

		return null;
	}

	public static IProxyFluidHandlerItem getProxyFluidHandler(ItemStack itemStack) {
		MetaHandler metaHandler = FluidHelper.getMetaHandler(itemStack);
		return metaHandler == null ? null : metaHandler.getHandler(itemStack);
	}

	public static IProxyFluidHandler getMatchingProxyFluidHandler(World world, BlockPos pos, @Nullable EnumFacing side, IProxyFluidHandlerItem fluidHandlerMatch) {
		int state = world.getBlockMetadata(pos.getX(), pos.getY(), pos.getZ());
		Block block = world.getBlock(pos.getX(), pos.getY(), pos.getZ());
		if (!block.hasTileEntity(state)) return null;
		TileEntity tileEntity = world.getTileEntity(pos.getX(), pos.getY(), pos.getZ());
		if (tileEntity == null) return null;
		return fluidHandlerMatch.isMatchingHandlerType(tileEntity, side) ? fluidHandlerMatch.getMatchingHandler(tileEntity, side) : null;
	}

	public static IProxyFluidStack tryFluidTransfer(IProxyFluidHandler fluidDestination, IProxyFluidHandler fluidSource, IProxyFluidStack resource, boolean doTransfer) {
		IProxyFluidStack drainable = fluidSource.drain(resource, false);
		if (drainable == null || drainable.getAmount() <= 0 || !resource.isFluidEqual(drainable)) return null;

		int fillableAmount = fluidDestination.fill(drainable, false);
		if (fillableAmount <= 0) return null;
		if (!doTransfer) return drainable.copy(drainable, fillableAmount);
		IProxyFluidStack drained = fluidSource.drain(drainable.copy(drainable, fillableAmount), true);
		return drained == null ? null : drained.copy(drained, fluidDestination.fill(drainable, true));
	}

	public static TransferrableFluidResult getTransferResult(IProxyFluidHandler handler1, int handler1TankIndex, IProxyFluidHandler handler2, int handler2TankIndex) {
		if (handler1 == null || handler2 == null) return new TransferrableFluidResult(handler1TankIndex, handler2TankIndex, false, false);
		IProxyFluidStack fluidStack1 = handler1.getTankProperties(handler1TankIndex).getContents();
		IProxyFluidStack fluidStack2 = handler2.getTankProperties(handler2TankIndex).getContents();
		if ((fluidStack1 == null && fluidStack2 == null) || (fluidStack1 != null && fluidStack2 != null && !fluidStack1.isFluidEqual(fluidStack2))) return new TransferrableFluidResult(handler1TankIndex, handler2TankIndex, false, false);;
		
		IProxyFluidStack simulatedExportFluid = null;
		if (fluidStack1 != null) simulatedExportFluid = FluidHelper.tryFluidTransfer(handler2, handler1, fluidStack1.copy(fluidStack1, Integer.MAX_VALUE), false);
		IProxyFluidStack simulatedImportFluid = null;
		if (fluidStack2 != null) simulatedImportFluid = FluidHelper.tryFluidTransfer(handler1, handler2, fluidStack2.copy(fluidStack2, Integer.MAX_VALUE), false);

		return new TransferrableFluidResult(handler1TankIndex, handler2TankIndex, simulatedExportFluid != null && simulatedExportFluid.getAmount() > 0, simulatedImportFluid != null && simulatedImportFluid.getAmount() > 0);
	}

	public static int getTransferAmount(IProxyFluidHandler handler1, IProxyFluidHandler handler2) {
		if (handler1 == null || handler2 == null || handler1.getClass() != handler2.getClass()) return 0;

		IProxyTankProperties itemOneProps = handler1.getTankProperties(0);
		IProxyTankProperties itemTwoProps = handler2.getTankProperties(0);
		IProxyFluidStack itemOneContents = itemOneProps.getContents();
		IProxyFluidStack itemTwoContents = itemTwoProps.getContents();
		if (itemOneContents != null && itemTwoContents != null && !itemOneContents.isFluidEqual(itemTwoContents)) return 0;

		int itemOneAmount = itemOneContents == null ? 0 : itemOneContents.getAmount();
		int itemTwoAmount = itemTwoContents == null ? 0 : itemTwoContents.getAmount();
		if ((itemOneAmount == itemOneProps.getCapacity() && itemTwoAmount == itemTwoProps.getCapacity()) || itemOneAmount + itemTwoAmount == 0) return 0;

		return itemTwoAmount == 0 || itemOneAmount == itemOneProps.getCapacity() ? -Math.min(itemTwoProps.getCapacity() - itemTwoAmount, itemOneAmount) : Math.min(itemOneProps.getCapacity() - itemOneAmount, itemTwoAmount);
	}

	/**
	 * A structure indicating a pair of tanks and whether the first tank can import from or export to the second.
	 */
	public static class TransferrableFluidResult {
		public final int sourceTank;
		public final int destinationTank;
		public final boolean canExport;
		public final boolean canImport;

		public TransferrableFluidResult(int leftTankIn, int rightTankIn, boolean canExport, boolean canImport) {
			this.sourceTank = leftTankIn;
			this.destinationTank = rightTankIn;
			this.canExport = canExport;
			this.canImport = canImport;
		}

		public boolean canTransfer() {
			return this.canExport || this.canImport;
		}
	}
}
