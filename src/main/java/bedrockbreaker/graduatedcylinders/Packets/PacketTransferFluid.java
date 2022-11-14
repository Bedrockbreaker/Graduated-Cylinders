package bedrockbreaker.graduatedcylinders.Packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class PacketTransferFluid implements IMessage {

	private BlockPos pos;
	private ItemStack container;
	private boolean export;
	private int amt;
	private boolean valid;

	public PacketTransferFluid() {
		this.valid = false;
	}

	public PacketTransferFluid(BlockPos pos, ItemStack container, boolean export, int amt) {
		this.pos = pos;
		this.container = container;
		this.export = export;
		this.amt = amt;
		this.valid = true;
	}

	@Override
	public void fromBytes(ByteBuf buffer) {
		try {
			this.pos = new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt());
			this.container = ByteBufUtils.readItemStack(buffer);
			this.export = buffer.readBoolean();
			this.amt = buffer.readInt();
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
		buffer.writeInt(this.amt);
	}

	public static class Handler implements IMessageHandler<PacketTransferFluid, IMessage> {
		
		@Override
		@SuppressWarnings("null")
		public IMessage onMessage(PacketTransferFluid message, MessageContext ctx) {
			if (!message.valid || ctx.side != Side.SERVER) return null;
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
				World world = ctx.getServerHandler().player.getServerWorld();
				if (message.export) {
					if (FluidUtil.tryFluidTransfer(FluidUtil.getFluidHandler(world, message.pos, null), FluidUtil.getFluidHandler(message.container), message.amt, true) != null) world.playSound(null, ctx.getServerHandler().player.getPosition(), SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.PLAYERS, 1.0F, 1.0F);
				} else {
					if (FluidUtil.tryFluidTransfer(FluidUtil.getFluidHandler(message.container), FluidUtil.getFluidHandler(world, message.pos, null), message.amt, true) != null) world.playSound(null, ctx.getServerHandler().player.getPosition(), SoundEvents.ITEM_BUCKET_FILL, SoundCategory.PLAYERS, 1.0F, 1.0F);
				}
				world.getTileEntity(message.pos).markDirty();
				ctx.getServerHandler().player.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, FluidUtil.getFluidHandler(message.container).getContainer());
			});
			return null;
		}
	}
}
