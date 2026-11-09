package com.perigrine3.createcybernetics.client;

import com.perigrine3.createcybernetics.screen.custom.arm_cannon.ArmCannonWheelScreen;
import com.perigrine3.createcybernetics.screen.custom.cyberdeck.CyberdeckQuickhackWheelScreen;
import com.perigrine3.createcybernetics.screen.custom.cyberdeck.MacroprogramWheelScreen;
import com.perigrine3.createcybernetics.screen.custom.toggle_wheel.CyberwareToggleWheelScreen;
import net.minecraft.client.Minecraft;

public final class RadialMenuController {
    public enum Wheel { QUICKHACKS, IMPLANTS, ARM_CANNON, MACROPROGRAMS }

    private RadialMenuController() {}

    public static void prepareToOpen(Wheel wheel) {
        if (wheel != Wheel.QUICKHACKS) CyberdeckQuickhackWheelScreen.close();
        if (wheel != Wheel.IMPLANTS) CyberwareToggleWheelScreen.closeWheel();
        if (wheel != Wheel.ARM_CANNON) ArmCannonWheelScreen.close();
        if (wheel != Wheel.MACROPROGRAMS) MacroprogramWheelScreen.closeWheel();

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.containerMenu != mc.player.inventoryMenu) {
            // Notify the server as well as dismissing the container's screen.
            mc.player.closeContainer();
        } else if (mc.screen != null) {
            mc.setScreen(null);
        }
    }
}
