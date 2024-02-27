package bedrockbreaker.graduatedcylinders;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;

@SideOnly(Side.CLIENT)
public class RegisterOverlays {

	public static IIcon hoveredFaceSprite;
	public static IIcon selectedFaceSprite;
	public static IIcon blockedFaceSprite;

	@SubscribeEvent
	public void registerSprites(TextureStitchEvent.Pre event) {
		if (event.map != Minecraft.getMinecraft().getTextureMapBlocks()) return;
		hoveredFaceSprite = event.map.registerIcon(new ResourceLocation(GraduatedCylinders.MODID, "overlay/hovered_face").toString());
		selectedFaceSprite = event.map.registerIcon(new ResourceLocation(GraduatedCylinders.MODID, "overlay/selected_face").toString());
		blockedFaceSprite = event.map.registerIcon(new ResourceLocation(GraduatedCylinders.MODID, "overlay/blocked_face").toString());
	}
}
