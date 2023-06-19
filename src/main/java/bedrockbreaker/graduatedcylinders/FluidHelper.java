package bedrockbreaker.graduatedcylinders;

import javax.annotation.Nullable;

import bedrockbreaker.graduatedcylinders.Proxy.FluidHandlers.EssentiaHandler;
import bedrockbreaker.graduatedcylinders.Proxy.FluidHandlers.EssentiaHandlerItem;
import bedrockbreaker.graduatedcylinders.Proxy.FluidHandlers.FluidHandler;
import bedrockbreaker.graduatedcylinders.Proxy.FluidHandlers.FluidHandlerItem;
import bedrockbreaker.graduatedcylinders.Proxy.FluidHandlers.GasHandler;
import bedrockbreaker.graduatedcylinders.Proxy.FluidHandlers.GasHandlerItem;
import bedrockbreaker.graduatedcylinders.Proxy.FluidHandlers.IProxyFluidHandler;
import bedrockbreaker.graduatedcylinders.Proxy.FluidHandlers.IProxyFluidHandlerItem;
import bedrockbreaker.graduatedcylinders.Proxy.FluidStacks.IProxyFluidStack;
import bedrockbreaker.graduatedcylinders.Proxy.TankProperties.IProxyTankProperties;
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
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.aspects.IEssentiaContainerItem;

public class FluidHelper {

	public static IProxyFluidHandlerItem getProxyFluidHandler(ItemStack itemStack) {
		if (itemStack.isEmpty() || itemStack.getCount() != 1) return null;

		IFluidHandlerItem fluidHandler = FluidUtil.getFluidHandler(itemStack);

		if (fluidHandler != null) return new FluidHandlerItem(fluidHandler);

		Item item = itemStack.getItem();

		if (GraduatedCylinders.isMekanismLoaded && item instanceof IGasItem) return new GasHandlerItem((IGasItem) item, itemStack);
		
		if (GraduatedCylinders.isThaumcraftLoaded && item instanceof IEssentiaContainerItem) {
			IEssentiaContainerItem essentiaHandlerItem = (IEssentiaContainerItem) item;
			if (!essentiaHandlerItem.ignoreContainedAspects()) return new EssentiaHandlerItem(essentiaHandlerItem, itemStack);
		}

		return null;
	}

	public static IProxyFluidHandler getMatchingProxyFluidHandler(World world, BlockPos pos, @Nullable EnumFacing side, IProxyFluidHandler fluidHandlerMatch) {
		IBlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		if (!block.hasTileEntity(state)) return null;
		TileEntity tileEntity = world.getTileEntity(pos);
		if (tileEntity == null) return null;

		if (fluidHandlerMatch instanceof FluidHandler && tileEntity.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side)) return new FluidHandler(tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side));
		if (GraduatedCylinders.isMekanismLoaded && (fluidHandlerMatch instanceof GasHandlerItem || fluidHandlerMatch instanceof GasHandler) && tileEntity instanceof IGasHandler) return new GasHandler((IGasHandler) tileEntity, side);
		if (GraduatedCylinders.isThaumcraftLoaded && (fluidHandlerMatch instanceof EssentiaHandlerItem || fluidHandlerMatch instanceof EssentiaHandler) && tileEntity instanceof IAspectContainer) return new EssentiaHandler((IAspectContainer) tileEntity, tileEntity, side);
		return null;
	}

	public static IProxyFluidStack tryFluidTransfer(IProxyFluidHandler fluidDestination, IProxyFluidHandler fluidSource, IProxyFluidStack resource, boolean doTransfer) {
		IProxyFluidStack drainable = fluidSource.drain(resource, false);
		return drainable != null && drainable.getAmount() > 0 && resource.isFluidEqual(drainable) ? FluidHelper.tryFluidTransfer_Internal(fluidDestination, fluidSource, drainable, doTransfer) : null;
	}

	public static FindTransferrableTankResult findTransferrableTank(IProxyFluidHandler handler1, IProxyFluidHandler handler2) {
		if (handler1 == null || handler2 == null) return null;
		for (int i = 0; i < handler1.getNumTanks(); i++) {
			IProxyFluidStack fluidStack1 = handler1.getTankProperties(i).getContents();
			for (int j = 0; j < handler2.getNumTanks(); j++) {
				IProxyFluidStack fluidStack2 = handler2.getTankProperties(j).getContents();
				if ((fluidStack1 == null && fluidStack2 == null) || (fluidStack1 != null && fluidStack2 != null && !fluidStack1.isFluidEqual(fluidStack2))) continue;
				IProxyFluidStack workingStack = fluidStack1 == null ? fluidStack2 : fluidStack1;
				if (workingStack == null) return null; // If this returns, something terrible must have happened.
				IProxyFluidStack simulatedFluidStack = workingStack.copy(workingStack, Integer.MAX_VALUE);
				IProxyFluidStack simulatedExportFluid = FluidHelper.tryFluidTransfer(handler2, handler1, simulatedFluidStack, false);
				IProxyFluidStack simulatedImportFluid = FluidHelper.tryFluidTransfer(handler1, handler2, simulatedFluidStack, false);
				if ((simulatedExportFluid != null && simulatedExportFluid.getAmount() > 0) || (simulatedImportFluid != null && simulatedImportFluid.getAmount() > 0)) return new FindTransferrableTankResult(i, j, simulatedExportFluid != null && simulatedExportFluid.getAmount() > 0, simulatedImportFluid != null && simulatedImportFluid.getAmount() > 0);
			}
		}
		return null;
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

	@Nullable
	private static IProxyFluidStack tryFluidTransfer_Internal(IProxyFluidHandler fluidDestination, IProxyFluidHandler fluidSource, IProxyFluidStack drainable, boolean doTransfer) {
		int fillableAmount = fluidDestination.fill(drainable, false);
		if (fillableAmount <= 0) return null;
		if (!doTransfer) return drainable.copy(drainable, fillableAmount);
		IProxyFluidStack drained = fluidSource.drain(fillableAmount, true);
		return drained == null ? null : drained.copy(drained, fluidDestination.fill(drainable, true));
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
