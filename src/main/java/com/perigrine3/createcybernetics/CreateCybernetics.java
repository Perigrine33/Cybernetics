
package com.perigrine3.createcybernetics;

import com.mojang.logging.LogUtils;
import com.perigrine3.createcybernetics.advancement.ModCriteria;
import com.perigrine3.createcybernetics.block.ModBlocks;
import com.perigrine3.createcybernetics.block.entity.ModBlockEntities;
import com.perigrine3.createcybernetics.block.ironsspells.AnomalyCoreRenderer;
import com.perigrine3.createcybernetics.common.attributes.ModAttributes;
import com.perigrine3.createcybernetics.common.capabilities.ModAttachments;
import com.perigrine3.createcybernetics.common.capabilities.ModBlockCapabilities;
import com.perigrine3.createcybernetics.common.capabilities.ModMobAttachments;
import com.perigrine3.createcybernetics.compat.CompatBootstrap;
import com.perigrine3.createcybernetics.compat.ModCompats;
import com.perigrine3.createcybernetics.compat.corpse.CorpseCompat;
import com.perigrine3.createcybernetics.compat.corpse.CorpseCyberwareScreen;
import com.perigrine3.createcybernetics.compat.corpse.ModCorpseCompatMenus;
import com.perigrine3.createcybernetics.compat.ironsspells.IronsSpellbooksCompatBlockEntities;
import com.perigrine3.createcybernetics.compat.ironsspells.IronsSpellbooksCompatBlocks;
import com.perigrine3.createcybernetics.compat.ironsspells.IronsSpellbooksCyberwareAttributes;
import com.perigrine3.createcybernetics.compat.neosync.NeoSyncCompat;
import com.perigrine3.createcybernetics.compat.neosync.NeoSyncCompatLoader;
import com.perigrine3.createcybernetics.component.ModDataComponents;
import com.perigrine3.createcybernetics.effect.ModEffects;
import com.perigrine3.createcybernetics.effect.PneumaticCalvesEffect;
import com.perigrine3.createcybernetics.enchantment.ModEnchantmentEffects;
import com.perigrine3.createcybernetics.entity.ModEntities;
import com.perigrine3.createcybernetics.entity.client.renderers.*;
import com.perigrine3.createcybernetics.item.ModCreativeModeTabs;
import com.perigrine3.createcybernetics.item.ModItems;
import com.perigrine3.createcybernetics.item.macroprogram.ModMacroprogramArmorMaterials;
import com.perigrine3.createcybernetics.loot.ModLootModifiers;
import com.perigrine3.createcybernetics.particle.ModParticles;
import com.perigrine3.createcybernetics.potion.ModPotions;
import com.perigrine3.createcybernetics.recipe.ModRecipeSerializers;
import com.perigrine3.createcybernetics.recipe.ModRecipes;
import com.perigrine3.createcybernetics.recipe.ingredient.ModIngredientTypes;
import com.perigrine3.createcybernetics.screen.ModMenuTypes;
import com.perigrine3.createcybernetics.screen.custom.arm_cannon.ArmCannonScreen;
import com.perigrine3.createcybernetics.screen.custom.chipware.ChipwareMiniScreen;
import com.perigrine3.createcybernetics.screen.custom.computer.ComputerScreen;
import com.perigrine3.createcybernetics.screen.custom.computer.ComputerTowerScreen;
import com.perigrine3.createcybernetics.screen.custom.crafting.EngineeringTableScreen;
import com.perigrine3.createcybernetics.screen.custom.crafting.ExpandedInventoryScreen;
import com.perigrine3.createcybernetics.screen.custom.crafting.GraftingTableScreen;
import com.perigrine3.createcybernetics.screen.custom.cyberdeck.CyberdeckScreen;
import com.perigrine3.createcybernetics.screen.custom.heat_engine.HeatEngineScreen;
import com.perigrine3.createcybernetics.screen.custom.spinal_injector.SpinalInjectorScreen;
import com.perigrine3.createcybernetics.screen.custom.surgery.ripper.RipperTradeScreen;
import com.perigrine3.createcybernetics.screen.custom.surgery.ripper.SurgeryPaymentScreen;
import com.perigrine3.createcybernetics.screen.custom.surgery.robosurgeon.RobosurgeonScreen;
import com.perigrine3.createcybernetics.screen.custom.surgery.surgery_table.SurgeryTableScreen;
import com.perigrine3.createcybernetics.screen.custom.tattoo_system.TattooArtistScreen;
import com.perigrine3.createcybernetics.screen.custom.vampyres.VampyresScreen;
import com.perigrine3.createcybernetics.sound.ModSounds;
import com.perigrine3.createcybernetics.world.dimension.ModDimensions;
import com.perigrine3.createcybernetics.worldgen.ModBiomeModifierSerializers;
import com.perigrine3.createcybernetics.worldgen.ModStructureTypes;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

@Mod(CreateCybernetics.MODID)
public class CreateCybernetics {
    public static final String MODID = "createcybernetics";

    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public CreateCybernetics(IEventBus eventBus, ModContainer modContainer) {
        eventBus.addListener(this::commonSetup);
        eventBus.addListener(this::addCreative);

        NeoForge.EVENT_BUS.register(this);

        NeoForge.EVENT_BUS.addListener(PneumaticCalvesEffect.Events::onLivingJump);
        NeoForge.EVENT_BUS.register(PneumaticCalvesEffect.Events.class);

        ModCreativeModeTabs.register(eventBus);
        ModItems.register(eventBus);
        ModBlocks.register(eventBus);
        ModBlockEntities.register(eventBus);
        ModSounds.register(eventBus);
        ModEntities.register(eventBus);
        ModEffects.register(eventBus);
        ModMenuTypes.register(eventBus);
        ModEnchantmentEffects.register(eventBus);
        ModPotions.register(eventBus);
        ModMacroprogramArmorMaterials.register(eventBus);

        ModLootModifiers.register(eventBus);
        ModCriteria.register(eventBus);
        ModDataComponents.register(eventBus);
        ModIngredientTypes.INGREDIENT_TYPES.register(eventBus);
        ModRecipes.register(eventBus);
        ModRecipeSerializers.register(eventBus);

        ModStructureTypes.register(eventBus);
        ModBiomeModifierSerializers.register(eventBus);

        ModDimensions.register(eventBus);

        ModAttachments.register(eventBus);
        ModMobAttachments.register(eventBus);
        ModAttributes.register(eventBus);
        ModParticles.register(eventBus);

        eventBus.addListener(ModBlockCapabilities::register);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        CompatBootstrap.bootstrap();

        if (CorpseCompat.isLoaded()) {
            ModCorpseCompatMenus.register(eventBus);
        }

        if (ModCompats.isInstalled("neosync")) {
            NeoSyncCompatLoader.initialize();
        }

        IronsSpellbooksCompatBlocks.register(eventBus);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        IronsSpellbooksCyberwareAttributes.register();
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
            insertCreativeAfter(event, Items.PITCHER_POD.getDefaultInstance(), ModItems.DATURA_SEED_POD.get().getDefaultInstance());
            insertCreativeAfter(event, Blocks.DEEPSLATE_IRON_ORE.asItem().getDefaultInstance(), ModBlocks.TITANIUMORE_BLOCK.get().asItem().getDefaultInstance());
            insertCreativeAfter(event, ModBlocks.TITANIUMORE_BLOCK.asItem().getDefaultInstance(), ModBlocks.DEEPSLATE_TITANIUMORE_BLOCK.get().asItem().getDefaultInstance());
            insertCreativeAfter(event, Blocks.RAW_IRON_BLOCK.asItem().getDefaultInstance(), ModBlocks.RAW_TITANIUM_BLOCK.get().asItem().getDefaultInstance());
        }

        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            insertCreativeAfter(event, Items.CHAIN.getDefaultInstance(), ModBlocks.TITANIUM_BLOCK.get().asItem().getDefaultInstance());
            insertCreativeAfter(event, ModBlocks.TITANIUM_BLOCK.asItem().getDefaultInstance(), ModBlocks.SMOOTH_TITANIUM.get().asItem().getDefaultInstance());
            insertCreativeAfter(event, ModBlocks.SMOOTH_TITANIUM.asItem().getDefaultInstance(), ModBlocks.TITANIUM_GRATE.get().asItem().getDefaultInstance());
            insertCreativeAfter(event, ModBlocks.TITANIUM_GRATE.asItem().getDefaultInstance(), ModBlocks.TITANIUM_CLAD_COPPER.get().asItem().getDefaultInstance());
            insertCreativeAfter(event, ModBlocks.TITANIUM_CLAD_COPPER.asItem().getDefaultInstance(), ModBlocks.ETCHED_TITANIUM_COPPER.get().asItem().getDefaultInstance());
            insertCreativeAfter(event, ModBlocks.SMOOTH_TITANIUM.asItem().getDefaultInstance(), ModBlocks.SMOOTH_TITANIUM_STAIRS.get().asItem().getDefaultInstance());
            insertCreativeAfter(event, ModBlocks.SMOOTH_TITANIUM_STAIRS.asItem().getDefaultInstance(), ModBlocks.SMOOTH_TITANIUM_SLAB.get().asItem().getDefaultInstance());
            insertCreativeAfter(event, ModBlocks.TITANIUM_CLAD_COPPER.asItem().getDefaultInstance(), ModBlocks.TITANIUM_CLAD_COPPER_STAIRS.get().asItem().getDefaultInstance());
            insertCreativeAfter(event, ModBlocks.TITANIUM_CLAD_COPPER_STAIRS.asItem().getDefaultInstance(), ModBlocks.TITANIUM_CLAD_COPPER_SLAB.get().asItem().getDefaultInstance());
            insertCreativeAfter(event, ModBlocks.ETCHED_TITANIUM_COPPER.asItem().getDefaultInstance(), ModBlocks.ETCHED_TITANIUM_COPPER_STAIRS.get().asItem().getDefaultInstance());
            insertCreativeAfter(event, ModBlocks.ETCHED_TITANIUM_COPPER_STAIRS.asItem().getDefaultInstance(), ModBlocks.ETCHED_TITANIUM_COPPER_SLAB.get().asItem().getDefaultInstance());

            if (IronsSpellbooksCompatBlocks.isLoaded()) {
                insertCreativeAfter(event, ModBlocks.ETCHED_TITANIUM_COPPER_SLAB.get().asItem().getDefaultInstance(), IronsSpellbooksCompatBlocks.ANOMALY_CORE_ITEM.get().getDefaultInstance());
            }
        }

        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(ModItems.RIPPER_SPAWN_EGG);

            event.accept(ModItems.SMASHER_SPAWN_EGG);
            event.accept(ModItems.CYBERZOMBIE_SPAWN_EGG);
            event.accept(ModItems.CYBERSKELETON_SPAWN_EGG);

            event.accept(ModItems.HOGBOY_SPAWN_EGG);
            event.accept(ModItems.PUNKLIN_SPAWN_EGG);
            event.accept(ModItems.PIGSTROM_SPAWN_EGG);
        }

        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            insertCreativeAfter(event, Items.PHANTOM_MEMBRANE.getDefaultInstance(), ModItems.DATURA_FLOWER.get().getDefaultInstance());
        }



        if (event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS) {
            insertCreativeAfter(event, Items.COOKED_RABBIT.getDefaultInstance(), ModItems.BODYPART_BRAIN.get().getDefaultInstance());
            insertCreativeAfter(event, ModItems.BODYPART_BRAIN.get().getDefaultInstance(), ModItems.COOKED_BRAIN.get().getDefaultInstance());
            insertCreativeAfter(event, ModItems.COOKED_BRAIN.get().getDefaultInstance(), ModItems.BODYPART_HEART.get().getDefaultInstance());
            insertCreativeAfter(event, ModItems.BODYPART_HEART.get().getDefaultInstance(), ModItems.COOKED_HEART.get().getDefaultInstance());
            insertCreativeAfter(event, ModItems.COOKED_HEART.get().getDefaultInstance(), ModItems.BODYPART_LIVER.get().getDefaultInstance());
            insertCreativeAfter(event, ModItems.BODYPART_LIVER.get().getDefaultInstance(), ModItems.COOKED_LIVER.get().getDefaultInstance());
            insertCreativeAfter(event, ModItems.COOKED_LIVER.get().getDefaultInstance(), ModItems.BONE_MARROW.get().getDefaultInstance());

            if (ModItems.ANDOUILLE_SAUSAGE != null
                    && ModItems.ROASTED_ANDOUILLE != null
                    && ModItems.GROUND_OFFAL != null
                    && ModItems.BRAIN_STEW != null) {
                insertCreativeAfter(event, ModItems.BONE_MARROW.get().getDefaultInstance(), ModItems.ANDOUILLE_SAUSAGE.get().getDefaultInstance());
                insertCreativeAfter(event, ModItems.ANDOUILLE_SAUSAGE.get().getDefaultInstance(), ModItems.ROASTED_ANDOUILLE.get().getDefaultInstance());
                insertCreativeAfter(event, ModItems.ROASTED_ANDOUILLE.get().getDefaultInstance(), ModItems.GROUND_OFFAL.get().getDefaultInstance());
                insertCreativeAfter(event, ModItems.GROUND_OFFAL.get().getDefaultInstance(), ModItems.BRAIN_STEW.get().getDefaultInstance());
            }
        }

        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {

            insertCreativeAfter(event, Items.MUSIC_DISC_PIGSTEP.getDefaultInstance(), ModItems.MUSIC_DISC_CYBERPSYCHO.get().getDefaultInstance());
            insertCreativeAfter(event, ModItems.MUSIC_DISC_CYBERPSYCHO.get().getDefaultInstance(), ModItems.MUSIC_DISC_NEON_OVERLORDS.get().getDefaultInstance());
            insertCreativeAfter(event, ModItems.MUSIC_DISC_NEON_OVERLORDS.get().getDefaultInstance(), ModItems.MUSIC_DISC_NEUROHACK.get().getDefaultInstance());
            insertCreativeAfter(event, ModItems.MUSIC_DISC_NEUROHACK.get().getDefaultInstance(), ModItems.MUSIC_DISC_THE_GRID.get().getDefaultInstance());
            insertCreativeAfter(event, ModItems.MUSIC_DISC_THE_GRID.get().getDefaultInstance(), ModItems.MUSIC_DISC_DANGER_SNOW.get().getDefaultInstance());
            insertCreativeAfter(event, ModItems.MUSIC_DISC_DANGER_SNOW.get().getDefaultInstance(), ModItems.MUSIC_DISC_DARK.get().getDefaultInstance());
            insertCreativeAfter(event, ModItems.MUSIC_DISC_DARK.get().getDefaultInstance(), ModItems.MUSIC_DISC_MY_RETRO_JACKET.get().getDefaultInstance());
            insertCreativeAfter(event, ModItems.MUSIC_DISC_MY_RETRO_JACKET.get().getDefaultInstance(), ModItems.MUSIC_DISC_GAMEBOY_GIRL.get().getDefaultInstance());
            insertCreativeAfter(event, ModItems.MUSIC_DISC_GAMEBOY_GIRL.get().getDefaultInstance(), ModItems.MUSIC_DISC_NIGHTMARES_ON_HAUPTSTRASSE.get().getDefaultInstance());
            insertCreativeAfter(event, ModItems.MUSIC_DISC_NIGHTMARES_ON_HAUPTSTRASSE.get().getDefaultInstance(), ModItems.MUSIC_DISC_INTERGALACTIC_WALK.get().getDefaultInstance());
            insertCreativeAfter(event, ModItems.MUSIC_DISC_INTERGALACTIC_WALK.get().getDefaultInstance(), ModItems.MUSIC_DISC_SENSATION.get().getDefaultInstance());
            insertCreativeAfter(event, ModItems.MUSIC_DISC_SENSATION.get().getDefaultInstance(), ModItems.MUSIC_DISC_SHOTGUN.get().getDefaultInstance());
            insertCreativeAfter(event, ModItems.MUSIC_DISC_SHOTGUN.get().getDefaultInstance(), ModItems.MUSIC_DISC_DESCENT_INTO_MADNESS.get().getDefaultInstance());
        }
    }



    private static void insertCreativeAfter(BuildCreativeModeTabContentsEvent event, ItemStack anchor, ItemStack entry) {
        boolean parentHasEntry = event.getParentEntries().contains(entry);
        boolean searchHasEntry = event.getSearchEntries().contains(entry);

        if (!parentHasEntry) {
            if (event.getParentEntries().contains(anchor)) {
                event.insertAfter(anchor, entry, CreativeModeTab.TabVisibility.PARENT_TAB_ONLY);
            } else {
                event.accept(entry, CreativeModeTab.TabVisibility.PARENT_TAB_ONLY);
            }
        }

        if (!searchHasEntry) {
            if (event.getSearchEntries().contains(anchor)) {
                event.insertAfter(anchor, entry, CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
            } else {
                event.accept(entry, CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
            }
        }
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("HELLO from CYBERNETICS on Server Start :P");
    }

    @EventBusSubscriber(modid = CreateCybernetics.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class ClientModEvents {
        private ClientModEvents() {
        }

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            EntityRenderers.register(ModEntities.NUGGET_PROJECTILE.get(), NuggetProjectileRenderer::new);
            EntityRenderers.register(ModEntities.EMP_GRENADE_PROJECTILE.get(), ThrownItemRenderer::new);
            EntityRenderers.register(ModEntities.GUARDIAN_BEAM.get(), GuardianBeamRenderer::new);
            EntityRenderers.register(ModEntities.ARC_LIGHTNING_BOLT.get(), ArcLightningBoltRenderer::new);
            EntityRenderers.register(ModEntities.JACKED_IN_PLAYER_BODY.get(), JackedInPlayerBodyRenderer::new);

            EntityRenderers.register(ModEntities.RIPPER.get(), RipperRenderer::new);

            EntityRenderers.register(ModEntities.SMASHER.get(), SmasherRenderer::new);
            EntityRenderers.register(ModEntities.CYBERZOMBIE.get(), CyberzombieRenderer::new);
            EntityRenderers.register(ModEntities.CYBERSKELETON.get(), CyberskeletonRenderer::new);

            EntityRenderers.register(ModEntities.HOGBOY.get(), HogBoyRenderer::new);
            EntityRenderers.register(ModEntities.PUNKLIN.get(), PunklinRenderer::new);
            EntityRenderers.register(ModEntities.PIGSTROM.get(), PigstromRenderer::new);

            event.enqueueWork(() -> {
                if (!IronsSpellbooksCompatBlocks.isLoaded()) return;

                ItemBlockRenderTypes.setRenderLayer(IronsSpellbooksCompatBlocks.ANOMALY_CORE.get(), RenderType.cutout());
            });
        }

        @SubscribeEvent
        public static void registerBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
            if (!IronsSpellbooksCompatBlocks.isLoaded()) {
                return;
            }

            event.registerBlockEntityRenderer(
                    IronsSpellbooksCompatBlockEntities.ANOMALY_CORE.get(),
                    AnomalyCoreRenderer::new
            );
        }

        @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event) {
            event.register(ModMenuTypes.ROBOSURGEON_MENU.get(), RobosurgeonScreen::new);
            event.register(ModMenuTypes.ENGINEERING_TABLE_MENU.get(), EngineeringTableScreen::new);
            event.register(ModMenuTypes.COMPUTER_MENU.get(), ComputerScreen::new);
            event.register(ModMenuTypes.COMPUTER_TOWER_MENU.get(), ComputerTowerScreen::new);
            event.register(ModMenuTypes.GRAFTING_TABLE_MENU.get(), GraftingTableScreen::new);
            event.register(ModMenuTypes.EXPANDED_INVENTORY_MENU.get(), ExpandedInventoryScreen::new);
            event.register(ModMenuTypes.CHIPWARE_MINI_MENU.get(), ChipwareMiniScreen::new);
            event.register(ModMenuTypes.SPINAL_INJECTOR_MENU.get(), SpinalInjectorScreen::new);
            event.register(ModMenuTypes.ARM_CANNON_MENU.get(), ArmCannonScreen::new);
            event.register(ModMenuTypes.HEAT_ENGINE_MENU.get(), HeatEngineScreen::new);
            event.register(ModMenuTypes.CYBERDECK_MENU.get(), CyberdeckScreen::new);
            event.register(ModMenuTypes.VAMPYRES_MENU.get(), VampyresScreen::new);
            event.register(ModMenuTypes.SURGERY_TABLE_MENU.get(), SurgeryTableScreen::new);
            event.register(ModMenuTypes.RIPPER_TRADE_MENU.get(), RipperTradeScreen::new);
            event.register(ModMenuTypes.SURGERY_PAYMENT_MENU.get(), SurgeryPaymentScreen::new);
            event.register(ModMenuTypes.TATTOO_MENU.get(), TattooArtistScreen::new);

            if (CorpseCompat.isLoaded()) {
                event.register(ModCorpseCompatMenus.CORPSE_CYBERWARE.get(), CorpseCyberwareScreen::new);
            }
        }
    }
}
