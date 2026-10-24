package com.perigrine3.createcybernetics.entity.custom;

import com.perigrine3.createcybernetics.block.ModBlocks;
import com.perigrine3.createcybernetics.block.SurgeryTableBlock;
import com.perigrine3.createcybernetics.block.entity.SurgeryTableBlockEntity;
import com.perigrine3.createcybernetics.screen.custom.surgery.ripper.RipperTradeMenu;
import com.perigrine3.createcybernetics.screen.custom.surgery.ripper.SurgeryPaymentMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class RipperEntity extends AbstractIllager {

    private static final String NBT_HOME_X = "cc_ripper_home_x";
    private static final String NBT_HOME_Y = "cc_ripper_home_y";
    private static final String NBT_HOME_Z = "cc_ripper_home_z";
    private static final String NBT_HAS_HOME = "cc_ripper_has_home";

    private static final String NBT_TRUST = "cc_ripper_trust";
    private static final String NBT_LAST_TRADE_DAY = "cc_ripper_last_trade_day";
    private static final String NBT_LAST_DECAY_DAY = "cc_ripper_last_decay_day";

    private static final int DEFAULT_TRUSTWORTHINESS = 50;
    private static final int MAX_TRUSTWORTHINESS = 100;
    private static final int MIN_TRUSTWORTHINESS = 0;

    private static final int TABLE_SEARCH_RADIUS = 16;
    private static final double MAX_HOME_DISTANCE_SQ = 12.0D * 12.0D;
    private static final double PATIENT_APPROACH_DISTANCE_SQ = 2.25D;
    private static final double TRADE_CUSTOMER_APPROACH_DISTANCE_SQ = 2.0D * 2.0D;

    @Nullable
    private BlockPos homeTablePos;

    private int trustworthiness = DEFAULT_TRUSTWORTHINESS;
    private long lastTradeDay = -1L;
    private long lastDecayDay = -1L;

    @Nullable
    private UUID currentTradeCustomerUuid;

    public RipperEntity(EntityType<? extends RipperEntity> entityType, Level level) {
        super(entityType, level);
        xpReward = 0;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 28.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.30D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.1D);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new RipperApproachPatientGoal(this, 1.0D));
        goalSelector.addGoal(2, new RipperApproachTradeCustomerGoal(this, 1.0D));
        goalSelector.addGoal(2, new OpenDoorGoal(this, true));
        goalSelector.addGoal(3, new RipperReturnToTableGoal(this, 0.9D));
        goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.1D, false));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    @Override
    public void applyRaidBuffs(ServerLevel serverLevel, int i, boolean b) {
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (level().isClientSide) {
            return;
        }

        if ((tickCount % 100) == 0) {
            if (!hasValidHomeTable()) {
                homeTablePos = null;
                tryAcquireNearbyTable();
            }
        }

        tickTradeCustomerState();
        tickTrustDecay();
    }

    private void tickTradeCustomerState() {
        Player customer = getCurrentTradeCustomer();
        if (customer == null) {
            currentTradeCustomerUuid = null;
            return;
        }

        if (!customer.isAlive()) {
            currentTradeCustomerUuid = null;
            return;
        }

        if (customer.containerMenu instanceof RipperTradeMenu menu) {
            if (menu.getRipper() != this) {
                currentTradeCustomerUuid = null;
            }
            return;
        }

        currentTradeCustomerUuid = null;
    }

    private void tickTrustDecay() {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }

        long day = serverLevel.getDayTime() / 24000L;

        if (lastDecayDay < 0L) {
            lastDecayDay = day;
            return;
        }

        if (day <= lastDecayDay) {
            return;
        }

        for (long d = lastDecayDay + 1; d <= day; d++) {
            if (lastTradeDay >= 0L && (d - lastTradeDay) > 3L) {
                trustworthiness = Math.max(MIN_TRUSTWORTHINESS, trustworthiness - 10);
            }
        }

        lastDecayDay = day;
    }

    public int getTrustworthiness() {
        return trustworthiness;
    }

    public void setTrustworthiness(int value) {
        trustworthiness = Math.max(MIN_TRUSTWORTHINESS, Math.min(MAX_TRUSTWORTHINESS, value));
    }

    public void addTrustworthiness(int amount) {
        setTrustworthiness(trustworthiness + amount);
    }

    public void markTradeOccurred() {
        if (level() instanceof ServerLevel serverLevel) {
            lastTradeDay = serverLevel.getDayTime() / 24000L;
            if (lastDecayDay < 0L) {
                lastDecayDay = lastTradeDay;
            }
        }
    }

    @Nullable
    public BlockPos getHomeTablePos() {
        return homeTablePos;
    }

    public void setHomeTablePos(@Nullable BlockPos pos) {
        homeTablePos = pos;
    }

    public boolean hasValidHomeTable() {
        if (homeTablePos == null) {
            return false;
        }

        BlockState state = level().getBlockState(homeTablePos);
        if (!state.is(ModBlocks.SURGERY_TABLE.get())) {
            return false;
        }

        if (!state.hasProperty(SurgeryTableBlock.PART)) {
            return false;
        }

        return state.getValue(SurgeryTableBlock.PART) == BedPart.HEAD;
    }

    public boolean tryAcquireNearbyTable() {
        BlockPos origin = blockPosition();

        for (BlockPos pos : BlockPos.betweenClosed(
                origin.offset(-TABLE_SEARCH_RADIUS, -4, -TABLE_SEARCH_RADIUS),
                origin.offset(TABLE_SEARCH_RADIUS, 4, TABLE_SEARCH_RADIUS)
        )) {
            BlockState state = level().getBlockState(pos);
            if (!state.is(ModBlocks.SURGERY_TABLE.get())) {
                continue;
            }

            if (!state.hasProperty(SurgeryTableBlock.PART)) {
                continue;
            }

            BlockPos headPos = resolveHeadPos(state, pos);
            if (headPos == null) {
                continue;
            }

            homeTablePos = headPos.immutable();
            return true;
        }

        return false;
    }

    @Nullable
    private BlockPos resolveHeadPos(BlockState state, BlockPos pos) {
        if (!state.is(ModBlocks.SURGERY_TABLE.get())) {
            return null;
        }

        BedPart part = state.getValue(SurgeryTableBlock.PART);
        if (part == BedPart.HEAD) {
            return pos;
        }

        return pos.relative(state.getValue(SurgeryTableBlock.FACING));
    }

    @Nullable
    public SurgeryTableBlockEntity getHomeTableEntity() {
        if (!hasValidHomeTable()) {
            return null;
        }

        if (level().getBlockEntity(homeTablePos) instanceof SurgeryTableBlockEntity table) {
            return table;
        }

        return null;
    }

    @Nullable
    public Player getHomeTablePatient() {
        SurgeryTableBlockEntity table = getHomeTableEntity();
        if (table == null) {
            return null;
        }

        Player patient = table.getPatient();
        if (patient == null || !patient.isAlive() || !patient.isSleeping()) {
            return null;
        }

        return patient;
    }

    @Nullable
    public Vec3 getPreferredPatientApproachPos() {
        if (!hasValidHomeTable()) {
            return null;
        }

        BlockState state = level().getBlockState(homeTablePos);
        if (!state.is(ModBlocks.SURGERY_TABLE.get())) {
            return null;
        }

        Direction facing = state.getValue(SurgeryTableBlock.FACING);
        Direction right = facing.getClockWise();
        Direction left = facing.getCounterClockWise();

        Vec3 rightPos = Vec3.atBottomCenterOf(homeTablePos.relative(right));
        Vec3 leftPos = Vec3.atBottomCenterOf(homeTablePos.relative(left));

        if (isApproachSideUsable(homeTablePos.relative(right))) {
            return rightPos;
        }
        if (isApproachSideUsable(homeTablePos.relative(left))) {
            return leftPos;
        }

        return rightPos;
    }

    @Override
    public boolean isPreventingPlayerRest(Player player) {
        return false;
    }

    private boolean isApproachSideUsable(BlockPos pos) {
        BlockState state = level().getBlockState(pos);
        return state.isAir() || state.canBeReplaced();
    }

    public void setCurrentTradeCustomer(@Nullable Player player) {
        currentTradeCustomerUuid = player != null ? player.getUUID() : null;
    }

    @Nullable
    public Player getCurrentTradeCustomer() {
        if (currentTradeCustomerUuid == null) {
            return null;
        }

        if (!(level() instanceof ServerLevel serverLevel)) {
            return null;
        }

        return serverLevel.getPlayerByUUID(currentTradeCustomerUuid);
    }

    @Nullable
    public Vec3 getTradeCustomerApproachPos() {
        Player customer = getCurrentTradeCustomer();
        if (customer == null) {
            return null;
        }

        Vec3 look = customer.getLookAngle();
        Vec3 side = new Vec3(-look.z, 0.0D, look.x).normalize();

        if (side.lengthSqr() < 1.0E-4D) {
            side = new Vec3(1.0D, 0.0D, 0.0D);
        }

        Vec3 right = customer.position().add(side.scale(1.25D));
        Vec3 left = customer.position().subtract(side.scale(1.25D));

        if (distanceToSqr(right) <= distanceToSqr(left)) {
            return right;
        }

        return left;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean requiresCustomPersistence() {
        return true;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return super.hurt(source, amount);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (level().isClientSide) {
            return InteractionResult.SUCCESS;
        }

        setCurrentTradeCustomer(player);

        if (player instanceof ServerPlayer serverPlayer) {
            MenuProvider provider = new SimpleMenuProvider(
                    (containerId, inventory, menuPlayer) -> new RipperTradeMenu(containerId, inventory, this),
                    getDisplayName()
            );

            serverPlayer.openMenu(provider, buf -> buf.writeVarInt(getId()));
        }

        return InteractionResult.CONSUME;
    }

    @Override
    public AbstractIllager.IllagerArmPose getArmPose() {
        return isAggressive() ? AbstractIllager.IllagerArmPose.ATTACKING : AbstractIllager.IllagerArmPose.CROSSED;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PILLAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.PILLAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PILLAGER_DEATH;
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
        if (homeTablePos == null) {
            tryAcquireNearbyTable();
        }
        return data;
    }

    @Override
    public SoundEvent getCelebrateSound() {
        return null;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        tag.putInt(NBT_TRUST, trustworthiness);
        tag.putLong(NBT_LAST_TRADE_DAY, lastTradeDay);
        tag.putLong(NBT_LAST_DECAY_DAY, lastDecayDay);

        if (homeTablePos != null) {
            tag.putBoolean(NBT_HAS_HOME, true);
            tag.putInt(NBT_HOME_X, homeTablePos.getX());
            tag.putInt(NBT_HOME_Y, homeTablePos.getY());
            tag.putInt(NBT_HOME_Z, homeTablePos.getZ());
        } else {
            tag.putBoolean(NBT_HAS_HOME, false);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        trustworthiness = tag.contains(NBT_TRUST) ? tag.getInt(NBT_TRUST) : DEFAULT_TRUSTWORTHINESS;
        trustworthiness = Math.max(MIN_TRUSTWORTHINESS, Math.min(MAX_TRUSTWORTHINESS, trustworthiness));

        lastTradeDay = tag.contains(NBT_LAST_TRADE_DAY) ? tag.getLong(NBT_LAST_TRADE_DAY) : -1L;
        lastDecayDay = tag.contains(NBT_LAST_DECAY_DAY) ? tag.getLong(NBT_LAST_DECAY_DAY) : -1L;

        if (tag.getBoolean(NBT_HAS_HOME)) {
            homeTablePos = new BlockPos(
                    tag.getInt(NBT_HOME_X),
                    tag.getInt(NBT_HOME_Y),
                    tag.getInt(NBT_HOME_Z)
            );
        } else {
            homeTablePos = null;
        }
    }

    public static class RipperApproachPatientGoal extends Goal {
        private final RipperEntity ripper;
        private final double speed;
        @Nullable
        private Player patient;
        @Nullable
        private Vec3 targetPos;

        public RipperApproachPatientGoal(RipperEntity ripper, double speed) {
            this.ripper = ripper;
            this.speed = speed;
        }

        @Override
        public boolean canUse() {
            if (ripper.getCurrentTradeCustomer() != null) {
                return false;
            }

            patient = ripper.getHomeTablePatient();
            if (patient == null) {
                return false;
            }

            targetPos = ripper.getPreferredPatientApproachPos();
            return targetPos != null;
        }

        @Override
        public boolean canContinueToUse() {
            if (patient == null || targetPos == null) {
                return false;
            }

            if (ripper.getCurrentTradeCustomer() != null) {
                return false;
            }

            if (!patient.isAlive() || !patient.isSleeping()) {
                return false;
            }

            return true;
        }

        @Override
        public void start() {
            if (targetPos != null) {
                ripper.getNavigation().moveTo(targetPos.x, targetPos.y, targetPos.z, speed);
            }
        }

        @Override
        public void tick() {
            if (patient == null || targetPos == null) {
                return;
            }

            ripper.getLookControl().setLookAt(patient, 30.0F, 30.0F);

            if (ripper.distanceToSqr(targetPos) > PATIENT_APPROACH_DISTANCE_SQ) {
                if (ripper.tickCount % 10 == 0) {
                    ripper.getNavigation().moveTo(targetPos.x, targetPos.y, targetPos.z, speed);
                }
                return;
            }

            ripper.getNavigation().stop();

            SurgeryTableBlockEntity table = ripper.getHomeTableEntity();
            if (table == null || table.isTimedSurgeryInProgress()) {
                return;
            }

            if (patient instanceof ServerPlayer serverPlayer) {
                if (!(serverPlayer.containerMenu instanceof SurgeryPaymentMenu menu) || menu.getRipper() != ripper) {
                    MenuProvider provider = new SimpleMenuProvider(
                            (containerId, inventory, menuPlayer) -> new SurgeryPaymentMenu(containerId, inventory, table, ripper),
                            ripper.getDisplayName()
                    );

                    serverPlayer.openMenu(provider, buf -> {
                        buf.writeBlockPos(table.getBlockPos());
                        buf.writeVarInt(ripper.getId());
                    });
                }
            }
        }

        @Override
        public void stop() {
            ripper.getNavigation().stop();
            patient = null;
            targetPos = null;
        }
    }

    public static class RipperApproachTradeCustomerGoal extends Goal {
        private final RipperEntity ripper;
        private final double speed;
        @Nullable
        private Player customer;
        @Nullable
        private Vec3 targetPos;

        public RipperApproachTradeCustomerGoal(RipperEntity ripper, double speed) {
            this.ripper = ripper;
            this.speed = speed;
        }

        @Override
        public boolean canUse() {
            customer = ripper.getCurrentTradeCustomer();
            if (customer == null || !customer.isAlive()) {
                return false;
            }

            if (!(customer.containerMenu instanceof RipperTradeMenu menu) || menu.getRipper() != ripper) {
                return false;
            }

            targetPos = ripper.getTradeCustomerApproachPos();
            return targetPos != null;
        }

        @Override
        public boolean canContinueToUse() {
            if (customer == null || !customer.isAlive()) {
                return false;
            }

            if (!(customer.containerMenu instanceof RipperTradeMenu menu) || menu.getRipper() != ripper) {
                return false;
            }

            return true;
        }

        @Override
        public void start() {
            if (targetPos != null) {
                ripper.getNavigation().moveTo(targetPos.x, targetPos.y, targetPos.z, speed);
            }
        }

        @Override
        public void tick() {
            if (customer == null) {
                return;
            }

            targetPos = ripper.getTradeCustomerApproachPos();
            if (targetPos == null) {
                return;
            }

            ripper.getLookControl().setLookAt(customer, 30.0F, 30.0F);

            if (ripper.distanceToSqr(targetPos) > TRADE_CUSTOMER_APPROACH_DISTANCE_SQ) {
                if (ripper.tickCount % 10 == 0) {
                    ripper.getNavigation().moveTo(targetPos.x, targetPos.y, targetPos.z, speed);
                }
            } else {
                ripper.getNavigation().stop();
            }
        }

        @Override
        public void stop() {
            ripper.getNavigation().stop();
            customer = null;
            targetPos = null;
        }
    }

    public static class RipperReturnToTableGoal extends Goal {
        private final RipperEntity ripper;
        private final double speed;

        public RipperReturnToTableGoal(RipperEntity ripper, double speed) {
            this.ripper = ripper;
            this.speed = speed;
        }

        @Override
        public boolean canUse() {
            if (!ripper.hasValidHomeTable()) {
                return false;
            }

            if (ripper.getTarget() != null) {
                return false;
            }

            if (ripper.getCurrentTradeCustomer() != null) {
                return false;
            }

            if (ripper.getHomeTablePatient() != null) {
                return false;
            }

            return ripper.distanceToSqr(Vec3.atBottomCenterOf(ripper.getHomeTablePos())) > MAX_HOME_DISTANCE_SQ;
        }

        @Override
        public boolean canContinueToUse() {
            if (!ripper.hasValidHomeTable()) {
                return false;
            }

            if (ripper.getCurrentTradeCustomer() != null) {
                return false;
            }

            if (ripper.getHomeTablePatient() != null) {
                return false;
            }

            return !ripper.getNavigation().isDone();
        }

        @Override
        public void start() {
            BlockPos home = ripper.getHomeTablePos();
            if (home != null) {
                ripper.getNavigation().moveTo(home.getX() + 0.5D, home.getY(), home.getZ() + 0.5D, speed);
            }
        }

        @Override
        public void tick() {
            BlockPos home = ripper.getHomeTablePos();
            if (home != null && ripper.tickCount % 40 == 0) {
                ripper.getNavigation().moveTo(home.getX() + 0.5D, home.getY(), home.getZ() + 0.5D, speed);
            }
        }

        @Override
        public void stop() {
            ripper.getNavigation().stop();
        }
    }
}