package bedrockbreaker.graduatedcylinders.Util;

import bedrockbreaker.graduatedcylinders.Proxy.FluidStacks.IProxyFluidStack;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiFluidSprite extends Gui {

	public IProxyFluidStack fluidStack;
	public TextureAtlasSprite sprite;
	public int tankIndex;
	public int renderIndex;
	
	public float x = 0;
	public float y = 0;
	public float width = 0;
	public float height = 0;
	public int color = 0xFFFFFFFF; // ARGB

	public float transitionLength = 10; // ticks
	public float elapsedTime = 10; // ticks

	public float startX = 0;
	public float startY = 0;
	public float startWidth = 0;
	public float startHeight = 0;
	public int startOpacity = 255; // 0-255

	public float endX = 0;
	public float endY = 0;
	public float endWidth = 0;
	public float endHeight = 0;
	public int endOpacity = 255; // 0-255

	public GuiFluidSprite(IProxyFluidStack fluidStack, float x, float y, float width, float height, int tankIndex, int renderIndex) {
		this.sprite = fluidStack.getSprite();
		this.fluidStack = fluidStack;
		this.tankIndex = tankIndex;
		this.renderIndex = renderIndex;

		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.color = fluidStack.getColor();

		this.startX = x;
		this.startY = y;
		this.startWidth = width;
		this.startHeight = height;
		this.startOpacity = color;

		this.endX = x;
		this.endY = y;
		this.endWidth = width;
		this.endHeight = height;
		this.endOpacity = color;
	}

	public void render(float partialTicks) {
		this.elapsedTime += partialTicks;
		float t = this.elapsedTime / this.transitionLength;
		
		// Interpolate properties. The conditional operators help alleviate some of the jittering resulting from casting float -> int
		this.x = this.x == this.endX ? this.x : MathHelper.clampedEaseInOutQuad(this.startX, this.endX, t);
		this.y = this.y == this.endY ? this.y : MathHelper.clampedEaseInOutQuad(this.startY, this.endY, t);
		this.width = this.width == this.endWidth ? this.width : MathHelper.clampedEaseInOutQuad(this.startWidth, this.endWidth, t);
		this.height = this.height == this.endHeight ? this.height : MathHelper.clampedEaseInOutQuad(this.startHeight, this.endHeight, t);
		this.color = this.color >>> 24 == this.endOpacity ? this.color : (this.color & 0xFFFFFF) | (((int) MathHelper.clampedEaseOutCubic(this.startOpacity, this.endOpacity, t)) << 24); // Only transition opacity


		if (this.color >>> 24 == 0) return;
		GlStateManager.color((this.color >>> 16 & 255) / 255f, (this.color >>> 8 & 255) / 255f, (this.color & 255) / 255f, (this.color >>> 24) / 255f);
		this.drawTexturedModalRect((int) this.x, (int) this.y, this.sprite, (int) this.width, (int) this.height);
		GlStateManager.color(1, 1, 1, 1);
	}

	// TODO: allow chaining?

	public void setX(float x) {
		this.x = x;
		this.startX = x;
		this.endX = x;
	}

	public void setY(float y) {
		this.y = y;
		this.startY = y;
		this.endY = y;
	}

	public void setPos(float x, float y) {
		this.setX(x);
		this.setY(y);
	}

	public void animateMove(float x, float y) {
		// animate from current interpolated position
		this.startX = this.x;
		this.startY = this.y;
		this.endX = x;
		this.endY = y;
		this.elapsedTime = 0;
	}

	public void setWidth(float width) {
		this.width = width;
		this.endWidth = width;
	}

	public void setHeight(float height) {
		this.height = height;
		this.endHeight = height;
	}

	public void setDimensions(float width, float height) {
		this.setWidth(width);
		this.setHeight(height);
	}

	public void animateScale(float width, float height) {
		// animate from current interpolated dimensions
		this.startWidth = this.width;
		this.endWidth = width;
		this.startHeight = this.height;
		this.endHeight = height;
		this.elapsedTime = 0;
	}

	public void setOpacity(int opacity) {
		this.color = (this.color & 0xFFFFFF) | ((opacity & 255) << 24);
		this.endOpacity = opacity;
	}

	public void animateOpacity(int opacity) {
		// animate from current interpolated opacity
		this.startOpacity = this.color >>> 24;
		this.endOpacity = opacity;
		this.elapsedTime = 0;
	}

	public boolean isMouseOver(int mouseX, int mouseY) {
		return mouseX >= this.x && mouseX < this.x + this.width && mouseY >= this.y && mouseY < this.y + this.height;
	}
}
