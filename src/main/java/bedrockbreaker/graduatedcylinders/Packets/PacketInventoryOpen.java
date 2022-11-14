package bedrockbreaker.graduatedcylinders.Packets;

import bedrockbreaker.graduatedcylinders.ContainerListener;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class PacketInventoryOpen implements IMessage {

	private ContainerListener listener;
	private boolean valid;

	public PacketInventoryOpen() {
		this.valid = true;
	}

	@Override
	public void fromBytes(ByteBuf buf) {}

	@Override
	public void toBytes(ByteBuf buf) {}

	public static class Handler implements IMessageHandler<PacketInventoryOpen, IMessage> {
		
		@Override
		public IMessage onMessage(PacketInventoryOpen message, MessageContext ctx) {
			if (!message.valid || ctx.side != Side.SERVER) return null;
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
				message.listener = new ContainerListener(ctx.getServerHandler().player);
				ctx.getServerHandler().player.inventoryContainer.addListener(message.listener);
			});
			return null;
		}
	}
}
