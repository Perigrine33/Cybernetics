package com.perigrine3.createcybernetics.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.perigrine3.createcybernetics.ConfigValues;
import com.perigrine3.createcybernetics.entity.ModEntities;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;

public record ConfigurableEntitySpawnBiomeModifier(
        HolderSet<Biome> biomes,
        String spawnId
) implements BiomeModifier {
    public static final MapCodec<ConfigurableEntitySpawnBiomeModifier> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Biome.LIST_CODEC.fieldOf("biomes").forGetter(ConfigurableEntitySpawnBiomeModifier::biomes),
            Codec.STRING.fieldOf("spawn_id").forGetter(ConfigurableEntitySpawnBiomeModifier::spawnId)
    ).apply(instance, ConfigurableEntitySpawnBiomeModifier::new));

    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase != Phase.ADD) return;
        if (!biomes.contains(biome)) return;

        SpawnConfig spawn = getSpawnConfig(spawnId);
        if (spawn == null) return;
        if (spawn.weight <= 0) return;

        builder.getMobSpawnSettings().addSpawn(spawn.entity.getCategory(), new MobSpawnSettings.SpawnerData(
                spawn.entity,
                spawn.weight,
                spawn.minGroup,
                spawn.maxGroup
        ));
    }

    @Override
    public MapCodec<? extends BiomeModifier> codec() {
        return ModBiomeModifierSerializers.CONFIGURABLE_ENTITY_SPAWN.get();
    }

    private static SpawnConfig getSpawnConfig(String spawnId) {
        return switch (spawnId) {
            case "hogboy" -> new SpawnConfig(
                    ModEntities.HOGBOY.get(),
                    ConfigValues.HOGBOY_SPAWN_WEIGHT,
                    ConfigValues.HOGBOY_MIN_GROUP,
                    ConfigValues.HOGBOY_MAX_GROUP
            );
            case "punklin" -> new SpawnConfig(
                    ModEntities.PUNKLIN.get(),
                    ConfigValues.PUNKLIN_SPAWN_WEIGHT,
                    ConfigValues.PUNKLIN_MIN_GROUP,
                    ConfigValues.PUNKLIN_MAX_GROUP
            );
            case "pigstrom" -> new SpawnConfig(
                    ModEntities.PIGSTROM.get(),
                    ConfigValues.PIGSTROM_SPAWN_WEIGHT,
                    ConfigValues.PIGSTROM_MIN_GROUP,
                    ConfigValues.PIGSTROM_MAX_GROUP
            );
            default -> null;
        };
    }

    private record SpawnConfig(EntityType<?> entity, int weight, int minGroup, int maxGroup) {
    }
}