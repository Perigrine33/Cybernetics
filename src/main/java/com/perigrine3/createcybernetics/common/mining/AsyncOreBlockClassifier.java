package com.perigrine3.createcybernetics.common.mining;

import com.perigrine3.createcybernetics.util.ModTags;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.Tags;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public final class AsyncOreBlockClassifier {

    private static final ExecutorService INDEX_EXECUTOR = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "createcybernetics-ore-classifier");
        thread.setDaemon(true);
        thread.setPriority(Thread.NORM_PRIORITY - 1);
        return thread;
    });

    private final AtomicReference<Map<Block, OreBlockCategory>> indexedSnapshot =
            new AtomicReference<>(Collections.emptyMap());
    private final ConcurrentHashMap<Block, OreBlockCategory> runtimeCache =
            new ConcurrentHashMap<>(256);
    private final AtomicLong generationCounter = new AtomicLong(0L);
    private final AtomicReference<CompletableFuture<Map<Block, OreBlockCategory>>> activeRebuild =
            new AtomicReference<>();

    public OreBlockCategory classify(BlockState state) {
        if (state == null || state.isAir()) {
            return OreBlockCategory.NON_ORE;
        }

        Block block = state.getBlock();
        Map<Block, OreBlockCategory> snapshot = indexedSnapshot.get();
        OreBlockCategory indexed = snapshot.get(block);
        if (indexed != null) {
            return indexed;
        }

        OreBlockCategory cached = runtimeCache.get(block);
        if (cached != null) {
            return cached;
        }

        OreBlockCategory evaluated = evaluateBlockState(state);
        runtimeCache.putIfAbsent(block, evaluated);
        return evaluated;
    }

    public CompletableFuture<Map<Block, OreBlockCategory>> rebuildIndexAsync() {
        final long generation = generationCounter.incrementAndGet();
        runtimeCache.clear();

        CompletableFuture<Map<Block, OreBlockCategory>> future = CompletableFuture.supplyAsync(
                () -> buildRegistrySnapshot(generation),
                INDEX_EXECUTOR
        ).whenComplete((builtMap, throwable) -> {
            if (throwable == null && builtMap != null && generationCounter.get() == generation) {
                indexedSnapshot.set(builtMap);
                runtimeCache.clear();
            }
        });

        activeRebuild.set(future);
        return future;
    }

    public void invalidateAll() {
        generationCounter.incrementAndGet();
        indexedSnapshot.set(Collections.emptyMap());
        runtimeCache.clear();
    }

    public int getIndexedBlockCount() {
        return indexedSnapshot.get().size();
    }

    private Map<Block, OreBlockCategory> buildRegistrySnapshot(long expectedGeneration) {
        IdentityHashMap<Block, OreBlockCategory> table = new IdentityHashMap<>(512);
        for (Block block : BuiltInRegistries.BLOCK) {
            if (generationCounter.get() != expectedGeneration) {
                return Collections.emptyMap();
            }
            if (block == null) {
                continue;
            }
            BlockState defaultState = block.defaultBlockState();
            OreBlockCategory category = evaluateBlockState(defaultState);
            table.put(block, category);
        }
        return Collections.unmodifiableMap(table);
    }

    public static OreBlockCategory evaluateBlockState(BlockState state) {
        if (state == null || state.isAir()) {
            return OreBlockCategory.NON_ORE;
        }
        if (state.is(Blocks.ANCIENT_DEBRIS)) {
            return OreBlockCategory.ANCIENT_DEBRIS;
        }
        if (state.is(Tags.Blocks.ORES) || state.is(ModTags.Blocks.C_TITANIUM)) {
            return OreBlockCategory.STANDARD_ORE;
        }
        if (matchesOreTagConvention(state)) {
            return OreBlockCategory.STANDARD_ORE;
        }
        return OreBlockCategory.NON_ORE;
    }

    public static boolean matchesOreTagConvention(BlockState state) {
        Iterator<TagKey<Block>> iterator = state.getTags().iterator();
        while (iterator.hasNext()) {
            TagKey<Block> tagKey = iterator.next();
            if (tagKey != null && isOreTagLocation(tagKey.location())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isOreTagLocation(ResourceLocation location) {
        if (location == null) {
            return false;
        }
        String namespace = location.getNamespace();
        String path = location.getPath();
        if ("c".equals(namespace) || "forge".equals(namespace)) {
            return "ores".equals(path)
                    || path.startsWith("ores/")
                    || path.startsWith("ores_in_ground/");
        }
        if ("minecraft".equals(namespace)) {
            return path.endsWith("_ores");
        }
        return false;
    }
}
