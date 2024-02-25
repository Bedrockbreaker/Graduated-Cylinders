package bedrockbreaker.graduatedcylinders.api;

import java.util.ArrayList;

import bedrockbreaker.graduatedcylinders.FluidHandlerRegistry;
import bedrockbreaker.graduatedcylinders.GraduatedCylinders;
import cpw.mods.fml.common.eventhandler.Event;
import net.minecraftforge.common.MinecraftForge;

public class FluidHandlerRegistryEvent extends Event {
	private boolean canPost = true;

	public boolean post() {
		GraduatedCylinders.console.info("Attempting to fire FluidHandlerRegistryEvent");
		if (!this.canPost) return false;
		this.canPost = false;
		GraduatedCylinders.console.info("Actually firing FluidHandlerRegistryEvent");
		return MinecraftForge.EVENT_BUS.post(this);
	}

	public ArrayList<MetaHandler> getRegistry() {
		return FluidHandlerRegistry.REGISTRY;
	}
}