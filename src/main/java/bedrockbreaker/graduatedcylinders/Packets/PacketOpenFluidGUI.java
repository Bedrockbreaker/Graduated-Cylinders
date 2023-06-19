package bedrockbreaker.graduatedcylinders.Packets;

import bedrockbreaker.graduatedcylinders.FluidHelper;
import bedrockbreaker.graduatedcylinders.FluidHelper.FindTransferrableTankResult;
import bedrockbreaker.graduatedcylinders.FluidTransferGui;
import bedrockbreaker.graduatedcylinders.Proxy.IProxyFluidStack;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketOpenFluidGUI implements IMessage {
	
	private ItemStack heldItem;
	private BlockPos pos;
	private int side;
	private FindTransferrableTankResult transferResults;
	private IProxyFluidStack heldFluidStack;
	private IProxyFluidStack blockFluidStack;
	private boolean valid;

	public PacketOpenFluidGUI() {
		this.valid = false;
	}

	public PacketOpenFluidGUI(ItemStack heldItem, BlockPos pos, int side, FindTransferrableTankResult transferResults, IProxyFluidStack heldFluidStack, IProxyFluidStack blockFluidStack) {
		this.heldItem = heldItem;
		this.pos = pos;
		this.side = side;
		this.transferResults = transferResults;
		this.heldFluidStack = heldFluidStack;
		this.blockFluidStack = blockFluidStack;
		this.valid = true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void fromBytes(ByteBuf buffer) {
		try {
			this.heldItem = new ItemStack(ByteBufUtils.readTag(buffer));
			this.pos = new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt());
			this.side = buffer.readInt();
			this.transferResults = new FindTransferrableTankResult(buffer.readInt(), buffer.readInt(), buffer.readBoolean(), buffer.readBoolean());
			IProxyFluidStack fluidStack = FluidHelper.getProxyFluidHandler(this.heldItem).getTankProperties(this.transferResults.leftTank).getContents();
			if (fluidStack == null) fluidStack = FluidHelper.getMatchingProxyFluidHandler(Minecraft.getMinecraft().world, this.pos, EnumFacing.getFront(this.side), FluidHelper.getProxyFluidHandler(this.heldItem)).getTankProperties(this.transferResults.rightTank).getContents();
			if (fluidStack == null) { // This shouldn't ever happen
				this.valid = false;
				return;
			}
			this.heldFluidStack = fluidStack.loadFromNBT(ByteBufUtils.readTag(buffer));
			this.blockFluidStack = fluidStack.loadFromNBT(ByteBufUtils.readTag(buffer));
		} catch(IndexOutOfBoundsException error) {
			System.out.println(error);
		}
		this.valid = true;
	}

	@Override
	public void toBytes(ByteBuf buffer) {
		if (!this.valid) return;
		// Transfer heldItem by NBT, which includes capabilities (looking at you ByteBufUtils.writeItemStack() -_-)
		ByteBufUtils.writeTag(buffer, this.heldItem.writeToNBT(new NBTTagCompound()));
		buffer.writeInt(this.pos.getX());
		buffer.writeInt(this.pos.getY());
		buffer.writeInt(this.pos.getZ());
		buffer.writeInt(this.side);
		buffer.writeInt(this.transferResults.leftTank);
		buffer.writeInt(this.transferResults.rightTank);
		buffer.writeBoolean(this.transferResults.canExport);
		buffer.writeBoolean(this.transferResults.canImport);
		ByteBufUtils.writeTag(buffer, heldFluidStack == null ? null : heldFluidStack.writeToNBT(new NBTTagCompound()));
		ByteBufUtils.writeTag(buffer, blockFluidStack == null ? null : blockFluidStack.writeToNBT(new NBTTagCompound()));
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
