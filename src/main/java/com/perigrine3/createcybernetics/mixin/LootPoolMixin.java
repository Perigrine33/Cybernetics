package com.perigrine3.createcybernetics.mixin;

import com.perigrine3.createcybernetics.common.mining.MiningRollEngine;
import com.perigrine3.createcybernetics.event.custom.OreMultiplierHandler;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BiFunction;
import java.util.function.Consumer;

@Mixin(LootPool.class)
public abstract class LootPoolMixin {

    @Redirect(
            method = "addRandomItems",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/storage/loot/functions/LootItemFunction;decorate(Ljava/util/function/BiFunction;Ljava/util/function/Consumer;Lnet/minecraft/world/level/storage/loot/LootContext;)Ljava/util/function/Consumer;"
            )
    )
    private Consumer<ItemStack> createcybernetics$decorateOreDropConsumer(
            BiFunction<ItemStack, LootContext, ItemStack> compositeFunction,
            Consumer<ItemStack> stackConsumer,
            LootContext context
    ) {
        Consumer<ItemStack> adaptedConsumer = OreMultiplierHandler.decorateDropConsumer(stackConsumer, context);
        return LootItemFunction.decorate(compositeFunction, adaptedConsumer, context);
    }

    @Redirect(
            method = "addRandomItems",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/storage/loot/providers/number/NumberProvider;getInt(Lnet/minecraft/world/level/storage/loot/LootContext;)I"
            )
    )
    private int createcybernetics$scaleOrePoolRolls(NumberProvider rollsProvider, LootContext context) {
        int baseRolls = rollsProvider.getInt(context);
        return OreMultiplierHandler.resolvePoolRolls(baseRolls, context);
    }

    @Inject(method = "addRandomItem", at = @At("HEAD"))
    private void createcybernetics$beforePoolEntryRoll(
            Consumer<ItemStack> stackConsumer,
            LootContext context,
            CallbackInfo ci
    ) {
        MiningRollEngine.enterPoolEntryRoll();
    }

    @Inject(method = "addRandomItem", at = @At("RETURN"))
    private void createcybernetics$afterPoolEntryRoll(
            Consumer<ItemStack> stackConsumer,
            LootContext context,
            CallbackInfo ci
    ) {
        MiningRollEngine.exitPoolEntryRoll();
    }
}
