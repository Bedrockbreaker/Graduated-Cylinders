package bedrockbreaker.graduatedcylinders;

import bedrockbreaker.graduatedcylinders.api.FluidHandlerRegistryEvent;
import bedrockbreaker.graduatedcylinders.network.PacketHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.common.MinecraftForge;

public class CommonProxy {
	
	public void preInit(FMLPreInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(new FluidHandlerRegistry());
		PacketHandler.register(GraduatedCylinders.MODID);
	}

	public void init(FMLInitializationEvent event) {
		new FluidHandlerRegistryEvent().post();
		MinecraftForge.EVENT_BUS.register(new OnBlockPunch());
	}
}
