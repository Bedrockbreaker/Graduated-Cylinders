package bedrockbreaker.graduatedcylinders.Packets;

import bedrockbreaker.graduatedcylinders.FluidTransferGui;
import bedrockbreaker.graduatedcylinders.FluidHelper.FindTransferrableTankResult;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class PacketOpenFluidGUI implements IMessage {
	
	private ItemStack heldItem;
	private BlockPos pos;
	private int side;
	private FindTransferrableTankResult transferResults;
	private FluidStack heldFluidStack;
	private FluidStack blockFluidStack;
	private boolean valid;

	public PacketOpenFluidGUI() {
		this.valid = false;
	}

	public PacketOpenFluidGUI(ItemStack heldItem, BlockPos pos, int side, FindTransferrableTankResult transferResults, FluidStack heldFluidStack, FluidStack blockFluidStack) {
		this.heldItem = heldItem;
		this.pos = pos;
		this.side = side;
		this.transferResults = transferResults;
		this.heldFluidStack = heldFluidStack;
		this.blockFluidStack = blockFluidStack;
		this.valid = true;
	}

	@Override
	public void fromBytes(ByteBuf buffer) {
		try {
			this.heldItem = new ItemStack(ByteBufUtils.readTag(buffer));
			this.pos = new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt());
			this.side = buffer.readInt();
			this.transferResults = new FindTransferrableTankResult(buffer.readInt(), buffer.readInt(), buffer.readBoolean(), buffer.readBoolean());
			this.heldFluidStack = FluidStack.loadFluidStackFromNBT(ByteBufUtils.readTag(buffer));
			this.blockFluidStack = FluidStack.loadFluidStackFromNBT(ByteBufUtils.readTag(buffer));
		} catch(IndexOutOfBoundsException error) {
			System.out.println(error);
		}
		this.valid = true;
	}

	@Override
	public void toBytes(ByteBuf buffer) {
		if (!this.valid) return;
		// Transfer heldItem by NBT, which carrries capabilities (looking at you, Astral Sorcery -_-)
		ByteBufUtils.writeTag(buffer, this.heldItem.writeToNBT(new NBTTagCompound()));
		buffer.writeInt(this.pos.getX());
		buffer.writeInt(this.pos.getY());
		buffer.writeInt(this.pos.getZ());
		buffer.writeInt(this.side);
		buffer.writeInt(this.transferResults.leftTank);
		buffer.writeInt(this.transferResults.rightTank);
		buffer.writeBoolean(this.transferResults.canExport);
		buffer.writeBoolean(this.transferResults.canImport);
		ByteBufUtils.writeTag(buffer, heldFluidStack != null ? heldFluidStack.writeToNBT(new NBTTagCompound()) : null);
		ByteBufUtils.writeTag(buffer, blockFluidStack != null ? blockFluidStack.writeToNBT(new NBTTagCompound()) : null);
	}

	public static class Handler implements IMessageHandler<PacketOpenFluidGUI, IMessage> {
		
		@Override
		public IMessage onMessage(PacketOpenFluidGUI message, MessageContext context) {
			if (!message.valid || context.side != Side.CLIENT) return null;
			FMLCommonHandler.instance().getWorldThread(context.netHandler).addScheduledTask(() -> FluidTransferGui.open(message.heldItem, message.pos, message.side, message.transferResults, message.heldFluidStack, message.blockFluidStack));
			return null;
		}
	}
}
