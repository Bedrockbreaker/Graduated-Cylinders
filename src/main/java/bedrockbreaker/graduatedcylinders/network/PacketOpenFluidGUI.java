package bedrockbreaker.graduatedcylinders.network;

import bedrockbreaker.graduatedcylinders.util.BufferHelper;
import bedrockbreaker.graduatedcylinders.util.FluidHelper;
import bedrockbreaker.graduatedcylinders.util.FluidHelper.TransferrableFluidResult;

import java.util.ArrayList;

import bedrockbreaker.graduatedcylinders.FluidTransferGui;
import bedrockbreaker.graduatedcylinders.api.IProxyFluidHandler;
import bedrockbreaker.graduatedcylinders.api.IProxyFluidStack;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class PacketOpenFluidGUI implements IMessage {
	
	private ItemStack heldItem;
	private BlockPos pos;
	private ArrayList<ArrayList<TransferrableFluidResult>> transferResults = new ArrayList<ArrayList<TransferrableFluidResult>>();
	private int heldTankIndex;
	private int side;
	private int blockTankIndex;
	private ArrayList<IProxyFluidStack> heldFluidStacks = new ArrayList<IProxyFluidStack>();
	private ArrayList<ArrayList<IProxyFluidStack>> blockFluidStacks = new ArrayList<ArrayList<IProxyFluidStack>>();

	public PacketOpenFluidGUI() {}

	public PacketOpenFluidGUI(ItemStack heldItem, BlockPos pos, ArrayList<ArrayList<TransferrableFluidResult>> transferResults, int heldTankIndex,  int side, int blockTankIndex, ArrayList<IProxyFluidStack> heldFluidStacks, ArrayList<ArrayList<IProxyFluidStack>> blockFluidStacks) {
		this.heldItem = heldItem;
		this.pos = pos;
		this.transferResults = transferResults;
		this.heldTankIndex = heldTankIndex;
		this.side = side;
		this.blockTankIndex = blockTankIndex;
		this.heldFluidStacks = heldFluidStacks;
		this.blockFluidStacks = blockFluidStacks;
	}

	@Override
	public void fromBytes(ByteBuf buffer) {
		try {
			this.heldItem = new ItemStack(ByteBufUtils.readTag(buffer));
			this.pos = new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt());
			this.side = buffer.readInt();

			int numSidedTransferResults = buffer.readInt(); // should theoretically always be 6
			for (int i = 0; i < numSidedTransferResults; i++) {
				this.transferResults.add(new ArrayList<TransferrableFluidResult>());
				int numTransferResults = buffer.readInt();
				for (int j = 0; j < numTransferResults; j++) {
					this.transferResults.get(i).add(BufferHelper.readTransferrableFluidResult(buffer));
				}
			}
			
			// The fluid handler is just for reconstructing the fluid stacks properly, it's not actually used to handle any fluids
			IProxyFluidHandler fluidHandler = FluidHelper.getProxyFluidHandler(this.heldItem);

			this.heldTankIndex = buffer.readInt();
			int numHeldFluidStacks = buffer.readInt();
			for (int i = 0; i < numHeldFluidStacks; i++) {
				this.heldFluidStacks.add(BufferHelper.readFluidStack(buffer, fluidHandler));
			}

			this.blockTankIndex = buffer.readInt();
			int numSidedBlockFluidStacks = buffer.readInt();
			for (int i = 0; i < numSidedBlockFluidStacks; i++) {
				int numBlockFluidStacks = buffer.readInt();
				this.blockFluidStacks.add(new ArrayList<IProxyFluidStack>());
				for (int j = 0; j < numBlockFluidStacks; j++) {
					this.blockFluidStacks.get(i).add(BufferHelper.readFluidStack(buffer, fluidHandler));
				}
			}
		} catch(IndexOutOfBoundsException error) {
			System.out.println(error);
		}
	}

	@Override
	public void toBytes(ByteBuf buffer) {
		// Transfer heldItem by NBT, which includes capabilities (looking at you ByteBufUtils.writeItemStack() -_-)
		ByteBufUtils.writeTag(buffer, this.heldItem.writeToNBT(new NBTTagCompound()));
		buffer.writeInt(this.pos.getX());
		buffer.writeInt(this.pos.getY());
		buffer.writeInt(this.pos.getZ());
		buffer.writeInt(this.side);

		buffer.writeInt(this.transferResults.size());
		for (ArrayList<TransferrableFluidResult> sidedTransferResults : this.transferResults) {
			buffer.writeInt(sidedTransferResults.size());
			for (TransferrableFluidResult transferResult : sidedTransferResults) {
				BufferHelper.writeTransferrableFluidResult(buffer, transferResult);
			}
		}

		buffer.writeInt(this.heldTankIndex);
		buffer.writeInt(this.heldFluidStacks.size());
		for (IProxyFluidStack fluidStack : this.heldFluidStacks) {
			BufferHelper.writeFluidStack(buffer, fluidStack);
		}

		buffer.writeInt(this.blockTankIndex);
		buffer.writeInt(this.blockFluidStacks.size());
		for (ArrayList<IProxyFluidStack> sidedFluidStacks : this.blockFluidStacks) {
			buffer.writeInt(sidedFluidStacks.size());
			for (IProxyFluidStack fluidStack : sidedFluidStacks) {
				BufferHelper.writeFluidStack(buffer, fluidStack);
			}
		}
	}

	public static class Handler implements IMessageHandler<PacketOpenFluidGUI, IMessage> {
		
		@Override
		public IMessage onMessage(PacketOpenFluidGUI message, MessageContext context) {
			if (context.side != Side.CLIENT) return null;
			FMLCommonHandler.instance().getWorldThread(context.netHandler).addScheduledTask(() -> FluidTransferGui.open(message.heldItem, message.pos, message.transferResults, message.heldTankIndex, message.side, message.blockTankIndex, message.heldFluidStacks, message.blockFluidStacks));
			return null;
		}
	}
}
