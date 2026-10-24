package com.perigrine3.createcybernetics.common.scheduling;

import com.perigrine3.createcybernetics.CreateCybernetics;
import com.perigrine3.createcybernetics.api.CyberwareSlot;
import com.perigrine3.createcybernetics.api.ICyberwareItem;
import com.perigrine3.createcybernetics.api.InstalledCyberware;
import com.perigrine3.createcybernetics.common.capabilities.EntityCyberwareData;
import com.perigrine3.createcybernetics.common.capabilities.ModAttachments;
import com.perigrine3.createcybernetics.common.capabilities.PlayerCyberwareData;
import com.perigrine3.createcybernetics.common.durability.CyberwareDurabilityManager;
import com.perigrine3.createcybernetics.common.scheduling.CyberwareLoadoutProfile.CapabilityFlag;
import com.perigrine3.createcybernetics.common.scheduling.CyberwareLoadoutProfile.FullBorgModel;
import com.perigrine3.createcybernetics.common.scheduling.CyberwareLoadoutProfile.InstalledSlotRef;
import com.perigrine3.createcybernetics.common.surgery.DefaultOrgans;
import com.perigrine3.createcybernetics.util.SculkedConversionHelper;
import com.perigrine3.createcybernetics.event.custom.CyberwareChipwareShardChangeEvent;
import com.perigrine3.createcybernetics.event.custom.CyberwarePlayerMovementStateEvent;
import com.perigrine3.createcybernetics.event.custom.CyberwarePlayerMovementStateEvent.MovementSnapshot;
import com.perigrine3.createcybernetics.event.custom.CyberwareStateChangeEvent;
import com.perigrine3.createcybernetics.event.custom.CyberwareStateChangeEvent.ChangeReason;
import com.perigrine3.createcybernetics.event.custom.FullBorgHandler;
import com.perigrine3.createcybernetics.event.custom.SculkedCyberwareTickEvents;
import com.perigrine3.createcybernetics.item.ModItems;
import com.perigrine3.createcybernetics.util.ModTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = CreateCybernetics.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class CyberwareProfileManager {

    private CyberwareProfileManager() {}

    public static CyberwareLoadoutProfile getProfile(Player player) {
        if (player == null || !player.hasData(ModAttachments.CYBERWARE)) {
            return null;
        }
        PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
        return getProfile(player, data);
    }

    public static CyberwareLoadoutProfile getProfile(Player player, PlayerCyberwareData data) {
        if (data == null) {
            return null;
        }
        CyberwareLoadoutProfile cached = data.getCachedProfile();
        int currentLoadoutVersion = data.getLoadoutVersion();
        int currentChipwareVersion = data.getChipwareVersion();
        if (cached != null
                && cached.loadoutVersion() == currentLoadoutVersion
                && cached.chipwareVersion() == currentChipwareVersion) {
            return cached;
        }
        CyberwareLoadoutProfile rebuilt = buildProfile(player, data, currentLoadoutVersion, currentChipwareVersion);
        data.setCachedProfile(rebuilt);
        return rebuilt;
    }

    public static void synchronizeAndPublish(Player player, PlayerCyberwareData data, ChangeReason reason) {
        if (player == null || data == null || player.level().isClientSide) {
            return;
        }
        CyberwareLoadoutProfile previous = data.getLastDispatchedProfile();
        int currentLoadoutVersion = data.getLoadoutVersion();
        int currentChipwareVersion = data.getChipwareVersion();

        boolean loadoutChanged = previous == null || previous.loadoutVersion() != currentLoadoutVersion;
        boolean chipwareChanged = previous == null || previous.chipwareVersion() != currentChipwareVersion;

        if (!loadoutChanged && !chipwareChanged && reason != ChangeReason.LIFECYCLE_INITIALIZED) {
            return;
        }

        CyberwareLoadoutProfile current = getProfile(player, data);
        data.setLastDispatchedProfile(current);

        if (chipwareChanged) {
            List<ItemStack> prevShards = previous != null ? previous.chipwareSnapshot() : List.of(ItemStack.EMPTY, ItemStack.EMPTY);
            NeoForge.EVENT_BUS.post(new CyberwareChipwareShardChangeEvent(player, data, prevShards, current.chipwareSnapshot()));
        }

        if (loadoutChanged || chipwareChanged || reason == ChangeReason.LIFECYCLE_INITIALIZED) {
            NeoForge.EVENT_BUS.post(new CyberwareStateChangeEvent(player, data, previous, current, reason));
        }
    }

    public static List<InstalledSlotRef> getEntityTickableSlots(EntityCyberwareData data) {
        if (data == null) {
            return List.of();
        }
        if (data.getCachedTickableVersion() == data.getLoadoutVersion()) {
            return data.getCachedTickableSlots();
        }
        rebuildEntityCache(data);
        return data.getCachedTickableSlots();
    }

    public static boolean hasEntityEyeOfDefender(EntityCyberwareData data) {
        if (data == null) {
            return false;
        }
        if (data.getCachedTickableVersion() != data.getLoadoutVersion()) {
            rebuildEntityCache(data);
        }
        return data.hasCachedEyeOfDefender();
    }

    private static void rebuildEntityCache(EntityCyberwareData data) {
        List<InstalledSlotRef> tickable = new ArrayList<>(4);
        boolean hasDefender = false;

        for (Map.Entry<CyberwareSlot, InstalledCyberware[]> entry : data.getAll().entrySet()) {
            CyberwareSlot slot = entry.getKey();
            InstalledCyberware[] arr = entry.getValue();
            if (arr == null) continue;

            for (int i = 0; i < arr.length; i++) {
                InstalledCyberware installed = arr[i];
                if (installed == null) continue;

                ItemStack stack = installed.getItem();
                if (stack == null || stack.isEmpty()) continue;

                if (stack.getItem() instanceof com.perigrine3.createcybernetics.item.cyberware.brain.EyeOfDefenderItem) {
                    hasDefender = true;
                }

                if (stack.getItem() instanceof ICyberwareItem cyberwareItem && TickableCyberwareClassifier.isTickable(cyberwareItem)) {
                    tickable.add(new InstalledSlotRef(slot, i, installed, stack, cyberwareItem));
                }
            }
        }

        data.updateEntityCache(data.getLoadoutVersion(), tickable.isEmpty() ? List.of() : List.copyOf(tickable), hasDefender);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerTickPre(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        if (!player.hasData(ModAttachments.CYBERWARE)) return;

        PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
        if (data == null) return;

        CyberwareLoadoutProfile lastDispatched = data.getLastDispatchedProfile();
        if (lastDispatched == null
                || lastDispatched.loadoutVersion() != data.getLoadoutVersion()
                || lastDispatched.chipwareVersion() != data.getChipwareVersion()) {
            synchronizeAndPublish(player, data, lastDispatched == null ? ChangeReason.LIFECYCLE_INITIALIZED : ChangeReason.LOADOUT_MUTATED);
        }

        MovementSnapshot currentMovement = MovementSnapshot.capture(player);
        int currentMask = currentMovement.toBitmask();
        int previousMask = data.getLastMovementBitmask();

        if (previousMask != currentMask) {
            MovementSnapshot previousMovement = data.getLastMovementSnapshot();
            data.setLastMovementState(currentMask, currentMovement);
            NeoForge.EVENT_BUS.post(new CyberwarePlayerMovementStateEvent(
                    player,
                    data,
                    previousMovement != null ? previousMovement : currentMovement,
                    currentMovement
            ));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        if (!player.hasData(ModAttachments.CYBERWARE)) return;

        PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
        if (data != null) {
            data.incrementLoadoutVersion();
            synchronizeAndPublish(player, data, ChangeReason.LIFECYCLE_INITIALIZED);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        if (!player.hasData(ModAttachments.CYBERWARE)) return;

        PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
        if (data != null) {
            data.incrementLoadoutVersion();
            synchronizeAndPublish(player, data, ChangeReason.LIFECYCLE_INITIALIZED);
        }
    }

    private static CyberwareLoadoutProfile buildProfile(
            Player player,
            PlayerCyberwareData data,
            int loadoutVersion,
            int chipwareVersion
    ) {
        List<InstalledSlotRef> tickable = new ArrayList<>(8);
        List<InstalledSlotRef> energyNodes = new ArrayList<>(8);
        List<InstalledCyberware> batteries = new ArrayList<>(4);
        int totalEnergyCapacity = 0;
        long flags = 0L;
        int nonOrganCount = 0;

        Item faceplate = ModItems.SKINUPGRADES_FACEPLATE.get();
        Item scavengedFaceplate = ModItems.SCAVENGED_FACEPLATE.get();
        Item cyberbrain = ModItems.BRAINUPGRADES_CYBERBRAIN.get();

        for (Map.Entry<CyberwareSlot, InstalledCyberware[]> entry : data.getAll().entrySet()) {
            CyberwareSlot slot = entry.getKey();
            InstalledCyberware[] arr = entry.getValue();
            if (arr == null) continue;

            for (int i = 0; i < arr.length; i++) {
                InstalledCyberware cw = arr[i];
                if (cw == null) continue;

                ItemStack stack = cw.getItem();
                if (stack == null || stack.isEmpty()) continue;

                Item rawItem = stack.getItem();
                boolean isDefault = DefaultOrgans.isOrganForSlot(stack, slot);
                if (!isDefault) {
                    flags |= CapabilityFlag.NON_DEFAULT_CYBERWARE.mask();
                    if (slot != CyberwareSlot.ORGANS && slot != CyberwareSlot.BRAIN) {
                        nonOrganCount++;
                    }
                }

                if (CyberwareDurabilityManager.usesDurability(cw)) {
                    flags |= CapabilityFlag.PASSIVE_DURABILITY_TRACKED.mask();
                }

                if (CyberwareDurabilityManager.isBattery(cw)) {
                    batteries.add(cw);
                }

                if (rawItem instanceof com.perigrine3.createcybernetics.item.cyberware.arm.MantisBladeItem) {
                    flags |= CapabilityFlag.MANTIS_BLADES.mask();
                } else if (rawItem instanceof com.perigrine3.createcybernetics.item.cyberware.arm.RetractableClawsItem) {
                    flags |= CapabilityFlag.RETRACTABLE_CLAWS.mask();
                } else if (rawItem instanceof com.perigrine3.createcybernetics.item.cyberware.arm.CraftingHandsItem) {
                    flags |= CapabilityFlag.CRAFTING_HANDS.mask();
                } else if (rawItem instanceof com.perigrine3.createcybernetics.item.cyberware.arm.DrillFistItem) {
                    flags |= CapabilityFlag.DRILL_FIST.mask();
                } else if (rawItem instanceof com.perigrine3.createcybernetics.item.cyberware.arm.ElectricArcCannonItem) {
                    flags |= CapabilityFlag.ELECTRIC_ARC_CANNON.mask();
                } else if (rawItem instanceof com.perigrine3.createcybernetics.item.cyberware.arm.CyberarmItem) {
                    flags |= CapabilityFlag.CYBERARM.mask();
                } else if (rawItem instanceof com.perigrine3.createcybernetics.item.cyberware.leg.CyberlegItem) {
                    flags |= CapabilityFlag.CYBERLEG.mask();
                } else if (rawItem instanceof com.perigrine3.createcybernetics.item.cyberware.leg.PneumaticCalvesItem) {
                    flags |= CapabilityFlag.PNEUMATIC_CALVES.mask();
                } else if (rawItem instanceof com.perigrine3.createcybernetics.item.cyberware.lungs.OxygenTankItem) {
                    flags |= CapabilityFlag.OXYGEN_TANK.mask();
                } else if (rawItem instanceof com.perigrine3.createcybernetics.item.cyberware.lungs.SynthLungsItem) {
                    flags |= CapabilityFlag.SYNTH_LUNGS.mask();
                } else if (rawItem instanceof com.perigrine3.createcybernetics.item.cyberware.bone.SpinalInjectorItem) {
                    flags |= CapabilityFlag.SPINAL_INJECTOR.mask();
                } else if (rawItem instanceof com.perigrine3.createcybernetics.item.cyberware.bone.DeployableElytraItem) {
                    flags |= CapabilityFlag.DEPLOYABLE_ELYTRA.mask();
                } else if (rawItem instanceof com.perigrine3.createcybernetics.item.cyberware.organs.OregrinderItem) {
                    flags |= CapabilityFlag.OREGRINDER.mask();
                } else if (rawItem instanceof com.perigrine3.createcybernetics.item.cyberware.wetware.PolarBearFurItem) {
                    flags |= CapabilityFlag.POLAR_BEAR_FUR.mask();
                } else if (rawItem instanceof com.perigrine3.createcybernetics.item.cyberware.wetware.BlubberItem) {
                    flags |= CapabilityFlag.BLUBBER.mask();
                } else if (rawItem instanceof com.perigrine3.createcybernetics.item.cyberware.brain.EyeOfDefenderItem) {
                    flags |= CapabilityFlag.EYE_OF_DEFENDER.mask();
                } else if (rawItem instanceof com.perigrine3.createcybernetics.item.cyberware.brain.CerebralProcessingUnitItem) {
                    flags |= CapabilityFlag.CEREBRAL_PROCESSING_UNIT.mask();
                } else if (rawItem instanceof com.perigrine3.createcybernetics.item.cyberware.brain.ChipwareSlotsItem) {
                    flags |= CapabilityFlag.CHIPWARE_SLOTS.mask();
                } else if (rawItem instanceof com.perigrine3.createcybernetics.item.cyberware.brain.RamdeckItem) {
                    flags |= CapabilityFlag.RAMDECK.mask();
                } else if (rawItem == faceplate || rawItem == scavengedFaceplate) {
                    flags |= CapabilityFlag.INTERCHANGEABLE_FACEPLATE.mask();
                } else if (rawItem == cyberbrain) {
                    flags |= CapabilityFlag.CYBERBRAIN.mask();
                } else if (rawItem instanceof com.perigrine3.createcybernetics.item.cyberware.brain.CyberdeckItem) {
                    flags |= CapabilityFlag.CYBERDECK.mask();
                } else if (rawItem instanceof com.perigrine3.createcybernetics.item.cyberware.arm.ArmCannonItem) {
                    flags |= CapabilityFlag.ARM_CANNON.mask();
                } else if (rawItem instanceof com.perigrine3.createcybernetics.item.cyberware.skin.SyntheticChromatophoresItem) {
                    flags |= CapabilityFlag.CHROMATOPHORES.mask();
                } else if (rawItem instanceof com.perigrine3.createcybernetics.item.cyberware.muscle.WiredReflexesItem) {
                    flags |= CapabilityFlag.WIRED_REFLEXES.mask();
                }

                if (SculkedCyberwareTickEvents.isSculkedCyberware(stack)) {
                    flags |= CapabilityFlag.SCULKED_IMPLANT.mask();
                }

                if (rawItem instanceof ICyberwareItem cyberwareItem) {
                    InstalledSlotRef ref = new InstalledSlotRef(slot, i, cw, stack, cyberwareItem);
                    TickableCyberwareClassifier.ClassTraits traits = TickableCyberwareClassifier.getTraits(cyberwareItem.getClass());

                    if (traits.tickable()) {
                        tickable.add(ref);
                    }
                    if (traits.energyNode()) {
                        energyNodes.add(ref);
                        if (player == null || CyberwareDurabilityManager.isFunctional(player, cw)) {
                            int cap = cyberwareItem.getEnergyCapacity(player, stack, slot);
                            if (cap > 0) {
                                totalEnergyCapacity += cap;
                            }
                        }
                    }
                }
            }
        }

        boolean hasGills = data.hasFunctionalSpecificItem(player, ModItems.WETWARE_WATERBREATHINGLUNGS.get(), CyberwareSlot.LUNGS);
        boolean allOrgansIntact = computeAllPrimaryOrgansFunctional(player, data, hasGills);
        FullBorgModel fullBorgModel = FullBorgHandler.detectModel(data);
        boolean sculkedBody = SculkedConversionHelper.hasFullDefaultSculkConversion(data);

        List<ItemStack> chipwareSnapshot = new ArrayList<>(PlayerCyberwareData.CHIPWARE_SLOT_COUNT);
        for (int i = 0; i < PlayerCyberwareData.CHIPWARE_SLOT_COUNT; i++) {
            ItemStack st = data.getChipwareStack(i);
            chipwareSnapshot.add(st == null || st.isEmpty() ? ItemStack.EMPTY : st.copy());
        }

        return new CyberwareLoadoutProfile(
                loadoutVersion,
                chipwareVersion,
                tickable.isEmpty() ? List.of() : List.copyOf(tickable),
                energyNodes.isEmpty() ? List.of() : List.copyOf(energyNodes),
                batteries.isEmpty() ? List.of() : List.copyOf(batteries),
                Math.max(0, totalEnergyCapacity),
                allOrgansIntact,
                hasGills,
                fullBorgModel,
                sculkedBody,
                flags,
                nonOrganCount,
                List.copyOf(chipwareSnapshot)
        );
    }

    private static boolean computeAllPrimaryOrgansFunctional(Player player, PlayerCyberwareData data, boolean hasGills) {
        return data.hasAnyFunctionalTagged(player, ModTags.Items.BRAIN_ITEMS, CyberwareSlot.BRAIN)
                && data.hasAnyFunctionalTagged(player, ModTags.Items.EYE_ITEMS, CyberwareSlot.EYES)
                && data.hasAnyFunctionalTagged(player, ModTags.Items.HEART_ITEMS, CyberwareSlot.HEART)
                && (data.hasAnyFunctionalTagged(player, ModTags.Items.LUNGS_ITEMS, CyberwareSlot.LUNGS) || hasGills)
                && data.hasAnyFunctionalTagged(player, ModTags.Items.LIVER_ITEMS, CyberwareSlot.ORGANS)
                && data.hasAnyFunctionalTagged(player, ModTags.Items.INTESTINES_ITEMS, CyberwareSlot.ORGANS)
                && data.hasAnyFunctionalTagged(player, ModTags.Items.BONE_ITEMS, CyberwareSlot.BONE)
                && data.hasAnyFunctionalTagged(player, ModTags.Items.MUSCLE_ITEMS, CyberwareSlot.MUSCLE)
                && data.hasAnyFunctionalTagged(player, ModTags.Items.SKIN_ITEMS, CyberwareSlot.SKIN)
                && data.hasAnyFunctionalTagged(player, ModTags.Items.RIGHTARM_ITEMS, CyberwareSlot.RARM)
                && data.hasAnyFunctionalTagged(player, ModTags.Items.LEFTARM_ITEMS, CyberwareSlot.LARM)
                && data.hasAnyFunctionalTagged(player, ModTags.Items.RIGHTLEG_ITEMS, CyberwareSlot.RLEG)
                && data.hasAnyFunctionalTagged(player, ModTags.Items.LEFTLEG_ITEMS, CyberwareSlot.LLEG);
    }
}
