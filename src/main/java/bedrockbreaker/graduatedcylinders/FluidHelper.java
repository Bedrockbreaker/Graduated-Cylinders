package bedrockbreaker.graduatedcylinders;

import javax.annotation.Nullable;

import bedrockbreaker.graduatedcylinders.Proxy.ProxyFluidHandler;
import bedrockbreaker.graduatedcylinders.Proxy.ProxyFluidHandlerItem;
import bedrockbreaker.graduatedcylinders.Proxy.ProxyFluidStack;
import bedrockbreaker.graduatedcylinders.Proxy.ProxyFluidHandler.ProxyType;
import bedrockbreaker.graduatedcylinders.Proxy.ProxyTanksProperties.ProxyTankProperties;
import mekanism.api.gas.IGasHandler;
import mekanism.api.gas.IGasItem;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

public class FluidHelper {

	public static ProxyFluidHandlerItem getProxyFluidHandler(ItemStack itemStack) {
		if (itemStack.isEmpty() || itemStack.getCount() != 1) return null;
		IFluidHandlerItem fluidHandler = FluidUtil.getFluidHandler(itemStack);
		if (fluidHandler != null) return new ProxyFluidHandlerItem(fluidHandler);
		if (!GraduatedCylinders.isMekLoaded) return null;
		Item item = itemStack.getItem();
		return (item instanceof IGasItem) ? new ProxyFluidHandlerItem((IGasItem) item, itemStack) : null;
	}

	public static ProxyFluidHandler getProxyFluidHandler(World world, BlockPos pos, @Nullable EnumFacing side, ProxyType type) {
		IBlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		if (!block.hasTileEntity(state)) return null;
		TileEntity tileEntity = world.getTileEntity(pos);
		if (tileEntity == null) return null;

		if (type == ProxyType.FLUID && tileEntity.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side)) return new ProxyFluidHandler(tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side));
		if (GraduatedCylinders.isMekLoaded && (type == ProxyType.GASITEM || type == ProxyType.GAS) && tileEntity instanceof IGasHandler) return new ProxyFluidHandler((IGasHandler) tileEntity, side);
		return null;
	}

	public static ProxyFluidStack tryFluidTransfer(ProxyFluidHandler fluidDestination, ProxyFluidHandler fluidSource, ProxyFluidStack resource, boolean doTransfer) {
		ProxyFluidStack drainable = fluidSource.drain(resource, false);
		return drainable != null && drainable.amount > 0 && resource.isFluidEqual(drainable) ? FluidHelper.tryFluidTransfer_Internal(fluidDestination, fluidSource, drainable, doTransfer) : null;
	}

	public static FindTransferrableTankResult findTransferrableTank(ProxyFluidHandler handler1, ProxyFluidHandler handler2) {
		if (handler1 == null || handler2 == null) return null;
		for (int i = 0; i < handler1.getTankProperties().getLength(); i++) {
			ProxyFluidStack fluidStack1 = handler1.getTankProperties().get(i).getContents();
			for (int j = 0; j < handler2.getTankProperties().getLength(); j++) {
				ProxyFluidStack fluidStack2 = handler2.getTankProperties().get(j).getContents();
				if ((fluidStack1 == null && fluidStack2 == null) || (fluidStack1 != null && fluidStack2 != null && !fluidStack1.isFluidEqual(fluidStack2))) continue;
				ProxyFluidStack simulatedFluidStack = new ProxyFluidStack(fluidStack1 == null ? fluidStack2 : fluidStack1, Integer.MAX_VALUE);
				ProxyFluidStack simulatedExportFluid = FluidHelper.tryFluidTransfer(handler2, handler1, simulatedFluidStack, false);
				ProxyFluidStack simulatedImportFluid = FluidHelper.tryFluidTransfer(handler1, handler2, simulatedFluidStack, false);
				if ((simulatedExportFluid != null && simulatedExportFluid.amount > 0) || (simulatedImportFluid != null && simulatedImportFluid.amount > 0)) return new FindTransferrableTankResult(i, j, simulatedExportFluid != null && simulatedExportFluid.amount > 0, simulatedImportFluid != null && simulatedImportFluid.amount > 0);
			}
		}
		return null;
	}

	public static int getTransferAmount(ProxyFluidHandler handler1, ProxyFluidHandler handler2) {
		if (handler1 == null || handler2 == null || handler1.getType() != handler2.getType()) return 0;

		ProxyTankProperties itemOneProps = handler1.getTankProperties().get(0);
		ProxyTankProperties itemTwoProps = handler2.getTankProperties().get(0);
		ProxyFluidStack itemOneContents = itemOneProps.getContents();
		ProxyFluidStack itemTwoContents = itemTwoProps.getContents();
		if (itemOneContents != null && itemTwoContents != null && !itemOneContents.isFluidEqual(itemTwoContents)) return 0;

		int itemOneAmount = itemOneContents == null ? 0 : itemOneContents.amount;
		int itemTwoAmount = itemTwoContents == null ? 0 : itemTwoContents.amount;
		if ((itemOneAmount == itemOneProps.getCapacity() && itemTwoAmount == itemTwoProps.getCapacity()) || itemOneAmount + itemTwoAmount == 0) return 0;

		return itemTwoAmount == 0 || itemOneAmount == itemOneProps.getCapacity() ? -Math.min(itemTwoProps.getCapacity() - itemTwoAmount, itemOneAmount) : Math.min(itemOneProps.getCapacity() - itemOneAmount, itemTwoAmount);
	}

	@Nullable
	private static ProxyFluidStack tryFluidTransfer_Internal(ProxyFluidHandler fluidDestination, ProxyFluidHandler fluidSource, ProxyFluidStack drainable, boolean doTransfer) {
		int fillableAmount = fluidDestination.fill(drainable, false);
		if (fillableAmount > 0) {
			if (doTransfer) {
				ProxyFluidStack drained = fluidSource.drain(fillableAmount, true);
				if (drained != null) {
					drained.amount = fluidDestination.fill(drainable, true);
					return drained;
				}
			} else {
				drainable.amount = fillableAmount;
				return drainable;
			}
		}
		return null;
	}

	public static class FindTransferrableTankResult {
		public final int leftTank;
		public final int rightTank;
		public final boolean canExport;
		public final boolean canImport;

		public FindTransferrableTankResult(int leftTankIn, int rightTankIn, boolean canExport, boolean canImport) {
			this.leftTank = leftTankIn;
			this.rightTank = rightTankIn;
			this.canExport = canExport;
			this.canImport = canImport;
		}
	}
}
