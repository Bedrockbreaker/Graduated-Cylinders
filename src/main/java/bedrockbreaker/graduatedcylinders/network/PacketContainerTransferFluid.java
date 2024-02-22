package bedrockbreaker.graduatedcylinders.network;

import bedrockbreaker.graduatedcylinders.api.IProxyFluidHandlerItem;
import bedrockbreaker.graduatedcylinders.api.IProxyFluidStack;
import bedrockbreaker.graduatedcylinders.util.FluidHelper;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

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

			EntityPlayer player = ctx.getServerHandler().playerEntity;
			Container container = player.openContainer;
			Slot hoveredSlot = container.inventorySlots.get(message.slot);

			IProxyFluidHandlerItem heldFluidHandler = FluidHelper.getProxyFluidHandler(player.inventory.getItemStack());
			IProxyFluidHandlerItem underFluidHandler = FluidHelper.getProxyFluidHandler(hoveredSlot.getStack());

			int transferAmount = FluidHelper.getTransferAmount(heldFluidHandler, underFluidHandler);
			if (transferAmount == 0) return null;

			IProxyFluidStack fluidStack = heldFluidHandler.getTankProperties(0).getContents();
			if (fluidStack == null) fluidStack = underFluidHandler.getTankProperties(0).getContents();
			if (fluidStack == null) return null;
			fluidStack = fluidStack.copy(fluidStack, Math.abs(transferAmount));

			if (FluidHelper.tryFluidTransfer(transferAmount < 0 ? underFluidHandler : heldFluidHandler, transferAmount < 0 ? heldFluidHandler : underFluidHandler, fluidStack, true) != null) player.worldObj.playSound(player.posX, player.posY, player.posZ, transferAmount < 0 ? fluidStack.getEmptySound() : fluidStack.getFillSound(), 1.0F, 1.0F, false);
			
			hoveredSlot.putStack(underFluidHandler.getContainer());
			player.inventory.setItemStack(heldFluidHandler.getContainer());

			if (!(player instanceof EntityPlayerMP)) return null;
			((EntityPlayerMP) player).isChangingQuantityOnly = false;
			((EntityPlayerMP) player).updateHeldItem();
			return null;
		}
	}
	
}
