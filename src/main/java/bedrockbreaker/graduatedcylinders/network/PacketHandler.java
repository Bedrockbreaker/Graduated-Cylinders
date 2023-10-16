package bedrockbreaker.graduatedcylinders.network;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PacketHandler {

	public static SimpleNetworkWrapper INSTANCE;

	private static int id = 0;

	public static void register(String channel) {
		INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(channel);

		// Side indicates the receiving end
		INSTANCE.registerMessage(PacketOpenFluidGUI.Handler.class, PacketOpenFluidGUI.class, id++, Side.CLIENT);
		INSTANCE.registerMessage(PacketBlockTransferFluid.Handler.class, PacketBlockTransferFluid.class, id++, Side.SERVER);
		INSTANCE.registerMessage(PacketContainerTransferFluid.Handler.class, PacketContainerTransferFluid.class, id++, Side.SERVER);
	}
}
