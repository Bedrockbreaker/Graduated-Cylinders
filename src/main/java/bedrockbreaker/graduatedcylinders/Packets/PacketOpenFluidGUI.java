package bedrockbreaker.graduatedcylinders.Packets;

import bedrockbreaker.graduatedcylinders.FluidTransferGui;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class PacketOpenFluidGUI implements IMessage {
	
	private BlockPos pos;
	private ItemStack container;
	private boolean export;
	private boolean forced;
	private int amt;
	private int max;
	private boolean valid;

	public PacketOpenFluidGUI() {
		this.valid = false;
	}

	public PacketOpenFluidGUI(BlockPos pos, ItemStack container, boolean export, boolean forced, int amt, int max) {
		this.pos = pos;
		this.container = container;
		this.export = export;
		this.forced = forced;
		this.amt = amt;
		this.max = max;
		this.valid = true;
	}

	@Override
	public void fromBytes(ByteBuf buffer) {
		try {
			this.pos = new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt());
			this.container = ByteBufUtils.readItemStack(buffer);
			this.export = buffer.readBoolean();
			this.forced = buffer.readBoolean();
			this.amt = buffer.readInt();
			this.max = buffer.readInt();
		} catch(IndexOutOfBoundsException error) {
			System.out.println(error);
		}
		this.valid = true;
	}

	@Override
	public void toBytes(ByteBuf buffer) {
		if (!this.valid) return;
		buffer.writeInt(this.pos.getX());
		buffer.writeInt(this.pos.getY());
		buffer.writeInt(this.pos.getZ());
		ByteBufUtils.writeItemStack(buffer, this.container);
		buffer.writeBoolean(this.export);
		buffer.writeBoolean(this.forced);
		buffer.writeInt(this.amt);
		buffer.writeInt(this.max);
	}

	public static class Handler implements IMessageHandler<PacketOpenFluidGUI, IMessage> {
		
		@Override
		public IMessage onMessage(PacketOpenFluidGUI message, MessageContext context) {
			if (!message.valid || context.side != Side.CLIENT) return null;
			FMLCommonHandler.instance().getWorldThread(context.netHandler).addScheduledTask(() -> FluidTransferGui.open(message.pos, message.container, message.export, message.forced, message.amt, message.max));
			return null;
		}
	}
}
