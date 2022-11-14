package bedrockbreaker.graduatedcylinders;

import bedrockbreaker.graduatedcylinders.Packets.PacketHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = GraduatedCylinders.MODID, name = GraduatedCylinders.NAME, version = GraduatedCylinders.VERSION)
public class GraduatedCylinders {
	public static final String MODID = "graduatedcylinders";
	public static final String NAME = "Graduated Cylinders";
	public static final String VERSION = "2.2.0";

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		PacketHandler.register(MODID);
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(new Events());
	}
}
