package bedrockbreaker.graduatedcylinders;

import java.util.ArrayList;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bedrockbreaker.graduatedcylinders.api.FluidHandlerRegistryEvent;
import bedrockbreaker.graduatedcylinders.api.GraduatedCylindersAPI;
import bedrockbreaker.graduatedcylinders.api.MetaHandler;
import bedrockbreaker.graduatedcylinders.network.PacketHandler;
import bedrockbreaker.graduatedcylinders.util.ColorCache;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.EventBus;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.MinecraftForge;

@Mod(modid = GraduatedCylinders.MODID, name = GraduatedCylinders.NAME, version = GraduatedCylinders.VERSION)
public class GraduatedCylinders {
	public static final String MODID = "graduatedcylinders";
	public static final String NAME = "Graduated Cylinders";
	public static final String VERSION = "3.0.0";

	public static Logger console = LogManager.getLogger("Graduated Cylinders");
	public static boolean isMekanismLoaded = false;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		if (!(Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment") && GraduatedCylindersAPI.class.getProtectionDomain().getCodeSource().getLocation().toString().toLowerCase(Locale.ROOT).contains("-api.java")) {
			throw new RuntimeException("Graduated Cylinders API jar (\"GraduatedCylinders-" + MinecraftForge.MC_VERSION + "-" + GraduatedCylindersAPI.API_VERSION + "-api.jar\") was detected in your mods folder. Please delete it and restart the game.");
		}

		GraduatedCylinders.isMekanismLoaded = Loader.isModLoaded("mekanism");
		FMLCommonHandler.instance().bus().register(new FluidHandlerRegistry());
		PacketHandler.register(MODID);
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		new FluidHandlerRegistryEvent(new ArrayList<MetaHandler>()).post();
		EventBus eventBus = FMLCommonHandler.instance().bus();
		eventBus.register(new OnBlockPunch());
		if (event.getSide() == Side.CLIENT) {
			eventBus.register(new ColorCache());
			eventBus.register(new InventoryHandler());
			eventBus.register(new RegisterOverlays());
		}
	}

	// BUG: convert all instances of EnumFacing to ForgeDirection. Notice that the east/west fields (indices 4,5) are swapped between the two.
}