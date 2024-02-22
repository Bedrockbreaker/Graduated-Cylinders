package bedrockbreaker.graduatedcylinders.api;

import java.util.ArrayList;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.item.ItemStack;

public interface IHandlerMode {

	/**
	 * Get the deltas for changing the fluid amount in the GUI
	 */
	@SideOnly(Side.CLIENT)
	public int[] getDeltas(int amount, int heldCapacity, int tankCapacity);

	/**
	 * Get the deltas displayed in the bottom-right of the GUI
	 */
	@SideOnly(Side.CLIENT)
	public ArrayList<String> getStringDeltas();

	/**
	 * Format the fluidstack amount for display.
	 * E.g. "1000 mb", "2 Ingot(s)", or "9 I, 2 N, 13 mB"
	 * @param useLongName Indicate whether the appended units could expand to their full name
	 */
	@SideOnly(Side.CLIENT)
	public String formatAmount(int amountIn, boolean useLongName);

	/**
	 * Get the item stack representing this mode
	 */
	@SideOnly(Side.CLIENT)
	public ItemStack getModeIcon();

	/**
	 * Get the mode name when hovering over the mode cycle button in the GUI
	 */
	@SideOnly(Side.CLIENT)
	public String getModeName();
}