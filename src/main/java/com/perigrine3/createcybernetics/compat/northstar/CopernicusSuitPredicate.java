package com.perigrine3.createcybernetics.compat.northstar;

import com.perigrine3.createcybernetics.common.scheduling.CyberwareProfileManager;
import com.perigrine3.createcybernetics.common.scheduling.CyberwareLoadoutProfile;
import com.perigrine3.createcybernetics.api.CyberwareSlot;
import com.perigrine3.createcybernetics.common.capabilities.ModAttachments;
import com.perigrine3.createcybernetics.common.capabilities.PlayerCyberwareData;
import com.perigrine3.createcybernetics.event.custom.FullBorgHandler;
import com.perigrine3.createcybernetics.item.ModItems;
import net.minecraft.world.entity.player.Player;

public final class CopernicusSuitPredicate {
    private CopernicusSuitPredicate() {}

    public static boolean hasCopernicusSetInstalled(Player player) {
        if (player == null) return false;

        PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
        CyberwareLoadoutProfile profile = CyberwareProfileManager.getProfile(player, data);
        if (profile != null) {
            return profile.fullBorgModel() == CyberwareLoadoutProfile.FullBorgModel.COPERNICUS;
        }
        return FullBorgHandler.isCopernicus(data);
    }
}
