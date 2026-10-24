package com.perigrine3.createcybernetics.item;

import com.perigrine3.createcybernetics.CreateCybernetics;
import com.perigrine3.createcybernetics.api.CyberwareSlot;
import com.perigrine3.createcybernetics.block.ModBlocks;
import com.perigrine3.createcybernetics.client.ModKeyMappings;
import com.perigrine3.createcybernetics.common.macroprogram.MacroprogramDefinition;
import com.perigrine3.createcybernetics.common.macroprogram.MacroprogramRegistry;
import com.perigrine3.createcybernetics.common.quickhack.MicroprogramDefinition;
import com.perigrine3.createcybernetics.common.quickhack.MicroprogramRegistry;
import com.perigrine3.createcybernetics.compat.ModCompats;
import com.perigrine3.createcybernetics.compat.ironsspells.IronsSpellbooksCompat;
import com.perigrine3.createcybernetics.compat.ironsspells.IronsSpellbooksStaffItems;
import com.perigrine3.createcybernetics.component.MicroprogramLibrary;
import com.perigrine3.createcybernetics.component.ModDataComponents;
import com.perigrine3.createcybernetics.entity.ModEntities;
import com.perigrine3.createcybernetics.item.cyberware.arm.*;
import com.perigrine3.createcybernetics.item.cyberware.bone.*;
import com.perigrine3.createcybernetics.item.cyberware.brain.*;
import com.perigrine3.createcybernetics.item.cyberware.eyes.*;
import com.perigrine3.createcybernetics.item.cyberware.heart.*;
import com.perigrine3.createcybernetics.item.cyberware.leg.*;
import com.perigrine3.createcybernetics.item.cyberware.lungs.HyperoxygenationBoostItem;
import com.perigrine3.createcybernetics.item.cyberware.lungs.OxygenTankItem;
import com.perigrine3.createcybernetics.item.cyberware.lungs.SynthLungsItem;
import com.perigrine3.createcybernetics.item.cyberware.lungs.VampyresItem;
import com.perigrine3.createcybernetics.item.cyberware.muscle.BallisticGelItem;
import com.perigrine3.createcybernetics.item.cyberware.muscle.ReinforcedTendonsItem;
import com.perigrine3.createcybernetics.item.cyberware.muscle.SynthMuscleItem;
import com.perigrine3.createcybernetics.item.cyberware.muscle.WiredReflexesItem;
import com.perigrine3.createcybernetics.item.cyberware.organs.*;
import com.perigrine3.createcybernetics.item.cyberware.skin.*;
import com.perigrine3.createcybernetics.item.cyberware.wetware.*;
import com.perigrine3.createcybernetics.item.food.BrainStewItem;
import com.perigrine3.createcybernetics.item.generic.*;
import com.perigrine3.createcybernetics.item.macroprogram.*;
import com.perigrine3.createcybernetics.item.organs.*;
import com.perigrine3.createcybernetics.item.sculked.*;
import com.perigrine3.createcybernetics.sound.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.DyedItemColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Supplier;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CreateCybernetics.MODID);


//BASIC ITEMS & COMPONENTS
    public static final DeferredItem<Item> RAWTITANIUM = ITEMS.register("rawtitanium",
            () -> new ConditionalNameItem(new Item.Properties(),
                    "item.createcybernetics.rawtitanium",
                    "item.createcybernetics.rawtitanium.northstar"));
    public static final DeferredItem<Item> TITANIUMINGOT = ITEMS.register("titaniumingot",
            () -> new ConditionalNameItem(new Item.Properties(),
                    "item.createcybernetics.titaniumingot",
                    "item.createcybernetics.titaniumingot.northstar"));
    public static final DeferredItem<Item> CRUSHEDTITANIUM = ITEMS.register("crushedtitanium",
            () -> new ConditionalNameItem(new Item.Properties(),
                    "item.createcybernetics.crushedtitanium",
                    "item.createcybernetics.crushedtitanium.northstar"));
    public static final DeferredItem<Item> TITANIUMNUGGET = ITEMS.register("titaniumnugget",
            () -> new ConditionalNameItem(new Item.Properties(),
                    "item.createcybernetics.titaniumnugget",
                    "item.createcybernetics.titaniumnugget.northstar"));
    public static final DeferredItem<Item> TITANIUMSHEET = ITEMS.register("titaniumsheet",
            () -> new ConditionalNameItem(new Item.Properties(),
                    "item.createcybernetics.titaniumsheet",
                    "item.createcybernetics.titaniumsheet.northstar"));

    public static final DeferredItem<Item> EYEUPGRADEBASE = ITEMS.register("eyeupgradebase",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> TITANIUM_HAND = ITEMS.register("titanium_hand",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> GRAPHENE_ELASTOMER = ITEMS.register("graphene_elastomer",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> HOLOIMPRINT_CHIP = ITEMS.register("holoimprint_chip",
            () -> new HoloProjectionChipItem(new Item.Properties()));
    public static final DeferredItem<Item> XP_CAPSULE = ITEMS.register("expcapsule",
            () -> new XPCapsuleItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> FACEPLATE = ITEMS.register("faceplate",
            () -> new FaceplateMaskItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> NETHERITE_QPU = ITEMS.register("netherite_qpu",
            () -> new Item(new Item.Properties()){
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.legendarycomponent_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.qpu_loc").withStyle(ChatFormatting.GRAY));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }});
    public static final DeferredItem<Item> EMP_GRENADE = ITEMS.register("emp_grenade",
            () -> new EmpGrenadeItem(new Item.Properties()));
    public static final DeferredItem<Item> LINK_CABLE = ITEMS.register("link_cable",
            () -> new Item(new Item.Properties()){
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.link_cable").withStyle(ChatFormatting.GRAY));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }});

    public static final DeferredItem<Item> TAR = ITEMS.register("tar",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> UNREFINED_POLYMER = ITEMS.register("unrefined_polymer",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> PLASTIC_SHEET = ITEMS.register("plastic_sheet",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> DATURA_FLOWER = ITEMS.register("datura_flower",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> DATURA_SEED_POD = ITEMS.register("datura_seed_pod",
            () -> new ItemNameBlockItem(ModBlocks.DATURA_BUSH.get(), new Item.Properties()));

    public static final DeferredItem<Item> EXOSUIT1 = ITEMS.register("exosuit1",
            () -> new ExosuitMK1(new Item.Properties()){
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    if (Screen.hasShiftDown()) {
                        tooltipComponents.add(Component.translatable("tootltip1.createcybernetics.exosuit1"));
                        tooltipComponents.add(Component.translatable("tootltip2.createcybernetics.exosuit1"));
                    } else {
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                    }
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }});

    public static final DeferredItem<Item> FRONTAL_LOBE = ITEMS.register("frontal_lobe",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> PARIETAL_LOBE = ITEMS.register("parietal_lobe",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> TEMPORAL_LOBE = ITEMS.register("temporal_lobe",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> OCCIPITAL_LOBE = ITEMS.register("occipital_lobe",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CEREBELLUM = ITEMS.register("cerebellum",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> BONE_SAW = ITEMS.register("bone_saw",
            () -> new SwordItem(Tiers.IRON, new Item.Properties().stacksTo(1).durability(128)));

    public static final DeferredItem<Item> NEUROPOZYNE_AUTOINJECTOR = ITEMS.register("neuropozyne_autoinjector",
            () -> new NeuropozyneAutoinjector(new Item.Properties().stacksTo(16)));

    public static final DeferredItem<Item> EMPTY_AUTOINJECTOR = ITEMS.register("empty_autoinjector",
            () -> new Item(new Item.Properties().stacksTo(64)));
    public static final DeferredItem<Item> INCOMPLETE_EMPTY_AUTOINJECTOR = ITEMS.register("incomplete_empty_autoinjector",
            () -> new Item(new Item.Properties().stacksTo(64)));
    public static final DeferredItem<Item> DYNAMIC_POTION_AUTOINJECTOR = ITEMS.register("potion_autoinjector",
            () -> new DynamicPotionAutoinjectorItem(new Item.Properties().stacksTo(16)));

    public static final DeferredItem<Item> MUSIC_DISC_CYBERPSYCHO = ITEMS.register("music_disc_cyberpsycho",
            () -> new Item(new Item.Properties().stacksTo(1).jukeboxPlayable(ModSounds.CYBERPSYCHO_KEY)));
        public static final DeferredItem<Item> MUSIC_DISC_NEON_OVERLORDS = ITEMS.register("music_disc_neon_overlords",
            () -> new Item(new Item.Properties().stacksTo(1).jukeboxPlayable(ModSounds.NEON_OVERLORDS_KEY)));
    public static final DeferredItem<Item> MUSIC_DISC_NEUROHACK = ITEMS.register("music_disc_neurohack",
            () -> new Item(new Item.Properties().stacksTo(1).jukeboxPlayable(ModSounds.NEUROHACK_KEY)));
    public static final DeferredItem<Item> MUSIC_DISC_THE_GRID = ITEMS.register("music_disc_the_grid",
            () -> new Item(new Item.Properties().stacksTo(1).jukeboxPlayable(ModSounds.THE_GRID_KEY)));

    public static final DeferredItem<Item> MUSIC_DISC_DANGER_SNOW = ITEMS.register("music_disc_danger_snow",
            () -> new Item(new Item.Properties().stacksTo(1).jukeboxPlayable(ModSounds.DANGER_SNOW_KEY)));
    public static final DeferredItem<Item> MUSIC_DISC_DARK = ITEMS.register("music_disc_dark",
            () -> new Item(new Item.Properties().stacksTo(1).jukeboxPlayable(ModSounds.DARK_KEY)));
    public static final DeferredItem<Item> MUSIC_DISC_MY_RETRO_JACKET = ITEMS.register("music_disc_my_retro_jacket",
            () -> new Item(new Item.Properties().stacksTo(1).jukeboxPlayable(ModSounds.MY_RETRO_JACKET_KEY)));
    public static final DeferredItem<Item> MUSIC_DISC_GAMEBOY_GIRL = ITEMS.register("music_disc_gameboy_girl",
            () -> new Item(new Item.Properties().stacksTo(1).jukeboxPlayable(ModSounds.GAMEBOY_GIRL_KEY)));
    public static final DeferredItem<Item> MUSIC_DISC_NIGHTMARES_ON_HAUPTSTRASSE = ITEMS.register("music_disc_nightmares_on_hauptstrasse",
            () -> new Item(new Item.Properties().stacksTo(1).jukeboxPlayable(ModSounds.NIGHTMARES_ON_HAUPTSTRASSE_KEY)));
    public static final DeferredItem<Item> MUSIC_DISC_INTERGALACTIC_WALK = ITEMS.register("music_disc_intergalactic_walk",
            () -> new Item(new Item.Properties().stacksTo(1).jukeboxPlayable(ModSounds.INTERGALACTIC_WALK_KEY)));
    public static final DeferredItem<Item> MUSIC_DISC_SENSATION = ITEMS.register("music_disc_sensation",
            () -> new Item(new Item.Properties().stacksTo(1).jukeboxPlayable(ModSounds.SENSATION_KEY)));
    public static final DeferredItem<Item> MUSIC_DISC_SHOTGUN = ITEMS.register("music_disc_shotgun",
            () -> new Item(new Item.Properties().stacksTo(1).jukeboxPlayable(ModSounds.SHOTGUN_KEY)));
    public static final DeferredItem<Item> MUSIC_DISC_DESCENT_INTO_MADNESS = ITEMS.register("music_disc_descent_into_madness",
            () -> new Item(new Item.Properties().stacksTo(1).jukeboxPlayable(ModSounds.DESCENT_INTO_MADNESS_KEY)));


//SPAWN EGGS
    public static final DeferredItem<Item> RIPPER_SPAWN_EGG = ITEMS.register("ripper_spawn_egg",
        () -> new DeferredSpawnEggItem(ModEntities.RIPPER, 0x969696, 0xFFFF003C,
                new Item.Properties()));
    public static final DeferredItem<Item> TATHOG_SPAWN_EGG = ITEMS.register("tathog_spawn_egg",
        () -> new DeferredSpawnEggItem(ModEntities.TATHOG, 0xC97A4A, 0x00AFAF,
                new Item.Properties()));
    public static final DeferredItem<Item> SMASHER_SPAWN_EGG = ITEMS.register("smasher_spawn_egg",
        () -> new DeferredSpawnEggItem(ModEntities.SMASHER, 0x7f7b7b, 0xf44336,
                new Item.Properties()));
    public static final DeferredItem<Item> CYBERZOMBIE_SPAWN_EGG = ITEMS.register("cyberzombie_spawn_egg",
        () -> new DeferredSpawnEggItem(ModEntities.CYBERZOMBIE, 0x00AFAF, 0xFFFF003C,
                new Item.Properties()));
    public static final DeferredItem<Item> CYBERSKELETON_SPAWN_EGG = ITEMS.register("cyberskeleton_spawn_egg",
        () -> new DeferredSpawnEggItem(ModEntities.CYBERSKELETON, 0xC1C1C1, 0xFFFF003C,
                new Item.Properties()));
    public static final DeferredItem<Item> HOGBOY_SPAWN_EGG = ITEMS.register("hogboy_spawn_egg",
        () -> new DeferredSpawnEggItem(ModEntities.HOGBOY, 0xC97A4A, 0x2F80ED,
                new Item.Properties()));
    public static final DeferredItem<Item> PUNKLIN_SPAWN_EGG = ITEMS.register("punklin_spawn_egg",
        () -> new DeferredSpawnEggItem(ModEntities.PUNKLIN, 0xC97A4A, 0x8E44AD,
                new Item.Properties()));
    public static final DeferredItem<Item> PIGSTROM_SPAWN_EGG = ITEMS.register("pigstrom_spawn_egg",
        () -> new DeferredSpawnEggItem(ModEntities.PIGSTROM, 0xC97A4A, 0xC0392B,
                new Item.Properties()));


//DATA SHARDS
    public static final DeferredItem<Item> DATA_SHARD_RED = ITEMS.register("data_shard_red",
            () -> new DataShardItem(new Item.Properties().stacksTo(16)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_red.title").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xD90804))));
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_red.desc"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }});
    public static final DeferredItem<Item> DATA_SHARD_ORANGE = ITEMS.register("data_shard_orange",
            () -> new DataShardItem(new Item.Properties().stacksTo(16)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_orange.title").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xD96704))));
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_orange.desc"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }});
    public static final DeferredItem<Item> DATA_SHARD_YELLOW = ITEMS.register("data_shard_yellow",
            () -> new DataShardItem(new Item.Properties().stacksTo(16)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_yellow.title").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xE8C602))));
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_yellow.desc"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }});
    public static final DeferredItem<Item> DATA_SHARD_GREEN = ITEMS.register("data_shard_green",
            () -> new DataShardItem(new Item.Properties().stacksTo(16)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_green.title").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x08B302))));
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_green.desc"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }});
    public static final DeferredItem<Item> DATA_SHARD_CYAN = ITEMS.register("data_shard_cyan",
            () -> new DataShardItem(new Item.Properties().stacksTo(16)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_cyan.title").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x02C97A))));
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_cyan.desc"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }});
    public static final DeferredItem<Item> DATA_SHARD_BLUE = ITEMS.register("data_shard_blue",
            () -> new DataShardItem(new Item.Properties().stacksTo(16)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_blue.title").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x023DE0))));
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_blue.desc"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }});
    public static final DeferredItem<Item> DATA_SHARD_PURPLE = ITEMS.register("data_shard_purple",
            () -> new DataShardItem(new Item.Properties().stacksTo(16)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_purple.title").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x9A02D6))));
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_purple.desc"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }});
    public static final DeferredItem<Item> DATA_SHARD_PINK = ITEMS.register("data_shard_pink",
            () -> new DataShardItem(new Item.Properties().stacksTo(16)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_pink.title").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xE002AD))));
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_pink.desc"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }});
    public static final DeferredItem<Item> DATA_SHARD_BROWN = ITEMS.register("data_shard_brown",
            () -> new DataShardItem(new Item.Properties().stacksTo(16)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_brown.title").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x783F19))));
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_brown.desc"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }});
    public static final DeferredItem<Item> DATA_SHARD_GRAY = ITEMS.register("data_shard_gray",
            () -> new DataShardItem(new Item.Properties().stacksTo(16)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_gray.title").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x5E5E5E))));
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_gray.desc"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }});
    public static final DeferredItem<Item> DATA_SHARD_BLACK = ITEMS.register("data_shard_black",
            () -> new DataShardItem(new Item.Properties().stacksTo(16)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_black.title").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x292929))));
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_black.desc"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }});
    public static final DeferredItem<Item> DATA_SHARD_BIOCHIP = ITEMS.register("data_shard_biochip",
            () -> new BiochipDataShardItem(new Item.Properties().stacksTo(1)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_biochip.desc"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }});

    public static final DeferredItem<Item> DATA_SHARD_SHARED_NAVIGATION = ITEMS.register("data_shard_shared_navigation",
            () -> new SharedNavigationShardItem(new Item.Properties().stacksTo(16)));
    public static final DeferredItem<Item> DATA_SHARD_INFOLOG = ITEMS.register("data_shard_infolog",
            () -> new InfologDataShardItem(new Item.Properties().stacksTo(1)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_infolog.desc"));

                    Component key = com.perigrine3.createcybernetics.client.ModKeyMappings.INFOLOG.get().getTranslatedKeyMessage();
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_infolog.desc2", key));

                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }});
    public static final DeferredItem<Item> DATA_SHARD_MICROPROGRAMS = ITEMS.register("data_shard_microprograms",
            () -> new MicroprogramDataShardItem(new Item.Properties()
                    .stacksTo(1)
                    .component(ModDataComponents.MICROPROGRAM_LIBRARY.get(), MicroprogramLibrary.EMPTY)));
    public static final DeferredItem<Item> DATA_SHARD_MICROPROGRAMS_CREATIVE = ITEMS.register("data_shard_microprograms_creative",
            () -> new MicroprogramCreativeShardItem(new Item.Properties()
                    .stacksTo(1)
                    .component(ModDataComponents.MICROPROGRAM_LIBRARY.get(), MicroprogramCreativeShardItem.createFullLibrary())));
    public static final DeferredItem<Item> QUICKHACK_SHARD = ITEMS.register("quickhack_shard",
            () -> new QuickhackShardItem(new Item.Properties().stacksTo(1)));

//TUTORIAL SHARDS
    public static final DeferredItem<Item> DATA_SHARD_INFOLOG_GETTING_STARTED = ITEMS.register("data_shard_infolog_getting_started",
            () -> new InfologDataShardItem(new Item.Properties().stacksTo(1).component(DataComponents.DYED_COLOR,
                    new DyedItemColor(0x00FF66, false)),

                    "item.createcybernetics.data_shard_infolog_getting_started.title",
                    "item.createcybernetics.data_shard_infolog_getting_started.text",

                    true
            ) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_infolog.desc"));

                    Component key = com.perigrine3.createcybernetics.client.ModKeyMappings.INFOLOG.get().getTranslatedKeyMessage();
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_infolog.desc2", key));

                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            }
    );

    public static final DeferredItem<Item> DATA_SHARD_INFOLOG_ROBOSURGEON_MANUAL = ITEMS.register("data_shard_infolog_robosurgeon_manual",
            () -> new InfologDataShardItem(new Item.Properties().stacksTo(1).component(DataComponents.DYED_COLOR,
                    new DyedItemColor(0x00a5cf, false)),

                    "item.createcybernetics.data_shard_infolog_robosurgeon_manual.title",
                    "item.createcybernetics.data_shard_infolog_robosurgeon_manual.text",

                    true
            ) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_infolog.desc"));

                    Component key = com.perigrine3.createcybernetics.client.ModKeyMappings.INFOLOG.get().getTranslatedKeyMessage();
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_infolog.desc2", key));

                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            }
    );

    public static final DeferredItem<Item> DATA_SHARD_INFOLOG_FBC_GUIDE = ITEMS.register("data_shard_infolog_fbc_guide",
            () -> new InfologDataShardItem(new Item.Properties().stacksTo(1).component(DataComponents.DYED_COLOR,
                    new DyedItemColor(0x720078, false)),

                    "item.createcybernetics.data_shard_infolog_fbc_guide.title",
                    true,
                    InfologGuideSection.always("item.createcybernetics.data_shard_infolog_fbc_guide.text"),
                    InfologGuideSection.always("item.createcybernetics.data_shard_infolog_fbc_guide.linebreak"),

                    InfologGuideSection.always("item.createcybernetics.data_shard_infolog_fbc_guide.gemini"),
                    InfologGuideSection.always("item.createcybernetics.data_shard_infolog_fbc_guide.linebreak"),

                    InfologGuideSection.always("item.createcybernetics.data_shard_infolog_fbc_guide.samson"),
                    InfologGuideSection.always("item.createcybernetics.data_shard_infolog_fbc_guide.linebreak"),

                    InfologGuideSection.always("item.createcybernetics.data_shard_infolog_fbc_guide.eclipse"),
                    InfologGuideSection.always("item.createcybernetics.data_shard_infolog_fbc_guide.linebreak"),

                    InfologGuideSection.always("item.createcybernetics.data_shard_infolog_fbc_guide.spyder"),
                    InfologGuideSection.always("item.createcybernetics.data_shard_infolog_fbc_guide.linebreak"),

                    InfologGuideSection.whenModLoaded("caelus","item.createcybernetics.data_shard_infolog_fbc_guide.wingman"),
                    InfologGuideSection.whenModLoaded("caelus","item.createcybernetics.data_shard_infolog_fbc_guide.linebreak"),

                    InfologGuideSection.always("item.createcybernetics.data_shard_infolog_fbc_guide.aquarius"),
                    InfologGuideSection.always("item.createcybernetics.data_shard_infolog_fbc_guide.linebreak"),

                    InfologGuideSection.always("item.createcybernetics.data_shard_infolog_fbc_guide.dymond"),
                    InfologGuideSection.always("item.createcybernetics.data_shard_infolog_fbc_guide.linebreak"),

                    InfologGuideSection.always("item.createcybernetics.data_shard_infolog_fbc_guide.dragoon"),
                    InfologGuideSection.always("item.createcybernetics.data_shard_infolog_fbc_guide.linebreak"),

                    InfologGuideSection.whenModLoaded("creatingspace","item.createcybernetics.data_shard_infolog_fbc_guide.copernicus"),
                    InfologGuideSection.whenModLoaded("creatingspace","item.createcybernetics.data_shard_infolog_fbc_guide.linebreak"),
                    InfologGuideSection.whenModLoaded("northstar","item.createcybernetics.data_shard_infolog_fbc_guide.copernicus"),
                    InfologGuideSection.whenModLoaded("northstar","item.createcybernetics.data_shard_infolog_fbc_guide.linebreak"),

                    InfologGuideSection.always("item.createcybernetics.data_shard_infolog_fbc_guide.genos"),
                    InfologGuideSection.always("item.createcybernetics.data_shard_infolog_fbc_guide.linebreak"),

                    InfologGuideSection.always("item.createcybernetics.data_shard_infolog_fbc_guide.kildare"),
                    InfologGuideSection.always("item.createcybernetics.data_shard_infolog_fbc_guide.linebreak"),

                    InfologGuideSection.whenModLoaded("irons_spellbooks", "item.createcybernetics.data_shard_infolog_fbc_guide.hexborg"),
                    InfologGuideSection.whenModLoaded("irons_spellbooks", "item.createcybernetics.data_shard_infolog_fbc_guide.linebreak")
            ) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_infolog.desc"));

                    Component key = com.perigrine3.createcybernetics.client.ModKeyMappings.INFOLOG.get().getTranslatedKeyMessage();
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_infolog.desc2", key));

                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            }
    );

    public static final DeferredItem<Item> DATA_SHARD_INFOLOG_CYBERBESTIARY = ITEMS.register("data_shard_infolog_cyberbestiary",
            () -> new InfologDataShardItem(new Item.Properties().stacksTo(1).component(DataComponents.DYED_COLOR,
                    new DyedItemColor(0xff1100, false)),

                    "item.createcybernetics.data_shard_infolog_cyberbestiary.title",
                    "item.createcybernetics.data_shard_infolog_cyberbestiary.text",

                    true
            ) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_infolog.desc"));

                    Component key = com.perigrine3.createcybernetics.client.ModKeyMappings.INFOLOG.get().getTranslatedKeyMessage();
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_infolog.desc2", key));

                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            }
    );

    public static final DeferredItem<Item> DATA_SHARD_INFOLOG_SURGERY_TABLE_MANUAL = ITEMS.register("data_shard_infolog_surgery_table_manual",
            () -> new InfologDataShardItem(new Item.Properties().stacksTo(1).component(DataComponents.DYED_COLOR,
                    new DyedItemColor(0x00a5cf, false)),

                    "item.createcybernetics.data_shard_infolog_surgery_table_manual.title",
                    "item.createcybernetics.data_shard_infolog_surgery_table_manual.text",

                    true
            ) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_infolog.desc"));

                    Component key = com.perigrine3.createcybernetics.client.ModKeyMappings.INFOLOG.get().getTranslatedKeyMessage();
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_infolog.desc2", key));

                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            }
    );

    public static final DeferredItem<Item> DATA_SHARD_INFOLOG_CYBERCHEMS = registerIfLoaded("cyberchems", "data_shard_infolog_cyberchems",
            () -> new InfologDataShardItem(new Item.Properties().stacksTo(1).component(DataComponents.DYED_COLOR,
                    new DyedItemColor(0xb7fa00, false)),

                    "item.createcybernetics.data_shard_infolog_cyberchems.title",
                    "item.createcybernetics.data_shard_infolog_cyberchems.text",

                    true
            ) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_infolog.desc"));

                    Component key = com.perigrine3.createcybernetics.client.ModKeyMappings.INFOLOG.get().getTranslatedKeyMessage();
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_infolog.desc2", key));

                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            }
    );

    public static final DeferredItem<Item> DATA_SHARD_INFOLOG_CYBERDECK = ITEMS.register("data_shard_infolog_cyberdeck",
            () -> new InfologDataShardItem(new Item.Properties().stacksTo(1).component(DataComponents.DYED_COLOR,
                    new DyedItemColor(0x00ffee, false)),

                    "item.createcybernetics.data_shard_infolog_cyberdeck.title",
                    "item.createcybernetics.data_shard_infolog_cyberdeck.text",

                    true
            ) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_infolog.desc"));

                    Component key = com.perigrine3.createcybernetics.client.ModKeyMappings.INFOLOG.get().getTranslatedKeyMessage();
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_infolog.desc2", key));

                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            }
    );

    public static final DeferredItem<Item> DATA_SHARD_INFOLOG_HARVESTER_ENCHANTMENT = ITEMS.register("data_shard_infolog_harvester_enchantment",
            () -> new InfologDataShardItem(new Item.Properties().stacksTo(1).component(DataComponents.DYED_COLOR,
                    new DyedItemColor(0x00ffee, false)),

                    "item.createcybernetics.data_shard_infolog_harvester_enchantment.title",
                    "item.createcybernetics.data_shard_infolog_harvester_enchantment.text",

                    true
            ) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_infolog.desc"));

                    Component key = com.perigrine3.createcybernetics.client.ModKeyMappings.INFOLOG.get().getTranslatedKeyMessage();
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_infolog.desc2", key));

                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            }
    );









// LORE SHARDS
    public static final DeferredItem<Item> DATA_SHARD_INFOLOG_SUNSET_SHIFT = ITEMS.register("data_shard_infolog_sunset_shift",
            () -> new InfologDataShardItem(new Item.Properties().stacksTo(1).component(DataComponents.DYED_COLOR,
                    new DyedItemColor(0x303030, false)),

                    "item.createcybernetics.data_shard_infolog_sunset_shift.title",
                    "item.createcybernetics.data_shard_infolog_sunset_shift.text",

                    true
            ) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_infolog.desc"));

                    Component key = com.perigrine3.createcybernetics.client.ModKeyMappings.INFOLOG.get().getTranslatedKeyMessage();
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_infolog.desc2", key));

                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            }
    );

    public static final DeferredItem<Item> DATA_SHARD_INFOLOG_HOUSE_RULES = ITEMS.register("data_shard_infolog_house_rules",
            () -> new InfologDataShardItem(new Item.Properties().stacksTo(1).component(DataComponents.DYED_COLOR,
                    new DyedItemColor(0x303030, false)),

                    "item.createcybernetics.data_shard_infolog_house_rules.title",
                    "item.createcybernetics.data_shard_infolog_house_rules.text",

                    true
            ) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_infolog.desc"));

                    Component key = com.perigrine3.createcybernetics.client.ModKeyMappings.INFOLOG.get().getTranslatedKeyMessage();
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_infolog.desc2", key));

                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            }
    );

    public static final DeferredItem<Item> DATA_SHARD_INFOLOG_DENSE_BATTERY_AD = ITEMS.register("data_shard_infolog_dense_battery_ad",
            () -> new InfologDataShardItem(new Item.Properties().stacksTo(1).component(DataComponents.DYED_COLOR,
                    new DyedItemColor(0x303030, false)),

                    "item.createcybernetics.data_shard_infolog_dense_battery_ad.title",
                    "item.createcybernetics.data_shard_infolog_dense_battery_ad.text",

                    true
            ) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_infolog.desc"));

                    Component key = com.perigrine3.createcybernetics.client.ModKeyMappings.INFOLOG.get().getTranslatedKeyMessage();
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_infolog.desc2", key));

                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            }
    );

    public static final DeferredItem<Item> DATA_SHARD_INFOLOG_IMMUNOSUPPRESSOR_ADVISORY = ITEMS.register("data_shard_infolog_immunosuppressor_advisory",
            () -> new InfologDataShardItem(new Item.Properties().stacksTo(1).component(DataComponents.DYED_COLOR,
                    new DyedItemColor(0x303030, false)),

                    "item.createcybernetics.data_shard_infolog_immunosuppressor_advisory.title",
                    "item.createcybernetics.data_shard_infolog_immunosuppressor_advisory.text",

                    true
            ) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_infolog.desc"));

                    Component key = com.perigrine3.createcybernetics.client.ModKeyMappings.INFOLOG.get().getTranslatedKeyMessage();
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_infolog.desc2", key));

                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            }
    );

    public static final DeferredItem<Item> DATA_SHARD_INFOLOG_CHROMATOPHORE_FIELD_NOTE = ITEMS.register("data_shard_infolog_chromatophore_field_note",
            () -> new InfologDataShardItem(new Item.Properties().stacksTo(1).component(DataComponents.DYED_COLOR,
                    new DyedItemColor(0x303030, false)),

                    "item.createcybernetics.data_shard_infolog_chromatophore_field_note.title",
                    "item.createcybernetics.data_shard_infolog_chromatophore_field_note.text",

                    true
            ) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_infolog.desc"));

                    Component key = com.perigrine3.createcybernetics.client.ModKeyMappings.INFOLOG.get().getTranslatedKeyMessage();
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_infolog.desc2", key));

                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            }
    );

    public static final DeferredItem<Item> DATA_SHARD_INFOLOG_THE_RAISE = ITEMS.register("data_shard_infolog_the_raise",
            () -> new InfologDataShardItem(new Item.Properties().stacksTo(1).component(DataComponents.DYED_COLOR,
                    new DyedItemColor(0x303030, false)),

                    "item.createcybernetics.data_shard_infolog_the_raise.title",
                    "item.createcybernetics.data_shard_infolog_the_raise.text",

                    true
            ) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_infolog.desc"));

                    Component key = com.perigrine3.createcybernetics.client.ModKeyMappings.INFOLOG.get().getTranslatedKeyMessage();
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_infolog.desc2", key));

                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            }
    );

    public static final DeferredItem<Item> DATA_SHARD_INFOLOG_OMNISCIENT_OPTICS_AD = ITEMS.register("data_shard_infolog_omniscient_optics_ad",
            () -> new InfologDataShardItem(new Item.Properties().stacksTo(1).component(DataComponents.DYED_COLOR,
                    new DyedItemColor(0x303030, false)),

                    "item.createcybernetics.data_shard_infolog_omniscient_optics_ad.title",
                    "item.createcybernetics.data_shard_infolog_omniscient_optics_ad.text",

                    true
            ) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_infolog.desc"));

                    Component key = com.perigrine3.createcybernetics.client.ModKeyMappings.INFOLOG.get().getTranslatedKeyMessage();
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_infolog.desc2", key));

                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            }
    );

    public static final DeferredItem<Item> DATA_SHARD_INFOLOG_PRIVATE_WARD_REPORT = ITEMS.register("data_shard_infolog_private_ward_report",
            () -> new InfologDataShardItem(new Item.Properties().stacksTo(1).component(DataComponents.DYED_COLOR,
                    new DyedItemColor(0x303030, false)),

                    "item.createcybernetics.data_shard_infolog_private_ward_report.title",
                    "item.createcybernetics.data_shard_infolog_private_ward_report.text",

                    true
            ) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_infolog.desc"));

                    Component key = com.perigrine3.createcybernetics.client.ModKeyMappings.INFOLOG.get().getTranslatedKeyMessage();
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_infolog.desc2", key));

                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            }
    );

    public static final DeferredItem<Item> DATA_SHARD_INFOLOG_DRILLFIST_AD = ITEMS.register("data_shard_infolog_drillfist_ad",
            () -> new InfologDataShardItem(new Item.Properties().stacksTo(1).component(DataComponents.DYED_COLOR,
                    new DyedItemColor(0x303030, false)),

                    "item.createcybernetics.data_shard_infolog_drillfist_ad.title",
                    "item.createcybernetics.data_shard_infolog_drillfist_ad.text",

                    true
            ) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_infolog.desc"));

                    Component key = com.perigrine3.createcybernetics.client.ModKeyMappings.INFOLOG.get().getTranslatedKeyMessage();
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_infolog.desc2", key));

                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            }
    );

    public static final DeferredItem<Item> DATA_SHARD_INFOLOG_IDEM_ARTICLE = ITEMS.register("data_shard_infolog_idem_article",
            () -> new InfologDataShardItem(new Item.Properties().stacksTo(1).component(DataComponents.DYED_COLOR,
                    new DyedItemColor(0x303030, false)),

                    "item.createcybernetics.data_shard_infolog_idem_article.title",
                    "item.createcybernetics.data_shard_infolog_idem_article.text",

                    true
            ) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_infolog.desc"));

                    Component key = com.perigrine3.createcybernetics.client.ModKeyMappings.INFOLOG.get().getTranslatedKeyMessage();
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_infolog.desc2", key));

                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            }
    );

    public static final DeferredItem<Item> DATA_SHARD_INFOLOG_MOM_IM_FINE = ITEMS.register("data_shard_infolog_mom_im_fine",
            () -> new InfologDataShardItem(new Item.Properties().stacksTo(1).component(DataComponents.DYED_COLOR,
                    new DyedItemColor(0x303030, false)),

                    "item.createcybernetics.data_shard_infolog_mom_im_fine.title",
                    "item.createcybernetics.data_shard_infolog_mom_im_fine.text",

                    true
            ) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_infolog.desc"));

                    Component key = com.perigrine3.createcybernetics.client.ModKeyMappings.INFOLOG.get().getTranslatedKeyMessage();
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_infolog.desc2", key));

                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            }
    );

    public static final DeferredItem<Item> DATA_SHARD_INFOLOG_CYBERDECK_SECURITY_BRIEF = ITEMS.register("data_shard_infolog_cyberdeck_security_brief",
            () -> new InfologDataShardItem(new Item.Properties().stacksTo(1).component(DataComponents.DYED_COLOR,
                    new DyedItemColor(0x303030, false)),

                    "item.createcybernetics.data_shard_infolog_cyberdeck_security_brief.title",
                    "item.createcybernetics.data_shard_infolog_cyberdeck_security_brief.text",

                    true
            ) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_infolog.desc"));

                    Component key = com.perigrine3.createcybernetics.client.ModKeyMappings.INFOLOG.get().getTranslatedKeyMessage();
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_infolog.desc2", key));

                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            }
    );

    public static final DeferredItem<Item> DATA_SHARD_INFOLOG_LUNCH_BREAK = ITEMS.register("data_shard_infolog_lunch_break",
            () -> new InfologDataShardItem(new Item.Properties().stacksTo(1).component(DataComponents.DYED_COLOR,
                    new DyedItemColor(0x303030, false)),

                    "item.createcybernetics.data_shard_infolog_lunch_break.title",
                    "item.createcybernetics.data_shard_infolog_lunch_break.text",

                    true
            ) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_infolog.desc"));

                    Component key = com.perigrine3.createcybernetics.client.ModKeyMappings.INFOLOG.get().getTranslatedKeyMessage();
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_infolog.desc2", key));

                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            }
    );

    public static final DeferredItem<Item> DATA_SHARD_INFOLOG_DATURA = ITEMS.register("data_shard_infolog_datura",
            () -> new InfologDataShardItem(new Item.Properties().stacksTo(1).component(DataComponents.DYED_COLOR,
                    new DyedItemColor(0x303030, false)),

                    "item.createcybernetics.data_shard_infolog_datura.title",
                    "item.createcybernetics.data_shard_infolog_datura.text",

                    true
            ) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_infolog.desc"));

                    Component key = com.perigrine3.createcybernetics.client.ModKeyMappings.INFOLOG.get().getTranslatedKeyMessage();
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_infolog.desc2", key));

                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            }
    );

    public static final DeferredItem<Item> DATA_SHARD_INFOLOG_QUICKHACK_TUTORIAL = ITEMS.register("data_shard_infolog_quickhack_tutorial",
            () -> new InfologDataShardItem(new Item.Properties().stacksTo(1).component(DataComponents.DYED_COLOR,
                    new DyedItemColor(0x94fc03f, false)),

                    "item.createcybernetics.data_shard_infolog_quickhack_tutorial.title",
                    "item.createcybernetics.data_shard_infolog_quickhack_tutorial.text",

                    true
            ) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_infolog.desc"));

                    Component key = com.perigrine3.createcybernetics.client.ModKeyMappings.INFOLOG.get().getTranslatedKeyMessage();
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_infolog.desc2", key));

                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            }
    );

    public static final DeferredItem<Item> DATA_SHARD_INFOLOG_WAIT_YOUR_TURN = ITEMS.register("data_shard_infolog_wait_your_turn",
            () -> new InfologDataShardItem(new Item.Properties().stacksTo(1).component(DataComponents.DYED_COLOR,
                    new DyedItemColor(0x303030, false)),

                    "item.createcybernetics.data_shard_infolog_wait_your_turn.title",
                    "item.createcybernetics.data_shard_infolog_wait_your_turn.text",

                    true
            ) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_infolog.desc"));

                    Component key = com.perigrine3.createcybernetics.client.ModKeyMappings.INFOLOG.get().getTranslatedKeyMessage();
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_infolog.desc2", key));

                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            }
    );

    public static final DeferredItem<Item> DATA_SHARD_INFOLOG_THE_BREACH = ITEMS.register("data_shard_infolog_the_breach",
            () -> new InfologDataShardItem(new Item.Properties().stacksTo(1).component(DataComponents.DYED_COLOR,
                    new DyedItemColor(0x303030, false)),

                    "item.createcybernetics.data_shard_infolog_the_breach.title",
                    "item.createcybernetics.data_shard_infolog_the_breach.text",

                    true
            ) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_infolog.desc"));

                    Component key = com.perigrine3.createcybernetics.client.ModKeyMappings.INFOLOG.get().getTranslatedKeyMessage();
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_infolog.desc2", key));

                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            }
    );

    public static final DeferredItem<Item> DATA_SHARD_INFOLOG_AI_CHAT = ITEMS.register("data_shard_infolog_ai_chat",
            () -> new InfologDataShardItem(new Item.Properties().stacksTo(1).component(DataComponents.DYED_COLOR,
                    new DyedItemColor(0x303030, false)),

                    "item.createcybernetics.data_shard_infolog_ai_chat.title",
                    "item.createcybernetics.data_shard_infolog_ai_chat.text",

                    true
            ) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_infolog.desc"));

                    Component key = com.perigrine3.createcybernetics.client.ModKeyMappings.INFOLOG.get().getTranslatedKeyMessage();
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_infolog.desc2", key));

                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            }
    );

    public static final DeferredItem<Item> DATA_SHARD_INFOLOG_CYBERPSYCHO_TRANSCRIPT = ITEMS.register("data_shard_infolog_cyberpsycho_transcript",
            () -> new InfologDataShardItem(new Item.Properties().stacksTo(1).component(DataComponents.DYED_COLOR,
                    new DyedItemColor(0x303030, false)),

                    "item.createcybernetics.data_shard_infolog_cyberpsycho_transcript.title",
                    "item.createcybernetics.data_shard_infolog_cyberpsycho_transcript.text",

                    true
            ) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_infolog.desc"));

                    Component key = com.perigrine3.createcybernetics.client.ModKeyMappings.INFOLOG.get().getTranslatedKeyMessage();
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_infolog.desc2", key));

                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            }
    );

    public static final DeferredItem<Item> DATA_SHARD_INFOLOG_CPU_INSTALL = ITEMS.register("data_shard_infolog_cpu_install",
            () -> new InfologDataShardItem(new Item.Properties().stacksTo(1).component(DataComponents.DYED_COLOR,
                    new DyedItemColor(0x303030, false)),

                    "item.createcybernetics.data_shard_infolog_cpu_install.title",
                    "item.createcybernetics.data_shard_infolog_cpu_install.text",

                    true
            ) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_infolog.desc"));

                    Component key = com.perigrine3.createcybernetics.client.ModKeyMappings.INFOLOG.get().getTranslatedKeyMessage();
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_infolog.desc2", key));

                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            }
    );








//GAME SHARDS
    public static final DeferredItem<Item> DATA_SHARD_GAME_MINESWEEPER = ITEMS.register("data_shard_game_minesweeper",
            () -> new GameShardItem(new Item.Properties().stacksTo(1).component(DataComponents.DYED_COLOR,
                    new DyedItemColor(0xC77DFF, false)), "minesweeper",
                    "gui.createcybernetics.computer.game.minesweeper") {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_game_minesweeper.desc").withStyle(ChatFormatting.GRAY));

                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });

    public static final DeferredItem<Item> DATA_SHARD_GAME_CHESS = ITEMS.register("data_shard_game_chess",
            () -> new GameShardItem(new Item.Properties().stacksTo(1).component(DataComponents.DYED_COLOR,
                    new DyedItemColor(0xC77DFF, false)), "chess",
                    "gui.createcybernetics.computer.game.chess") {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("item.createcybernetics.data_shard_game_chess.desc").withStyle(ChatFormatting.GRAY));

                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });




//MICROPROGRAMS
    public static final DeferredItem<Item> MICROPROGRAM_SYSTEM_ALL = registerMicroprogram("microprogram_system_all", MicroprogramRegistry.SYSTEM_ALL);
    public static final DeferredItem<Item> MICROPROGRAM_PROCESSOR = registerMicroprogram("microprogram_processor", MicroprogramRegistry.PROCESSOR);
    public static final DeferredItem<Item> MICROPROGRAM_CYBEROPTICS = registerMicroprogram("microprogram_cyberoptics", MicroprogramRegistry.CYBEROPTICS);
    public static final DeferredItem<Item> MICROPROGRAM_AUDITORY = registerMicroprogram("microprogram_auditory", MicroprogramRegistry.AUDITORY);
    public static final DeferredItem<Item> MICROPROGRAM_MOTOR_CONTROL = registerMicroprogram("microprogram_motor_control", MicroprogramRegistry.MOTOR_CONTROL);
    public static final DeferredItem<Item> MICROPROGRAM_ARMS = registerMicroprogram("microprogram_arms", MicroprogramRegistry.ARMS);
    public static final DeferredItem<Item> MICROPROGRAM_LEGS = registerMicroprogram("microprogram_legs", MicroprogramRegistry.LEGS);
    public static final DeferredItem<Item> MICROPROGRAM_INTERNAL_ORGANS = registerMicroprogram("microprogram_internal_organs", MicroprogramRegistry.INTERNAL_ORGANS);
    public static final DeferredItem<Item> MICROPROGRAM_SKELETON = registerMicroprogram("microprogram_skeleton", MicroprogramRegistry.SKELETON);
    public static final DeferredItem<Item> MICROPROGRAM_DERMAL = registerMicroprogram("microprogram_dermal", MicroprogramRegistry.DERMAL);
    public static final DeferredItem<Item> MICROPROGRAM_POWER_SYSTEM = registerMicroprogram("microprogram_power_system", MicroprogramRegistry.POWER_SYSTEM);
    public static final DeferredItem<Item> MICROPROGRAM_CYBERDECK = registerMicroprogram("microprogram_cyberdeck", MicroprogramRegistry.CYBERDECK);
    public static final DeferredItem<Item> MICROPROGRAM_NEURAL_SYSTEM = registerMicroprogram("microprogram_neural_system", MicroprogramRegistry.NEURAL_SYSTEM);
    public static final DeferredItem<Item> MICROPROGRAM_ACTIVE_CYBERWARE = registerMicroprogram("microprogram_active_cyberware", MicroprogramRegistry.ACTIVE_CYBERWARE);
    public static final DeferredItem<Item> MICROPROGRAM_HIGHEST_DRAW = registerMicroprogram("microprogram_highest_draw", MicroprogramRegistry.HIGHEST_DRAW);
    public static final DeferredItem<Item> MICROPROGRAM_RANDOM_IMPLANT = registerMicroprogram("microprogram_random_implant", MicroprogramRegistry.RANDOM_IMPLANT);

    public static final DeferredItem<Item> MICROPROGRAM_REBOOT = registerMicroprogram("microprogram_reboot", MicroprogramRegistry.REBOOT);
    public static final DeferredItem<Item> MICROPROGRAM_OVERCLOCK = registerMicroprogram("microprogram_overclock", MicroprogramRegistry.OVERCLOCK);
    public static final DeferredItem<Item> MICROPROGRAM_DRAIN = registerMicroprogram("microprogram_drain", MicroprogramRegistry.DRAIN);
    public static final DeferredItem<Item> MICROPROGRAM_SPIKE = registerMicroprogram("microprogram_spike", MicroprogramRegistry.SPIKE);
    public static final DeferredItem<Item> MICROPROGRAM_BURNOUT = registerMicroprogram("microprogram_burnout", MicroprogramRegistry.BURNOUT);
    public static final DeferredItem<Item> MICROPROGRAM_TOGGLE_OFF = registerMicroprogram("microprogram_toggle_off", MicroprogramRegistry.TOGGLE_OFF);
    public static final DeferredItem<Item> MICROPROGRAM_TOGGLE_ON = registerMicroprogram("microprogram_toggle_on", MicroprogramRegistry.TOGGLE_ON);
    public static final DeferredItem<Item> MICROPROGRAM_MALFUNCTION = registerMicroprogram("microprogram_malfunction", MicroprogramRegistry.MALFUNCTION);
    public static final DeferredItem<Item> MICROPROGRAM_LOOP = registerMicroprogram("microprogram_loop", MicroprogramRegistry.LOOP);
    public static final DeferredItem<Item> MICROPROGRAM_PING = registerMicroprogram("microprogram_ping", MicroprogramRegistry.PING);
    public static final DeferredItem<Item> MICROPROGRAM_TRACE = registerMicroprogram("microprogram_trace", MicroprogramRegistry.TRACE);
    public static final DeferredItem<Item> MICROPROGRAM_CORRUPT = registerMicroprogram("microprogram_corrupt", MicroprogramRegistry.CORRUPT);
    public static final DeferredItem<Item> MICROPROGRAM_PURGE = registerMicroprogram("microprogram_purge", MicroprogramRegistry.PURGE);
    public static final DeferredItem<Item> MICROPROGRAM_DESYNC = registerMicroprogram("microprogram_desync", MicroprogramRegistry.DESYNC);

    public static final DeferredItem<Item> MICROPROGRAM_DURATION_PLUS = registerMicroprogram("microprogram_duration_plus", MicroprogramRegistry.DURATION_PLUS);
    public static final DeferredItem<Item> MICROPROGRAM_DURATION_PLUS_PLUS = registerMicroprogram("microprogram_duration_plus_plus", MicroprogramRegistry.DURATION_PLUS_PLUS);
    public static final DeferredItem<Item> MICROPROGRAM_INTENSITY_PLUS = registerMicroprogram("microprogram_intensity_plus", MicroprogramRegistry.INTENSITY_PLUS);
    public static final DeferredItem<Item> MICROPROGRAM_EFFICIENCY = registerMicroprogram("microprogram_efficiency", MicroprogramRegistry.EFFICIENCY);
    public static final DeferredItem<Item> MICROPROGRAM_DELAY = registerMicroprogram("microprogram_delay", MicroprogramRegistry.DELAY);
    public static final DeferredItem<Item> MICROPROGRAM_REPEAT = registerMicroprogram("microprogram_repeat", MicroprogramRegistry.REPEAT);
    public static final DeferredItem<Item> MICROPROGRAM_CASCADE = registerMicroprogram("microprogram_cascade", MicroprogramRegistry.CASCADE);
    public static final DeferredItem<Item> MICROPROGRAM_SILENT = registerMicroprogram("microprogram_silent", MicroprogramRegistry.SILENT);
    public static final DeferredItem<Item> MICROPROGRAM_PERSISTENT = registerMicroprogram("microprogram_persistent", MicroprogramRegistry.PERSISTENT);
    public static final DeferredItem<Item> MICROPROGRAM_RANDOMIZE = registerMicroprogram("microprogram_randomize", MicroprogramRegistry.RANDOMIZE);
    public static final DeferredItem<Item> MICROPROGRAM_CONTAGION = registerMicroprogram("microprogram_contagion", MicroprogramRegistry.CONTAGION);


    //MACROPROGRAMS
    public static final DeferredItem<Item> MACROPROGRAM_DATA_MINER = registerMacroprogram("macroprogram_data_miner", MacroprogramRegistry.DATA_MINER);
    public static final DeferredItem<Item> MACROPROGRAM_BIT_CRUSHER = registerMacroprogram("macroprogram_bit_crusher", MacroprogramRegistry.BIT_CRUSHER);
    public static final DeferredItem<Item> MACROPROGRAM_STACK_OVERFLOW = registerMacroprogram("macroprogram_stack_overflow", MacroprogramRegistry.STACK_OVERFLOW);
    public static final DeferredItem<Item> MACROPROGRAM_ICEBREAKER = registerMacroprogram("macroprogram_icebreaker", MacroprogramRegistry.ICEBREAKER);
    public static final DeferredItem<Item> MACROPROGRAM_PING = registerMacroprogram("macroprogram_ping", MacroprogramRegistry.PING);
    public static final DeferredItem<Item> MACROPROGRAM_DDOS = registerMacroprogram("macroprogram_ddos", MacroprogramRegistry.DDOS);
    public static final DeferredItem<Item> MACROPROGRAM_BANHAMMER = registerMacroprogram("macroprogram_banhammer", MacroprogramRegistry.BANHAMMER);
    public static final DeferredItem<Item> MACROPROGRAM_FIREWALL = registerMacroprogram("macroprogram_firewall", MacroprogramRegistry.FIREWALL);
    public static final DeferredItem<Item> MACROPROGRAM_BASIC_ARMOR = registerMacroprogram("macroprogram_basic_armor", MacroprogramRegistry.BASIC_ARMOR);
    public static final DeferredItem<Item> MACROPROGRAM_INTERMEDIATE_ARMOR = registerMacroprogram("macroprogram_intermediate_armor", MacroprogramRegistry.INTERMEDIATE_ARMOR);
    public static final DeferredItem<Item> MACROPROGRAM_ADVANCED_ARMOR = registerMacroprogram("macroprogram_advanced_armor", MacroprogramRegistry.ADVANCED_ARMOR);
    public static final DeferredItem<Item> MACROPROGRAM_ADMINISTRATOR_ACCESS_ARMOR = registerMacroprogram("macroprogram_administrator_access_armor", MacroprogramRegistry.ADMINISTRATOR_ACCESS_ARMOR);

    public static final DeferredItem<Item> MACROPROGRAM_PACKET_SNIFFER = registerMacroprogram("macroprogram_packet_sniffer", MacroprogramRegistry.PACKET_SNIFFER);
    public static final DeferredItem<Item> MACROPROGRAM_VPN_SHROUD = registerMacroprogram("macroprogram_vpn_shroud", MacroprogramRegistry.VPN_SHROUD);

    //MATERIALIZED MACROPROGRAMS
    public static final DeferredItem<Item> MATERIALIZED_DATA_MINER = ITEMS.register("materialized_data_miner",
            () -> new MaterializedDataMinerItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> MATERIALIZED_BIT_CRUSHER = ITEMS.register("materialized_bit_crusher",
            () -> new MaterializedBitCrusherItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> MATERIALIZED_STACK_OVERFLOW = ITEMS.register("materialized_stack_overflow",
            () -> new MaterializedStackOverflowItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> MATERIALIZED_ICEBREAKER = ITEMS.register("materialized_icebreaker",
            () -> new MaterializedICEBreakerItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> MATERIALIZED_PING = ITEMS.register("materialized_ping",
            () -> new MaterializedPingItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> MATERIALIZED_DDOS = ITEMS.register("materialized_ddos",
            () -> new MaterializedDDoSItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> MATERIALIZED_BANHAMMER = ITEMS.register("materialized_banhammer",
            () -> new MaterializedBanHammerItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> MATERIALIZED_FIREWALL = ITEMS.register("materialized_firewall",
            () -> new MaterializedFirewallItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> MACROPROGRAM_RECOVERY = ITEMS.register("macroprogram_recovery",
            () -> new MacroprogramItem(new Item.Properties().stacksTo(1), MacroprogramRegistry.RECOVERY));
    public static final DeferredItem<Item> MACROPROGRAM_BASIC_ARMOR_HELMET = ITEMS.register("macroprogram_basic_armor_helmet",
            () -> new MaterializedMacroprogramArmorItem(ModMacroprogramArmorMaterials.BASIC, ArmorItem.Type.HELMET, new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> MACROPROGRAM_BASIC_ARMOR_CHESTPLATE = ITEMS.register("macroprogram_basic_armor_chestplate",
            () -> new MaterializedMacroprogramArmorItem(ModMacroprogramArmorMaterials.BASIC, ArmorItem.Type.CHESTPLATE, new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> MACROPROGRAM_BASIC_ARMOR_LEGGINGS = ITEMS.register("macroprogram_basic_armor_leggings",
            () -> new MaterializedMacroprogramArmorItem(ModMacroprogramArmorMaterials.BASIC, ArmorItem.Type.LEGGINGS, new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> MACROPROGRAM_BASIC_ARMOR_BOOTS = ITEMS.register("macroprogram_basic_armor_boots",
            () -> new MaterializedMacroprogramArmorItem(ModMacroprogramArmorMaterials.BASIC, ArmorItem.Type.BOOTS, new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> MACROPROGRAM_INTERMEDIATE_ARMOR_HELMET = ITEMS.register("macroprogram_intermediate_armor_helmet",
            () -> new MaterializedMacroprogramArmorItem(ModMacroprogramArmorMaterials.INTERMEDIATE, ArmorItem.Type.HELMET, new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> MACROPROGRAM_INTERMEDIATE_ARMOR_CHESTPLATE = ITEMS.register("macroprogram_intermediate_armor_chestplate",
            () -> new MaterializedMacroprogramArmorItem(ModMacroprogramArmorMaterials.INTERMEDIATE, ArmorItem.Type.CHESTPLATE, new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> MACROPROGRAM_INTERMEDIATE_ARMOR_LEGGINGS = ITEMS.register("macroprogram_intermediate_armor_leggings",
            () -> new MaterializedMacroprogramArmorItem(ModMacroprogramArmorMaterials.INTERMEDIATE, ArmorItem.Type.LEGGINGS, new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> MACROPROGRAM_INTERMEDIATE_ARMOR_BOOTS = ITEMS.register("macroprogram_intermediate_armor_boots",
            () -> new MaterializedMacroprogramArmorItem(ModMacroprogramArmorMaterials.INTERMEDIATE, ArmorItem.Type.BOOTS, new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> MACROPROGRAM_ADVANCED_ARMOR_HELMET = ITEMS.register("macroprogram_advanced_armor_helmet",
            () -> new MaterializedMacroprogramArmorItem(ModMacroprogramArmorMaterials.ADVANCED, ArmorItem.Type.HELMET, new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> MACROPROGRAM_ADVANCED_ARMOR_CHESTPLATE = ITEMS.register("macroprogram_advanced_armor_chestplate",
            () -> new MaterializedMacroprogramArmorItem(ModMacroprogramArmorMaterials.ADVANCED, ArmorItem.Type.CHESTPLATE, new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> MACROPROGRAM_ADVANCED_ARMOR_LEGGINGS = ITEMS.register("macroprogram_advanced_armor_leggings",
            () -> new MaterializedMacroprogramArmorItem(ModMacroprogramArmorMaterials.ADVANCED, ArmorItem.Type.LEGGINGS, new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> MACROPROGRAM_ADVANCED_ARMOR_BOOTS = ITEMS.register("macroprogram_advanced_armor_boots",
            () -> new MaterializedMacroprogramArmorItem(ModMacroprogramArmorMaterials.ADVANCED, ArmorItem.Type.BOOTS, new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> MACROPROGRAM_ADMINISTRATOR_ACCESS_ARMOR_HELMET = ITEMS.register("macroprogram_administrator_access_armor_helmet",
            () -> new MaterializedMacroprogramArmorItem(ModMacroprogramArmorMaterials.ADMINISTRATOR_ACCESS, ArmorItem.Type.HELMET, new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> MACROPROGRAM_ADMINISTRATOR_ACCESS_ARMOR_CHESTPLATE = ITEMS.register("macroprogram_administrator_access_armor_chestplate",
            () -> new MaterializedMacroprogramArmorItem(ModMacroprogramArmorMaterials.ADMINISTRATOR_ACCESS, ArmorItem.Type.CHESTPLATE, new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> MACROPROGRAM_ADMINISTRATOR_ACCESS_ARMOR_LEGGINGS = ITEMS.register("macroprogram_administrator_access_armor_leggings",
            () -> new MaterializedMacroprogramArmorItem(ModMacroprogramArmorMaterials.ADMINISTRATOR_ACCESS, ArmorItem.Type.LEGGINGS, new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> MACROPROGRAM_ADMINISTRATOR_ACCESS_ARMOR_BOOTS = ITEMS.register("macroprogram_administrator_access_armor_boots",
            () -> new MaterializedMacroprogramArmorItem(ModMacroprogramArmorMaterials.ADMINISTRATOR_ACCESS, ArmorItem.Type.BOOTS, new Item.Properties().stacksTo(1)));





    public static final DeferredItem<Item> COOKED_BRAIN = ITEMS.register("cooked_brain",
            () -> new Item(new Item.Properties().stacksTo(64).food(ModFoods.COOKED_BRAIN)));
    public static final DeferredItem<Item> COOKED_LIVER = ITEMS.register("cooked_liver",
            () -> new Item(new Item.Properties().stacksTo(64).food(ModFoods.COOKED_LIVER)));
    public static final DeferredItem<Item> BONE_MARROW = ITEMS.register("bone_marrow",
            () -> new Item(new Item.Properties().stacksTo(64).food(ModFoods.BONE_MARROW)));
    public static final DeferredItem<Item> COOKED_HEART = ITEMS.register("cooked_heart",
            () -> new Item(new Item.Properties().stacksTo(64).food(ModFoods.COOKED_HEART)));
    public static final DeferredItem<Item> ANDOUILLE_SAUSAGE = registerIfLoaded("farmersdelight", "andouille_sausage",
            () -> new Item(new Item.Properties().stacksTo(64).food(ModFoods.ANDOUILLE_SAUSAGE)));
    public static final DeferredItem<Item> ROASTED_ANDOUILLE = registerIfLoaded("farmersdelight", "roasted_andouille",
            () -> new Item(new Item.Properties().stacksTo(64).food(ModFoods.ROASTED_ANDOUILLE)));
    public static final DeferredItem<Item> GROUND_OFFAL = registerIfLoaded("farmersdelight", "ground_offal",
            () -> new Item(new Item.Properties().stacksTo(64).food(ModFoods.GROUND_OFFAL)));
    public static final DeferredItem<Item> BRAIN_STEW = registerIfLoaded("farmersdelight", "brain_stew",
            () -> new BrainStewItem(new Item.Properties().stacksTo(64).food(ModFoods.BRAIN_STEW)));




//BASIC COMPONENTS
    public static final DeferredItem<Item> COMPONENT_ACTUATOR = ITEMS.register("component_actuator",
            () -> new Item(new Item.Properties()) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.basiccomponent_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }});
    public static final DeferredItem<Item> COMPONENT_FIBEROPTICS = ITEMS.register("component_fiberoptics",
            () -> new Item(new Item.Properties()) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.basiccomponent_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }});
    public static final DeferredItem<Item> COMPONENT_WIRING = ITEMS.register("component_wiring",
            () -> new Item(new Item.Properties()) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.basiccomponent_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }});
    public static final DeferredItem<Item> COMPONENT_DIODES = ITEMS.register("component_diodes",
            () -> new Item(new Item.Properties()) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.basiccomponent_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }});
    public static final DeferredItem<Item> COMPONENT_PLATING = ITEMS.register("component_plating",
            () -> new Item(new Item.Properties()) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.basiccomponent_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }});
    public static final DeferredItem<Item> COMPONENT_GRAPHICSCARD = ITEMS.register("component_graphicscard",
            () -> new Item(new Item.Properties()) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.basiccomponent_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }});
    public static final DeferredItem<Item> COMPONENT_SSD = ITEMS.register("component_ssd",
            () -> new Item(new Item.Properties()) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.basiccomponent_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }});
    public static final DeferredItem<Item> COMPONENT_STORAGE = ITEMS.register("component_storage",
            () -> new Item(new Item.Properties()) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.basiccomponent_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }});
    public static final DeferredItem<Item> COMPONENT_SYNTHNERVES = ITEMS.register("component_synthnerves",
            () -> new Item(new Item.Properties()) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.basiccomponent_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }});
    public static final DeferredItem<Item> COMPONENT_MESH = ITEMS.register("component_mesh",
            () -> new Item(new Item.Properties()) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.basiccomponent_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }});
    public static final DeferredItem<Item> COMPONENT_LED = registerIfNotLoaded("create", "component_led",
            () -> new Item(new Item.Properties()) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.basiccomponent_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }});
    public static final DeferredItem<Item> COMPONENT_TITANIUMROD = registerIfNotLoaded("create", "component_titaniumrod",
            () -> new Item(new Item.Properties()) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.basiccomponent_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }});




//BODY PARTS
    public static final DeferredItem<Item> BODYPART_RIGHTLEG = ITEMS.register("bodypart_rightleg",
            () -> new RightLegItem(new Item.Properties().stacksTo(1), 0) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetware_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> BODYPART_LEFTLEG = ITEMS.register("bodypart_leftleg",
            () -> new LeftLegItem(new Item.Properties().stacksTo(1), 0) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetware_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> BODYPART_RIGHTARM = ITEMS.register("bodypart_rightarm",
            () -> new RightArmItem(new Item.Properties().stacksTo(1), 0) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetware_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> BODYPART_LEFTARM = ITEMS.register("bodypart_leftarm",
            () -> new LeftArmItem(new Item.Properties().stacksTo(1), 0) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetware_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });

//INTERNAL WETWARE
    public static final DeferredItem<Item> BODYPART_SKELETON = ITEMS.register("bodypart_skeleton",
            () -> new SkeletonItem(new Item.Properties().stacksTo(16), 0) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetware_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> BODYPART_BRAIN = ITEMS.register("bodypart_brain",
            () -> new BrainItem(new Item.Properties().stacksTo(16).food(ModFoods.RAW_BRAIN), 0) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetware_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> BODYPART_EYEBALLS = ITEMS.register("bodypart_eyeballs",
            () -> new EyeballItem(new Item.Properties().stacksTo(16), 0) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetware_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> BODYPART_HEART = ITEMS.register("bodypart_heart",
            () -> new HeartItem(new Item.Properties().stacksTo(16).food(ModFoods.RAW_HEART), 0) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetware_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> BODYPART_LUNGS = ITEMS.register("bodypart_lungs",
            () -> new LungsItem(new Item.Properties().stacksTo(16), 0) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetware_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> BODYPART_LIVER = ITEMS.register("bodypart_liver",
            () -> new LiverItem(new Item.Properties().stacksTo(16).food(ModFoods.RAW_LIVER), 0) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetware_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> BODYPART_INTESTINES = ITEMS.register("bodypart_intestines",
            () -> new IntestinesItem(new Item.Properties().stacksTo(16), 0) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetware_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> BODYPART_MUSCLE = ITEMS.register("bodypart_muscle",
            () -> new MuscleItem(new Item.Properties().stacksTo(16), 0) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetware_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> BODYPART_SKIN = ITEMS.register("bodypart_skin",
            () -> new SkinItem(new Item.Properties().stacksTo(16), 0) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetware_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });

//ANIMAL BODYPARTS
    public static final DeferredItem<Item> BODYPART_GUARDIANRETINA = ITEMS.register("bodypart_guardianretina",
            () -> new Item(new Item.Properties().stacksTo(16)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetware_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> BODYPART_WARDENESOPHAGUS = ITEMS.register("bodypart_wardenesophagus",
            () -> new Item(new Item.Properties().stacksTo(16)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetware_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> BODYPART_GYROSCOPICBLADDER = ITEMS.register("bodypart_gyroscopicbladder",
            () -> new Item(new Item.Properties().stacksTo(16)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetware_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> BODYPART_SPINNERETTE = ITEMS.register("bodypart_spinnerette",
            () -> new Item(new Item.Properties().stacksTo(16)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetware_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> BODYPART_FIREGLAND = ITEMS.register("bodypart_firegland",
            () -> new Item(new Item.Properties().stacksTo(16)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetware_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> BODYPART_GILLS = ITEMS.register("bodypart_gills",
            () -> new Item(new Item.Properties().stacksTo(16)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetware_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> BODYPART_AXOLOTLMARROW = ITEMS.register("bodypart_axolotlmarrow",
            () -> new Item(new Item.Properties().stacksTo(16)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetware_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> BODYPART_DRAGONSCALE = ITEMS.register("bodypart_dragonscale",
            () -> new Item(new Item.Properties().stacksTo(16)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetware_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });

//SCULK PARTS
    public static final DeferredItem<Item> BODYPART_SCULKBRAIN = ITEMS.register("bodypart_sculkbrain",
            () -> new SculkedBrainItem(new Item.Properties().stacksTo(16), 5) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetware_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> BODYPART_SCULKLIVER = ITEMS.register("bodypart_sculkliver",
            () -> new SculkedLiverItem(new Item.Properties().stacksTo(16), 5) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetware_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> BODYPART_SCULKINTESTINES = ITEMS.register("bodypart_sculkintestines",
            () -> new SculkedIntestinesItem(new Item.Properties().stacksTo(16), 5) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetware_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> BODYPART_SCULKMUSCLE = ITEMS.register("bodypart_sculkmuscle",
            () -> new SculkedMuscleItem(new Item.Properties().stacksTo(16), 5) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetware_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> BODYPART_SCULKSKIN = ITEMS.register("bodypart_sculkskin",
            () -> new SculkedSkinItem(new Item.Properties().stacksTo(16), 5) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetware_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> BODYPART_SCULKRIGHTLEG = ITEMS.register("bodypart_sculkrightleg",
            () -> new SculkedRightLegItem(new Item.Properties().stacksTo(1), 5) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetware_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> BODYPART_SCULKLEFTLEG = ITEMS.register("bodypart_sculkleftleg",
            () -> new SculkedLeftLegItem(new Item.Properties().stacksTo(1), 5) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetware_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> BODYPART_SCULKRIGHTARM = ITEMS.register("bodypart_sculkrightarm",
            () -> new SculkedRightArmItem(new Item.Properties().stacksTo(1), 5) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetware_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> BODYPART_SCULKLEFTARM = ITEMS.register("bodypart_sculkleftarm",
            () -> new SculkedLeftArmItem(new Item.Properties().stacksTo(1), 5) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetware_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });



    //UPGRADE_TEMPLATES
    public static final DeferredItem<Item> COPPER_UPGRADE_TEMPLATE = ITEMS.register("copper_upgrade_template",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> IRON_UPGRADE_TEMPLATE = ITEMS.register("iron_upgrade_template",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> GOLD_UPGRADE_TEMPLATE = ITEMS.register("gold_upgrade_template",
            () -> new Item(new Item.Properties()));


//BASE CYBERWARE
    public static final DeferredItem<Item> BASECYBERWARE_RIGHTLEG = ITEMS.register("basecyberware_rightleg",
            () -> new CyberlegItem(new Item.Properties().stacksTo(1), 5, CyberwareSlot.RLEG) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.basecyberware_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.limb_replacement"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.basecyberware_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> BASECYBERWARE_LEFTLEG = ITEMS.register("basecyberware_leftleg",
            () -> new CyberlegItem(new Item.Properties().stacksTo(1), 5, CyberwareSlot.LLEG) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    if (Screen.hasShiftDown()) {
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.basecyberware_tooltip"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.limb_replacement"));
                    } else {
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.basecyberware_tooltip"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                    }
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> BASECYBERWARE_RIGHTARM = ITEMS.register("basecyberware_rightarm",
            () -> new CyberarmItem(new Item.Properties().stacksTo(1), 5, CyberwareSlot.RARM) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.basecyberware_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.limb_replacement"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.basecyberware_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> BASECYBERWARE_LEFTARM = ITEMS.register("basecyberware_leftarm",
            () -> new CyberarmItem(new Item.Properties().stacksTo(1), 5, CyberwareSlot.LARM) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    if (Screen.hasShiftDown()) {
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.basecyberware_tooltip"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.limb_replacement"));
                    } else {
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.basecyberware_tooltip"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                    }
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> BASECYBERWARE_LINEARFRAME = ITEMS.register("basecyberware_linearframe",
            () -> new LinearFrameItem(new Item.Properties().stacksTo(1), 15) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    if (Screen.hasShiftDown()) {
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.basecyberware_tooltip"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.skeleton_replacement"));
                    } else {
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.basecyberware_tooltip"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                    }
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> BASECYBERWARE_CYBEREYES = ITEMS.register("basecyberware_cybereyes",
            () -> new CybereyeItem(new Item.Properties().stacksTo(16), 5) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    if (Screen.hasShiftDown()) {
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.basecyberware_tooltip"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.eye_replacement"));
                    } else {
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.basecyberware_tooltip"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                    }
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });

    public static final DeferredItem<Item> BASECYBERWARE_RIGHTLEG_COPPERPLATED = ITEMS.register("basecyberware_rightleg_copperplated",
            () -> new CyberlegItem(new Item.Properties().stacksTo(1), 5, CyberwareSlot.RLEG) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    if (Screen.hasShiftDown()) {
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.basecyberware_tooltip"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.limb_replacement"));
                    } else {
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.basecyberware_tooltip"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                    }
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> BASECYBERWARE_LEFTLEG_COPPERPLATED = ITEMS.register("basecyberware_leftleg_copperplated",
            () -> new CyberlegItem(new Item.Properties().stacksTo(1), 5, CyberwareSlot.LLEG) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    if (Screen.hasShiftDown()) {
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.basecyberware_tooltip"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.limb_replacement"));
                    } else {
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.basecyberware_tooltip"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                    }
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> BASECYBERWARE_RIGHTARM_COPPERPLATED = ITEMS.register("basecyberware_rightarm_copperplated",
            () -> new CyberarmItem(new Item.Properties().stacksTo(1), 5, CyberwareSlot.RARM) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    if (Screen.hasShiftDown()) {
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.basecyberware_tooltip"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.limb_replacement"));
                    } else {
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.basecyberware_tooltip"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                    }
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> BASECYBERWARE_LEFTARM_COPPERPLATED = ITEMS.register("basecyberware_leftarm_copperplated",
            () -> new CyberarmItem(new Item.Properties().stacksTo(1), 5, CyberwareSlot.LARM) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    if (Screen.hasShiftDown()) {
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.basecyberware_tooltip"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.limb_replacement"));
                    } else {
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.basecyberware_tooltip"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                    }
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });

    public static final DeferredItem<Item> BASECYBERWARE_RIGHTLEG_IRONPLATED = ITEMS.register("basecyberware_rightleg_ironplated",
            () -> new CyberlegItem(new Item.Properties().stacksTo(1), 5, CyberwareSlot.RLEG) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    if (Screen.hasShiftDown()) {
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.basecyberware_tooltip"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.limb_replacement"));
                    } else {
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.basecyberware_tooltip"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                    }
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> BASECYBERWARE_LEFTLEG_IRONPLATED = ITEMS.register("basecyberware_leftleg_ironplated",
            () -> new CyberlegItem(new Item.Properties().stacksTo(1), 5, CyberwareSlot.LLEG) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    if (Screen.hasShiftDown()) {
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.basecyberware_tooltip"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.limb_replacement"));
                    } else {
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.basecyberware_tooltip"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                    }
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> BASECYBERWARE_RIGHTARM_IRONPLATED = ITEMS.register("basecyberware_rightarm_ironplated",
            () -> new CyberarmItem(new Item.Properties().stacksTo(1), 5, CyberwareSlot.RARM) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    if (Screen.hasShiftDown()) {
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.basecyberware_tooltip"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.limb_replacement"));
                    } else {
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.basecyberware_tooltip"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                    }
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> BASECYBERWARE_LEFTARM_IRONPLATED = ITEMS.register("basecyberware_leftarm_ironplated",
            () -> new CyberarmItem(new Item.Properties().stacksTo(1), 5, CyberwareSlot.LARM) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    if (Screen.hasShiftDown()) {
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.basecyberware_tooltip"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.limb_replacement"));
                    } else {
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.basecyberware_tooltip"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                    }
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });

    public static final DeferredItem<Item> BASECYBERWARE_RIGHTLEG_GOLDPLATED = ITEMS.register("basecyberware_rightleg_goldplated",
            () -> new CyberlegItem(new Item.Properties().stacksTo(1), 5, CyberwareSlot.RLEG) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    if (Screen.hasShiftDown()) {
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.basecyberware_tooltip"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.limb_replacement"));
                    } else {
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.basecyberware_tooltip"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                    }
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> BASECYBERWARE_LEFTLEG_GOLDPLATED = ITEMS.register("basecyberware_leftleg_goldplated",
            () -> new CyberlegItem(new Item.Properties().stacksTo(1), 5, CyberwareSlot.LLEG) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    if (Screen.hasShiftDown()) {
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.basecyberware_tooltip"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.limb_replacement"));
                    } else {
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.basecyberware_tooltip"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                    }
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> BASECYBERWARE_RIGHTARM_GOLDPLATED = ITEMS.register("basecyberware_rightarm_goldplated",
            () -> new CyberarmItem(new Item.Properties().stacksTo(1), 5, CyberwareSlot.RARM) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    if (Screen.hasShiftDown()) {
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.basecyberware_tooltip"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.limb_replacement"));
                    } else {
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.basecyberware_tooltip"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                    }
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> BASECYBERWARE_LEFTARM_GOLDPLATED = ITEMS.register("basecyberware_leftarm_goldplated",
            () -> new CyberarmItem(new Item.Properties().stacksTo(1), 5, CyberwareSlot.LARM) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    if (Screen.hasShiftDown()) {
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.basecyberware_tooltip"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.limb_replacement"));
                    } else {
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.basecyberware_tooltip"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                    }
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });



    //EYE UPGRADES
    public static final DeferredItem<Item> EYEUPGRADES_MONOVISION = ITEMS.register("eyeupgrades_monovision",
            () -> new MonovisionOpticsItem(new Item.Properties().stacksTo(64), 8) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    if (Screen.hasShiftDown()) {
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.eyeupgrades_tooltip"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.eyeupgrades_monovision.tooltip1"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.eye_replacement"));
                    } else {
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.eyeupgrades_tooltip"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                    }
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> EYEUPGRADES_MULTIOPTICS1 = ITEMS.register("eyeupgrades_multioptics1",
            () -> new MultiopticsItem(new Item.Properties().stacksTo(64), 8) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    if (Screen.hasShiftDown()) {
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.eyeupgrades_tooltip"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.eyeupgrades_multioptics1.tooltip1"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.eye_replacement"));
                    } else {
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.eyeupgrades_tooltip"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                    }
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> EYEUPGRADES_MULTIOPTICS2 = ITEMS.register("eyeupgrades_multioptics2",
            () -> new MultiopticsItem(new Item.Properties().stacksTo(64), 8) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    if (Screen.hasShiftDown()) {
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.eyeupgrades_tooltip"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.eyeupgrades_multioptics2.tooltip1"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.eye_replacement"));
                    } else {
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.eyeupgrades_tooltip"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                    }
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> EYEUPGRADES_MULTIOPTICS3 = ITEMS.register("eyeupgrades_multioptics3",
            () -> new MultiopticsItem(new Item.Properties().stacksTo(64), 8) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    if (Screen.hasShiftDown()) {
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.eyeupgrades_tooltip"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.eyeupgrades_multioptics3.tooltip1"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.eye_replacement"));
                    } else {
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.eyeupgrades_tooltip"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                    }
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> EYEUPGRADES_MULTIOPTICS4 = ITEMS.register("eyeupgrades_multioptics4",
            () -> new MultiopticsItem(new Item.Properties().stacksTo(64), 8) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    if (Screen.hasShiftDown()) {
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.eyeupgrades_tooltip"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.eyeupgrades_multioptics4.tooltip1"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.eye_replacement"));
                    } else {
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.eyeupgrades_tooltip"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                    }
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> EYEUPGRADES_HUDLENS = ITEMS.register("eyeupgrades_hudlens",
        () -> new HUDlensItem(new Item.Properties().stacksTo(64), 1) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.eyeupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.eyeupgrades_hudlens.tooltip1"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.eyeupgrades_hudlens.tooltip2"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.eyeupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> EYEUPGRADES_NAVIGATIONCHIP = ITEMS.register("eyeupgrades_navigationchip",
        () -> new NavigationChipItem(new Item.Properties().stacksTo(16), 3) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.eyeupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.eyeupgrades_navigationchip.tooltip1"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.eyeupgrades_navigationchip.tooltip2"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.eyeupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> EYEUPGRADES_HUDJACK = ITEMS.register("eyeupgrades_hudjack",
        () -> new HUDjackItem(new Item.Properties().stacksTo(16), 3) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.eyeupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.eyeupgrades_hudjack.tooltip1"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.eyeupgrades_hudjack.tooltip2"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.eyeupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> EYEUPGRADES_NIGHTVISION = ITEMS.register("eyeupgrades_nightvision",
        () -> new NightVisionModuleItem(new Item.Properties().stacksTo(16), 3) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.eyeupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.eyeupgrades_nightvision.tooltip1"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.eyeupgrades_nightvision.tooltip2"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.eyeupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> EYEUPGRADES_TARGETING = ITEMS.register("eyeupgrades_targeting",
        () -> new TargetingModuleItem(new Item.Properties().stacksTo(16), 3) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.eyeupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.eyeupgrades_targeting.tooltip1"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.eyeupgrades_targeting.tooltip2"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.eyeupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> EYEUPGRADES_UNDERWATERVISION = ITEMS.register("eyeupgrades_underwatervision",
        () -> new UnderwaterVisionModuleItem(new Item.Properties().stacksTo(16), 3) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.eyeupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.eyeupgrades_underwatervision.tooltip1"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.eyeupgrades_underwatervision.tooltip2"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.eyeupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> EYEUPGRADES_ZOOM = ITEMS.register("eyeupgrades_zoom",
        () -> new OpticZoomModuleItem(new Item.Properties().stacksTo(16), 3) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.eyeupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.eyeupgrades_zoom.tooltip1"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.eyeupgrades_zoom.tooltip2"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.eyeupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> EYEUPGRADES_TRAJECTORYCALCULATOR = ITEMS.register("eyeupgrades_trajectorycalculator",
        () -> new TrajectoryCalculatorModuleItem(new Item.Properties().stacksTo(16), 3) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.eyeupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.eyeupgrades_trajectorycalculator.tooltip1"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.eyeupgrades_trajectorycalculator.tooltip2"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.eyeupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> EYEUPGRADES_BIOMONITOR = ITEMS.register("eyeupgrades_biomonitor",
        () -> new BiomonitorModuleItem(new Item.Properties().stacksTo(16), 3) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.eyeupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.eyeupgrades_biomonitor.tooltip1"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.eyeupgrades_biomonitor.tooltip2"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.eyeupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });

//ARM UPGRADES
    public static final DeferredItem<Item> ARMUPGRADES_ARMCANNON = ITEMS.register("armupgrades_armcannon",
        () -> new ArmCannonItem(new Item.Properties().stacksTo(16), 7) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.armupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.armupgrades_armcannon.tooltip1"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.armupgrades_armcannon.tooltip2"));

                    Component key = ModKeyMappings.ARM_CANNON.get().getTranslatedKeyMessage();
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.armupgrades_armcannon.desc", key));
                    Component key2 = ModKeyMappings.ARM_CANNON_WHEEL.get().getTranslatedKeyMessage();
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.armupgrades_armcannon.desc2", key2));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.armupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> ARMUPGRADES_FLYWHEEL = ITEMS.register("armupgrades_flywheel",
        () -> new QuickdrawFlywheelItem(new Item.Properties().stacksTo(16), 3) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.armupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.armupgrades_flywheel.tooltip1"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.armupgrades_flywheel.tooltip2"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.armupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> ARMUPGRADES_CLAWS = ITEMS.register("armupgrades_claws",
        () -> new RetractableClawsItem(new Item.Properties().stacksTo(16), 5) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.armupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.armupgrades_claws.tooltip1"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.armupgrades_claws.tooltip2"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.armupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> ARMUPGRADES_CRAFTHANDS = ITEMS.register("armupgrades_crafthands",
        () -> new CraftingHandsItem(new Item.Properties().stacksTo(16), 5) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.armupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.armupgrades_crafthands.tooltip1"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.armupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> ARMUPGRADES_DRILLFIST = ITEMS.register("armupgrades_drillfist",
        () -> new DrillFistItem(new Item.Properties().stacksTo(16), 7) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.armupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.armupgrades_drillfist.tooltip1"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.armupgrades_drillfist.tooltip2"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.armupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> ARMUPGRADES_FIRESTARTER = ITEMS.register("armupgrades_firestarter",
        () -> new FirestarterItem(new Item.Properties().stacksTo(16), 1) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.armupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.armupgrades_firestarter.tooltip1"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.armupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> ARMUPGRADES_PNEUMATICWRIST = ITEMS.register("armupgrades_pneumaticwrist",
        () -> new PneumaticWristItem(new Item.Properties().stacksTo(16), 3) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.armupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.armupgrades_pneumaticwrist.tooltip1"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.armupgrades_pneumaticwrist.tooltip2"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.armupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> ARMUPGRADES_REINFORCEDKNUCKLES = ITEMS.register("armupgrades_reinforcedknuckles",
        () -> new ReinforcedKnucklesItem(new Item.Properties().stacksTo(16), 4) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.armupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.armupgrades_reinforcedknuckles.tooltip1"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.armupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> ARMUPGRADES_RIPPERCLAW = ITEMS.register("armupgrades_ripperclaw",
        () -> new RipperClawItem(new Item.Properties().stacksTo(16), 7) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.armupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.armupgrades_ripperclaw.tooltip1"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.armupgrades_ripperclaw.tooltip2"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.armupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> ARMUPGRADES_ARCCANNON = ITEMS.register("armupgrades_arccannon",
        () -> new ElectricArcCannonItem(new Item.Properties().stacksTo(1), 15) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.armupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.armupgrades_arccannon.tooltip1"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.armupgrades_arccannon.tooltip2"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.limb_replacement"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.armupgrades_arccannon.energy"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.armupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> ARMUPGRADES_HEAVYKNUCKLES = ITEMS.register("armupgrades_heavyknuckles",
        () -> new HeavyKnucklesItem(new Item.Properties().stacksTo(1), 15) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.armupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.armupgrades_heavyknuckles.tooltip1"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.armupgrades_heavyknuckles.tooltip2"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.armupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> ARMUPGRADES_MANTISBLADE_IRON = ITEMS.register("armupgrades_mantisblade_iron",
            () -> new MantisBladeItem(new Item.Properties().stacksTo(16), MantisBladeItem.Variant.IRON, 15));
    public static final DeferredItem<Item> ARMUPGRADES_MANTISBLADE_COPPER = ITEMS.register("armupgrades_mantisblade_copper",
            () -> new MantisBladeItem(new Item.Properties().stacksTo(16), MantisBladeItem.Variant.COPPER, 15));
    public static final DeferredItem<Item> ARMUPGRADES_MANTISBLADE_TITANIUM = ITEMS.register("armupgrades_mantisblade_titanium",
            () -> new MantisBladeItem(new Item.Properties().stacksTo(16), MantisBladeItem.Variant.TITANIUM, 15));
    public static final DeferredItem<Item> ARMUPGRADES_MANTISBLADE_GOLD = ITEMS.register("armupgrades_mantisblade_gold",
            () -> new MantisBladeItem(new Item.Properties().stacksTo(16), MantisBladeItem.Variant.GOLD, 15));
    public static final DeferredItem<Item> ARMUPGRADES_MANTISBLADE_DIAMOND = ITEMS.register("armupgrades_mantisblade_diamond",
            () -> new MantisBladeItem(new Item.Properties().stacksTo(16), MantisBladeItem.Variant.DIAMOND, 15));
    public static final DeferredItem<Item> ARMUPGRADES_MANTISBLADE_NETHERITE = ITEMS.register("armupgrades_mantisblade_netherite",
            () -> new MantisBladeItem(new Item.Properties().stacksTo(16), MantisBladeItem.Variant.NETHERITE, 15));

//LEG UPGRADES
    public static final DeferredItem<Item> LEGUPGRADES_METALDETECTOR = ITEMS.register("legupgrades_metaldetector",
        () -> new MetalDetectorItem(new Item.Properties().stacksTo(16), 3) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.legupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.legupgrades_metaldetector.tooltip1"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.legupgrades_metaldetector.tooltip2"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.legupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> LEGUPGRADES_ANKLEBRACERS = ITEMS.register("legupgrades_anklebracers",
        () -> new AnkleBracerItem(new Item.Properties().stacksTo(16), 5) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.legupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.legupgrades_anklebracers.tooltip1"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.legupgrades_anklebracers.tooltip2"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.legupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> LEGUPGRADES_JUMPBOOST = ITEMS.register("legupgrades_jumpboost",
        () -> new PneumaticCalvesItem(new Item.Properties().stacksTo(16), 5) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.legupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.legupgrades_jumpboost.tooltip1"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.legupgrades_jumpboost.tooltip2"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.legupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> LEGUPGRADES_PROPELLERS = ITEMS.register("legupgrades_propellers",
        () -> new PropellersItem(new Item.Properties().stacksTo(16), 3) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.legupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.legupgrades_propellers.tooltip1"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.legupgrades_propellers.tooltip2"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.legupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> LEGUPGRADES_SPURS = ITEMS.register("legupgrades_spurs",
        () -> new SpursItem(new Item.Properties().stacksTo(16), 2) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.legupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.legupgrades_spurs.tooltip1"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.legupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> LEGUPGRADES_OCELOTPAWS = ITEMS.register("legupgrades_ocelotpaws",
        () -> new OcelotPawsItem(new Item.Properties().stacksTo(16), 5) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.legupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.legupgrades_ocelotpaws.tooltip1"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.legupgrades_ocelotpaws.tooltip2"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.legupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });

//BONE UPGRADES
    public static final DeferredItem<Item> BONEUPGRADES_BONEBATTERY = ITEMS.register("boneupgrades_bonebattery",
        () -> new MarrowBatteryItem(new Item.Properties().stacksTo(16), 2) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.boneupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.boneupgrades_bonebattery.tooltip1"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.boneupgrades_bonebattery.tooltip2"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.boneupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> BONEUPGRADES_BONEFLEX = ITEMS.register("boneupgrades_boneflex",
        () -> new BoneflexItem(new Item.Properties().stacksTo(16), 3) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.boneupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.boneupgrades_boneflex.tooltip1"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.boneupgrades_boneflex.tooltip2"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.boneupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> BONEUPGRADES_BONELACING = ITEMS.register("boneupgrades_bonelacing",
        () -> new BonelacingItem(new Item.Properties().stacksTo(16), 5) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.boneupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.boneupgrades_bonelacing.tooltip1"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.boneupgrades_bonelacing.tooltip2"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.boneupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> BONEUPGRADES_CAPACITORFRAME = ITEMS.register("boneupgrades_capacitorframe",
        () -> new VoltaicCapacitorFrameItem(new Item.Properties().stacksTo(16), 10) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.boneupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.boneupgrades_capacitorframe.tooltip1"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.boneupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> BONEUPGRADES_ELYTRA = registerIfLoaded("caelus", "boneupgrades_elytra",
        () -> new DeployableElytraItem(new Item.Properties().stacksTo(1), 10) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.boneupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.boneupgrades_elytra.tooltip1"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.boneupgrades_elytra.tooltip2"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.boneupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> BONEUPGRADES_PIEZO = ITEMS.register("boneupgrades_piezo",
        () -> new PiezoelectricEnergyGeneratorItem(new Item.Properties().stacksTo(16), 3) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.boneupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.boneupgrades_piezo.tooltip1"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.boneupgrades_piezo.tooltip2"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.boneupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> BONEUPGRADES_SPINALINJECTOR = ITEMS.register("boneupgrades_spinalinjector",
        () -> new SpinalInjectorItem(new Item.Properties().stacksTo(16), 10) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.boneupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.boneupgrades_spinalinjector.tooltip1"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.boneupgrades_spinalinjector.tooltip2"));

                    Component key = ModKeyMappings.SPINAL_INJECTOR.get().getTranslatedKeyMessage();
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.boneupgrades_spinalinjector.desc", key));

                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.boneupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> BONEUPGRADES_SANDEVISTAN = ITEMS.register("boneupgrades_sandevistan",
        () -> new SandevistanItem(new Item.Properties().stacksTo(16), 10) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.boneupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.boneupgrades_sandevistan.tooltip1"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.boneupgrades_sandevistan.tooltip2"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.boneupgrades_sandevistan.tooltip3"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.boneupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> BONEUPGRADES_CYBERSKULL = ITEMS.register("boneupgrades_cyberskull",
        () -> new TitaniumSkullItem(new Item.Properties().stacksTo(16), 10) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.boneupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.boneupgrades_cyberskull.tooltip1"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.boneupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });

//BRAIN UPGRADES
    public static final DeferredItem<Item> BRAINUPGRADES_CYBERBRAIN = ITEMS.register("brainupgrades_cyberbrain",
        () -> new CerebralProcessingUnitItem(new Item.Properties().stacksTo(16), 12) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrade_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrades_cyberbrain.tooltip1"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brain_replacement"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrades_cyberbrain.ram"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrades_cyberbrain.ramregen"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrade_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> BRAINUPGRADES_EYEOFDEFENDER = ITEMS.register("brainupgrades_eyeofdefender",
        () -> new EyeOfDefenderItem(new Item.Properties().stacksTo(16), 8) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrade_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrades_eyeofdefender.tooltip1"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrades_eyeofdefender.tooltip2"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrades_eyeofdefender.ram"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrade_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> BRAINUPGRADES_CONSCIOUSNESSTRANSMITTER = registerIfLoaded("create_enchantment_industry","brainupgrades_consciousnesstransmitter",
            () -> new NeedlecasterItem(new Item.Properties().stacksTo(16), 5) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrade_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrades_consciousnesstransmitter.tooltip1"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrades_consciousnesstransmitter.tooltip2"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrade_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> BRAINUPGRADES_CORTICALSTACK = registerIfLoaded("create_enchantment_industry","brainupgrades_corticalstack",
            () -> new CorticalStackItem(new Item.Properties().stacksTo(16), 10) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrade_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrades_corticalstack.tooltip1"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrades_corticalstack.tooltip2"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrade_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> BRAINUPGRADES_ENDERJAMMER = ITEMS.register("brainupgrades_enderjammer",
        () -> new EnderJammerItem(new Item.Properties().stacksTo(16), 2) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrade_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrades_enderjammer.tooltip1"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrades_enderjammer.ram"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrades_enderjammer.ramregen"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrade_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> BRAINUPGRADES_MATRIX = ITEMS.register("brainupgrades_matrix",
        () -> new ThreatMatrixItem(new Item.Properties().stacksTo(16), 4) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrade_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrades_matrix.tooltip1"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrades_matrix.tooltip2"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrade_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> BRAINUPGRADES_NEURALCONTEXTUALIZER = ITEMS.register("brainupgrades_neuralcontextualizer",
        () -> new NeuralContextualizerItem(new Item.Properties().stacksTo(16), 3) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrade_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrades_neuralcontextualizer.tooltip1"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrades_neuralcontextualizer.tooltip2"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrade_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> BRAINUPGRADES_CYBERDECK = ITEMS.register("brainupgrades_cyberdeck",
        () -> new CyberdeckItem(new Item.Properties().stacksTo(16), 8) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrade_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrades_cyberdeck.tooltip1"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrades_cyberdeck.tooltip2"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrades_cyberdeck.ram"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrades_cyberdeck.ramregen"));

                    Component invKey = ModKeyMappings.CYBERDECK.get().getTranslatedKeyMessage();
                    Component wheelKey = ModKeyMappings.CYBERDECK_WHEEL.get().getTranslatedKeyMessage();
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrades_cyberdeck.desc1", invKey));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrades_cyberdeck.desc2", wheelKey));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrade_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> BRAINUPGRADES_IDEM = ITEMS.register("brainupgrades_idem",
        () -> new IDEMItem(new Item.Properties().stacksTo(16), 6) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrade_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrades_idem.tooltip1"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrades_idem.tooltip2"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrade_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> BRAINUPGRADES_CHIPWARESLOTS = ITEMS.register("brainupgrades_chipwareslots",
        () -> new ChipwareSlotsItem(new Item.Properties().stacksTo(16), 6) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrade_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrades_chipwareslots.tooltip1"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrades_chipwareslots.tooltip2"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrades_chipwareslots.ram"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrades_chipwareslots.ramregen"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrade_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> BRAINUPGRADES_SPELLJAMMER = registerIfLoaded("irons_spellbooks", "brainupgrades_spelljammer",
            () -> new SpellJammerItem(new Item.Properties().stacksTo(16), 6) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrade_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrades_spelljammer.tooltip1"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrades_spelljammer.tooltip2"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrade_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> BRAINUPGRADES_NEURALPROCESSOR = ITEMS.register("brainupgrades_neuralprocessor",
        () -> new NeuralProcessorItem(new Item.Properties().stacksTo(16), 8) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    if (Screen.hasShiftDown()) {
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrade_tooltip"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrades_neuralprocessor.tooltip1"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrades_neuralprocessor.tooltip2"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrades_neuralprocessor.ram"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrades_neuralprocessor.ramregen"));
                    } else {
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrade_tooltip"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                    }
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> BRAINUPGRADES_ICEPROTOCOL = ITEMS.register("brainupgrades_iceprotocol",
        () -> new ICEProtocolItem(new Item.Properties().stacksTo(16), 5) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    if (Screen.hasShiftDown()) {
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrade_tooltip"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrades_iceprotocol.tooltip1"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrades_iceprotocol.ram"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrades_iceprotocol.ramregen"));
                    } else {
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrade_tooltip"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                    }
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> BRAINUPGRADES_COAXIALPORT = ITEMS.register("brainupgrades_coaxialport",
        () -> new CoaxialPortItem(new Item.Properties().stacksTo(16), 13) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    if (Screen.hasShiftDown()) {
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrade_tooltip"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrades_coaxialport.tooltip1"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrades_coaxialport.ram"));
                    } else {
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrade_tooltip"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                    }
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> BRAINUPGRADES_KERENZIKOV = ITEMS.register("brainupgrades_kerenzikov",
        () -> new KerenzikovItem(new Item.Properties().stacksTo(16), 16) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    if (Screen.hasShiftDown()) {
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrade_tooltip"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrades_kerenzikov.tooltip1"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrades_kerenzikov.tooltip2"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrades_kerenzikov.ram"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrades_kerenzikov.ramregen"));
                    } else {
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrade_tooltip"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                    }
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> BRAINUPGRADES_BERSERK = ITEMS.register("brainupgrades_berserk",
        () -> new BerserkItem(new Item.Properties().stacksTo(16), 16) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    if (Screen.hasShiftDown()) {
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrade_tooltip"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrades_berserk.tooltip1"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrades_berserk.tooltip2"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrades_berserk.tooltip3"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrades_berserk.ram"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrades_berserk.ramregen"));
                    } else {
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrade_tooltip"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                    }
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> BRAINUPGRADES_RAMDECK = ITEMS.register("brainupgrades_ramdeck",
        () -> new RamdeckItem(new Item.Properties().stacksTo(16), 16) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    if (Screen.hasShiftDown()) {
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrade_tooltip"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrades_ramdeck.tooltip1"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrades_ramdeck.tooltip2"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrades_ramdeck.tooltip3"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrades_ramdeck.ram"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrades_ramdeck.ramregen"));
                    } else {
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.brainupgrade_tooltip"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                    }
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });

    //HEART UPGRADES
    public static final DeferredItem<Item> HEARTUPGRADES_CYBERHEART = ITEMS.register("heartupgrades_cyberheart",
        () -> new MechanicalHeartItem(new Item.Properties().stacksTo(16), 8) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.heartupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.heartupgrades_cyberheart.tooltip1"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.heartupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> HEARTUPGRADES_COUPLER = ITEMS.register("heartupgrades_coupler",
        () -> new CardiovascularCouplerItem(new Item.Properties().stacksTo(16), 3) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.heartupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.heartupgrades_coupler.tooltip1"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.heartupgrades_coupler.tooltip2"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.heartupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> HEARTUPGRADES_CREEPERHEART = ITEMS.register("heartupgrades_creeperheart",
        () -> new CreeperheartItem(new Item.Properties().stacksTo(16), 5) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.heartupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.heartupgrades_creeperheart.tooltip1"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.heartupgrades_creeperheart.tooltip2"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.heartupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> HEARTUPGRADES_DEFIBRILLATOR = ITEMS.register("heartupgrades_defibrillator",
        () -> new InternalDefibrillatorItem(new Item.Properties().stacksTo(16), 8) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.heartupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.heartupgrades_defibrillator.tooltip1"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.heartupgrades_defibrillator.tooltip2"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.heartupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> HEARTUPGRADES_STEMCELL = ITEMS.register("heartupgrades_stemcell",
        () -> new StemCellsItem(new Item.Properties().stacksTo(16), 6) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.heartupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.heartupgrades_stemcell.tooltip1"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.heartupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> HEARTUPGRADES_PLATELETS = ITEMS.register("heartupgrades_platelets",
        () -> new PlateletDispatcherItem(new Item.Properties().stacksTo(16), 6) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.heartupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.heartupgrades_platelets.tooltip1"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.heartupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> HEARTUPGRADES_ANOMALY = registerIfLoaded("irons_spellbooks", "heartupgrades_anomaly",
        () -> new ArcaneAnomalyItem(new Item.Properties().stacksTo(1), 15) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.heartupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.heartupgrades_anomaly.tooltip1"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.heartupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });

//LUNG UPGRADES
    public static final DeferredItem<Item> LUNGSUPGRADES_HYPEROXYGENATION = ITEMS.register("lungsupgrades_hyperoxygenation",
        () -> new HyperoxygenationBoostItem(new Item.Properties().stacksTo(16), 3){
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.lungsupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.lungsupgrades_hyperoxygenation.tooltip1"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.lungsupgrades_hyperoxygenation.tooltip2"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.lungsupgrades_hyperoxygenation.tooltip3"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.lungsupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> LUNGSUPGRADES_OXYGEN = ITEMS.register("lungsupgrades_oxygen",
        () -> new OxygenTankItem(new Item.Properties().stacksTo(16), 5) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.lungsupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.lungsupgrades_oxygen.tooltip1"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.lungsupgrades_oxygen.tooltip2"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.lungsupgrades_oxygen.tooltip3"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.lungsupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> LUNGSUPGRADES_SYNTHLUNGS = ITEMS.register("lungsupgrades_synthlungs",
        () -> new SynthLungsItem(new Item.Properties().stacksTo(16), 5) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.lungsupgrades_tooltip"));
                    if(ModCompats.isInstalled("parcool")) {
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.lungsupgrades_synthlungs.tooltip1_parcool"));
                    } else {
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.lungsupgrades_synthlungs.tooltip1"));
                    }
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.lung_replacement"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.lungsupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> LUNGSUPGRADES_VAMPYRES = ITEMS.register("lungsupgrades_vampyres",
        () -> new VampyresItem(new Item.Properties().stacksTo(16), 15) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.lungsupgrades_vampyres.tooltip1"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.lungsupgrades_vampyres.desc"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.lungsupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });

//ORGAN UPGRADES
    public static final DeferredItem<Item> ORGANSUPGRADES_ADRENALINE = ITEMS.register("organsupgrades_adrenaline",
        () -> new AdrenalPumpItem(new Item.Properties().stacksTo(16), 5) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.organsupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.organsupgrades_adrenaline.tooltip1"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.organsupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> ORGANSUPGRADES_BATTERY = ITEMS.register("organsupgrades_battery",
        () -> new InternalBatteryItem(new Item.Properties().stacksTo(16), 3) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.organsupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.organsupgrades_battery.tooltip1"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.organsupgrades_battery.tooltip2"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.organsupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> ORGANSUPGRADES_DIAMONDWAFERSTACK = ITEMS.register("organsupgrades_diamondwaferstack",
        () -> new DiamondWaferstackItem(new Item.Properties().stacksTo(16), 1) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.organsupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.organsupgrades_diamondwaferstack.tooltip1"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.organsupgrades_diamondwaferstack.tooltip2"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.organsupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> ORGANSUPGRADES_DUALISTICCONVERTER = ITEMS.register("organsupgrades_dualisticconverter",
        () -> new DualisticConverterItem(new Item.Properties().stacksTo(16), 8){
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    Component.translatable("tooltip.createcybernetics.organsupgrades_heartupgrades_tooltip");
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.organsupgrades_dualisticconverter.tooltip1"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.organsupgrades_heartupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.organsupgrades_dualisticconverter.tooltip1"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> ORGANSUPGRADES_LIVERFILTER = ITEMS.register("organsupgrades_liverfilter",
        () -> new LiverFilterItem(new Item.Properties().stacksTo(16), 3) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.organsupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.organsupgrades_liverfilter.tooltip1"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.organsupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> ORGANSUPGRADES_MAGICCATALYST = ITEMS.register("organsupgrades_magiccatalyst",
        () -> new MagicCatalystItem(new Item.Properties().stacksTo(16), 15) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    Component.translatable("tooltip.createcybernetics.organsupgrades_heartupgrades_tooltip");
                tooltipComponents.add(Component.translatable("tooltip.createcybernetics.organsupgrades_magiccatalyst.tooltip1"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.organsupgrades_heartupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> ORGANSUPGRADES_METABOLIC = ITEMS.register("organsupgrades_metabolic",
        () -> new MetabolicConverterItem(new Item.Properties().stacksTo(16), 4) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.organsupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.organsupgrades_metabolic.tooltip1"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.organsupgrades_metabolic.tooltip2"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.organsupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> ORGANSUPGRADES_DENSEBATTERY = ITEMS.register("organsupgrade_densebattery",
        () -> new DenseBatteryItem(new Item.Properties().stacksTo(16), 8) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.organsupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.organsupgrade_densebattery.tooltip1"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.organsupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> ORGANSUPGRADES_CREATIVEBATTERY = ITEMS.register("organsupgrade_creativebattery",
        () -> new CreativeBatteryItem(new Item.Properties().stacksTo(16), 0) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.organsupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.organsupgrade_creativebattery.tooltip1"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.organsupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> ORGANSUPGRADES_HEATENGINE = ITEMS.register("organsupgrades_heatengine",
        () -> new HeatEngineItem(new Item.Properties().stacksTo(16), 10) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.organsupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.organsupgrades_heatengine.tooltip1"));

                    Component key = ModKeyMappings.HEAT_ENGINE.get().getTranslatedKeyMessage();
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.organsupgrades_heatengine.desc", key));

                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.organsupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> ORGANSUPGRADES_MANABATTERY = registerIfLoaded("irons_spellbooks", "organsupgrades_manabattery",
        () -> new ManaBatteryItem(new Item.Properties().stacksTo(16), 5) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.organsupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.organsupgrades_manabattery.tooltip1"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.organsupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> ORGANSUPGRADES_OREGRINDER = ITEMS.register("organsupgrades_oregrinder",
        () -> new OregrinderItem(new Item.Properties().stacksTo(16), 10) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    if (Screen.hasShiftDown()) {
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.organsupgrades_tooltip"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.organsupgrades_oregrinder.tooltip1"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.organsupgrades_oregrinder.tooltip2"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.organsupgrades_oregrinder.tooltip3"));

                    } else {
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.organsupgrades_tooltip"));
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                    }
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });


    //SKIN UPGRADES
    public static final DeferredItem<Item> SKINUPGRADES_ARTERIALTURBINE = ITEMS.register("skinupgrades_arterialturbine",
        () -> new ArterialTurbineItem(new Item.Properties().stacksTo(16), 3) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.skinupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.skinupgrades_arterialturbine.tooltip1"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.skinupgrades_arterialturbine.tooltip2"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.skinupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> SKINUPGRADES_CHROMATOPHORES = ITEMS.register("skinupgrades_chromatophores",
        () -> new SyntheticChromatophoresItem(new Item.Properties().stacksTo(16), 8) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.skinupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.skinupgrades_chromatophores.tooltip1"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.skinupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> SKINUPGRADES_SYNTHSKIN = ITEMS.register("skinupgrades_synthskin",
        () -> new SynthSkinItem(new Item.Properties().stacksTo(16), 1) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.skinupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.skinupgrades_synthskin.tooltip1"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.skinupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> SKINUPGRADES_IMMUNO = ITEMS.register("skinupgrades_immuno",
        () -> new ImmunosuppressorItem(new Item.Properties().stacksTo(16), -25) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.skinupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.skinupgrades_immuno.tooltip1"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.skinupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> SKINUPGRADES_FACEPLATE = ITEMS.register("skinupgrades_faceplate",
        () -> new InterchangeableFaceplateItem(new Item.Properties().stacksTo(16), 8) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.skinupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.skinupgrades_faceplate.tooltip1"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.skinupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> SKINUPGRADES_NETHERITEPLATING = ITEMS.register("skinupgrades_netheriteplating",
        () -> new IsothermalSkinItem(new Item.Properties().stacksTo(16), 10) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.skinupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.skinupgrades_netheriteplating.tooltip1"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.skinupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> SKINUPGRADES_SOLARSKIN = ITEMS.register("skinupgrades_solarskin",
        () -> new SolarskinItem(new Item.Properties().stacksTo(16), 3) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.skinupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.skinupgrades_solarskin.tooltip1"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.boneupgrades_bonebattery.tooltip2"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.skinupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> SKINUPGRADES_SUBDERMALARMOR = ITEMS.register("skinupgrades_subdermalarmor",
        () -> new SubdermalArmorItem(new Item.Properties().stacksTo(16), 6) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.skinupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.skinupgrades_subdermalarmor.tooltip1"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.skinupgrades_subdermalarmor.tooltip2"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.skinupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> SKINUPGRADES_SUBDERMALSPIKES = ITEMS.register("skinupgrades_subdermalspikes",
        () -> new SubdermalSpikesItem(new Item.Properties().stacksTo(16), 7) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.skinupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.skinupgrades_subdermalspikes.tooltip1"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.skinupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> SKINUPGRADES_SYNTHETICSETULES = ITEMS.register("skinupgrades_syntheticsetules",
        () -> new SyntheticSetulesItem(new Item.Properties().stacksTo(16), 4) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.skinupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.skinupgrades_syntheticsetules.tooltip1"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.skinupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> SKINUPGRADES_METALPLATING = ITEMS.register("skinupgrades_metalplating",
        () -> new MetalSkinItem(new Item.Properties().stacksTo(16), 4) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.skinupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.skinupgrades_metalplating.tooltip1"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.skinupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> SKINUPGRADES_EMPTHREADING = ITEMS.register("skinupgrades_empthreading",
        () -> new EMPThreadingItem(new Item.Properties().stacksTo(16), 4) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.skinupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.skinupgrades_empthreading.tooltip1"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.skinupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> SKINUPGRADES_MANASKIN = registerIfLoaded("irons_spellbooks", "skinupgrades_manaskin",
        () -> new ManaSkinItem(new Item.Properties().stacksTo(16), 5) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.skinupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.skinupgrades_manaskin.tooltip1"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.skinupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> SKINUPGRADES_SWEAT = registerIfLoaded("cold_sweat","skinupgrades_sweat",
            () -> new SweatGlandsItem(new Item.Properties().stacksTo(16), 3) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.skinupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.skinupgrades_sweat.tooltip1"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.skinupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> SKINUPGRADES_ULTRAVIOLENT = registerIfLoaded("vampirism","skinupgrades_ultraviolent",
            () -> new UltraViolentFlashSkinItem(new Item.Properties().stacksTo(16), 9) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.skinupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.skinupgrades_ultraviolent.tooltip1"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.skinupgrades_ultraviolent.tooltip2"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.skinupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });

//MUSCLE UPGRADES
    public static final DeferredItem<Item> MUSCLEUPGRADES_SYNTHMUSCLE = ITEMS.register("muscleupgrades_synthmuscle",
        () -> new SynthMuscleItem(new Item.Properties().stacksTo(16), 5) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.muscleupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.muscleupgrades_synthmuscle.tooltip1"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.muscleupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> MUSCLEUPGRADES_REINFORCEDTENDONS = ITEMS.register("muscleupgrades_reinforcedtendons",
        () -> new ReinforcedTendonsItem(new Item.Properties().stacksTo(16), 5) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.muscleupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.muscleupgrades_reinforcedtendons.tooltip1"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.muscleupgrades_reinforcedtendons.tooltip2"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.muscleupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> MUSCLEUPGRADES_WIREDREFLEXES = ITEMS.register("muscleupgrades_wiredreflexes",
        () -> new WiredReflexesItem(new Item.Properties().stacksTo(16), 4) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.muscleupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.muscleupgrades_wiredreflexes.tooltip1"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.muscleupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> MUSCLEUPGRADES_BALLISTICGEL = ITEMS.register("muscleupgrades_ballisticgel",
        () -> new BallisticGelItem(new Item.Properties().stacksTo(16), 7) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.muscleupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.muscleupgrades_ballisticgel.tooltip1"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.muscleupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });

//WETWARE UPGRADES
    public static final DeferredItem<Item> WETWARE_BLUBBER = registerIfLoaded("cold_sweat","wetware_blubber",
            () -> new BlubberItem(new Item.Properties().stacksTo(16), 3) {
                    @Override
                    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                        if (Screen.hasShiftDown()) {
                            tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetwareupgrades_tooltip"));
                            tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetware_blubber.tooltip1"));
                        } else {
                            tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetwareupgrades_tooltip"));
                            tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                        }
                        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                    }
                });
    public static final DeferredItem<Item> WETWARE_FIREBREATHINGLUNGS = ITEMS.register("wetware_firebreathinglungs",
        () -> new IgniphorusGlandItem(new Item.Properties().stacksTo(16), 15) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetwareupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetware_firebreathinglungs.tooltip1"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetware_firebreathinglungs.desc"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetwareupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> WETWARE_WATERBREATHINGLUNGS = ITEMS.register("wetware_waterbreathinglungs",
        () -> new GillsItem(new Item.Properties().stacksTo(16), 12) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetwareupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetware_waterbreathinglungs.tooltip1"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetwareupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> WETWARE_GUARDIANEYE = ITEMS.register("wetware_guardianeye",
        () -> new GuardianEyeItem(new Item.Properties().stacksTo(16), 15) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetwareupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetware_guardianeye.tooltip1"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetware_guardianeye.desc"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetwareupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> WETWARE_POLARBEARFUR = ITEMS.register("wetware_polarbearfur",
        () -> new PolarBearFurItem(new Item.Properties().stacksTo(16), 12) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetwareupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetware_polarbearfur.tooltip1"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetwareupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> WETWARE_RAVAGERTENDONS = ITEMS.register("wetware_ravagertendons",
        () -> new RavagerTendonsItem(new Item.Properties().stacksTo(16), 15) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetwareupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetware_ravagertendons.tooltip1"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetwareupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> WETWARE_SCULKLUNGS = ITEMS.register("wetware_sculklungs",
        () -> new SculkLungsItem(new Item.Properties().stacksTo(16), 6) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetwareupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetware_sculklungs.tooltip1"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetware_sculklungs.desc"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetwareupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> WETWARE_TACTICALINKSAC = ITEMS.register("wetware_tacticalinksac",
        () -> new TacticalInkSacItem(new Item.Properties().stacksTo(16), 8) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetwareupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetware_tacticalinksac.tooltip1"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetwareupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> WETWARE_AEROSTASISGYROBLADDER = ITEMS.register("wetware_aerostasisgyrobladder",
        () -> new AerostasisItem(new Item.Properties().stacksTo(16), 15) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetwareupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetware_aerostasisgyrobladder.tooltip1"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetware_aerostasisgyrobladder.desc"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetwareupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> WETWARE_GRASSFEDSTOMACH = ITEMS.register("wetware_grassfedstomach",
        () -> new GrassfedStomachItem(new Item.Properties().stacksTo(16), 7) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetwareupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetware_grassfedstomach.tooltip1"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetwareupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> WETWARE_WEBSHOOTINGINTESTINES = ITEMS.register("wetware_webshootingintestines",
        () -> new IntestineSpinneretteItem(new Item.Properties().stacksTo(16), 10) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetwareupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetware_webshootingintestines.tooltip1"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetware_webshootingintestines.desc"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetwareupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> WETWARE_WEBSHOOTING_LEFTARM = ITEMS.register("wetware_webshooting_leftarm",
        () -> new ArmSpinneretteItem(new Item.Properties().stacksTo(16), 7) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetwareupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetware_webshooting_leftarm.tooltip1"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetware_webshooting_leftarm.desc"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetwareupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> WETWARE_WEBSHOOTING_RIGHTARM = ITEMS.register("wetware_webshooting_rightarm",
        () -> new ArmSpinneretteItem(new Item.Properties().stacksTo(16), 7) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetwareupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetware_webshooting_rightarm.tooltip1"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetware_webshooting_rightarm.desc"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetwareupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> WETWARE_SPIDEREYES = ITEMS.register("wetware_spidereyes",
        () -> new SpiderEyesItem(new Item.Properties().stacksTo(16), 7) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetwareupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetware_spidereyes.tooltip1"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetwareupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> WETWARE_BLASTEMASKELETON = ITEMS.register("wetware_blastemaskeleton",
        () -> new BlastemaInfusedSkeletonItem(new Item.Properties().stacksTo(16), 8) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetwareupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetware_blastemaskeleton.tooltip1"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetware_blastemaskeleton.desc"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetwareupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> WETWARE_DRAGONSKIN = ITEMS.register("wetware_dragonskin",
        () -> new DragonSkinItem(new Item.Properties().stacksTo(16), 10) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetwareupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetware_dragonskin.tooltip1"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetwareupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> WETWARE_WARDENANTLERS = ITEMS.register("wetware_wardenantlers",
        () -> new WardenAntlersItem(new Item.Properties().stacksTo(16), 6) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetwareupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetware_wardenantlers.tooltip1"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetwareupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> WETWARE_SCULKHEART = ITEMS.register("wetware_sculkheart",
        () -> new SculkHeartItem(new Item.Properties().stacksTo(16), 8) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetwareupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetware_sculkheart.tooltip1"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetwareupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> WETWARE_GOOEYMUSCLE = ITEMS.register("wetware_gooeymuscle",
        () -> new GooeyMusculatureItem(new Item.Properties().stacksTo(16), 9) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetwareupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetware_gooeymuscle.tooltip1"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetwareupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });
    public static final DeferredItem<Item> WETWARE_ELECTROCYTEMUSCLE = ITEMS.register("wetware_electrocytemuscle",
        () -> new ElectrocyteTissueItem(new Item.Properties().stacksTo(16), 9) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                if (Screen.hasShiftDown()) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetwareupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetware_electrocytemuscle.tooltip1"));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.wetwareupgrades_tooltip"));
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
                }
                super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            }
        });



//SCAVENGED CYBERWARE
    public static final DeferredItem<Item> SCAVENGED_RIGHTLEG = ITEMS.register("scavenged_rightleg",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_LEFTLEG = ITEMS.register("scavenged_leftleg",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_RIGHTARM = ITEMS.register("scavenged_rightarm",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_LEFTARM = ITEMS.register("scavenged_leftarm",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_LINEARFRAME = ITEMS.register("scavenged_linearframe",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_CYBEREYES = ITEMS.register("scavenged_cybereyes",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_HUDLENS = ITEMS.register("scavenged_hudlens",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_MONOVISION = ITEMS.register("scavenged_monovision",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_NAVIGATIONCHIP = ITEMS.register("scavenged_navigationchip",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_HUDJACK = ITEMS.register("scavenged_hudjack",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_NIGHTVISION = ITEMS.register("scavenged_nightvision",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_TARGETING = ITEMS.register("scavenged_targeting",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item>SCAVENGED_UNDERWATERVISION = ITEMS.register("scavenged_underwatervision",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_ZOOM = ITEMS.register("scavenged_zoom",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_TRAJECTORYCALCULATOR = ITEMS.register("scavenged_trajectorycalculator",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_BIOMONITOR = ITEMS.register("scavenged_biomonitor",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_ARMCANNON = ITEMS.register("scavenged_armcannon",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_FLYWHEEL = ITEMS.register("scavenged_flywheel",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_CLAWS = ITEMS.register("scavenged_claws",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_CRAFTHANDS = ITEMS.register("scavenged_crafthands",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_DRILLFIST = ITEMS.register("scavenged_drillfist",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_FIRESTARTER = ITEMS.register("scavenged_firestarter",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_PNEUMATICWRIST = ITEMS.register("scavenged_pneumaticwrist",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_REINFORCEDKNUCKLES = ITEMS.register("scavenged_reinforcedknuckles",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_ARCCANNON = ITEMS.register("scavenged_arccannon",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_MANTISBLADE_TITANIUM = ITEMS.register("scavenged_mantisblade_titanium",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_MANTISBLADE_IRON = ITEMS.register("scavenged_mantisblade_iron",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_MANTISBLADE_COPPER = ITEMS.register("scavenged_mantisblade_copper",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_MANTISBLADE_GOLD = ITEMS.register("scavenged_mantisblade_gold",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_MANTISBLADE_DIAMOND = ITEMS.register("scavenged_mantisblade_diamond",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_MANTISBLADE_NETHERITE = ITEMS.register("scavenged_mantisblade_netherite",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_METALDETECTOR = ITEMS.register("scavenged_metaldetector",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_ANKLEBRACERS = ITEMS.register("scavenged_anklebracers",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_JUMPBOOST = ITEMS.register("scavenged_jumpboost",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_PROPELLERS = ITEMS.register("scavenged_propellers",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_SPURS = ITEMS.register("scavenged_spurs",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_OCELOTPAWS = ITEMS.register("scavenged_ocelotpaws",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_BONEBATTERY = ITEMS.register("scavenged_bonebattery",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_BONEFLEX = ITEMS.register("scavenged_boneflex",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_BONELACING = ITEMS.register("scavenged_bonelacing",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_CAPACITORFRAME = ITEMS.register("scavenged_capacitorframe",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_ELYTRA = registerIfLoaded("caelus", "scavenged_elytra",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_PIEZO = ITEMS.register("scavenged_piezo",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_SPINALINJECTOR = ITEMS.register("scavenged_spinalinjector",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_SANDEVISTAN = ITEMS.register("scavenged_sandevistan",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_EYEOFDEFENDER = ITEMS.register("scavenged_eyeofdefender",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_CONSCIOUSNESSTRANSMITTER = registerIfLoaded("createenchantmentindustry","scavenged_consciousnesstransmitter",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_CORTICALSTACK = registerIfLoaded("createenchantmentindustry","scavenged_corticalstack",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_ENDERJAMMER = ITEMS.register("scavenged_enderjammer",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_MATRIX = ITEMS.register("scavenged_matrix",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_NEURALCONTEXTUALIZER = ITEMS.register("scavenged_neuralcontextualizer",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_CYBERDECK = ITEMS.register("scavenged_cyberdeck",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_IDEM = ITEMS.register("scavenged_idem",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_CHIPWARESLOTS = ITEMS.register("scavenged_chipwareslots",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_NEURALPROCESSOR = ITEMS.register("scavenged_neuralprocessor",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_ICEPROTOCOL = ITEMS.register("scavenged_iceprotocol",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_COAXIALPORT = ITEMS.register("scavenged_coaxialport",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_KERENZIKOV = ITEMS.register("scavenged_kerenzikov",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_BERSERK = ITEMS.register("scavenged_berserk",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_RAMDECK = ITEMS.register("scavenged_ramdeck",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_SPELLJAMMER = registerIfLoaded("irons_spellbooks", "scavenged_spelljammer",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_CYBERHEART = ITEMS.register("scavenged_cyberheart",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_COUPLER = ITEMS.register("scavenged_coupler",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_CREEPERHEART = ITEMS.register("scavenged_creeperheart",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_DEFIBRILLATOR = ITEMS.register("scavenged_defibrillator",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_STEMCELL = ITEMS.register("scavenged_stemcell",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_PLATELETS = ITEMS.register("scavenged_platelets",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_HYPEROXYGENATION = ITEMS.register("scavenged_hyperoxygenation",
            () -> new Item(new Item.Properties().stacksTo(64)){
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_OXYGEN = ITEMS.register("scavenged_oxygen",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_SYNTHLUNGS = ITEMS.register("scavenged_synthlungs",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_VAMPYRES = ITEMS.register("scavenged_vampyres",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_ADRENALINE = ITEMS.register("scavenged_adrenaline",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_BATTERY = ITEMS.register("scavenged_battery",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_OREGRINDER = ITEMS.register("scavenged_oregrinder",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_MANABATTERY = registerIfLoaded("irons_spellbooks", "scavenged_manabattery",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_DIAMONDWAFERSTACK = ITEMS.register("scavenged_diamondwaferstack",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_DUALISTICCONVERTER = ITEMS.register("scavenged_dualisticconverter",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_LIVERFILTER = ITEMS.register("scavenged_liverfilter",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_METABOLIC = ITEMS.register("scavenged_metabolic",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_DENSEBATTERY = ITEMS.register("scavenged_densebattery",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_HEATENGINE = ITEMS.register("scavenged_heatengine",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_ARTERIALTURBINE = ITEMS.register("scavenged_arterialturbine",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_CHROMATOPHORES = ITEMS.register("scavenged_chromatophores",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_SYNTHSKIN = ITEMS.register("scavenged_synthskin",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_IMMUNO = ITEMS.register("scavenged_immuno",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_FACEPLATE = ITEMS.register("scavenged_faceplate",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_NETHERITEPLATING = ITEMS.register("scavenged_netheriteplating",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_SOLARSKIN = ITEMS.register("scavenged_solarskin",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_SUBDERMALARMOR = ITEMS.register("scavenged_subdermalarmor",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_SUBDERMALSPIKES = ITEMS.register("scavenged_subdermalspikes",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_SYNTHETICSETULES = ITEMS.register("scavenged_syntheticsetules",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_METALPLATING = ITEMS.register("scavenged_metalplating",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_MANASKIN = registerIfLoaded("irons_spellbooks", "scavenged_manaskin",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_SWEAT = registerIfLoaded("cold_sweat","scavenged_sweat",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_ULTRAVIOLENT = registerIfLoaded("vampirism","scavenged_ultraviolent",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_SYNTHMUSCLE = ITEMS.register("scavenged_synthmuscle",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_REINFORCEDTENDONS = ITEMS.register("scavenged_reinforcedtendons",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_WIREDREFLEXES = ITEMS.register("scavenged_wiredreflexes",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<Item> SCAVENGED_BALLISTICGEL = ITEMS.register("scavenged_ballisticgel",
            () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                        tooltipComponents.add(Component.translatable("tooltip.createcybernetics.scavenged_tooltip"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });







    public static final Supplier<Item> ANOMALOUS_STAFF = registerIronsSpellbooksItem(
            "anomalous_staff", IronsSpellbooksStaffItems::createAnomalousStaff
    );


    public static <T extends Item> DeferredItem<T> registerIfLoaded(String modid, String name, Supplier<T> supplier) {
        if (ModList.get().isLoaded(modid)) {
            return ModItems.ITEMS.register(name, supplier);
        }
        return null;
    }

    public static <T extends Item> DeferredItem<T> registerIfNotLoaded(String modid, String name, Supplier<T> supplier) {
        if (!ModList.get().isLoaded(modid)) {
            return ModItems.ITEMS.register(name, supplier);
        }
        return null;
    }

    private static DeferredItem<Item> registerMicroprogram(String name, MicroprogramDefinition definition) {
        return ITEMS.register(name, () -> new MicroprogramItem(new Item.Properties().stacksTo(1), definition));
    }

    private static DeferredItem<Item> registerMacroprogram(String name, MacroprogramDefinition definition) {
        return ITEMS.register(name, () -> new MacroprogramItem(new Item.Properties().stacksTo(1), definition));
    }

    private static Supplier<Item> registerIronsSpellbooksItem(String name, Supplier<Item> supplier) {
        if (!IronsSpellbooksCompat.isLoaded()) {
            return () -> Items.AIR;
        }

        return ITEMS.register(name, supplier);
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

}
