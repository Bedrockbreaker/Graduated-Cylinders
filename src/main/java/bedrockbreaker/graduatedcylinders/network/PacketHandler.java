package bedrockbreaker.graduatedcylinders.network;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

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
