package bedrockbreaker.graduatedcylinders;

import bedrockbreaker.graduatedcylinders.util.ColorCache;
// import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
// import cpw.mods.fml.common.eventhandler.EventBus;
import net.minecraftforge.common.MinecraftForge;

public class ClientProxy extends CommonProxy {
	
	public static ColorCache colorCache;
	public static InventoryHandler inventoryHandler;
	public static RegisterOverlays registerOverlays;

	@Override
	public void init(FMLInitializationEvent event) {
		super.init(event);
		GraduatedCylinders.console.info("ClientProxy is initializing");
		
		ClientProxy.colorCache = new ColorCache();
		ClientProxy.inventoryHandler = new InventoryHandler();
		ClientProxy.registerOverlays = new RegisterOverlays();

		// eventBus.register(new ColorCache());
		// eventBus.register(new InventoryHandler());
		// eventBus.register(new RegisterOverlays());

		MinecraftForge.EVENT_BUS.register(ClientProxy.colorCache);
		MinecraftForge.EVENT_BUS.register(ClientProxy.inventoryHandler);
		MinecraftForge.EVENT_BUS.register(ClientProxy.registerOverlays);
	}
}