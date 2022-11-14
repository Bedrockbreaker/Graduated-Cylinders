package bedrockbreaker.graduatedcylinders.Packets;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PacketHandler {

	public static SimpleNetworkWrapper INSTANCE;

	private static int Id = 0;

	public static void register(String channel) {
		INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(channel);

		// Side indicates the receiving end
		INSTANCE.registerMessage(PacketOpenFluidGUI.Handler.class, PacketOpenFluidGUI.class, Id++, Side.CLIENT);
		INSTANCE.registerMessage(PacketTransferFluid.Handler.class, PacketTransferFluid.class, Id++, Side.SERVER);
		INSTANCE.registerMessage(PacketInventoryOpen.Handler.class, PacketInventoryOpen.class, Id++, Side.SERVER);
		INSTANCE.registerMessage(PacketUpdateMouseSlot.Handler.class, PacketUpdateMouseSlot.class, Id++, Side.CLIENT);
	}
}
