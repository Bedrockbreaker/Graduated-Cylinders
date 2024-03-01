package bedrockbreaker.graduatedcylinders.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import bedrockbreaker.graduatedcylinders.network.PacketContainerTransferFluid;
import bedrockbreaker.graduatedcylinders.network.PacketHandler;
import bedrockbreaker.graduatedcylinders.util.FluidHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

@Mixin(GuiContainer.class)
public class MixinMouseClicked {

	@Unique
	boolean clicked = false;

	@Inject(at = @At("HEAD"), method = "Lnet/minecraft/client/gui/GuiScreen;mouseClicked(III)V", cancellable = true)
	private void mouseClicked(int mouseX, int mouseY, int mouseButton, CallbackInfo callbackInfo) {
		if (mouseButton != 1) return;

		ItemStack heldStack = Minecraft.getMinecraft().thePlayer.inventory.getItemStack();
		if (heldStack == null || heldStack.getItem() == null) return;

		Slot hoveredSlot = this.getSlotAtPosition(mouseX, mouseY);
		if (hoveredSlot == null || !hoveredSlot.getHasStack() || hoveredSlot.getStack().getItem() == null || !hoveredSlot.canTakeStack(Minecraft.getMinecraft().thePlayer)) return;
		if (FluidHelper.getTransferAmount(FluidHelper.getProxyFluidHandler(heldStack), FluidHelper.getProxyFluidHandler(hoveredSlot.getStack())) == 0) return;

		this.clicked = true;
		PacketHandler.INSTANCE.sendToServer(new PacketContainerTransferFluid(hoveredSlot.slotNumber));
		callbackInfo.cancel();
	}

	@Inject(at = @At("HEAD"), method = "Lnet/minecraft/client/gui/GuiScreen;mouseMovedOrUp(III)V", cancellable = true)
	private void mouseMovedOrUp(int mouseX, int mouseY, int mouseButton, CallbackInfo callbackInfo) {
		if (mouseButton != 1 || !this.clicked) return;
		this.clicked = false;
		callbackInfo.cancel();
	}

	@Shadow
	private Slot getSlotAtPosition(int x, int y) {
		throw new RuntimeException("Graduated Cylinders was unable to shadow GuiContainer#getSlotAtPosition");
	}
}