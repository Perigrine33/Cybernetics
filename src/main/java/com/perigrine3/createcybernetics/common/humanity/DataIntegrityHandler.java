package com.perigrine3.createcybernetics.common.humanity;

import com.perigrine3.createcybernetics.common.scheduling.CyberwareProfileManager;
import com.perigrine3.createcybernetics.common.scheduling.CyberwareLoadoutProfile;
import com.perigrine3.createcybernetics.common.scheduling.StaggeredTickScheduler;
import com.perigrine3.createcybernetics.CreateCybernetics;
import com.perigrine3.createcybernetics.api.CyberwareSlot;
import com.perigrine3.createcybernetics.api.InstalledCyberware;
import com.perigrine3.createcybernetics.common.capabilities.ModAttachments;
import com.perigrine3.createcybernetics.common.capabilities.PlayerCyberwareData;
import com.perigrine3.createcybernetics.item.cyberware.brain.CerebralProcessingUnitItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = CreateCybernetics.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class DataIntegrityHandler {

    public static final int TICKS_TO_QUARTER_INTEGRITY = 7 * 24000;
    public static final int TICKS_UNTIL_REJECTION = 6 * 24000;

    private static final int DOWNLOAD_MESSAGE_TICKS = 20 * 3;
    public static final int BOOT_DOWN_TICKS_REQUIRED = 20 * 4;

    private static final int SYNC_INTERVAL_TICKS = 20;
    private static final int FINAL_MESSAGE_TICKS = 60;
    private static final int INTEGRITY_ALERT_MESSAGE_TICKS = 60;

    private static final Map<UUID, SleepState> SLEEP_STATES = new HashMap<>();

    private DataIntegrityHandler() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        if (player.level().isClientSide) {
            return;
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        PlayerCyberwareData data = serverPlayer.getData(ModAttachments.CYBERWARE);
        if (!hasCerebralProcessingUnit(serverPlayer, data)) {
            SLEEP_STATES.remove(serverPlayer.getUUID());
            return;
        }

        tickDataIntegrity(serverPlayer, data);
    }

    private static boolean hasCerebralProcessingUnit(Player player, PlayerCyberwareData data) {
        CyberwareLoadoutProfile profile = CyberwareProfileManager.getProfile(player, data);
        if (profile != null) {
            return profile.hasCapability(CyberwareLoadoutProfile.CapabilityFlag.CEREBRAL_PROCESSING_UNIT);
        }
        return hasCerebralProcessingUnit(data);
    }

    public static boolean hasCerebralProcessingUnit(PlayerCyberwareData data) {
        if (data == null) {
            return false;
        }

        InstalledCyberware[] brains = data.getAll().get(CyberwareSlot.BRAIN);
        if (brains == null) {
            return false;
        }

        for (InstalledCyberware installed : brains) {
            if (installed == null) {
                continue;
            }

            ItemStack stack = installed.getItem();
            if (stack == null || stack.isEmpty()) {
                continue;
            }

            if (stack.getItem() instanceof CerebralProcessingUnitItem) {
                return true;
            }
        }

        return false;
    }

    public static boolean usesDataIntegrity(Player player) {
        if (player == null) {
            return false;
        }

        return hasCerebralProcessingUnit(player.getData(ModAttachments.CYBERWARE));
    }

    public static boolean hasMissedBootDown(Player player) {
        if (!usesDataIntegrity(player)) {
            return false;
        }

        PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
        return data.getTicksSinceIntegrityBootDown() >= TICKS_UNTIL_REJECTION;
    }

    public static int getMaxIntegrity() {
        return HumanityAttributeModifiers.getConfiguredBaseHumanity();
    }

    public static int getIntegrity(Player player) {
        if (player == null) {
            return getMaxIntegrity();
        }

        PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
        if (data == null) {
            return getMaxIntegrity();
        }

        return data.getDataIntegrity();
    }

    public static double getIntegrityPrecise(Player player) {
        if (player == null) {
            return getMaxIntegrity();
        }

        PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
        if (data == null) {
            return getMaxIntegrity();
        }

        return data.getDataIntegrityPrecise();
    }

    public static float getIntegrityPercent(Player player) {
        return Mth.clamp(
                (float) (getIntegrityPrecise(player) / Math.max(1, getMaxIntegrity())),
                0.0F,
                1.0F
        );
    }

    public static void restoreIntegrity(Player player) {
        if (player == null || player.level().isClientSide) {
            return;
        }

        PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);

        data.setDataIntegrity(getMaxIntegrity());
        data.setTicksSinceIntegrityBootDown(0);
        data.setIntegrityBedTicks(0);

        SleepState sleepState = SLEEP_STATES.get(player.getUUID());
        if (sleepState != null) {
            sleepState.integrityAlertLevel = IntegrityAlertLevel.NORMAL;
            sleepState.hasObservedIntegrity = true;
            sleepState.integrityAlertMessageTicks = 0;
            sleepState.pendingIntegrityAlert = null;
        }

        sync(player);
    }

    public static void restoreIntegrity(Player player, int amount) {
        if (player == null || player.level().isClientSide || amount <= 0) {
            return;
        }

        PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);

        data.setDataIntegrityPrecise(
                Math.min(
                        getMaxIntegrity(),
                        data.getDataIntegrityPrecise() + amount
                )
        );

        if (player instanceof ServerPlayer serverPlayer) {
            updateIntegrityAlertState(serverPlayer, data);
        }

        sync(player);
    }

    private static void tickDataIntegrity(ServerPlayer player, PlayerCyberwareData data) {
        int maxIntegrity = getMaxIntegrity();
        UUID playerId = player.getUUID();

        if (!data.hasInitializedDataIntegrity()) {
            data.setDataIntegrity(maxIntegrity);
        }

        SleepState sleepState = SLEEP_STATES.computeIfAbsent(playerId, ignored -> new SleepState());

        boolean sleeping = player.isSleeping() || player.getSleepTimer() > 0;

        if (sleeping) {
            tickSleeping(player, data, sleepState);
            return;
        }

        tickAwake(player, data, sleepState, maxIntegrity);

        if (StaggeredTickScheduler.shouldRun(player, SYNC_INTERVAL_TICKS, StaggeredTickScheduler.TickPhase.DATA_INTEGRITY)) {
            sync(player);
        }
    }

    private static void tickSleeping(ServerPlayer player, PlayerCyberwareData data, SleepState sleepState) {
        int sleepTimer = player.getSleepTimer();

        if (!sleepState.wasSleeping) {
            sleepState.wasSleeping = true;
            sleepState.completed = false;
            sleepState.failed = false;
            sleepState.finalMessageTicks = 0;

            data.setIntegrityBedTicks(0);
        }

        int bedTicks = Math.max(data.getIntegrityBedTicks() + 1, sleepTimer);
        data.setIntegrityBedTicks(bedTicks);

        if (!sleepState.completed && bedTicks <= DOWNLOAD_MESSAGE_TICKS) {
            sendActionBar(
                    player,
                    "message.createcybernetics.data_integrity.downloading",
                    ChatFormatting.ITALIC
            );
        }

        if (!sleepState.completed && bedTicks >= BOOT_DOWN_TICKS_REQUIRED) {
            restoreIntegrity(player);

            sleepState.completed = true;
            sleepState.finalMessageTicks = FINAL_MESSAGE_TICKS;

            sendActionBar(
                    player,
                    "message.createcybernetics.data_integrity.restoration_success",
                    ChatFormatting.GREEN
            );
        }

        if (sleepState.completed) {
            sendActionBar(
                    player,
                    "message.createcybernetics.data_integrity.restoration_success",
                    ChatFormatting.GREEN
            );
        }

        if (StaggeredTickScheduler.shouldRun(player, SYNC_INTERVAL_TICKS, StaggeredTickScheduler.TickPhase.DATA_INTEGRITY)) {
            sync(player);
        }
    }

    private static void tickAwake(
            ServerPlayer player,
            PlayerCyberwareData data,
            SleepState sleepState,
            int maxIntegrity
    ) {
        if (sleepState.wasSleeping) {
            sleepState.wasSleeping = false;

            if (sleepState.completed) {
                sleepState.finalMessageTicks = FINAL_MESSAGE_TICKS;
            } else if (data.getIntegrityBedTicks() > 0) {
                sleepState.failed = true;
                sleepState.finalMessageTicks = FINAL_MESSAGE_TICKS;
            }

            data.setIntegrityBedTicks(0);
        }

        if (sleepState.finalMessageTicks > 0) {
            if (sleepState.completed) {
                sendActionBar(
                        player,
                        "message.createcybernetics.data_integrity.restoration_success",
                        ChatFormatting.GREEN
                );
            } else if (sleepState.failed) {
                sendActionBar(
                        player,
                        "message.createcybernetics.data_integrity.restoration_failed",
                        ChatFormatting.RED
                );
            }

            sleepState.finalMessageTicks--;

            if (sleepState.finalMessageTicks <= 0) {
                sleepState.completed = false;
                sleepState.failed = false;
            }

            return;
        }

        if (sleepState.integrityAlertMessageTicks > 0 && sleepState.pendingIntegrityAlert != null) {
            sendIntegrityAlert(player, sleepState.pendingIntegrityAlert);

            sleepState.integrityAlertMessageTicks--;

            if (sleepState.integrityAlertMessageTicks <= 0) {
                sleepState.pendingIntegrityAlert = null;
            }
        }

        double lossPerTick = (maxIntegrity * 0.75D) / TICKS_TO_QUARTER_INTEGRITY;

        data.setDataIntegrityPrecise(
                data.getDataIntegrityPrecise() - lossPerTick
        );

        data.setTicksSinceIntegrityBootDown(
                data.getTicksSinceIntegrityBootDown() + 1
        );

        updateIntegrityAlertState(player, data);
    }

    private static void updateIntegrityAlertState(ServerPlayer player, PlayerCyberwareData data) {
        SleepState sleepState = SLEEP_STATES.computeIfAbsent(
                player.getUUID(),
                ignored -> new SleepState()
        );

        IntegrityAlertLevel currentLevel = getIntegrityAlertLevel(
                data.getDataIntegrityPrecise(),
                getMaxIntegrity()
        );

        if (!sleepState.hasObservedIntegrity) {
            sleepState.hasObservedIntegrity = true;
            sleepState.integrityAlertLevel = IntegrityAlertLevel.NORMAL;
        }

        if (currentLevel.id < sleepState.integrityAlertLevel.id) {
            sleepState.pendingIntegrityAlert = currentLevel;
            sleepState.integrityAlertMessageTicks = INTEGRITY_ALERT_MESSAGE_TICKS;
        }

        sleepState.integrityAlertLevel = currentLevel;
    }

    private static IntegrityAlertLevel getIntegrityAlertLevel(double integrity, int maxIntegrity) {
        if (integrity < 0.0D) {
            return IntegrityAlertLevel.NEGATIVE;
        }

        double percent = integrity / Math.max(1, maxIntegrity);

        if (percent <= 0.0D) {
            return IntegrityAlertLevel.ZERO;
        }

        if (percent <= 0.25D) {
            return IntegrityAlertLevel.TWENTY_FIVE;
        }

        if (percent <= 0.50D) {
            return IntegrityAlertLevel.FIFTY;
        }

        if (percent <= 0.75D) {
            return IntegrityAlertLevel.SEVENTY_FIVE;
        }

        return IntegrityAlertLevel.NORMAL;
    }

    private static void sendIntegrityAlert(ServerPlayer player, IntegrityAlertLevel level) {
        switch (level) {
            case SEVENTY_FIVE -> sendActionBar(
                    player,
                    "message.createcybernetics.data_integrity.seventy_five",
                    ChatFormatting.BLUE
            );

            case FIFTY -> sendActionBar(
                    player,
                    "message.createcybernetics.data_integrity.fifty",
                    ChatFormatting.GOLD
            );

            case TWENTY_FIVE -> sendActionBar(
                    player,
                    "message.createcybernetics.data_integrity.twenty_five",
                    ChatFormatting.RED
            );

            case ZERO -> sendActionBar(
                    player,
                    "message.createcybernetics.data_integrity.zero",
                    ChatFormatting.DARK_RED
            );

            case NEGATIVE -> sendActionBar(
                    player,
                    "message.createcybernetics.data_integrity.negative",
                    ChatFormatting.DARK_RED
            );

            default -> {
            }
        }
    }

    private static void sendActionBar(ServerPlayer player, String translationKey, ChatFormatting color) {
        player.displayClientMessage(
                Component.translatable(translationKey).withStyle(color),
                true
        );
    }

    private static void sync(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            ModAttachments.syncCyberware(serverPlayer);
        }
    }

    private enum IntegrityAlertLevel {
        NEGATIVE(0),
        ZERO(1),
        TWENTY_FIVE(2),
        FIFTY(3),
        SEVENTY_FIVE(4),
        NORMAL(5);

        private final int id;

        IntegrityAlertLevel(int id) {
            this.id = id;
        }
    }

    private static final class SleepState {
        private boolean wasSleeping = false;
        private boolean completed = false;
        private boolean failed = false;
        private int finalMessageTicks = 0;

        private boolean hasObservedIntegrity = false;
        private IntegrityAlertLevel integrityAlertLevel = IntegrityAlertLevel.NORMAL;

        private IntegrityAlertLevel pendingIntegrityAlert = null;
        private int integrityAlertMessageTicks = 0;
    }
}