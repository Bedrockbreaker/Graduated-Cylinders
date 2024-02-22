package bedrockbreaker.graduatedcylinders.proxy.mode;

import java.util.ArrayList;

import bedrockbreaker.graduatedcylinders.api.IHandlerMode;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class BucketMode implements IHandlerMode {

	public static final BucketMode INSTANCE = new BucketMode();

	public ItemStack icon = new ItemStack(Items.bucket);

	public int[] getDeltas(int amount, int heldCapacity, int tankCapacity) {
		return new int[] {1, 10, 100, 1000, 10000, 100000, Math.min(heldCapacity, tankCapacity)};
	}

	public ArrayList<String> getStringDeltas() {
		ArrayList<String> keys = new ArrayList<String>(7);
		keys.add(I18n.format("gc.gui.1mb"));
		keys.add(I18n.format("gc.gui.10mb"));
		keys.add(I18n.format("gc.gui.100mb"));
		keys.add(I18n.format("gc.gui.1000mb"));
		keys.add(I18n.format("gc.gui.10000mb"));
		keys.add(I18n.format("gc.gui.100000mb"));
		keys.add(I18n.format("gc.gui.allmb"));
		return keys;
	}

	public String formatAmount(int amountIn, boolean useLongName) {
		return useLongName ? I18n.format("gc.gui.bucket", amountIn / 1000f) : I18n.format("gc.gui.mb", amountIn);
	}

	public ItemStack getModeIcon() {
		return this.icon;
	}

	public String getModeName() {
		return I18n.format("gc.gui.bucketmode");
	}
}
