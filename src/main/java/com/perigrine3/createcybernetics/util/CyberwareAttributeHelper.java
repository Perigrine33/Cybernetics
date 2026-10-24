package com.perigrine3.createcybernetics.util;

import com.perigrine3.createcybernetics.CreateCybernetics;
import com.perigrine3.createcybernetics.common.attributes.ModAttributes;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.neoforged.neoforge.common.NeoForgeMod;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CyberwareAttributeHelper {
    private static final Map<String, AttributeModifierData> MODIFIER_REGISTRY = new HashMap<>();

    static {
        // Vanilla holders
        Holder<Attribute> maxHealthAttribute = Attributes.MAX_HEALTH;
        Holder<Attribute> armorAttribute = Attributes.ARMOR;
        Holder<Attribute> armorToughnessAttribute = Attributes.ARMOR_TOUGHNESS;
        Holder<Attribute> oxygenBonusAttribute = Attributes.OXYGEN_BONUS;
        Holder<Attribute> speedAttribute = Attributes.MOVEMENT_SPEED;
        Holder<Attribute> knockbackResistAttribute = Attributes.KNOCKBACK_RESISTANCE;
        Holder<Attribute> jumpStrengthAttribute = Attributes.JUMP_STRENGTH;
        Holder<Attribute> attackDamageAttribute = Attributes.ATTACK_DAMAGE;
        Holder<Attribute> attackSpeedAttribute = Attributes.ATTACK_SPEED;
        Holder<Attribute> attackKnockbackAttribute = Attributes.ATTACK_KNOCKBACK;
        Holder<Attribute> luckAttribute = Attributes.LUCK;
        Holder<Attribute> blockReachAttribute = Attributes.BLOCK_INTERACTION_RANGE;
        Holder<Attribute> entityReachAttribute = Attributes.ENTITY_INTERACTION_RANGE;
        Holder<Attribute> stepHeightAttribute = Attributes.STEP_HEIGHT;
        Holder<Attribute> gravityAttribute = Attributes.GRAVITY;
        Holder<Attribute> scaleAttribute = Attributes.SCALE;
        Holder<Attribute> flyingSpeedAttribute = Attributes.FLYING_SPEED;
        Holder<Attribute> blockBreakSpeedAttribute = Attributes.BLOCK_BREAK_SPEED;
        Holder<Attribute> safeFallDistanceAttribute = Attributes.SAFE_FALL_DISTANCE;
        Holder<Attribute> burningTimeAttribute = Attributes.BURNING_TIME;
        Holder<Attribute> underwaterMiningAttribute = Attributes.SUBMERGED_MINING_SPEED;
        Holder<Attribute> waterMovementEfficiency = Attributes.WATER_MOVEMENT_EFFICIENCY;
        Holder<Attribute> miningSpeedAttribute = Attributes.MINING_EFFICIENCY;
        Holder<Attribute> crouchSpeedAttribute = Attributes.SNEAKING_SPEED;

        Holder<Attribute> swimSpeedAttribute = NeoForgeMod.SWIM_SPEED;

        // Modded attributes
        Holder<Attribute> maxRamAttribute = ModAttributes.RAM;
        Holder<Attribute> ramRegenAttribute = ModAttributes.RAM_REGEN;

        Holder<Attribute> xpMultiplierAttribute = ModAttributes.XP_GAIN_MULTIPLIER;
        Holder<Attribute> oreMultiplierAttribute = ModAttributes.ORE_DROP_MULTIPLIER;
        Holder<Attribute> hagglingAttribute = ModAttributes.HAGGLING;
        Holder<Attribute> arrowInaccuracyAttribute = ModAttributes.ARROW_INACCURACY;
        Holder<Attribute> breedingMultiplierAttribute = ModAttributes.BREEDING_MULTIPLIER;
        Holder<Attribute> cropMultiplierAttribute = ModAttributes.CROP_MULTIPLIER;
        Holder<Attribute> elytraSpeedAttribute = ModAttributes.ELYTRA_SPEED;
        Holder<Attribute> elytraHandlingAttribute = ModAttributes.ELYTRA_HANDLING;
        Holder<Attribute> insomniaAttribute = ModAttributes.INSOMNIA;
        Holder<Attribute> enderDamageAttribute = ModAttributes.ENDER_PEARL_DAMAGE;









        registerModifier("cyberleg_speed1", new AttributeModifierData(speedAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "cyberleg_speed_boost1"),
                0.005, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("cyberleg_jump1", new AttributeModifierData(jumpStrengthAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "cyberleg_jump_boost1"),
                0.025, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("cyberleg_speed2", new AttributeModifierData(speedAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "cyberleg_speed_boost2"),
                0.005, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("cyberleg_jump2", new AttributeModifierData(jumpStrengthAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "cyberleg_jump_boost2"),
                0.025, AttributeModifier.Operation.ADD_VALUE));

        registerModifier("cyberarm_strength1", new AttributeModifierData(attackDamageAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "cyberarm_strength_boost1"),
                0.5, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("cyberarm_blockbreak1", new AttributeModifierData(blockBreakSpeedAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "cyberarm_blockbreak_speed1"),
                0.25, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("cyberarm_strength2", new AttributeModifierData(attackDamageAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "cyberarm_strength_boost2"),
                0.5, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("cyberarm_blockbreak2", new AttributeModifierData(blockBreakSpeedAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "cyberarm_blockbreak_speed2"),
                0.25, AttributeModifier.Operation.ADD_VALUE));

        registerModifier("linear_frame_health", new AttributeModifierData(maxHealthAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "linear_frame_health_boost"),
                8.0, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("linear_frame_knockback_resist", new AttributeModifierData(knockbackResistAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "linear_frame_knockback_resistance"),
                1.0, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("linear_frame_blockbreak", new AttributeModifierData(blockBreakSpeedAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "linear_frame_blockbreak_speed"),
                1.0, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("linear_frame_speed", new AttributeModifierData(speedAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "linear_frame_speed_stifle"),
                -0.02, AttributeModifier.Operation.ADD_VALUE));

        registerModifier("subdermalarmor_armor_1", new AttributeModifierData(armorAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "subdermal_armor_boost_1"),
                4.0, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("subdermalarmor_armor_2", new AttributeModifierData(armorAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "subdermal_armor_boost_2"),
                4.0, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("subdermalarmor_armor_3", new AttributeModifierData(armorAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "subdermal_armor_boost_3"),
                4.0, AttributeModifier.Operation.ADD_VALUE));

        registerModifier("metalplating_armor", new AttributeModifierData(armorToughnessAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "metalplating_armor"),
                6.0, AttributeModifier.Operation.ADD_VALUE));

        registerModifier("bonelacing_health_1", new AttributeModifierData(maxHealthAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "bonelacing_health_boost_1"),
                4.0, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("bonelacing_health_2", new AttributeModifierData(maxHealthAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "bonelacing_health_boost_2"),
                4.0, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("bonelacing_health_3", new AttributeModifierData(maxHealthAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "bonelacing_health_boost_3"),
                4.0, AttributeModifier.Operation.ADD_VALUE));

        registerModifier("boneflex_fall_1", new AttributeModifierData(safeFallDistanceAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "boneflex_fall_save_1"),
                3, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("boneflex_fall_2", new AttributeModifierData(safeFallDistanceAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "boneflex_fall_save_2"),
                3, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("boneflex_fall_3", new AttributeModifierData(safeFallDistanceAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "boneflex_fall_save_3"),
                3, AttributeModifier.Operation.ADD_VALUE));

        registerModifier("fall_bracer_fall_1", new AttributeModifierData(safeFallDistanceAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "fall_bracer_fall_save_1"),
                11, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("fall_bracer_fall_2", new AttributeModifierData(safeFallDistanceAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "fall_bracer_fall_save_2"),
                11, AttributeModifier.Operation.ADD_VALUE));

        registerModifier("pneumatic_wrist_block", new AttributeModifierData(blockReachAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "pneumatic_wrist_block_reach"),
                2, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("pneumatic_wrist_entity", new AttributeModifierData(entityReachAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "pneumatic_wrist_entity_reach"),
                2, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("pneumatic_wrist_knockback", new AttributeModifierData(attackKnockbackAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "pneumatic_wrist_knockback_bonus"),
                1, AttributeModifier.Operation.ADD_VALUE));

        registerModifier("oxygen_tank_oxygen", new AttributeModifierData(oxygenBonusAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "oxygen_tank_oxygen_bonus"),
                10.0, AttributeModifier.Operation.ADD_VALUE));

        registerModifier("propeller_swim_1", new AttributeModifierData(swimSpeedAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "propeller_swim_speed_1"),
                3, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("propeller_swim_2", new AttributeModifierData(swimSpeedAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "propeller_swim_speed_2"),
                3, AttributeModifier.Operation.ADD_VALUE));

        registerModifier("isothermal_burning", new AttributeModifierData(burningTimeAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "isothermal_burning_time"),
                -0.95, AttributeModifier.Operation.ADD_VALUE));

        registerModifier("ravager_tendons_size", new AttributeModifierData(scaleAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "ravager_tendons_size_increase"),
                0.2, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("ravager_tendons_strength", new AttributeModifierData(attackDamageAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "ravager_tendons_attack_boost"),
                5, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("ravager_tendons_knockback_resist", new AttributeModifierData(knockbackResistAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "ravager_tendons_knockback_resistance"),
                3, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("ravager_tendons_knockback", new AttributeModifierData(attackKnockbackAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "ravager_tendons_attack_knockback"),
                3, AttributeModifier.Operation.ADD_VALUE));

        registerModifier("hyperoxygenation_speed_1", new AttributeModifierData(speedAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "hyperoxygenation_speed_boost_1"),
                0.03, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("hyperoxygenation_speed_2", new AttributeModifierData(speedAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "hyperoxygenation_speed_boost_2"),
                0.03, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("hyperoxygenation_speed_3", new AttributeModifierData(speedAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "hyperoxygenation_speed_boost_3"),
                0.03, AttributeModifier.Operation.ADD_VALUE));

        registerModifier("synthmuscle_strength", new AttributeModifierData(attackDamageAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "synthmuscle_strength_boost"),
                2, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("synthmuscle_size", new AttributeModifierData(scaleAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "synthmuscle_size_boost"),
                0.1, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("synthmuscle_knockback_resist", new AttributeModifierData(knockbackResistAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "synthmuscle_knockback_resistance"),
                1.5, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("synthmuscle_knockback", new AttributeModifierData(attackKnockbackAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "synthmuscle_attack_knockback"),
                1.5, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("synthmuscle_speed", new AttributeModifierData(speedAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "synthmuscle_speed_boost"),
                0.05, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("synthmuscle_jump", new AttributeModifierData(jumpStrengthAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "synthmuscle_jump_boost"),
                0.1, AttributeModifier.Operation.ADD_VALUE));

        registerModifier("reinforced_knuckles_damage1", new AttributeModifierData(attackDamageAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "reinforced_knuckles_damage_boost1"),
                1.5, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("reinforced_knuckles_damage2", new AttributeModifierData(attackDamageAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "reinforced_knuckles_damage_boost2"),
                1.5, AttributeModifier.Operation.ADD_VALUE));

        registerModifier("calves_sprint", new AttributeModifierData(speedAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "calves_sprint_boost"),
                0.05, AttributeModifier.Operation.ADD_VALUE));

        registerModifier("gyrobladder_speed", new AttributeModifierData(speedAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "gyrobladder_speed_stifle"),
                -0.75, AttributeModifier.Operation.ADD_VALUE));

        registerModifier("claws_attack1", new AttributeModifierData(attackDamageAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "claws_attack_boost1"),
                2.5, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("claws_attack2", new AttributeModifierData(attackDamageAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "claws_attack_boost2"),
                2.5, AttributeModifier.Operation.ADD_VALUE));

        registerModifier("sandevistan_speed", new AttributeModifierData(speedAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "sandevistan_speed_boost"),
                0.5, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("sandevistan_stepheight", new AttributeModifierData(stepHeightAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "sandevistan_stepheight_boost"),
                2, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("sandevistan_jump", new AttributeModifierData(stepHeightAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "sandevistan_jump_boost"),
                2, AttributeModifier.Operation.ADD_VALUE));

        registerModifier("cyberbrain_learn", new AttributeModifierData(xpMultiplierAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "cyberbrain_learn_boost"),
                2, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("cyberbrain_insomnia", new AttributeModifierData(insomniaAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "cyberbrain_insomnia"),
                3, AttributeModifier.Operation.ADD_VALUE));

        registerModifier("neuralprocessor_learn", new AttributeModifierData(xpMultiplierAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "neuralprocessor_learn"),
                1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        registerModifier("neuralprocessor_insomnia", new AttributeModifierData(insomniaAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "neuralprocessor_insomnia"),
                2, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("neuralprocessor_speed", new AttributeModifierData(attackSpeedAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "neuralprocessor_speed"),
                1, AttributeModifier.Operation.ADD_VALUE));

        registerModifier("dragonskin_armor", new AttributeModifierData(armorAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "dragonskin_armor"),
                5, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("dragonskin_toughness", new AttributeModifierData(armorToughnessAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "dragonskin_toughness"),
                5, AttributeModifier.Operation.ADD_VALUE));

        registerModifier("ripperclaw_damage", new AttributeModifierData(attackDamageAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "ripperclaw_damage"),
                2, AttributeModifier.Operation.ADD_VALUE));

        registerModifier("gooeymuscle_fall", new AttributeModifierData(safeFallDistanceAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "gooeymuscle_fall"),
                7, AttributeModifier.Operation.ADD_VALUE));

        registerModifier("ironmantis_damage1", new AttributeModifierData(attackDamageAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "ironmantis_damage1"),
                3, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("ironmantis_speed1", new AttributeModifierData(attackSpeedAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "ironmantis_speed1"),
                0.6, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("ironmantis_damage2", new AttributeModifierData(attackDamageAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "ironmantis_damage2"),
                3, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("ironmantis_speed2", new AttributeModifierData(attackSpeedAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "ironmantis_speed2"),
                0.6, AttributeModifier.Operation.ADD_VALUE));

        registerModifier("coppermantis_damage1", new AttributeModifierData(attackDamageAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "coppermantis_damage1"),
                2, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("coppermantis_speed1", new AttributeModifierData(attackSpeedAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "coppermantis_speed1"),
                0.6, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("coppermantis_damage2", new AttributeModifierData(attackDamageAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "coppermantis_damage2"),
                2, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("coppermantis_speed2", new AttributeModifierData(attackSpeedAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "coppermantis_speed2"),
                0.6, AttributeModifier.Operation.ADD_VALUE));

        registerModifier("titaniummantis_damage1", new AttributeModifierData(attackDamageAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "titaniummantis_damage1"),
                3, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("titaniummantis_speed1", new AttributeModifierData(attackSpeedAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "titaniummantis_speed1"),
                0.8, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("titaniummantis_damage2", new AttributeModifierData(attackDamageAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "titaniummantis_damage2"),
                3, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("titaniummantis_speed2", new AttributeModifierData(attackSpeedAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "titaniummantis_speed2"),
                0.8, AttributeModifier.Operation.ADD_VALUE));

        registerModifier("goldmantis_damage1", new AttributeModifierData(attackDamageAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "goldmantis_damage1"),
                1, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("goldmantis_speed1", new AttributeModifierData(attackSpeedAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "goldmantis_speed1"),
                0.3, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("goldmantis_damage2", new AttributeModifierData(attackDamageAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "goldmantis_damage2"),
                1, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("goldmantis_speed2", new AttributeModifierData(attackSpeedAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "goldmantis_speed2"),
                0.3, AttributeModifier.Operation.ADD_VALUE));

        registerModifier("diamondmantis_damage1", new AttributeModifierData(attackDamageAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "diamondmantis_damage1"),
                4, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("diamondmantis_speed1", new AttributeModifierData(attackSpeedAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "diamondmantis_speed1"),
                0.7, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("diamondmantis_damage2", new AttributeModifierData(attackDamageAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "diamondmantis_damage2"),
                4, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("diamondmantis_speed2", new AttributeModifierData(attackSpeedAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "diamondmantis_speed2"),
                0.7, AttributeModifier.Operation.ADD_VALUE));

        registerModifier("netheritemantis_damage1", new AttributeModifierData(attackDamageAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "netheritemantis_damage1"),
                5, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("netheritemantis_speed1", new AttributeModifierData(attackSpeedAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "netheritemantis_speed1"),
                0.8, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("netheritemantis_damage2", new AttributeModifierData(attackDamageAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "netheritemantis_damage2"),
                5, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("netheritemantis_speed2", new AttributeModifierData(attackSpeedAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "netheritemantis_speed2"),
                0.8, AttributeModifier.Operation.ADD_VALUE));

        registerModifier("synthlungs_breath", new AttributeModifierData(oxygenBonusAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "synthlungs_breath"),
                3, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("synthlungs_speed", new AttributeModifierData(speedAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "synthlungs_speed"),
                0.05, AttributeModifier.Operation.ADD_VALUE));

        registerModifier("ballisticgel_toughness", new AttributeModifierData(armorToughnessAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "ballisticgel_toughness"),
                5, AttributeModifier.Operation.ADD_VALUE));

        registerModifier("heavyknuckles_attackspeed", new AttributeModifierData(attackSpeedAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "heavyknuckles_attackspeed"),
                -3.4, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("heavyknuckles_attackdamage", new AttributeModifierData(attackDamageAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "heavyknuckles_attackdamage"),
                6, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("heavyknuckles_knockback", new AttributeModifierData(attackDamageAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "heavyknuckles_knockback"),
                0.25, AttributeModifier.Operation.ADD_VALUE));




//CHIPWARE
        registerModifier("redshard_strength", new AttributeModifierData(attackDamageAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "redshard_strength_boost"),
                2.5, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("redshard_speed", new AttributeModifierData(attackSpeedAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "redshard_speed_boost"),
                0.3, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        registerModifier("redshard_knockback", new AttributeModifierData(attackKnockbackAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "redshard_knockback_boost"),
                3, AttributeModifier.Operation.ADD_VALUE ));

        registerModifier("orangeshard_ore", new AttributeModifierData(oreMultiplierAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "orangeshard_ore_multiplier"),
                1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        registerModifier("orangeshard_ore_2", new AttributeModifierData(oreMultiplierAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "orangeshard_ore_multiplier_2"),
                1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        registerModifier("orangeshard_mining", new AttributeModifierData(miningSpeedAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "orangeshard_mining_speed"),
                0.5, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("orangeshard_mining_2", new AttributeModifierData(miningSpeedAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "orangeshard_mining_speed_2"),
                0.5, AttributeModifier.Operation.ADD_VALUE));

        registerModifier("yellowshard_haggling", new AttributeModifierData(hagglingAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "yellowshard_haggling_boost"),
                2, AttributeModifier.Operation.ADD_VALUE));

        registerModifier("greenshard_xp", new AttributeModifierData(xpMultiplierAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "greenshard_xp_multiplier"),
                1, AttributeModifier.Operation.ADD_VALUE));

        registerModifier("cyanshard_aim", new AttributeModifierData(arrowInaccuracyAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "cyanshard_aim_bot"),
                -1, AttributeModifier.Operation.ADD_VALUE));

        registerModifier("blueshard_swim", new AttributeModifierData(swimSpeedAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "blueshard_swim_speed"),
                1.5, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("blueshard_mining", new AttributeModifierData(underwaterMiningAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "blueshard_mining_speed"),
                1.5, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("blueshard_movement", new AttributeModifierData(waterMovementEfficiency,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "blueshard_movement_speed"),
                4, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("blueshard_oxygen", new AttributeModifierData(oxygenBonusAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "blueshard_oxygen_boost"),
                7, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

        registerModifier("purpleshard_pearl", new AttributeModifierData(enderDamageAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "purpleshard_pearl_negate"),
                -1, AttributeModifier.Operation.ADD_VALUE));

        registerModifier("pinkshard_breeding", new AttributeModifierData(breedingMultiplierAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "pinkshard_breeding_multiplier"),
                1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

        registerModifier("brownshard_crops", new AttributeModifierData(cropMultiplierAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "brownshard_crops_multiplier"),
                1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

        registerModifier("grayshard_speed", new AttributeModifierData(cropMultiplierAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "grayshard_speed_boost"),
                5, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("grayshard_handling", new AttributeModifierData(cropMultiplierAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "grayshard_handling_boost"),
                5, AttributeModifier.Operation.ADD_VALUE));

        registerModifier("blackshard_crouch", new AttributeModifierData(crouchSpeedAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "blackshard_crouch_speed"),
                2.5, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("blackshard_sprint", new AttributeModifierData(crouchSpeedAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "blackshard_crouch_sprint"),
                3.5, AttributeModifier.Operation.ADD_VALUE));







// FBCs
        registerModifier("gemini_attackstrength", new AttributeModifierData(attackDamageAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "gemini_attackstrength_add"),
                1, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("gemini_attackspeed", new AttributeModifierData(miningSpeedAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "gemini_attackspeed_add"),
                1, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("gemini_miningstrength", new AttributeModifierData(miningSpeedAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "gemini_miningstrength_add"),
                1, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("gemini_speed", new AttributeModifierData(speedAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "gemini_speed_add"),
                0.02, AttributeModifier.Operation.ADD_VALUE));

        registerModifier("samson_attackstrength", new AttributeModifierData(attackDamageAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "samson_attackstrength_add"),
                2, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("samson_miningstrength", new AttributeModifierData(miningSpeedAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "samson_miningstrength_add"),
                2, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("samson_durability", new AttributeModifierData(armorAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "samson_durability_add"),
                8, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("samson_watermove", new AttributeModifierData(waterMovementEfficiency,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "samson_watermove_subtract"),
                -0.75, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("samson_weight", new AttributeModifierData(gravityAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "samson_weight_add"),
                0.1, AttributeModifier.Operation.ADD_VALUE));

        registerModifier("eclipse_speed", new AttributeModifierData(speedAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "eclipse_speed_add"),
                0.1, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("eclipse_sprintspeed", new AttributeModifierData(speedAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "eclipse_sprintspeed_add"),
                0.2, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("eclipse_crouchspeed", new AttributeModifierData(crouchSpeedAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "eclipse_crouchspeed_add"),
                0.5, AttributeModifier.Operation.ADD_VALUE));

        registerModifier("spyder_crouchspeed", new AttributeModifierData(crouchSpeedAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "spyder_crouchspeed_add"),
                0.5, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("spyder_jumpheight", new AttributeModifierData(jumpStrengthAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "spyder_jumpheight_add"),
                0.1, AttributeModifier.Operation.ADD_VALUE));

        registerModifier("wingman_elytraspeed", new AttributeModifierData(elytraSpeedAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "wingman_elytraspeed_add"),
                1, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("wingman_elytrahandling", new AttributeModifierData(elytraHandlingAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "wingman_elytrahandling_add"),
                4, AttributeModifier.Operation.ADD_VALUE));

        registerModifier("aquarius_movement", new AttributeModifierData(waterMovementEfficiency,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "aquarius_movement_add"),
                5, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("aquarius_mining", new AttributeModifierData(underwaterMiningAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "aquarius_mining_add"),
                2, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("aquarius_swim", new AttributeModifierData(swimSpeedAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "aquarius_swim_add"),
                5, AttributeModifier.Operation.ADD_VALUE));

        registerModifier("dymond_miningspeed", new AttributeModifierData(miningSpeedAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "dymond_miningspeed_add"),
                3, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("dymond_weight", new AttributeModifierData(gravityAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "dymond_weight_add"),
                0.01, AttributeModifier.Operation.ADD_VALUE));

        registerModifier("dragoon_weight", new AttributeModifierData(gravityAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "dragoon_weight_add"),
                0.1, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("dragoon_size", new AttributeModifierData(scaleAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "dragoon_size_add"),
                0.3, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("dragoon_attack", new AttributeModifierData(attackDamageAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "dragoon_attack_add"),
                7, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("dragoon_resist", new AttributeModifierData(knockbackResistAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "dragoon_resist_add"),
                5, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("dragoon_knockback", new AttributeModifierData(attackKnockbackAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "dragoon_knockback_add"),
                5, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("dragoon_jump", new AttributeModifierData(jumpStrengthAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "dragoon_jump_add"),
                0.5, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("dragoon_step", new AttributeModifierData(stepHeightAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "dragoon_step_add"),
                0.5, AttributeModifier.Operation.ADD_VALUE));

        registerModifier("copernicus_oxygen", new AttributeModifierData(oxygenBonusAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "copernicus_oxygen_add"),
                20, AttributeModifier.Operation.ADD_VALUE));

        registerModifier("genos_speed", new AttributeModifierData(speedAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "genos_sprintspeed"),
                0.05, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("genos_strength", new AttributeModifierData(attackDamageAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "genos_strength_add"),
                4, AttributeModifier.Operation.ADD_VALUE));

        registerModifier("kildare_strength", new AttributeModifierData(attackDamageAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "kildare_strength_add"),
                1, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("kildare_speed", new AttributeModifierData(speedAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "kildare_speed_add"),
                0.01, AttributeModifier.Operation.ADD_VALUE));





        registerModifier("sculked_strength", new AttributeModifierData(attackDamageAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "sculked_strength"),
                1, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("sculked_speed", new AttributeModifierData(speedAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "sculked_speed"),
                0.01, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("sculked_size1", new AttributeModifierData(scaleAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "sculked_size1"),
                0.1, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("sculked_size2", new AttributeModifierData(scaleAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "sculked_size2"),
                0.1, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("sculked_size3", new AttributeModifierData(scaleAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "sculked_size3"),
                0.1, AttributeModifier.Operation.ADD_VALUE));

        registerModifier("cyberdeck_ram", new AttributeModifierData(maxRamAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "cyberdeck_ram"),
                15, AttributeModifier.Operation.ADD_VALUE));

        registerModifier("ice_defense_ram", new AttributeModifierData(maxRamAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "ice_defense_ram"),
                -5, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("ice_defense_ram_regen", new AttributeModifierData(ramRegenAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "ice_defense_ram_regen"),
                2, AttributeModifier.Operation.ADD_VALUE));

        registerModifier("neural_ram", new AttributeModifierData(maxRamAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "neural_ram"),
                10, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("neural_ram_regen", new AttributeModifierData(ramRegenAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "neural_ram_regen"),
                3, AttributeModifier.Operation.ADD_VALUE));

        registerModifier("cpu_ram", new AttributeModifierData(maxRamAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "cpu_ram"),
                20, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("cpu_ram_regen", new AttributeModifierData(ramRegenAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "cpu_ram_regen"),
                5, AttributeModifier.Operation.ADD_VALUE));

        registerModifier("eyeofdedender_ram", new AttributeModifierData(maxRamAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "eyeofdedender_ram"),
                -3, AttributeModifier.Operation.ADD_VALUE));

        registerModifier("enderjammer_ram", new AttributeModifierData(maxRamAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "enderjammer_ram"),
                -3, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("enderjammer_ram_regen", new AttributeModifierData(ramRegenAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "enderjammer_ram_regen"),
                -2, AttributeModifier.Operation.ADD_VALUE));

        registerModifier("chipware_ram", new AttributeModifierData(maxRamAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "chipware_ram"),
                4, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("chipware_ram_regen", new AttributeModifierData(ramRegenAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "chipware_ram_regen"),
                2, AttributeModifier.Operation.ADD_VALUE));

        registerModifier("coaxial_ram", new AttributeModifierData(maxRamAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "coaxial_ram"),
                5, AttributeModifier.Operation.ADD_VALUE));

        registerModifier("kerenzikov_ram", new AttributeModifierData(maxRamAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "kerenzikov_ram"),
                -4, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("kerenzikov_ram_regen", new AttributeModifierData(ramRegenAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "kerenzikov_ram_regen"),
                5, AttributeModifier.Operation.ADD_VALUE));

        registerModifier("berserk_ram", new AttributeModifierData(maxRamAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "berserk_ram"),
                -5, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("berserk_ram_regen", new AttributeModifierData(ramRegenAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "berserk_ram_regen"),
                1, AttributeModifier.Operation.ADD_VALUE));

        registerModifier("ramdeck1_ram", new AttributeModifierData(maxRamAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "ramdeck1_ram"),
                4, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("ramdeck2_ram", new AttributeModifierData(maxRamAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "ramdeck2_ram"),
                4, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("ramdeck3_ram", new AttributeModifierData(maxRamAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "ramdeck3_ram"),
                4, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("ramdeck1_ram_regen", new AttributeModifierData(ramRegenAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "ramdeck1_ram_regen"),
                1, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("ramdeck2_ram_regen", new AttributeModifierData(ramRegenAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "ramdeck2_ram_regen"),
                1, AttributeModifier.Operation.ADD_VALUE));
        registerModifier("ramdeck3_ram_regen", new AttributeModifierData(ramRegenAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "ramdeck3_ram_regen"),
                1, AttributeModifier.Operation.ADD_VALUE));







// EXOSUITS
        registerModifier("exosuit_strength", new AttributeModifierData(attackDamageAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "exosuit_strength_add"),
                1.5, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        registerModifier("exosuit_knockback", new AttributeModifierData(attackKnockbackAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "exosuit_knockback_add"),
                1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        registerModifier("exosuit_movementspeed", new AttributeModifierData(speedAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "exosuit_movementspeed_add"),
                0.04, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        registerModifier("exosuit_attackspeed", new AttributeModifierData(attackSpeedAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "exosuit_attackspeed_add"),
                0.25, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        registerModifier("exosuit_miningspeed", new AttributeModifierData(miningSpeedAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "exosuit_miningspeed_add"),
                0.25, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        registerModifier("exosuit_jumpheight", new AttributeModifierData(jumpStrengthAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "exosuit_jumpheight_add"),
                0.5, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));

        registerModifier("exosuit_no_strength", new AttributeModifierData(attackDamageAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "exosuit_strength_remove"),
                -1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        registerModifier("exosuit_no_knockback", new AttributeModifierData(attackDamageAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "exosuit_knockback_remove"),
                -0.5, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        registerModifier("exosuit_no_movementspeed", new AttributeModifierData(speedAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "exosuit_movementspeed_remove"),
                -0.25, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        registerModifier("exosuit_no_attackspeed", new AttributeModifierData(attackSpeedAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "exosuit_attackspeed_remove"),
                -0.5, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        registerModifier("exosuit_no_miningspeed", new AttributeModifierData(miningSpeedAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "exosuit_miningspeed_remove"),
                -0.5, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        registerModifier("exosuit_no_jumpheight", new AttributeModifierData(jumpStrengthAttribute,
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "exosuit_jumpheight_remove"),
                -0.25, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));

    }




    public static void registerModifier(String id, AttributeModifierData data) {
        MODIFIER_REGISTRY.put(id, data);
    }

    public static void registerModifierDynamicAttribute(String id, ResourceLocation attributeId,
                                                        ResourceLocation modifierName, double amount,
                                                        AttributeModifier.Operation op) {
        registerModifier(id, new AttributeModifierData(attributeId, modifierName, amount, op));
    }

    public static void applyModifier(LivingEntity entity, String modifierId) {
        AttributeModifierData data = MODIFIER_REGISTRY.get(modifierId);
        if (data == null) {
            CreateCybernetics.LOGGER.error("Attempted to apply unknown modifier: " + modifierId);
            return;
        }

        Holder<Attribute> attr = data.resolveAttribute();
        if (attr == null) return;

        var inst = entity.getAttribute(attr);
        if (inst == null) return;

        AttributeModifier existing = inst.getModifier(data.name);
        if (existing != null
                && Double.compare(existing.amount(), data.amount) == 0
                && existing.operation() == data.operation) {
            return;
        }

        inst.addOrReplacePermanentModifier(new AttributeModifier(data.name, data.amount, data.operation));
    }

    public static void removeModifier(LivingEntity entity, String modifierId) {
        AttributeModifierData data = MODIFIER_REGISTRY.get(modifierId);
        if (data == null) {
            CreateCybernetics.LOGGER.error("Attempted to remove unknown modifier: " + modifierId);
            return;
        }

        Holder<Attribute> attr = data.resolveAttribute();
        if (attr == null) return;

        var inst = entity.getAttribute(attr);
        if (inst == null || !inst.hasModifier(data.name)) return;

        inst.removeModifier(data.name);
    }

    public static void removeAllRegisteredModifiers(LivingEntity entity) {
        for (AttributeModifierData data : MODIFIER_REGISTRY.values()) {
            Holder<Attribute> attr = data.resolveAttribute();
            if (attr == null) continue;

            var inst = entity.getAttribute(attr);
            if (inst == null || !inst.hasModifier(data.name)) continue;

            inst.removeModifier(data.name);
        }
    }

    public static boolean hasModifier(LivingEntity entity, String modifierId) {
        AttributeModifierData data = MODIFIER_REGISTRY.get(modifierId);
        if (data == null) return false;

        Holder<Attribute> attr = data.resolveAttribute();
        if (attr == null) return false;

        var inst = entity.getAttribute(attr);
        return inst != null && inst.getModifier(data.name) != null;
    }



    private static class AttributeModifierData {
        final Holder<Attribute> attribute;
        final ResourceLocation attributeId;
        final ResourceLocation name;
        final double amount;
        final AttributeModifier.Operation operation;
        @Nullable
        private volatile Holder<Attribute> cachedResolvedAttribute;

        AttributeModifierData(Holder<Attribute> attribute, ResourceLocation name,
                              double amount, AttributeModifier.Operation operation) {
            this.attribute = attribute;
            this.attributeId = null;
            this.name = name;
            this.amount = amount;
            this.operation = operation;
            this.cachedResolvedAttribute = attribute;
        }

        AttributeModifierData(ResourceLocation attributeId, ResourceLocation name,
                              double amount, AttributeModifier.Operation operation) {
            this.attribute = null;
            this.attributeId = attributeId;
            this.name = name;
            this.amount = amount;
            this.operation = operation;
        }

        @Nullable
        Holder<Attribute> resolveAttribute() {
            Holder<Attribute> resolved = cachedResolvedAttribute;
            if (resolved != null) return resolved;
            if (attribute != null) return attribute;
            if (attributeId == null) return null;

            var key = net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ATTRIBUTE, attributeId);
            resolved = net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE.getHolder(key)
                    .map(h -> (Holder<Attribute>) h)
                    .orElse(null);
            if (resolved != null) {
                cachedResolvedAttribute = resolved;
            }
            return resolved;
        }
    }

    public static boolean setPermanentModifier(
            LivingEntity entity,
            Holder<Attribute> attribute,
            ResourceLocation modifierId,
            double amount,
            AttributeModifier.Operation operation
    ) {
        if (entity == null || attribute == null || modifierId == null || operation == null) {
            return false;
        }

        var instance = entity.getAttribute(attribute);
        if (instance == null) {
            return false;
        }

        AttributeModifier existing = instance.getModifier(modifierId);
        if (existing != null
                && Double.compare(existing.amount(), amount) == 0
                && existing.operation() == operation) {
            return false;
        }

        instance.addOrReplacePermanentModifier(new AttributeModifier(modifierId, amount, operation));
        return true;
    }

    public static boolean removePermanentModifier(
            LivingEntity entity,
            Holder<Attribute> attribute,
            ResourceLocation modifierId
    ) {
        if (entity == null || attribute == null || modifierId == null) {
            return false;
        }

        var instance = entity.getAttribute(attribute);
        if (instance == null || !instance.hasModifier(modifierId)) {
            return false;
        }

        return instance.removeModifier(modifierId);
    }

    public static int getIntValue(LivingEntity entity, Holder<Attribute> attribute, int fallback) {
        if (entity == null || attribute == null) {
            return fallback;
        }

        var instance = entity.getAttribute(attribute);
        if (instance == null) {
            return fallback;
        }

        return net.minecraft.util.Mth.floor(instance.getValue());
    }

    public static void setBaseValue(LivingEntity entity, Holder<Attribute> attribute, double value) {
        if (entity == null || attribute == null) {
            return;
        }

        var instance = entity.getAttribute(attribute);
        if (instance == null) {
            return;
        }

        instance.setBaseValue(value);
    }
}