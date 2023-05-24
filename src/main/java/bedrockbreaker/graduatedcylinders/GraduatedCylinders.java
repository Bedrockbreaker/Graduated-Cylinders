package bedrockbreaker.graduatedcylinders;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bedrockbreaker.graduatedcylinders.Packets.PacketHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = GraduatedCylinders.MODID, name = GraduatedCylinders.NAME, version = GraduatedCylinders.VERSION)
public class GraduatedCylinders {
	public static final String MODID = "graduatedcylinders";
	public static final String NAME = "Graduated Cylinders";
	public static final String VERSION = "2.6.4";

	public static Logger console = LogManager.getLogger("Graduated Cylinders");
	public static boolean isMekLoaded = false;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		PacketHandler.register(MODID);
		GraduatedCylinders.isMekLoaded = Loader.isModLoaded("mekanism");
	}
}