package bedrockbreaker.graduatedcylinders.Util;

import javax.annotation.Nullable;

import bedrockbreaker.graduatedcylinders.GraduatedCylinders;
import bedrockbreaker.graduatedcylinders.Proxy.FluidHandlers.FluidHandlerItem;
import bedrockbreaker.graduatedcylinders.Proxy.FluidHandlers.GasHandlerItem;
import bedrockbreaker.graduatedcylinders.Proxy.FluidHandlers.IProxyFluidHandler;
import bedrockbreaker.graduatedcylinders.Proxy.FluidHandlers.IProxyFluidHandlerItem;
import bedrockbreaker.graduatedcylinders.Proxy.FluidStacks.IProxyFluidStack;
import bedrockbreaker.graduatedcylinders.Proxy.TankProperties.IProxyTankProperties;
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
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

public class FluidHelper {

	public static IProxyFluidHandlerItem getProxyFluidHandler(ItemStack itemStack) {
		if (itemStack.isEmpty() || itemStack.getCount() != 1) return null;

		// TODO: implement some form of regsitry/api

		IFluidHandlerItem fluidHandler = FluidUtil.getFluidHandler(itemStack);
		if (fluidHandler != null) return new FluidHandlerItem(fluidHandler);

		Item item = itemStack.getItem();

		if (GraduatedCylinders.isMekanismLoaded && item instanceof IGasItem) return new GasHandlerItem((IGasItem) item, itemStack);

		return null;
	}

	public static IProxyFluidHandler getMatchingProxyFluidHandler(World world, BlockPos pos, @Nullable EnumFacing side, IProxyFluidHandlerItem fluidHandlerMatch) {
		IBlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		if (!block.hasTileEntity(state)) return null;
		TileEntity tileEntity = world.getTileEntity(pos);
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
	}
}
