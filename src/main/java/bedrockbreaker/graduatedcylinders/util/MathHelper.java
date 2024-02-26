package bedrockbreaker.graduatedcylinders.util;

public class MathHelper {

	// TODO: move more stuff here?
	// i.e. color bit shift stuff?
	public static int clamp(int x, int a, int b) {
		return Math.max(Math.min(x, b), a);
	}

	public static float clamp(float x, float a, float b) {
		return Math.max(Math.min(x, b), a);
	}

	public static double clamp(double x, double a, double b) {
		return Math.max(Math.min(x, b), a);
	}

	public static float lerp(float a, float b, float t) {
		return a * (1 - t) + b * t;
	}

	public static float clampedLerp(float a, float b, float t) {
		return MathHelper.lerp(a, b, MathHelper.clamp(t, 0, 1));
	}

	public static float easeInOutQuad(float t) {
		return t < .5 ? 2 * t * t : 1 - (float) Math.pow(-2 * t + 2, 2) / 2f;
	}

	public static float easeInOutQuad(float a, float b, float t) {
		return MathHelper.lerp(a, b, MathHelper.easeInOutQuad(t));
	}

	public static float clampedEaseInOutQuad(float t) {
		return MathHelper.easeInOutQuad(MathHelper.clamp(t, 0, 1));
	}

	public static float clampedEaseInOutQuad(float a, float b, float t) {
		return MathHelper.lerp(a, b, MathHelper.clampedEaseInOutQuad(t));
	}

	public static float easeOutCubic(float t) {
		return 1 - (float) Math.pow(1 - t, 3);
	}

	public static float easeOutCubic(float a, float b, float t) {
		return MathHelper.lerp(a, b, MathHelper.easeOutCubic(t));
	}

	public static float clampedEaseOutCubic(float t) {
		return MathHelper.easeOutCubic(MathHelper.clamp(t, 0, 1));
	}

	public static float clampedEaseOutCubic(float a, float b, float t) {
		return MathHelper.lerp(a, b, MathHelper.clampedEaseOutCubic(t));
	}
}
