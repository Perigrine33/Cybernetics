package com.perigrine3.createcybernetics.client;

import com.perigrine3.createcybernetics.CreateCybernetics;
import com.perigrine3.createcybernetics.compat.ModCompats;
import com.perigrine3.createcybernetics.item.ModItems;
import com.perigrine3.createcybernetics.item.generic.DynamicPotionAutoinjectorItem;
import com.perigrine3.createcybernetics.util.SecondaryDyeColor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.DyedItemColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

@EventBusSubscriber(modid = CreateCybernetics.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientItemColors {
    private ClientItemColors() {}

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> {
                    if (tintIndex == 1) {
                        DyedItemColor dyed =
                                stack.get(DataComponents.DYED_COLOR);

                        if (dyed == null) {
                            return 0x00FFFFFF;
                        }

                        return 0xFF000000
                                | (dyed.rgb() & 0x00FFFFFF);
                    }

                    if (tintIndex == 2) {
                        if (!SecondaryDyeColor.hasColor(stack)) {
                            return 0x00FFFFFF;
                        }

                        return SecondaryDyeColor.getColor(stack);
                    }

                    return 0xFFFFFFFF;
                },

            //DYEABLE ITEMS
                ModItems.DATA_SHARD_MICROPROGRAMS.get(),
                ModItems.QUICKHACK_SHARD.get(),
                ModItems.DATA_SHARD_SHARED_NAVIGATION.get(),
                ModItems.DATA_SHARD_INFOLOG.get(),
                ModItems.DATA_SHARD_INFOLOG_GETTING_STARTED.get(),
                ModItems.DATA_SHARD_INFOLOG_ROBOSURGEON_MANUAL.get(),
                ModItems.DATA_SHARD_INFOLOG_SURGERY_TABLE_MANUAL.get(),
                ModItems.DATA_SHARD_INFOLOG_FBC_GUIDE.get(),
                ModItems.DATA_SHARD_INFOLOG_CYBERBESTIARY.get(),
                ModItems.DATA_SHARD_INFOLOG_CYBERDECK.get(),
                ModItems.DATA_SHARD_INFOLOG_HARVESTER_ENCHANTMENT.get(),
                ModItems.DATA_SHARD_INFOLOG_DATURA.get(),
                ModItems.DATA_SHARD_INFOLOG_QUICKHACK_TUTORIAL.get(),

                ModItems.BASECYBERWARE_LEFTARM.get(),
                ModItems.BASECYBERWARE_RIGHTARM.get(),
                ModItems.BASECYBERWARE_LEFTLEG.get(),
                ModItems.BASECYBERWARE_RIGHTLEG.get(),
                ModItems.BASECYBERWARE_CYBEREYES.get(),
                ModItems.EYEUPGRADES_MONOVISION.get(),
                ModItems.EYEUPGRADES_MULTIOPTICS1.get(),
                ModItems.EYEUPGRADES_MULTIOPTICS2.get(),
                ModItems.EYEUPGRADES_MULTIOPTICS3.get(),
                ModItems.EYEUPGRADES_MULTIOPTICS4.get(),
                ModItems.SKINUPGRADES_METALPLATING.get(),
                ModItems.LEGUPGRADES_OCELOTPAWS.get(),
                ModItems.ARMUPGRADES_ARCCANNON.get(),
                ModItems.BONEUPGRADES_SANDEVISTAN.get(),
                ModItems.BRAINUPGRADES_COAXIALPORT.get(),
                ModItems.BRAINUPGRADES_CYBERDECK.get(),

                        ModItems.DATA_SHARD_INFOLOG_SUNSET_SHIFT.get(),
                        ModItems.DATA_SHARD_INFOLOG_HOUSE_RULES.get(),
                        ModItems.DATA_SHARD_INFOLOG_DENSE_BATTERY_AD.get(),
                        ModItems.DATA_SHARD_INFOLOG_IMMUNOSUPPRESSOR_ADVISORY.get(),
                        ModItems.DATA_SHARD_INFOLOG_CHROMATOPHORE_FIELD_NOTE.get(),
                        ModItems.DATA_SHARD_INFOLOG_THE_RAISE.get(),
                        ModItems.DATA_SHARD_INFOLOG_OMNISCIENT_OPTICS_AD.get(),
                        ModItems.DATA_SHARD_INFOLOG_PRIVATE_WARD_REPORT.get(),
                        ModItems.DATA_SHARD_INFOLOG_DRILLFIST_AD.get(),
                        ModItems.DATA_SHARD_INFOLOG_IDEM_ARTICLE.get(),
                        ModItems.DATA_SHARD_INFOLOG_MOM_IM_FINE.get(),
                        ModItems.DATA_SHARD_INFOLOG_CYBERDECK_SECURITY_BRIEF.get(),
                        ModItems.DATA_SHARD_INFOLOG_LUNCH_BREAK.get(),
                        ModItems.DATA_SHARD_INFOLOG_WAIT_YOUR_TURN.get(),
                        ModItems.DATA_SHARD_INFOLOG_THE_BREACH.get(),
                        ModItems.DATA_SHARD_INFOLOG_AI_CHAT.get(),
                        ModItems.DATA_SHARD_INFOLOG_CYBERPSYCHO_TRANSCRIPT.get(),
                        ModItems.DATA_SHARD_INFOLOG_CPU_INSTALL.get(),

                        ModItems.DATA_SHARD_GAME_MINESWEEPER.get(),
                        ModItems.DATA_SHARD_GAME_CHESS.get()
        );

        if (ModItems.DATA_SHARD_INFOLOG_CYBERCHEMS != null) {
            event.register((stack, tintIndex) -> {
                        if (tintIndex == 1) {
                            DyedItemColor dyed =
                                    stack.get(DataComponents.DYED_COLOR);

                            if (dyed == null) {
                                return 0x00FFFFFF;
                            }

                            return 0xFF000000
                                    | (dyed.rgb() & 0x00FFFFFF);
                        }

                        if (tintIndex == 2) {
                            if (!SecondaryDyeColor.hasColor(stack)) {
                                return 0x00FFFFFF;
                            }

                            return SecondaryDyeColor.getColor(stack);
                        }

                        return 0xFFFFFFFF;
                    },

                        ModItems.DATA_SHARD_INFOLOG_CYBERCHEMS.get());
        }






        event.register(
                (stack, tintIndex) -> tintIndex == 1
                        ? DynamicPotionAutoinjectorItem.getAutoinjectorColor(stack)
                        : -1,

                ModItems.DYNAMIC_POTION_AUTOINJECTOR.get()
        );
    }
}
