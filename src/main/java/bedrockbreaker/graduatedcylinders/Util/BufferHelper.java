package bedrockbreaker.graduatedcylinders.Util;

import bedrockbreaker.graduatedcylinders.Proxy.FluidHandlers.IProxyFluidHandler;
import bedrockbreaker.graduatedcylinders.Proxy.FluidStacks.IProxyFluidStack;
import bedrockbreaker.graduatedcylinders.Util.FluidHelper.TransferrableFluidResult;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class BufferHelper {
	
	public static TransferrableFluidResult readTransferrableFluidResult(ByteBuf buffer) {
		return new TransferrableFluidResult(buffer.readInt(), buffer.readInt(), buffer.readBoolean(), buffer.readBoolean());
	}

	public static IProxyFluidStack readFluidStack(ByteBuf buffer, IProxyFluidHandler handlerType) {
		return handlerType.loadFluidStackFromNBT(ByteBufUtils.readTag(buffer));
	}

	public static void writeTransferrableFluidResult(ByteBuf buffer, TransferrableFluidResult transferResult) {
		buffer.writeInt(transferResult.sourceTank);
		buffer.writeInt(transferResult.destinationTank);
		buffer.writeBoolean(transferResult.canExport);
		buffer.writeBoolean(transferResult.canImport);
	}

	public static void writeFluidStack(ByteBuf buffer, IProxyFluidStack fluidStack) {
		ByteBufUtils.writeTag(buffer, fluidStack == null ? null : fluidStack.writeToNBT(new NBTTagCompound()));
	}
}
