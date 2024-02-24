package bedrockbreaker.graduatedcylinders;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.client.event.TextureStitchEvent;

@SideOnly(Side.CLIENT)
public class RegisterOverlays {

	public static TextureAtlasSprite hoveredFaceSprite;
	public static TextureAtlasSprite selectedFaceSprite;
	public static TextureAtlasSprite blockedFaceSprite;

	@SubscribeEvent
	public void registerSprites(TextureStitchEvent.Pre event) {
		RegisterOverlays.hoveredFaceSprite = event.map.getAtlasSprite(GraduatedCylinders.MODID + "overlay/hovered_face");
		RegisterOverlays.selectedFaceSprite = event.map.getAtlasSprite(GraduatedCylinders.MODID + "overlay/selected_face");
		RegisterOverlays.blockedFaceSprite = event.map.getAtlasSprite(GraduatedCylinders.MODID + "overlay/blocked_face");
	}
}
