package bedrockbreaker.graduatedcylinders.api;

import java.util.ArrayList;

import bedrockbreaker.graduatedcylinders.FluidHandlerRegistry;
import cpw.mods.fml.common.eventhandler.Event;
import net.minecraftforge.common.MinecraftForge;

public class FluidHandlerRegistryEvent extends Event {
	private boolean canPost = true;

	public boolean post() {
		if (!this.canPost) return false;
		this.canPost = false;
		return MinecraftForge.EVENT_BUS.post(this);
	}

	public ArrayList<MetaHandler> getRegistry() {
		return FluidHandlerRegistry.REGISTRY;
	}
}