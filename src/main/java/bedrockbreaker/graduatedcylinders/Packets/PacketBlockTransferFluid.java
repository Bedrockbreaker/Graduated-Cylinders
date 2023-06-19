package bedrockbreaker.graduatedcylinders.Packets;

import bedrockbreaker.graduatedcylinders.FluidHelper;
import bedrockbreaker.graduatedcylinders.Proxy.IProxyFluidHandler;
import bedrockbreaker.graduatedcylinders.Proxy.IProxyFluidHandlerItem;
import bedrockbreaker.graduatedcylinders.Proxy.IProxyFluidStack;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class PacketBlockTransferFluid implements IMessage {

	private ItemStack heldItem;
	private int heldTankIndex;
	private BlockPos pos;
	private int side;
	private int blockTankIndex;
	private int amount;
	private boolean valid;

	public PacketBlockTransferFluid() {
		this.valid = false;
	}

	public PacketBlockTransferFluid(ItemStack heldItem, int heldTankIndex, BlockPos pos, int side, int blockTankIndex, int amount) {
		this.heldItem = heldItem;
		this.heldTankIndex = heldTankIndex;
		this.pos = pos;
		this.side = side;
		this.blockTankIndex = blockTankIndex;
		this.amount = amount;
		this.valid = true;
	}

	@Override
	public void fromBytes(ByteBuf buffer) {
		try {
			this.heldItem = new ItemStack(ByteBufUtils.readTag(buffer));
			this.heldTankIndex = buffer.readInt();
			this.pos = new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt());
			this.side = buffer.readInt();
			this.blockTankIndex = buffer.readInt();
			this.amount = buffer.readInt();
		} catch(IndexOutOfBoundsException error) {
			System.out.println(error);
		}
		this.valid = true;
	}

	@Override
	public void toBytes(ByteBuf buffer) {
		if (!this.valid) return;
		ByteBufUtils.writeTag(buffer, heldItem.writeToNBT(new NBTTagCompound()));
		buffer.writeInt(this.heldTankIndex);
		buffer.writeInt(this.pos.getX());
		buffer.writeInt(this.pos.getY());
		buffer.writeInt(this.pos.getZ());
		buffer.writeInt(this.side);
		buffer.writeInt(this.blockTankIndex);
		buffer.writeInt(this.amount);
	}

	public static class Handler implements IMessageHandler<PacketBlockTransferFluid, IMessage> {

		@Override
		@SuppressWarnings("null")
		public IMessage onMessage(PacketBlockTransferFluid message, MessageContext ctx) {
			if (!message.valid || ctx.side != Side.SERVER) return null;
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
				if (message.amount == 0) return;

				EntityPlayer player = ctx.getServerHandler().player;
				int slot = -1;
				for (int i = 0; i < InventoryPlayer.getHotbarSize(); i++) {
					ItemStack stack = player.inventory.getStackInSlot(i);
					if (!stack.isEmpty() && ItemStack.areItemStacksEqual(stack, message.heldItem)) {
						slot = i;
						break;
					}
				}
				if (slot == -1) return;

				World world = ctx.getServerHandler().player.getServerWorld();
				IProxyFluidHandlerItem heldFluidHandler = FluidHelper.getProxyFluidHandler(message.heldItem);
				IProxyFluidHandler blockFluidHandler = FluidHelper.getMatchingProxyFluidHandler(world, message.pos, EnumFacing.getFront(message.side), heldFluidHandler);
				if (heldFluidHandler == null || blockFluidHandler == null) return;

				IProxyFluidStack fluidStack = heldFluidHandler.getTankProperties(message.heldTankIndex).getContents();
				if (fluidStack == null) fluidStack = blockFluidHandler.getTankProperties(message.blockTankIndex).getContents();
				if (fluidStack == null) return;
				fluidStack = fluidStack.copy(fluidStack, Math.abs(message.amount));

				if (FluidHelper.tryFluidTransfer(message.amount < 0 ? blockFluidHandler : heldFluidHandler, message.amount < 0 ? heldFluidHandler : blockFluidHandler, fluidStack, true) != null) world.playSound(null, player.getPosition(), message.amount < 0 ? fluidStack.getEmptySound() : fluidStack.getFillSound(), SoundCategory.PLAYERS, 1.0F, 1.0F);
				world.getTileEntity(message.pos).markDirty();

				player.inventory.setInventorySlotContents(slot, heldFluidHandler.getContainer());
			});
			return null;
		}
	}
}
