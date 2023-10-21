package bedrockbreaker.graduatedcylinders.api;

import java.util.ArrayList;

import bedrockbreaker.graduatedcylinders.proxy.mode.BucketMode;
import net.minecraft.item.ItemStack;
import net.minecraftforge.registries.IForgeRegistryEntry;

/**
 * To make your fluid handler work with GC, register a singleton instance of a child of this class in the `RegistryEvent.Register<MetaHandler>` event
 * @See {@link bedrockbreaker.graduatedcylinders.FluidHandlerRegistry#registerHandlers} for example
 */
public abstract class MetaHandler extends IForgeRegistryEntry.Impl<MetaHandler> {

	public ArrayList<IHandlerMode> modes = new ArrayList<IHandlerMode>();

	public MetaHandler() {
		this.modes.add(BucketMode.INSTANCE);
	}

	/**
	 * Get whether the item stack has a handler for any fluids
	 */
	public abstract boolean hasHandler(ItemStack itemStack);

	/**
	 * Get the fluid handler for the item stack
	 */
	public abstract IProxyFluidHandlerItem getHandler(ItemStack itemStack);

}