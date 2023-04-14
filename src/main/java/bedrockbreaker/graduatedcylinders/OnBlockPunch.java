package bedrockbreaker.graduatedcylinders;

import bedrockbreaker.graduatedcylinders.FluidHelper.FindTransferrableTankResult;
import bedrockbreaker.graduatedcylinders.Packets.PacketHandler;
import bedrockbreaker.graduatedcylinders.Packets.PacketOpenFluidGUI;
import bedrockbreaker.graduatedcylinders.Proxy.ProxyFluidHandler;
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
		ProxyFluidHandler heldFluidHandler = FluidHelper.getProxyFluidHandler(heldItem);
		if (heldFluidHandler == null) return;

		EnumFacing eventSide = event.getFace();
		EnumFacing defaultSide = eventSide == null ? EnumFacing.NORTH : eventSide;

		ProxyFluidHandler blockFluidHandler = FluidHelper.getProxyFluidHandler(event.getWorld(), event.getPos(), defaultSide, heldFluidHandler.getType());
		FindTransferrableTankResult transferResults = FluidHelper.findTransferrableTank(heldFluidHandler, blockFluidHandler);
		if (transferResults == null) {
			for (EnumFacing side : EnumFacing.VALUES) {
				if (side == defaultSide) continue;
				blockFluidHandler = FluidHelper.getProxyFluidHandler(event.getWorld(), event.getPos(), side, heldFluidHandler.getType());
				transferResults = FluidHelper.findTransferrableTank(heldFluidHandler, blockFluidHandler);
				if (transferResults != null) {
					defaultSide = side;
					break;
				}
			}
			if (transferResults == null) return;
		}
		if (blockFluidHandler == null) throw new NullPointerException("Something terribly wrong has happened..."); // This should logically never throw.

		PacketHandler.INSTANCE.sendTo(new PacketOpenFluidGUI(heldItem, event.getPos(), defaultSide.getIndex(), transferResults, heldFluidHandler.getTankProperties().get(transferResults.leftTank).getContents(), blockFluidHandler.getTankProperties().get(transferResults.rightTank).getContents()), (EntityPlayerMP) event.getEntityPlayer());
	}
}