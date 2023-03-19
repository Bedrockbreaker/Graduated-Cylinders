package bedrockbreaker.graduatedcylinders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

public class FluidHelper {

	public static IFluidHandlerItem getValidFluidHandler(ItemStack item) {
		return item.isEmpty() || item.getCount() > 1 ? null : FluidUtil.getFluidHandler(item);
	}

	public static IFluidHandler[] getAllFluidHandlers(World world, BlockPos pos, EnumFacing defaultSide) {
		HashSet<IFluidHandler> handlers = new HashSet<IFluidHandler>(); // Collect only unique IFluidHandlers

		// Sort the FluidHandlers from the default side to the start of the array
		IFluidHandler defaultHandler = FluidUtil.getFluidHandler(world, pos, defaultSide);
		if (defaultHandler != null) handlers.add(defaultHandler);
		
		for (EnumFacing side : EnumFacing.VALUES) {
			if (side == defaultSide) continue;
			IFluidHandler sidedHandler = FluidUtil.getFluidHandler(world, pos, side);
			if (sidedHandler != null) handlers.add(sidedHandler);
		}
		
		return handlers.toArray(new IFluidHandler[0]);
	}

	public static FindTransferrableTankResult findTransferrableTank(IFluidHandler handler1, IFluidHandler handler2) {
		if (handler1 == null || handler2 == null) return null;
		for (int i = 0; i < handler1.getTankProperties().length; i++) {
			FluidStack fluidStack1 = handler1.getTankProperties()[i].getContents();
			for (int j = 0; j < handler2.getTankProperties().length; j++) {
				FluidStack fluidStack2 = handler2.getTankProperties()[j].getContents();
				if ((fluidStack1 == null && fluidStack2 == null) || (fluidStack1 != null && fluidStack2 != null && !fluidStack1.isFluidEqual(fluidStack2))) continue;
				FluidStack simulatedFluidStack = new FluidStack(fluidStack1 == null ? fluidStack2 : fluidStack1, Integer.MAX_VALUE);
				FluidStack simulatedExportFluid = FluidUtil.tryFluidTransfer(handler2, handler1, simulatedFluidStack, false);
				FluidStack simulatedImportFluid = FluidUtil.tryFluidTransfer(handler1, handler2, simulatedFluidStack, false);
				if ((simulatedExportFluid != null && simulatedExportFluid.amount > 0) || (simulatedImportFluid != null && simulatedImportFluid.amount > 0)) return new FindTransferrableTankResult(i, j, simulatedExportFluid != null && simulatedExportFluid.amount > 0, simulatedImportFluid != null && simulatedImportFluid.amount > 0);
			}
		}
		return null;
	}

	public static IFluidTankProperties[] getAllTankProperties(IFluidHandler[] fluidHandlers) {
		List<IFluidTankProperties> tankProperties = new ArrayList<IFluidTankProperties>();
		for (int i = 0; i < fluidHandlers.length; i++) {
			tankProperties.addAll(Arrays.asList(fluidHandlers[i].getTankProperties()));
		}
		return tankProperties.toArray(new IFluidTankProperties[0]);
	}

	public static boolean testTransferFluids(IFluidHandler handler1, IFluidTankProperties[] properties1, IFluidHandler handler2, IFluidTankProperties[] properties2) {
		for (int i = 0; i < properties1.length; i++) {
			FluidStack fluidStack1 = properties1[i].getContents();
			for (int j = 0; j < properties2.length; j++) {
				FluidStack fluidStack2 = properties2[j].getContents();
				if (fluidStack1 != null && fluidStack2 != null && !fluidStack1.isFluidEqual(fluidStack2)) continue;
				FluidStack simulatedFluidStack = new FluidStack(fluidStack1 == null ? fluidStack2 : fluidStack1, Integer.MAX_VALUE);
				FluidStack simulatedExportFluid = FluidUtil.tryFluidTransfer(handler2, handler1, simulatedFluidStack, false);
				FluidStack simulatedImportFluid = FluidUtil.tryFluidTransfer(handler1, handler2, simulatedFluidStack, false);
				if ((simulatedExportFluid != null && simulatedExportFluid.amount > 0) || (simulatedImportFluid != null && simulatedImportFluid.amount > 0)) return true;
			}
		}
		return false;
	}

	public static int getTransferAmount(ItemStack itemOne, ItemStack itemTwo) {
		IFluidHandlerItem fluidHandlerOne = getValidFluidHandler(itemOne);
		if (fluidHandlerOne == null) return 0;
		
		IFluidHandlerItem fluidHandlerTwo = getValidFluidHandler(itemTwo);
		if (fluidHandlerTwo == null) return 0;

		IFluidTankProperties itemOneProps = fluidHandlerOne.getTankProperties()[0];
		IFluidTankProperties itemTwoProps = fluidHandlerTwo.getTankProperties()[0];
		FluidStack itemOneContents = itemOneProps.getContents();
		FluidStack itemTwoContents = itemTwoProps.getContents();
		if (itemOneContents != null && itemTwoContents != null && !itemOneContents.isFluidEqual(itemTwoContents)) return 0;

		int itemOneAmount = itemOneContents == null ? 0 : itemOneContents.amount;
		int itemTwoAmount = itemTwoContents == null ? 0 : itemTwoContents.amount;
		if ((itemOneAmount == itemOneProps.getCapacity() && itemTwoAmount == itemOneProps.getCapacity()) || itemOneAmount + itemTwoAmount == 0) return 0;

		return itemTwoAmount == 0 || itemOneAmount == itemOneProps.getCapacity() ? -Math.min(itemTwoProps.getCapacity() - itemTwoAmount, itemOneAmount) : Math.min(itemOneProps.getCapacity() - itemOneAmount, itemTwoAmount);
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
