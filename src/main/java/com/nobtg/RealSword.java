package com.nobtg;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.nobtg.Mixins.Mixins;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.TierSortingRegistry;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static com.nobtg.RealSwordMod.*;

@Mod.EventBusSubscriber
public class RealSword extends SwordItem {
    public RealSword() {
        super(new Tier() {
            public int getUses() {
                return 0;
            }

            public float getSpeed() {
                return Float.MAX_VALUE;
            }

            public float getAttackDamageBonus() {
                return Float.MAX_VALUE;
            }

            public int getLevel() {
                return 0;
            }

            public int getEnchantmentValue() {
                return 0;
            }

            public @NotNull Ingredient getRepairIngredient() {
                return Ingredient.EMPTY;
            }
        }, Integer.MAX_VALUE, Float.MAX_VALUE, new Properties());
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack) {
        return 72000;
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.valueOf("SWORD:BLOCK");
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level world, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack shield = player.getItemInHand(InteractionHand.OFF_HAND);
        boolean flag = shield.getItem() instanceof ShieldItem;
        if (hand == InteractionHand.MAIN_HAND && !flag) {
            player.startUsingItem(hand);
            if (!world.isClientSide()) {
                TimeDataHandler handler = TimeDataHandler.get();
                Minecraft mc = Minecraft.getInstance();
                if (!handler.isTimeMultiplier(player)) handler.setTimeManipulator(player);
                if (!handler.canTimeStopped()) mc.getSoundManager().pause();
                else mc.getSoundManager().resume();
                handler.stopTime = Integer.MAX_VALUE;
                handler.setTimeStopped(!handler.canTimeStopped());
                handler.syncData(world);
            }
            return InteractionResultHolder.success(player.getItemInHand(hand));
        }
        if (hand == InteractionHand.OFF_HAND || flag) {
            return InteractionResultHolder.fail(player.getItemInHand(hand));
        }
        return super.use(world, player, hand);
    }

    @Override
    public @NotNull Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(@NotNull EquipmentSlot p_41388_) {
        return ImmutableMultimap.of();
    }

    @SubscribeEvent
    public static void LeftClickBlock(PlayerInteractEvent.@NotNull LeftClickBlock event) {
        if (event.getItemStack().getItem() == RealSwordMod.REAL_SWORD.get()) {
            BlockPos pos = event.getPos();
            Level level = event.getLevel();
            BlockState blockState = level.getBlockState(pos);
            int harvestLevel = TierSortingRegistry.getSortedTiers()
                    .stream()
                    .filter(t -> t.getTag() != null && blockState.is(t.getTag()))
                    .map(Tier::getLevel)
                    .findFirst()
                    .orElse(0);
            float destroySpeed = blockState.getDestroySpeed(level, pos);
            if (harvestLevel > 300 || harvestLevel < 0 || destroySpeed > 300 || destroySpeed < 0) {
                if (!level.isClientSide()) {
                    ItemEntity entityToSpawn = new ItemEntity(level, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(blockState.getBlock()));
                    entityToSpawn.setPickUpDelay(10);
                    level.addFreshEntity(entityToSpawn);
                }
            } else {
                Block.dropResources(blockState, level, pos, null);
            }
            level.destroyBlock(pos, false);
            level.updateNeighborsAt(pos, blockState.getBlock());
        }
    }

    public static void AttackEntity(Player player, @NotNull Entity e) {
        if (RealSwordMod.REAL_ENTITIES.contains(e.getUUID())) return;
        if (player.isShiftKeyDown()) {
            NO_SPAWN_ENTITIES.add(e.getEncodeId());
            remove(e);
        }
        if (e instanceof LivingEntity living) {
            RealSwordMod.ModVariables.MapVariables.get(living.level()).isNoRespawn = true;
            RealSwordMod.ModVariables.MapVariables.get(living.level()).syncData(living.level());
            ScheduledExecutorService executor = Executors.newScheduledThreadPool(6);
            DamageSource source = new DamageSources(e.level().registryAccess()).playerAttack(player);
            DamageSource magic = new DamageSources(e.level().registryAccess()).magic();
            living.setLastHurtByPlayer(player);
            living.hurt(source, Float.POSITIVE_INFINITY);
            for (int i = 0; i < 10; i++)
                living.hurt(magic, Float.POSITIVE_INFINITY);
            executor.schedule(() -> {
                RealSwordMod.ModVariables.MapVariables.get(living.level()).isNoRespawn = false;
                RealSwordMod.ModVariables.MapVariables.get(living.level()).syncData(living.level());
            }, 102, TimeUnit.MILLISECONDS);
            executor.schedule(() -> {
                executor.schedule(() -> {
                    if (living.isAlive()) living.die(source);
                }, 51, TimeUnit.MILLISECONDS);
                executor.schedule(() -> {
                    RealSwordMod.DEAD_ENTITIES.add(e.getUUID());
                }, 49, TimeUnit.MILLISECONDS);
                executor.schedule(() -> {
                    remove(e);
                    executor.shutdown();
                }, 32000, TimeUnit.MILLISECONDS);
            }, 32000, TimeUnit.MILLISECONDS);
        } else {
            RealSwordMod.DEAD_ENTITIES.add(e.getUUID());
            remove(e);
        }
    }

    private static void remove(Entity e) {
        if (!e.isRemoved()) {
            Entity.RemovalReason reason = Entity.RemovalReason.KILLED;
            Mixins.EntityProxyMixin ins = ((Mixins.EntityProxyMixin) e);
            if (ins.getRemovalReason() == null) ins.setRemovalReason(reason);
            if (ins.getRemovalReason().shouldDestroy()) e.stopRiding();
            e.getPassengers().forEach(Entity::stopRiding);
            ins.getLevelCallback().onRemove(reason);
            e.invalidateCaps();
        }
    }

    public static void KillAllEntities(@NotNull ServerLevel level) {
        queueServerWork(1, () -> {
            List<Entity> entityIndex = Lists.newArrayList(level.getAllEntities());
            for (int i = 1; i < entityIndex.size(); i++) {
                Entity entity = entityIndex.get(i);
                if (entityIndex.indexOf(entity) > 0) {
                    if (entity != null) {
                        AttackEntity(entity instanceof Player ? (Player) entity : Minecraft.getInstance().player, entity);
                    }
                }
            }
        });
    }

    public static void UnReal(@NotNull Entity entity) {
        for (int i = 0; i < 10; i++) {
            entity.getTags().add("unreal");
            REAL_ENTITIES.remove(entity.getUUID());
            if (entity instanceof Player p) {
                p.getAbilities().invulnerable = p.isInvulnerable();
                p.getAbilities().mayfly = p.isSpectator() || p.isCreative();
                p.onUpdateAbilities();
                p.getInventory().clearContent();
            }
        }
    }

    public static void Tick(@NotNull Entity entity) {
        if (entity.getTags().contains("unreal")) return;
        if ((entity instanceof LivingEntity living &&
                (living.getMainHandItem().getItem() == RealSwordMod.REAL_SWORD.get() ||
                        living.getOffhandItem().getItem() == RealSwordMod.REAL_SWORD.get())) || entity instanceof Player p && p.getInventory().contains(new ItemStack(RealSwordMod.REAL_SWORD.get())))
            RealSwordMod.REAL_ENTITIES.add(entity.getUUID());
        if (RealSwordMod.REAL_ENTITIES.contains(entity.getUUID())) {
            if (entity instanceof Player p) {
                if (Math.random() < 0.1) {
                    p.getAbilities().mayfly = true;
                    p.onUpdateAbilities();
                    p.getAbilities().invulnerable = true;
                }
                ItemStack sword_stack = new ItemStack(RealSwordMod.REAL_SWORD.get());
                if (!p.getInventory().contains(sword_stack)) {
                    AtomicBoolean b = new AtomicBoolean(false);
                    p.getInventory().items.forEach((stack) -> {
                        if (stack.isEmpty()) b.set(true);
                    });
                    if (b.get()) p.getInventory().add(sword_stack);
                    else {
                        p.drop(p.getInventory().getItem(8), false);
                        p.getInventory().setItem(8, sword_stack);
                    }
                }
            }
            entity.getEntityData().set(((Mixins.LivingEntityProxyMixin) entity).getDATA_HEALTH_ID(), 20.0F);
            ServerLevel serverLevel = entity.level() instanceof ServerLevel serverLevel1 ? serverLevel1 : null;
            if (serverLevel != null)
                serverLevel.getChunkSource().broadcastAndSend(entity, new ClientboundSetHealthPacket(20.0F, 20, 20.0F));
        }
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public Font getFont(ItemStack stack, FontContext context) {
                return RealFont.getInstance();
            }
        });
    }
}
