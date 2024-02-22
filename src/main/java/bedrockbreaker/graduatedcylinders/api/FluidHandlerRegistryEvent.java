package bedrockbreaker.graduatedcylinders.api;

import java.util.ArrayList;

import cpw.mods.fml.common.eventhandler.Event;
import net.minecraftforge.common.MinecraftForge;

public class FluidHandlerRegistryEvent extends Event {
	private boolean canPost = true;
	private final ArrayList<MetaHandler> REGISTRY;

	public FluidHandlerRegistryEvent(ArrayList<MetaHandler> registry) {
		this.REGISTRY = registry;
	}

	public boolean post() {
		if (!this.canPost) return false;
		this.canPost = false;
		return MinecraftForge.EVENT_BUS.post(this);
	}

	public ArrayList<MetaHandler> getRegistry() {
		return this.REGISTRY;
	}
}
