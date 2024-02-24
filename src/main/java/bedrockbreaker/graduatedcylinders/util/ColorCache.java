package bedrockbreaker.graduatedcylinders.util;

import java.util.HashMap;

import bedrockbreaker.graduatedcylinders.api.IProxyFluidStack;
import cpw.mods.fml.relauncher.SideOnly;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.client.event.TextureStitchEvent;

@SideOnly(Side.CLIENT)
public class ColorCache {

	public static final HashMap<String, String> fluidColorCodeCache = new HashMap<String, String>();
	
	// All text formatting colors converted to CIELAB, except black and dark blue (for readability purposes)
	private static final float[][] colors = {
		{60.558161922033406f, -63.60622884707961f, 61.388107727989414f}, // DARK_GREEN
		{63.05155612723679f, -35.491527513547226f, -10.428629999069727f}, // DARK_AQUA
		{35.100998867592786f, 59.10786904576126f, 49.40267826885024f}, // DARK_RED
		{40.32869359617732f, 72.49665540287553f, -44.889471368298864f}, // DARK_PURPLE
		{76.07835616569596f, 21.322464302968637f, 79.70035203503518f}, // GOLD
		{69.61016868370909f, -0.0024711132977639494f, 0.0004567899615004478f}, // GRAY
		{36.14585257791481f, -0.0015051752812744734f, 0.00027823449431085834f}, // DARK_GRAY
		{46.50661316152838f, 51.09789918443841f, -84.20021500468766f}, // BLUE
		{88.97105970563061f, -74.23237499065088f, 65.93333354005557f}, // GREEN
		{91.98227066517902f, -42.44944381138615f, -12.74098208286869f}, // AQUA
		{60.265857746225635f, 64.2067510207705f, 36.54832235525697f}, // RED
		{65.73414645544588f, 82.57641840362167f, -52.210401101698054f}, // LIGHT_PURPLE
		{97.4051424700016f, -19.41876989740804f, 77.3104600861761f}, // YELLOW
		{100f, -0.0033483072923723434f, 0.0006189409285983771f} // WHITE (no clue if those decimals are just float imprecision or not)
	};

	public static String getFluidColorCode(IProxyFluidStack fluidStack, int baseColor) {
		String registryName = fluidStack.getUnlocalizedName();
		if (fluidColorCodeCache.containsKey(registryName)) return fluidColorCodeCache.get(registryName);

		// Average the color of the sprite using CIELAB color space
		// See http://www.brucelindbloom.com/index.html?Math.html
		float totalL = 0;
		float totalA = 0;
		float totalB = 0;
		int totalTexels = 0;
		TextureAtlasSprite fluidSprite = fluidStack.getSprite();
		int[][] texels = (fluidSprite == null || fluidSprite.getFrameCount() == 0) ? new int[][]{{0xFFFFFFFF}} : fluidSprite.getFrameTextureData(0);
		for (int[] row : texels) {
			for (int argb : row) {
				if ((argb & 0xFF000000) >>> 24 < 16) continue; // Skip past mostly transparent texels

				float r = ((argb >> 16) & 255) * ((baseColor >> 16) & 255) / 65025f;
				float g = ((argb >> 8) & 255) * ((baseColor >> 8) & 255) / 65025f;
				float b = (argb & 255) * (baseColor & 255) / 65025f;

				// Convert rgb to CIEXYZ first (assume starting from sRGB)
				// 1. Inverse sRGB Compand (yes, "compand")
				r = r <= .04045 ? r / 12.92f : (float) Math.pow((r + .055) / 1.055, 2.4);
				g = g <= .04045 ? g / 12.92f : (float) Math.pow((g + .055) / 1.055, 2.4);
				b = b <= .04045 ? b / 12.92f : (float) Math.pow((b + .055) / 1.055, 2.4);
				// 2. Do some matrix math, only without the actual matrix since I'd immediately have to decompose it after this
				float x = .4124564f * r + .3575761f * g + .1804375f * b;
				float y = .2126729f * r + .7151522f * g + .0721750f * b;
				float z = .0193339f * r + .1191920f * g + .9503041f * b;

				// Convert CIEXYZ to CIELAB
				// 1. Ratio of XYZ to reference D65 white
				float xr = x / .950489f;
				float yr = y; // y / 1f;
				float zr = z / 1.088840f;
				// 2. Funky piecewise function
				float fx = xr > 216 / 24389f ? (float) Math.pow(xr, 1 / 3.0) : xr * 841 / 108f + 4 / 29f;
				float fy = yr > 216 / 24389f ? (float) Math.pow(yr, 1 / 3.0) : yr * 841 / 108f + 4 / 29f;
				float fz = zr > 216 / 24389f ? (float) Math.pow(zr, 1 / 3.0) : zr * 841 / 108f + 4 / 29f;
				// 3. The final showdown
				totalL += 116 * fy - 16;
				totalA += 500 * (fx - fy);
				totalB += 200 * (fy - fz);
				totalTexels++;
			}
		}

		if (totalTexels == 0) {
			fluidColorCodeCache.put(registryName, TextFormatting.WHITE.toString());
			return fluidColorCodeCache.get(registryName);
		}

		float avgL = totalL / (float) totalTexels;
		float avgA = totalA / (float) totalTexels;
		float avgB = totalB / (float) totalTexels;

		float minDistance = Integer.MAX_VALUE;
		int chosenColorIndex = 13; // White
		for (int i = 0; i < colors.length; i++) {
			float[] color = colors[i];
			float distanceSquared = (color[0] - avgL) * (color[0] - avgL) + (color[1] - avgA) * (color[1] - avgA) + (color[2] - avgB) * (color[2] - avgB);
			if (distanceSquared >= minDistance) continue;
			minDistance = distanceSquared;
			chosenColorIndex = i;
		}

		// The index in the array + 2 corresponds to the text formatting code (black: 0 and dark blue: 1 are skipped)
		fluidColorCodeCache.put(registryName, "\u00A7" + Integer.toHexString(chosenColorIndex + 2)); // Range 2-9A-F
		return fluidColorCodeCache.get(registryName);
	}

	@SubscribeEvent
	public void onResourceReload(TextureStitchEvent.Pre event) {
		ColorCache.fluidColorCodeCache.clear();
	}
}
