package com.perigrine3.createcybernetics.block;

import com.perigrine3.createcybernetics.CreateCybernetics;
import com.perigrine3.createcybernetics.item.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(CreateCybernetics.MODID);



//OREBLOCKS
    public static final DeferredBlock<Block> TITANIUMORE_BLOCK = registerBlock("titaniumore_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(3f).requiresCorrectToolForDrops().sound(SoundType.STONE)), true);
    public static final DeferredBlock<Block> DEEPSLATE_TITANIUMORE_BLOCK = registerBlock("deepslate_titaniumore_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(3f).requiresCorrectToolForDrops().sound(SoundType.DEEPSLATE)), true);

//BUILDING BLOCKS
    public static final DeferredBlock<Block> TITANIUM_BLOCK = registerBlock("titanium_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(6f).requiresCorrectToolForDrops().sound(SoundType.METAL)), true);
    public static final DeferredBlock<Block> RAW_TITANIUM_BLOCK = registerBlock("raw_titanium_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(3f).requiresCorrectToolForDrops().sound(SoundType.STONE)), true);

    public static final DeferredBlock<Block> SMOOTH_TITANIUM = registerBlock("smooth_titanium",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(6f).requiresCorrectToolForDrops().sound(SoundType.METAL)), true);
    public static final DeferredBlock<Block> TITANIUM_GRATE = registerBlock("titanium_grate",
            () -> new TitaniumGrateBlock(BlockBehaviour.Properties.of().noOcclusion()
                    .strength(6f).requiresCorrectToolForDrops().sound(SoundType.METAL)), true);
    public static final DeferredBlock<Block> TITANIUM_CLAD_COPPER = registerBlock("titanium_clad_copper",
            () -> new TitaniumCladCopperBlock(BlockBehaviour.Properties.of()
                    .strength(5f).requiresCorrectToolForDrops().sound(SoundType.METAL)), true);
    public static final DeferredBlock<Block> ETCHED_TITANIUM_COPPER = registerBlock("etched_titanium_copper",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(5f).requiresCorrectToolForDrops().sound(SoundType.METAL)), true);

//NON-BLOCK BLOCKS
    public static final DeferredBlock<StairBlock> SMOOTH_TITANIUM_STAIRS = registerBlock("smooth_titanium_stairs",
            () -> new StairBlock(ModBlocks.SMOOTH_TITANIUM.get().defaultBlockState(),
                    BlockBehaviour.Properties.of().strength(6f).requiresCorrectToolForDrops().sound(SoundType.METAL)), true);
    public static final DeferredBlock<SlabBlock> SMOOTH_TITANIUM_SLAB = registerBlock("smooth_titanium_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.of().strength(6f).requiresCorrectToolForDrops().sound(SoundType.METAL)), true);

    public static final DeferredBlock<StairBlock> TITANIUM_CLAD_COPPER_STAIRS = registerBlock("titanium_clad_copper_stairs",
            () -> new StairBlock(ModBlocks.SMOOTH_TITANIUM.get().defaultBlockState(),
                    BlockBehaviour.Properties.of().strength(6f).requiresCorrectToolForDrops().sound(SoundType.METAL)), true);
    public static final DeferredBlock<SlabBlock> TITANIUM_CLAD_COPPER_SLAB = registerBlock("titanium_clad_copper_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.of().strength(6f).requiresCorrectToolForDrops().sound(SoundType.METAL)), true);

    public static final DeferredBlock<StairBlock> ETCHED_TITANIUM_COPPER_STAIRS = registerBlock("etched_titanium_copper_stairs",
            () -> new StairBlock(ModBlocks.SMOOTH_TITANIUM.get().defaultBlockState(),
                    BlockBehaviour.Properties.of().strength(6f).requiresCorrectToolForDrops().sound(SoundType.METAL)), true);
    public static final DeferredBlock<SlabBlock> ETCHED_TITANIUM_COPPER_SLAB = registerBlock("etched_titanium_copper_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.of().strength(6f).requiresCorrectToolForDrops().sound(SoundType.METAL)), true);

//FUNCTIONAL BLOCKS
    public static final DeferredBlock<Block> SURGERY_CHAMBER_BOTTOM = registerBlock("surgery_chamber",
            () -> new SurgeryChamberBlockBottom(BlockBehaviour.Properties.of().strength(6, 8)
                    .noOcclusion().sound(SoundType.METAL)), true);
    public static final DeferredBlock<Block> SURGERY_CHAMBER_TOP = registerBlock("surgery_chamber_top",
            () -> new SurgeryChamberBlockTop(BlockBehaviour.Properties.of().strength(6, 8)
                    .noOcclusion().sound(SoundType.METAL)), false);
    public static final DeferredBlock<Block> ROBOSURGEON = registerBlock("robosurgeon",
            () -> new RobosurgeonBlock(BlockBehaviour.Properties.of().strength(6, 8)
                    .noOcclusion().sound(SoundType.METAL)), true);
    public static final DeferredBlock<Block> CHARGING_BLOCK = registerBlock("charging_block",
            () -> new ChargingBlock(BlockBehaviour.Properties.of().strength(6, 8)
                    .noOcclusion().sound(SoundType.METAL)), true);
    public static final DeferredBlock<Block> ENGINEERING_TABLE = registerBlock("engineering_table",
            () -> new EngineeringTableBlock(BlockBehaviour.Properties.of().strength(6, 6)
                    .noOcclusion().sound(SoundType.METAL)), true);
    public static final DeferredBlock<Block> GRAFTING_TABLE = registerBlock("grafting_table",
            () -> new GraftingTableBlock(BlockBehaviour.Properties.of().strength(3, 3)
                    .noOcclusion().sound(SoundType.METAL)), true);
    public static final DeferredBlock<Block> HOLOPROJECTOR = registerBlock("holoprojector",
            () -> new HoloprojectorBlock(BlockBehaviour.Properties.of().strength(3, 3)
                    .noOcclusion().sound(SoundType.METAL)), true);
    public static final DeferredBlock<Block> SURGERY_TABLE = registerBlock("surgery_table",
            () -> new SurgeryTableBlock(BlockBehaviour.Properties.of().strength(2.5F, 3)
                    .noOcclusion().sound(SoundType.METAL)), true);

    public static final DeferredBlock<Block> COMPUTER = registerBlock("computer",
            () -> new ComputerBlock(BlockBehaviour.Properties.of().strength(3, 4)
                    .noOcclusion().sound(SoundType.METAL)
                    .lightLevel(state -> state.getValue(ComputerBlock.POWERED) ? 5 : 0)), true);
    public static final DeferredBlock<Block> COMPUTER_TOWER = registerBlock("computer_tower",
            () -> new ComputerTowerBlock(BlockBehaviour.Properties.of().strength(3, 4)
                    .noOcclusion().sound(SoundType.METAL)), true);

    public static final DeferredBlock<Block> MACHINE_CASING = registerBlock("machine_casing",
            () -> new MachineCasingBlock(BlockBehaviour.Properties.of().strength(6, 8)
                    .requiresCorrectToolForDrops().sound(SoundType.METAL)), true);

    public static final DeferredBlock<Block> SERVER_TOWER = registerBlock("server_tower",
            () -> new ServerTowerBlock(BlockBehaviour.Properties.of().strength(6, 8)
                    .noOcclusion().pushReaction(PushReaction.BLOCK).sound(SoundType.METAL)), false);
    public static final DeferredBlock<Block> SERVER_TOWER_PART = registerBlock("server_tower_part",
            () -> new ServerTowerPartBlock(BlockBehaviour.Properties.of().strength(6, 8)
                    .noLootTable().noOcclusion().pushReaction(PushReaction.BLOCK).sound(SoundType.METAL)), false);
//CYBERSPACE BLOCKS
    public static final DeferredBlock<Block> DIGITAL_ARCHITECTURE = registerBlock("digital_architecture",
            () -> new DigitalArchitectureBlock(BlockBehaviour.Properties.of()
                    .strength(-1.0F, 3600000.0F).sound(SoundType.METAL)), true);
    public static final DeferredBlock<Block> DAEMON_ANCHOR = registerBlock("daemon_anchor",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(-1.0F, 3600000.0F).sound(SoundType.METAL)), true);
    public static final DeferredBlock<DataCacheBlock> DATA_CACHE = registerBlock("data_cache",
            () -> new DataCacheBlock(BlockBehaviour.Properties.of()
                    .strength(-1.0F, 3600000.0F).sound(SoundType.METAL)), true);
    public static final DeferredBlock<Block> ACCESS_NODE = registerBlock("access_node",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(-1.0F, 3600000.0F).sound(SoundType.METAL)), true);
    public static final DeferredBlock<DigitalGateBlock> DIGITAL_GATE = registerBlock("digital_gate",
            () -> new DigitalGateBlock(BlockBehaviour.Properties.of()
                    .strength(-1.0F, 3600000.0F)
                    .pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK)
                    .sound(SoundType.METAL)), true);

    public static final DeferredBlock<AmethystClusterBlock> TARGET_CODE_FRAGMENT = registerBlock("target_code_fragment",
            () -> new AmethystClusterBlock(7.0F, 3.0F, BlockBehaviour.Properties.ofFullCopy(Blocks.AMETHYST_CLUSTER)), true);
    public static final DeferredBlock<AmethystClusterBlock> FUNCTION_CODE_FRAGMENT = registerBlock("function_code_fragment",
            () -> new AmethystClusterBlock(7.0F, 3.0F, BlockBehaviour.Properties.ofFullCopy(Blocks.AMETHYST_CLUSTER)), true);
    public static final DeferredBlock<AmethystClusterBlock> MODIFIER_CODE_FRAGMENT = registerBlock("modifier_code_fragment",
            () -> new AmethystClusterBlock(7.0F, 3.0F, BlockBehaviour.Properties.ofFullCopy(Blocks.AMETHYST_CLUSTER)), true);
    public static final DeferredBlock<AmethystClusterBlock> RAW_DATA_FRAGMENT = registerBlock("raw_data_fragment",
            () -> new AmethystClusterBlock(7.0F, 3.0F, BlockBehaviour.Properties.ofFullCopy(Blocks.AMETHYST_CLUSTER)), true);

//PLANT BLOCKS
    public static final DeferredBlock<Block> DATURA_BUSH = BLOCKS.register("datura_bush",
            () -> new DaturaBushBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SWEET_BERRY_BUSH)));
    public static final DeferredBlock<Block> POTTED_DATURA = BLOCKS.register("potted_datura",
            () -> new PottedDaturaBlock(() -> (FlowerPotBlock) Blocks.FLOWER_POT, BlockBehaviour.Properties.ofFullCopy(Blocks.POTTED_POPPY).noOcclusion()));

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block, boolean registerItem) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);

        if (registerItem) {
            registerBlockItem(name, toReturn);
        }

        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}