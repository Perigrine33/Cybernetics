package com.perigrine3.createcybernetics.common.scheduling;

import com.perigrine3.createcybernetics.api.CyberwareSlot;
import com.perigrine3.createcybernetics.api.ICyberwareItem;
import com.perigrine3.createcybernetics.api.InstalledCyberware;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record CyberwareLoadoutProfile(
        int loadoutVersion,
        int chipwareVersion,
        List<InstalledSlotRef> tickableSlots,
        List<InstalledSlotRef> energyNodes,
        List<InstalledCyberware> batterySlots,
        int totalEnergyCapacity,
        boolean allPrimaryOrgansFunctional,
        boolean hasGills,
        FullBorgModel fullBorgModel,
        boolean sculkedBody,
        long capabilityFlags,
        int nonOrganCyberwareCount,
        List<ItemStack> chipwareSnapshot
) {

    public enum FullBorgModel {
        NONE,
        GEMINI,
        SAMSON,
        ECLIPSE,
        SPYDER,
        WINGMAN,
        AQUARIUS,
        DYMOND,
        DRAGOON,
        COPERNICUS,
        GENOS,
        KILDARE,
        HEXBORG
    }

    public enum CapabilityFlag {
        MANTIS_BLADES,
        RETRACTABLE_CLAWS,
        CRAFTING_HANDS,
        DRILL_FIST,
        ELECTRIC_ARC_CANNON,
        CYBERARM,
        CYBERLEG,
        PNEUMATIC_CALVES,
        OXYGEN_TANK,
        SYNTH_LUNGS,
        SPINAL_INJECTOR,
        DEPLOYABLE_ELYTRA,
        OREGRINDER,
        POLAR_BEAR_FUR,
        BLUBBER,
        EYE_OF_DEFENDER,
        CEREBRAL_PROCESSING_UNIT,
        CHIPWARE_SLOTS,
        RAMDECK,
        INTERCHANGEABLE_FACEPLATE,
        CYBERBRAIN,
        CYBERDECK,
        ARM_CANNON,
        CHROMATOPHORES,
        SCULKED_IMPLANT,
        NON_DEFAULT_CYBERWARE,
        PASSIVE_DURABILITY_TRACKED,
        WIRED_REFLEXES;

        private final long mask;

        CapabilityFlag() {
            this.mask = 1L << ordinal();
        }

        public long mask() {
            return mask;
        }
    }

    public record InstalledSlotRef(
            CyberwareSlot slot,
            int index,
            InstalledCyberware cyberware,
            ItemStack stack,
            ICyberwareItem item
    ) {}

    public boolean hasCapability(CapabilityFlag flag) {
        return flag != null && (capabilityFlags & flag.mask()) != 0L;
    }

    public boolean hasEnergyWork() {
        return !energyNodes.isEmpty() || totalEnergyCapacity > 0;
    }
}
