package bedrockbreaker.graduatedcylinders;

import java.util.ArrayList;

import bedrockbreaker.graduatedcylinders.api.FluidHandlerRegistryEvent;
import bedrockbreaker.graduatedcylinders.api.MetaHandler;
import bedrockbreaker.graduatedcylinders.proxy.meta.MetaFluidHandler;
// import bedrockbreaker.graduatedcylinders.proxy.meta.MetaGasHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class FluidHandlerRegistry {

	public static final ArrayList<MetaHandler> REGISTRY = new ArrayList<MetaHandler>();

	@SubscribeEvent
	public void registerHandlers(FluidHandlerRegistryEvent event) {
		event.getRegistry().add(new MetaFluidHandler());
		// FIXME: mekanism 1.7.10 gas api is broken
		// if (GraduatedCylinders.isMekanismLoaded) event.getRegistry().add(new MetaGasHandler());
	}
}