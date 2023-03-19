package bedrockbreaker.graduatedcylinders;

import bedrockbreaker.graduatedcylinders.FluidHelper.FindTransferrableTankResult;
import bedrockbreaker.graduatedcylinders.Packets.PacketHandler;
import bedrockbreaker.graduatedcylinders.Packets.PacketOpenFluidGUI;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class OnBlockPunch {

	@SubscribeEvent
	public void onBlockLeftClicked(PlayerInteractEvent.LeftClickBlock event) {
		// Return if running on logical client, or player is in creative mode
		if (event.getWorld().isRemote || event.getEntityPlayer().isCreative()) return;

		ItemStack heldItem = event.getItemStack();
		IFluidHandlerItem heldFluidHandler = FluidHelper.getValidFluidHandler(heldItem);
		if (heldFluidHandler == null) return;

		EnumFacing eventSide = event.getFace();
		EnumFacing defaultSide = eventSide == null ? EnumFacing.NORTH : eventSide;

		IFluidHandler blockFluidHandler = FluidUtil.getFluidHandler(event.getWorld(), event.getPos(), defaultSide);
		FindTransferrableTankResult transferResults = FluidHelper.findTransferrableTank(heldFluidHandler, blockFluidHandler);
		if (transferResults == null) {
			for (EnumFacing side : EnumFacing.VALUES) {
				if (side == defaultSide) continue;
				blockFluidHandler = FluidUtil.getFluidHandler(event.getWorld(), event.getPos(), side);
				transferResults = FluidHelper.findTransferrableTank(heldFluidHandler, blockFluidHandler);
				if (transferResults != null) {
					defaultSide = side;
					break;
				}
			}
			if (transferResults == null) return;
		}
		if (blockFluidHandler == null) throw new NullPointerException("Something terribly wrong has happened..."); // This should logically never throw.

		PacketHandler.INSTANCE.sendTo(new PacketOpenFluidGUI(heldItem, event.getPos(), defaultSide.getIndex(), transferResults, heldFluidHandler.getTankProperties()[transferResults.leftTank].getContents(), blockFluidHandler.getTankProperties()[transferResults.rightTank].getContents()), (EntityPlayerMP) event.getEntityPlayer());
	}
}
