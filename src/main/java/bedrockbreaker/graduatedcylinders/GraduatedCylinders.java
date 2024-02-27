package bedrockbreaker.graduatedcylinders;

import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bedrockbreaker.graduatedcylinders.api.GraduatedCylindersAPI;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.MinecraftForge;

@Mod(modid = GraduatedCylinders.MODID, name = GraduatedCylinders.NAME, version = GraduatedCylinders.VERSION)
public class GraduatedCylinders {
	public static final String MODID = "graduatedcylinders";
	public static final String NAME = "Graduated Cylinders";
	public static final String VERSION = "3.0.0";
	public static final boolean IN_DEV = (boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");

	public static Logger console = LogManager.getLogger(GraduatedCylinders.NAME);
	public static boolean isMekanismLoaded = false;

	@SidedProxy(serverSide = "bedrockbreaker.graduatedcylinders.CommonProxy", clientSide = "bedrockbreaker.graduatedcylinders.ClientProxy")
	public static CommonProxy proxy;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		if (!GraduatedCylinders.IN_DEV && GraduatedCylindersAPI.class.getProtectionDomain().getCodeSource().getLocation().toString().toLowerCase(Locale.ROOT).contains("-api.java")) {
			throw new RuntimeException("Graduated Cylinders API jar (\"GraduatedCylinders-" + MinecraftForge.MC_VERSION + "-" + GraduatedCylindersAPI.API_VERSION + "-api.jar\") was detected in your mods folder. Please delete it and restart the game.");
		}

		GraduatedCylinders.isMekanismLoaded = Loader.isModLoaded("mekanism");

		GraduatedCylinders.proxy.preInit(event);
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		GraduatedCylinders.proxy.init(event);
	}

	// BUG: convert all instances of EnumFacing to ForgeDirection. Notice that the east/west fields (indices 4,5) are swapped between the two.
}