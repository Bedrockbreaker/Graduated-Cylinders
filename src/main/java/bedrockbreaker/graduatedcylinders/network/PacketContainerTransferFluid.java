package bedrockbreaker.graduatedcylinders.network;

import bedrockbreaker.graduatedcylinders.api.IProxyFluidHandlerItem;
import bedrockbreaker.graduatedcylinders.api.IProxyFluidStack;
import bedrockbreaker.graduatedcylinders.util.FluidHelper;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class PacketContainerTransferFluid implements IMessage {

	private int slot;

	public PacketContainerTransferFluid() {}

	public PacketContainerTransferFluid(int slot) {
		this.slot = slot;
	}

	@Override
	public void fromBytes(ByteBuf buffer) {
		try {
			this.slot = buffer.readInt();
		} catch(IndexOutOfBoundsException error) {
			System.out.println(error);
		}
	}

	@Override
	public void toBytes(ByteBuf buffer) {
		buffer.writeInt(this.slot);
	}

	public static class Handler implements IMessageHandler<PacketContainerTransferFluid, IMessage> {
		
		@Override
		public IMessage onMessage(PacketContainerTransferFluid message, MessageContext ctx) {
			if (ctx.side != Side.SERVER) return null;
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
				EntityPlayer player = ctx.getServerHandler().player;
				Container container = player.openContainer;
				Slot hoveredSlot = container.inventorySlots.get(message.slot);

				IProxyFluidHandlerItem heldFluidHandler = FluidHelper.getProxyFluidHandler(player.inventory.getItemStack());
				IProxyFluidHandlerItem underFluidHandler = FluidHelper.getProxyFluidHandler(hoveredSlot.getStack());

				int transferAmount = FluidHelper.getTransferAmount(heldFluidHandler, underFluidHandler);
				if (transferAmount == 0) return;

				IProxyFluidStack fluidStack = heldFluidHandler.getTankProperties(0).getContents();
				if (fluidStack == null) fluidStack = underFluidHandler.getTankProperties(0).getContents();
				if (fluidStack == null) return;
				fluidStack = fluidStack.copy(fluidStack, Math.abs(transferAmount));

				if (FluidHelper.tryFluidTransfer(transferAmount < 0 ? underFluidHandler : heldFluidHandler, transferAmount < 0 ? heldFluidHandler : underFluidHandler, fluidStack, true) != null) player.world.playSound(null, player.getPosition(), transferAmount < 0 ? fluidStack.getEmptySound() : fluidStack.getFillSound(), SoundCategory.PLAYERS, 1.0F, 1.0F);
				
				// Despite cancelling the right click event, I have to manually swap the items, resulting in a brief flash of the items swapping.
				// See InventoryHandler.onRightClick for more details
				hoveredSlot.putStack(heldFluidHandler.getContainer());
				player.inventory.setItemStack(underFluidHandler.getContainer());

				if (!(player instanceof EntityPlayerMP)) return;
				((EntityPlayerMP) player).isChangingQuantityOnly = false;
				((EntityPlayerMP) player).updateHeldItem();
			});
			return null;
		}
	}
	
}
