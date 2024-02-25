package bedrockbreaker.graduatedcylinders;

import bedrockbreaker.graduatedcylinders.api.FluidHandlerRegistryEvent;
import bedrockbreaker.graduatedcylinders.network.PacketHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.common.MinecraftForge;

public class CommonProxy {

	public static OnBlockPunch onBlockPunch;
	public static FluidHandlerRegistry fluidHandlerRegistry;
	
	public void preInit(FMLPreInitializationEvent event) {
		GraduatedCylinders.console.info("CommonProxy is preinitializing");
		CommonProxy.fluidHandlerRegistry = new FluidHandlerRegistry();
		MinecraftForge.EVENT_BUS.register(CommonProxy.fluidHandlerRegistry);
		PacketHandler.register(GraduatedCylinders.MODID);
	}

	public void init(FMLInitializationEvent event) {
		GraduatedCylinders.console.info("CommonProxy is initializing");
		new FluidHandlerRegistryEvent().post();
		CommonProxy.onBlockPunch = new OnBlockPunch();

		// FMLCommonHandler.instance().bus().register(CommonProxy.onBlockPunch);
		MinecraftForge.EVENT_BUS.register(CommonProxy.onBlockPunch);
	}
}
