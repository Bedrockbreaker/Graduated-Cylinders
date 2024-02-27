package bedrockbreaker.graduatedcylinders;

import bedrockbreaker.graduatedcylinders.util.ColorCache;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.common.MinecraftForge;

public class ClientProxy extends CommonProxy {

	@Override
	public void init(FMLInitializationEvent event) {
		super.init(event);

		MinecraftForge.EVENT_BUS.register(new ColorCache());
		MinecraftForge.EVENT_BUS.register(new RegisterOverlays());
		InventoryHandler inventoryHandler = new InventoryHandler();
		MinecraftForge.EVENT_BUS.register(inventoryHandler);
		FMLCommonHandler.instance().bus().register(inventoryHandler);
	}
}