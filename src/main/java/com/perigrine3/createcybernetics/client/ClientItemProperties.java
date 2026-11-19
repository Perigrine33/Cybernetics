package com.perigrine3.createcybernetics.client;

import com.perigrine3.createcybernetics.CreateCybernetics;
import com.perigrine3.createcybernetics.item.ModItems;
import com.perigrine3.createcybernetics.util.SecondaryDyeColor;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = CreateCybernetics.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientItemProperties {
    private ClientItemProperties() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register(ModItems.DATA_SHARD_SHARED_NAVIGATION.get(), ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "dyed"),
                    (stack, level, entity, seed) -> stack.has(DataComponents.DYED_COLOR) || SecondaryDyeColor.hasColor(stack) ? 1.0F : 0.0F);
            ItemProperties.register(ModItems.DATA_SHARD_MICROPROGRAMS.get(), ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "dyed"),
                    (stack, level, entity, seed) -> stack.has(DataComponents.DYED_COLOR) || SecondaryDyeColor.hasColor(stack) ? 1.0F : 0.0F);
            ItemProperties.register(ModItems.QUICKHACK_SHARD.get(), ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "dyed"),
                    (stack, level, entity, seed) -> stack.has(DataComponents.DYED_COLOR) || SecondaryDyeColor.hasColor(stack) ? 1.0F : 0.0F);
            ItemProperties.register(ModItems.DATA_SHARD_INFOLOG.get(), ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "dyed"),
                    (stack, level, entity, seed) -> stack.has(DataComponents.DYED_COLOR) || SecondaryDyeColor.hasColor(stack) ? 1.0F : 0.0F);
            ItemProperties.register(ModItems.DATA_SHARD_INFOLOG_GETTING_STARTED.get(), ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "dyed"),
                    (stack, level, entity, seed) -> stack.has(DataComponents.DYED_COLOR) || SecondaryDyeColor.hasColor(stack) ? 1.0F : 0.0F);
            ItemProperties.register(ModItems.DATA_SHARD_INFOLOG_ROBOSURGEON_MANUAL.get(), ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "dyed"),
                    (stack, level, entity, seed) -> stack.has(DataComponents.DYED_COLOR) || SecondaryDyeColor.hasColor(stack) ? 1.0F : 0.0F);
            ItemProperties.register(ModItems.DATA_SHARD_INFOLOG_FBC_GUIDE.get(), ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "dyed"),
                    (stack, level, entity, seed) -> stack.has(DataComponents.DYED_COLOR) || SecondaryDyeColor.hasColor(stack) ? 1.0F : 0.0F);
            ItemProperties.register(ModItems.DATA_SHARD_INFOLOG_CYBERBESTIARY.get(), ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "dyed"),
                    (stack, level, entity, seed) -> stack.has(DataComponents.DYED_COLOR) || SecondaryDyeColor.hasColor(stack) ? 1.0F : 0.0F);
            ItemProperties.register(ModItems.DATA_SHARD_INFOLOG_SURGERY_TABLE_MANUAL.get(), ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "dyed"),
                    (stack, level, entity, seed) -> stack.has(DataComponents.DYED_COLOR) || SecondaryDyeColor.hasColor(stack) ? 1.0F : 0.0F);
            ItemProperties.register(ModItems.DATA_SHARD_INFOLOG_CYBERDECK.get(), ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "dyed"),
                    (stack, level, entity, seed) -> stack.has(DataComponents.DYED_COLOR) || SecondaryDyeColor.hasColor(stack) ? 1.0F : 0.0F);
            ItemProperties.register(ModItems.DATA_SHARD_INFOLOG_HARVESTER_ENCHANTMENT.get(), ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "dyed"),
                    (stack, level, entity, seed) -> stack.has(DataComponents.DYED_COLOR) || SecondaryDyeColor.hasColor(stack) ? 1.0F : 0.0F);
            ItemProperties.register(ModItems.DATA_SHARD_INFOLOG_DATURA.get(), ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "dyed"),
                    (stack, level, entity, seed) -> stack.has(DataComponents.DYED_COLOR) || SecondaryDyeColor.hasColor(stack) ? 1.0F : 0.0F);
            ItemProperties.register(ModItems.DATA_SHARD_INFOLOG_QUICKHACK_TUTORIAL.get(), ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "dyed"),
                    (stack, level, entity, seed) -> stack.has(DataComponents.DYED_COLOR) || SecondaryDyeColor.hasColor(stack) ? 1.0F : 0.0F);
            ItemProperties.register(ModItems.DATA_SHARD_INFOLOG_WAIT_YOUR_TURN.get(), ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "dyed"),
                    (stack, level, entity, seed) -> stack.has(DataComponents.DYED_COLOR) || SecondaryDyeColor.hasColor(stack) ? 1.0F : 0.0F);
            ItemProperties.register(ModItems.DATA_SHARD_INFOLOG_THE_BREACH.get(), ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "dyed"),
                    (stack, level, entity, seed) -> stack.has(DataComponents.DYED_COLOR) || SecondaryDyeColor.hasColor(stack) ? 1.0F : 0.0F);
            ItemProperties.register(ModItems.DATA_SHARD_INFOLOG_AI_CHAT.get(), ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "dyed"),
                    (stack, level, entity, seed) -> stack.has(DataComponents.DYED_COLOR) || SecondaryDyeColor.hasColor(stack) ? 1.0F : 0.0F);
            ItemProperties.register(ModItems.DATA_SHARD_INFOLOG_CYBERPSYCHO_TRANSCRIPT.get(), ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "dyed"),
                    (stack, level, entity, seed) -> stack.has(DataComponents.DYED_COLOR) || SecondaryDyeColor.hasColor(stack) ? 1.0F : 0.0F);
            ItemProperties.register(ModItems.DATA_SHARD_INFOLOG_CPU_INSTALL.get(), ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "dyed"),
                    (stack, level, entity, seed) -> stack.has(DataComponents.DYED_COLOR) || SecondaryDyeColor.hasColor(stack) ? 1.0F : 0.0F);
            if (ModItems.DATA_SHARD_INFOLOG_CYBERCHEMS != null) {
                ItemProperties.register(ModItems.DATA_SHARD_INFOLOG_CYBERCHEMS.get(), ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "dyed"),
                        (stack, level, entity, seed) -> stack.has(DataComponents.DYED_COLOR) || SecondaryDyeColor.hasColor(stack) ? 1.0F : 0.0F);
            }

            ItemProperties.register(ModItems.DATA_SHARD_GAME_MINESWEEPER.get(), ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "dyed"),
                    (stack, level, entity, seed) -> stack.has(DataComponents.DYED_COLOR) || SecondaryDyeColor.hasColor(stack) ? 1.0F : 0.0F);
            ItemProperties.register(ModItems.DATA_SHARD_GAME_CHESS.get(), ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "dyed"),
                    (stack, level, entity, seed) -> stack.has(DataComponents.DYED_COLOR) || SecondaryDyeColor.hasColor(stack) ? 1.0F : 0.0F);


            ItemProperties.register(ModItems.BASECYBERWARE_LEFTARM.get(), ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "dyed"),
                    (stack, level, entity, seed) -> stack.has(DataComponents.DYED_COLOR) || SecondaryDyeColor.hasColor(stack) ? 1.0F : 0.0F);
            ItemProperties.register(ModItems.BASECYBERWARE_RIGHTARM.get(), ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "dyed"),
                    (stack, level, entity, seed) -> stack.has(DataComponents.DYED_COLOR) || SecondaryDyeColor.hasColor(stack) ? 1.0F : 0.0F);
            ItemProperties.register(ModItems.BASECYBERWARE_LEFTLEG.get(), ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "dyed"),
                    (stack, level, entity, seed) -> stack.has(DataComponents.DYED_COLOR) || SecondaryDyeColor.hasColor(stack) ? 1.0F : 0.0F);
            ItemProperties.register(ModItems.BASECYBERWARE_RIGHTLEG.get(), ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "dyed"),
                    (stack, level, entity, seed) -> stack.has(DataComponents.DYED_COLOR) || SecondaryDyeColor.hasColor(stack) ? 1.0F : 0.0F);

            ItemProperties.register(ModItems.BASECYBERWARE_CYBEREYES.get(), ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "dyed"),
                    (stack, level, entity, seed) -> stack.has(DataComponents.DYED_COLOR) || SecondaryDyeColor.hasColor(stack) ? 1.0F : 0.0F);
            ItemProperties.register(ModItems.EYEUPGRADES_MONOVISION.get(), ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "dyed"),
                    (stack, level, entity, seed) -> stack.has(DataComponents.DYED_COLOR) || SecondaryDyeColor.hasColor(stack) ? 1.0F : 0.0F);
            ItemProperties.register(ModItems.EYEUPGRADES_MULTIOPTICS1.get(), ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "dyed"),
                    (stack, level, entity, seed) -> stack.has(DataComponents.DYED_COLOR) || SecondaryDyeColor.hasColor(stack) ? 1.0F : 0.0F);
            ItemProperties.register(ModItems.EYEUPGRADES_MULTIOPTICS2.get(), ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "dyed"),
                    (stack, level, entity, seed) -> stack.has(DataComponents.DYED_COLOR) || SecondaryDyeColor.hasColor(stack) ? 1.0F : 0.0F);
            ItemProperties.register(ModItems.EYEUPGRADES_MULTIOPTICS3.get(), ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "dyed"),
                    (stack, level, entity, seed) -> stack.has(DataComponents.DYED_COLOR) || SecondaryDyeColor.hasColor(stack) ? 1.0F : 0.0F);
            ItemProperties.register(ModItems.EYEUPGRADES_MULTIOPTICS4.get(), ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "dyed"),
                    (stack, level, entity, seed) -> stack.has(DataComponents.DYED_COLOR) || SecondaryDyeColor.hasColor(stack) ? 1.0F : 0.0F);

            ItemProperties.register(ModItems.ARMUPGRADES_ARCCANNON.get(), ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "dyed"),
                    (stack, level, entity, seed) -> stack.has(DataComponents.DYED_COLOR) || SecondaryDyeColor.hasColor(stack) ? 1.0F : 0.0F);
            ItemProperties.register(ModItems.BONEUPGRADES_SANDEVISTAN.get(), ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "dyed"),
                    (stack, level, entity, seed) -> stack.has(DataComponents.DYED_COLOR) || SecondaryDyeColor.hasColor(stack) ? 1.0F : 0.0F);
            ItemProperties.register(ModItems.BRAINUPGRADES_COAXIALPORT.get(), ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "dyed"),
                    (stack, level, entity, seed) -> stack.has(DataComponents.DYED_COLOR) || SecondaryDyeColor.hasColor(stack) ? 1.0F : 0.0F);
            ItemProperties.register(ModItems.BRAINUPGRADES_CYBERDECK.get(), ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "dyed"),
                    (stack, level, entity, seed) -> stack.has(DataComponents.DYED_COLOR) || SecondaryDyeColor.hasColor(stack) ? 1.0F : 0.0F);
        });
    }
}
