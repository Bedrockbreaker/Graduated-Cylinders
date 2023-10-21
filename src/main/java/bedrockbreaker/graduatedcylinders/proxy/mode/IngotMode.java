package bedrockbreaker.graduatedcylinders.proxy.mode;

import java.util.ArrayList;

import bedrockbreaker.graduatedcylinders.api.IHandlerMode;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class IngotMode implements IHandlerMode {

	// TODO: render mB as well while in ingot mode?
	public static final IngotMode INSTANCE = new IngotMode();

	public ItemStack icon = new ItemStack(Items.IRON_INGOT);

	public int[] getDeltas(int amount, int heldCapacity, int tankCapacity) {
		int maxCapacity = Math.min(heldCapacity, tankCapacity);
		return new int[] {1, 16, 72, 144, 1296, 9216, maxCapacity - maxCapacity % 144};
	}

	public ArrayList<String> getStringDeltas() {
		ArrayList<String> keys = new ArrayList<String>(7);
		keys.add(I18n.format("gc.gui.1mb"));
		keys.add(I18n.format("gc.gui.16mb"));
		keys.add(I18n.format("gc.gui.72mb"));
		keys.add(I18n.format("gc.gui.144mb"));
		keys.add(I18n.format("gc.gui.1296mb"));
		keys.add(I18n.format("gc.gui.9216mb"));
		keys.add(I18n.format("gc.gui.allmb"));
		return keys;
	}

	// Yes, I'm ignoring useLongName here
	public String formatAmount(int amountIn, boolean useLongName) {
		float ingots = amountIn / 144 + (amountIn % 144 > 0 && amountIn % 72 == 0 ? .5f : 0);
		amountIn -= ingots * 144;
		int nuggets = amountIn / 16;
		amountIn -= nuggets * 16;

		if ((ingots > 0 ? 1 : 0) + (nuggets > 0 ? 1 : 0) + (amountIn > 0 ? 1 : 0) > 1) { // If (need to display multiple units) {use shorthand unit names}
			ArrayList<String> units = new ArrayList<String>(3);
			if (ingots > 0) units.add(I18n.format("gc.gui.ingot.short", String.format("%.1f", ingots).replaceFirst("[.,]0$", "")));
			if (nuggets > 0) units.add(I18n.format("gc.gui.nugget.short", nuggets));
			if (amountIn > 0) units.add(I18n.format("gc.gui.mb", amountIn));
			return String.join(", ", units);
		} else if (ingots > 0) {
			return I18n.format("gc.gui.ingot", String.format("%.1f", ingots).replaceFirst("[.,]0$", ""));
		} else if (nuggets > 0) {
			return I18n.format("gc.gui.nugget", nuggets);
		} else if (amountIn > 0) {
			return I18n.format("gc.gui.mb", amountIn);
		}
		return I18n.format("gc.gui.ingot", "0");
	}

	public ItemStack getModeIcon() {
		return this.icon;
	}

	public String getModeName() {
		return I18n.format("gc.gui.ingotmode");
	}
}
