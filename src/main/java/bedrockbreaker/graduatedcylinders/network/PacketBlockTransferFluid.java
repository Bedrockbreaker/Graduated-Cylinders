package bedrockbreaker.graduatedcylinders.network;

import bedrockbreaker.graduatedcylinders.api.IProxyFluidHandler;
import bedrockbreaker.graduatedcylinders.api.IProxyFluidHandlerItem;
import bedrockbreaker.graduatedcylinders.api.IProxyFluidStack;
import bedrockbreaker.graduatedcylinders.util.BlockPos;
import bedrockbreaker.graduatedcylinders.util.FluidHelper;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class PacketBlockTransferFluid implements IMessage {

	private ItemStack heldItem;
	private int heldTankIndex;
	private BlockPos pos;
	private int side;
	private int blockTankIndex;
	private int amount;

	public PacketBlockTransferFluid() {}

	public PacketBlockTransferFluid(ItemStack heldItem, int heldTankIndex, BlockPos pos, int side, int blockTankIndex, int amount) {
		this.heldItem = heldItem;
		this.heldTankIndex = heldTankIndex;
		this.pos = pos;
		this.side = side;
		this.blockTankIndex = blockTankIndex;
		this.amount = amount;
	}

	@Override
	public void fromBytes(ByteBuf buffer) {
		try {
			this.heldItem = ItemStack.loadItemStackFromNBT(ByteBufUtils.readTag(buffer));
			this.heldTankIndex = buffer.readInt();
			this.pos = new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt());
			this.side = buffer.readInt();
			this.blockTankIndex = buffer.readInt();
			this.amount = buffer.readInt();
		} catch(IndexOutOfBoundsException error) {
			System.out.println(error);
		}
	}

	@Override
	public void toBytes(ByteBuf buffer) {
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
			if (ctx.side != Side.SERVER || message.amount == 0) return null;

			EntityPlayer player = ctx.getServerHandler().playerEntity;
			int slot = -1;
			for (int i = 0; i < InventoryPlayer.getHotbarSize(); i++) {
				ItemStack stack = player.inventory.getStackInSlot(i);
				if (stack != null && stack.getItem() != null && stack.stackSize > 0 && ItemStack.areItemStacksEqual(stack, message.heldItem)) {
					slot = i;
					break;
				}
			}
			if (slot == -1) return null;

			World world = ctx.getServerHandler().playerEntity.worldObj;
			IProxyFluidHandlerItem heldFluidHandler = FluidHelper.getProxyFluidHandler(message.heldItem);
			IProxyFluidHandler blockFluidHandler = FluidHelper.getMatchingProxyFluidHandler(world, message.pos, ForgeDirection.getOrientation(message.side), heldFluidHandler);
			if (heldFluidHandler == null || blockFluidHandler == null) return null;

			IProxyFluidStack fluidStack = heldFluidHandler.getTankProperties(message.heldTankIndex).getContents();
			if (fluidStack == null) fluidStack = blockFluidHandler.getTankProperties(message.blockTankIndex).getContents();
			if (fluidStack == null) return null;
			fluidStack = fluidStack.copy(fluidStack, Math.abs(message.amount));

			if (FluidHelper.tryFluidTransfer(message.amount < 0 ? blockFluidHandler : heldFluidHandler, message.amount < 0 ? heldFluidHandler : blockFluidHandler, fluidStack, true) != null) world.playSound(player.posX, player.posY, player.posZ, message.amount < 0 ? fluidStack.getEmptySound() : fluidStack.getFillSound(), 1.0F, 1.0F, false);
			world.getTileEntity(message.pos.getX(), message.pos.getY(), message.pos.getZ());

			player.inventory.setInventorySlotContents(slot, heldFluidHandler.getContainer());

			return null;
		}
	}
}
