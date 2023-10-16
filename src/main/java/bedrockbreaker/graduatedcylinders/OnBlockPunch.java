package bedrockbreaker.graduatedcylinders;

import java.util.ArrayList;

import org.apache.commons.lang3.tuple.Pair;

import bedrockbreaker.graduatedcylinders.api.IProxyFluidHandler;
import bedrockbreaker.graduatedcylinders.api.IProxyFluidHandlerItem;
import bedrockbreaker.graduatedcylinders.api.IProxyFluidStack;
import bedrockbreaker.graduatedcylinders.network.PacketHandler;
import bedrockbreaker.graduatedcylinders.network.PacketOpenFluidGUI;
import bedrockbreaker.graduatedcylinders.util.FluidHelper;
import bedrockbreaker.graduatedcylinders.util.FluidHelper.TransferrableFluidResult;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

// For some unknown reason, this code needs to exist on the client as well
@EventBusSubscriber(modid = GraduatedCylinders.MODID)
public class OnBlockPunch {

	@SubscribeEvent
	public static void onBlockLeftClicked(PlayerInteractEvent.LeftClickBlock event) {
		// Return if running on logical client, or player is in creative mode
		if (event.getWorld().isRemote || event.getEntityPlayer().isCreative()) return;

		ItemStack heldItem = event.getItemStack();
		IProxyFluidHandlerItem heldFluidHandler = FluidHelper.getProxyFluidHandler(heldItem);
		if (heldFluidHandler == null) return;

		EnumFacing eventSide = event.getFace();
		EnumFacing defaultSide = eventSide == null ? EnumFacing.DOWN : eventSide;

		ArrayList<ArrayList<TransferrableFluidResult>> allTransferResults = new ArrayList<ArrayList<TransferrableFluidResult>>(6);
		ArrayList<IProxyFluidStack> heldFluidStacks = new ArrayList<IProxyFluidStack>();
		ArrayList<ArrayList<IProxyFluidStack>> blockFluidStacks = new ArrayList<ArrayList<IProxyFluidStack>>();
		Pair<EnumFacing, TransferrableFluidResult> defaultIndex = Pair.of(defaultSide, new TransferrableFluidResult(0, 0, false, false));
		for (int i = 0; i < 6; i++) { // Pre-fill the parent array
			allTransferResults.add(new ArrayList<TransferrableFluidResult>());
			blockFluidStacks.add(new ArrayList<IProxyFluidStack>());
		}

		// Calculate transfer results for all item-block tank pairs on each face
		// Ew, triple for-loop, I know, but the total loop count will almost never even be > 100 (which would require numHeldTanks * numBlockTanks > 100/6)
		for (int i = -1; i < 6; i++) {
			if (i == defaultSide.getIndex()) continue;
			EnumFacing side = i == -1 ? defaultSide : EnumFacing.getFront(i);
			IProxyFluidHandler blockFluidHandler = FluidHelper.getMatchingProxyFluidHandler(event.getWorld(), event.getPos(), side, heldFluidHandler);
			if (blockFluidHandler == null) continue;
			for (int j = 0; j < heldFluidHandler.getNumTanks(); j++) {
				if (heldFluidStacks.size() < heldFluidHandler.getNumTanks()) heldFluidStacks.add(heldFluidHandler.getTankProperties(j).getContents());
				for (int k = 0; k < blockFluidHandler.getNumTanks(); k++) {
					if (j == 0) blockFluidStacks.get(side.getIndex()).add(blockFluidHandler.getTankProperties(k).getContents());
					TransferrableFluidResult transferResult = FluidHelper.getTransferResult(heldFluidHandler, j, blockFluidHandler, k);
					Pair<EnumFacing, TransferrableFluidResult> index = Pair.of(side, transferResult);
					allTransferResults.get(side.getIndex()).add(transferResult);
					if ((!(defaultIndex.getRight().canExport || defaultIndex.getRight().canImport) && (transferResult.canExport || transferResult.canImport)) || ((defaultIndex.getRight().canExport ^ defaultIndex.getRight().canImport) && transferResult.canExport && transferResult.canImport)) defaultIndex = index;
				}
			}
		}
		if (!(defaultIndex.getRight().canExport || defaultIndex.getRight().canImport)) return;

		PacketHandler.INSTANCE.sendTo(new PacketOpenFluidGUI(heldItem, event.getPos(), allTransferResults, defaultIndex.getRight().sourceTank, defaultIndex.getLeft().getIndex(), defaultIndex.getRight().destinationTank, heldFluidStacks, blockFluidStacks), (EntityPlayerMP) event.getEntityPlayer());
	}
}