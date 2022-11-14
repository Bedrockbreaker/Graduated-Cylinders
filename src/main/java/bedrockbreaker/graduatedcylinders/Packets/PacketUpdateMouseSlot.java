package bedrockbreaker.graduatedcylinders.Packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class PacketUpdateMouseSlot implements IMessage {
	
	private ItemStack stack;
	private boolean valid;

	public PacketUpdateMouseSlot() {
		this.valid = false;
	}

	public PacketUpdateMouseSlot(ItemStack stack) {
		this.stack = stack;
		this.valid = true;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		try {
			this.stack = ByteBufUtils.readItemStack(buf);
		} catch(IndexOutOfBoundsException error) {
			System.out.println(error);
		}
		this.valid = true;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		if (!this.valid) return;
		ByteBufUtils.writeItemStack(buf, this.stack);
	}

	public static class Handler implements IMessageHandler<PacketUpdateMouseSlot, IMessage> {

		@Override
		public IMessage onMessage(PacketUpdateMouseSlot message, MessageContext ctx) {
			if (!message.valid || ctx.side != Side.CLIENT) return null;
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> Minecraft.getMinecraft().player.inventory.setItemStack(message.stack));
			return null;
		}
	}
}
