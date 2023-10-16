package bedrockbreaker.graduatedcylinders;

import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bedrockbreaker.graduatedcylinders.api.GraduatedCylindersAPI;
import bedrockbreaker.graduatedcylinders.network.PacketHandler;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = GraduatedCylinders.MODID, name = GraduatedCylinders.NAME, version = GraduatedCylinders.VERSION)
public class GraduatedCylinders {
	public static final String MODID = "graduatedcylinders";
	public static final String NAME = "Graduated Cylinders";
	public static final String VERSION = "2.9.0";

	public static Logger console = LogManager.getLogger("Graduated Cylinders");
	public static boolean isMekanismLoaded = false;
	public static boolean isThaumcraftLoaded = false;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		if (!(Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment") && GraduatedCylindersAPI.class.getProtectionDomain().getCodeSource().getLocation().toString().toLowerCase(Locale.ROOT).contains("-api.java")) {
			throw new RuntimeException("Graduated Cylinders API jar (\"GraduatedCylinders-" + ForgeVersion.mcVersion  + "-" + GraduatedCylindersAPI.API_VERSION + "-api.jar\") was detected in your mods folder. Please delete it and restart the game.");
		}

		PacketHandler.register(MODID);
		GraduatedCylinders.isMekanismLoaded = Loader.isModLoaded("mekanism");
		GraduatedCylinders.isMekanismLoaded = Loader.isModLoaded("thaumcraft");
	}
}