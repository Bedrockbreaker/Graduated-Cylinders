package bedrockbreaker.graduatedcylinders;

import bedrockbreaker.graduatedcylinders.api.MetaHandler;
import bedrockbreaker.graduatedcylinders.proxy.meta.MetaEssentiaHandler;
import bedrockbreaker.graduatedcylinders.proxy.meta.MetaFluidHandler;
import bedrockbreaker.graduatedcylinders.proxy.meta.MetaGasHandler;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.RegistryEvent.MissingMappings.Mapping;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

@EventBusSubscriber(modid = GraduatedCylinders.MODID)
public class FluidHandlerRegistry {

	public static IForgeRegistry<MetaHandler> registry = null;

	@SubscribeEvent
	public static void makeRegistry(RegistryEvent.NewRegistry event) {
		RegistryBuilder<MetaHandler> registryBuilder = new RegistryBuilder<>();
		registryBuilder.setName(new ResourceLocation(GraduatedCylinders.MODID, "fluidhandlers"));
		registryBuilder.setType(MetaHandler.class);
		registry = registryBuilder.create();
	}

	@SubscribeEvent
	public static void registerHandlers(RegistryEvent.Register<MetaHandler> event) {
		event.getRegistry().register(new MetaFluidHandler().setRegistryName(GraduatedCylinders.MODID, "fluid"));
		if (GraduatedCylinders.isMekanismLoaded) event.getRegistry().register(new MetaGasHandler().setRegistryName(GraduatedCylinders.MODID, "gas"));
		if (GraduatedCylinders.isThaumcraftLoaded) event.getRegistry().register(new MetaEssentiaHandler().setRegistryName(GraduatedCylinders.MODID, "essentia"));
	}

	@SubscribeEvent
	public static void onMissingHandlers(RegistryEvent.MissingMappings<MetaHandler> event) {
		event.getMappings().stream().forEach(Mapping::ignore);
	}
}