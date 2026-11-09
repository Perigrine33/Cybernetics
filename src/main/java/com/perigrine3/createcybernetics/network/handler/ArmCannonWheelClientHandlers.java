package com.perigrine3.createcybernetics.network.handler;

import com.perigrine3.createcybernetics.network.payload.ArmCannonWheelPayloads;
import com.perigrine3.createcybernetics.screen.custom.arm_cannon.ArmCannonWheelScreen;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ArmCannonWheelClientHandlers {
    private ArmCannonWheelClientHandlers() {}

    public static void handleOpen(ArmCannonWheelPayloads.OpenArmCannonWheelClientPayload msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;

            ArmCannonWheelScreen.open(msg.segments());
            ArmCannonWheelScreen.setPreselectedIndex(msg.selectedIndex());
        });
    }
}
