package com.nobtg.Mixins;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.nobtg.RealFont;
import com.nobtg.RealSword;
import com.nobtg.RealSwordMod;
import com.nobtg.TimeDataHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.entity.EntityInLevelCallback;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

import static com.nobtg.RealSwordMod.*;

public class Mixins {

    @Mixin(Entity.class)
    public interface EntityProxyMixin {
        @Accessor("levelCallback")
        EntityInLevelCallback getLevelCallback();

        @Accessor("removalReason")
        Entity.RemovalReason getRemovalReason();

        @Accessor("removalReason")
        void setRemovalReason(Entity.RemovalReason reason);
    }

    @Mixin(LivingEntity.class)
    public interface LivingEntityProxyMixin {
        @Accessor("DATA_HEALTH_ID")
        EntityDataAccessor<Float> getDATA_HEALTH_ID();
    }

    @Mixin(EntityRenderDispatcher.class)
    public static abstract class EntityRenderDispatcherMixin<E extends Entity> {
        @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;getRenderer(Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/client/renderer/entity/EntityRenderer;"), cancellable = true)
        public void render(E e, double p_114386_, double p_114387_, double p_114388_, float p_114389_, float p_114390_, PoseStack p_114391_, MultiBufferSource p_114392_, int p_114393_, CallbackInfo ci) {
            if (NO_SPAWN_ENTITIES.contains(e.getEncodeId())) ci.cancel();
        }

        @Inject(method = "renderFlame", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/Material;sprite()Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;", ordinal = 0), cancellable = true)
        public void renderFlame(PoseStack p_114454_, MultiBufferSource p_114455_, Entity e, CallbackInfo ci) {
            if (NO_SPAWN_ENTITIES.contains(e.getEncodeId())) ci.cancel();
        }

        @Inject(method = "renderShadow", at = @At("HEAD"), cancellable = true)
        private static void renderShadow(PoseStack p_114458_, MultiBufferSource p_114459_, Entity e, float p_114461_, float p_114462_, LevelReader p_114463_, float p_114464_, CallbackInfo ci) {
            if (NO_SPAWN_ENTITIES.contains(e.getEncodeId())) ci.cancel();
        }

        @Inject(method = "renderHitbox", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getBoundingBox()Lnet/minecraft/world/phys/AABB;"), cancellable = true)
        private static void renderHitbox(PoseStack p_114442_, VertexConsumer p_114443_, Entity e, float p_114445_, CallbackInfo ci) {
            if (NO_SPAWN_ENTITIES.contains(e.getEncodeId())) ci.cancel();
        }
    }

    @Mixin(ServerLevel.class)
    public static abstract class ServerLevelMixin {
        @Inject(method = "addFreshEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;addEntity(Lnet/minecraft/world/entity/Entity;)Z"), cancellable = true)
        public void addFreshEntity(Entity e, CallbackInfoReturnable<Boolean> cir) {
            if ((RealSwordMod.ModVariables.MapVariables.get((ServerLevel) (Object) this).isNoRespawn || NO_SPAWN_ENTITIES.contains(e.getEncodeId()) || DEAD_ENTITIES.contains(e.getUUID())) && !(e instanceof Player || e instanceof ItemEntity || e instanceof ExperienceOrb))
                cir.setReturnValue(Boolean.FALSE);
        }

        @Inject(method = "save", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/eventbus/api/IEventBus;post(Lnet/minecraftforge/eventbus/api/Event;)Z", remap = false))
        public void save(CallbackInfo ci) {
            if (((ServerLevel) (Object) this).players().size() == 0) {
                TimeDataHandler handler = TimeDataHandler.get();
                Minecraft.getInstance().getSoundManager().resume();
                handler.stopTime = 0;
                handler.setTimeStopped(false);
                handler.syncData((ServerLevel) (Object) this);
                REAL_ENTITIES.clear();
                DEAD_ENTITIES.clear();
                NO_SPAWN_ENTITIES.clear();
                RealSwordMod.ModVariables.MapVariables.get((ServerLevel) (Object) this).isNoRespawn = false;
                RealSwordMod.ModVariables.MapVariables.get((ServerLevel) (Object) this).syncData((ServerLevel) (Object) this);
            }
        }
    }

    @Mixin(value = Entity.class, priority = Integer.MIN_VALUE)
    public static abstract class EntityMixin {
        @Shadow public abstract UUID getUUID();

        @Inject(method = "getFireImmuneTicks", at = @At("HEAD"), cancellable = true)
        public void getFireImmuneTicks(CallbackInfoReturnable<Integer> cir) {
            if (REAL_ENTITIES.contains(getUUID())) cir.setReturnValue(0);
        }

        @Inject(method = "displayFireAnimation", at = @At("HEAD"), cancellable = true)
        public void displayFireAnimation(CallbackInfoReturnable<Boolean> cir) {
            if (REAL_ENTITIES.contains(getUUID())) cir.setReturnValue(Boolean.FALSE);
        }

        @Inject(method = "getRemainingFireTicks", at = @At("HEAD"), cancellable = true)
        public void getRemainingFireTicks(CallbackInfoReturnable<Integer> cir) {
            if (REAL_ENTITIES.contains(getUUID())) cir.setReturnValue(0);
        }

        @Inject(method = "getTicksFrozen", at = @At("HEAD"), cancellable = true)
        public void getTicksFrozen(CallbackInfoReturnable<Integer> cir) {
            if (REAL_ENTITIES.contains(getUUID())) cir.setReturnValue(0);
        }

        @Inject(method = "getTicksRequiredToFreeze", at = @At("HEAD"), cancellable = true)
        public void getTicksRequiredToFreeze(CallbackInfoReturnable<Integer> cir) {
            if (REAL_ENTITIES.contains(getUUID())) cir.setReturnValue(0);
        }
    }

    @Mixin(value = LivingEntity.class, priority = Integer.MAX_VALUE)
    public static abstract class LivingEntityMixin {

        /**
         * @author NOBTG
         * @reason LockHealth(20.0F) -> Invincible And LockHealth(0.0F) -> SetDeadEntity
         */
        @Overwrite
        public float getHealth() {
            LivingEntity living = (LivingEntity) (Object) this;
            return RealSwordMod.REAL_ENTITIES.contains(living.getUUID()) ? 20 : RealSwordMod.DEAD_ENTITIES.contains(living.getUUID()) ? 0 : living.getEntityData().get(((LivingEntityProxyMixin) living).getDATA_HEALTH_ID());
        }

        @Inject(method = "tickEffects", at = @At("HEAD"), cancellable = true)
        public void tickEffects(CallbackInfo ci) {
            if (TimeDataHandler.get().canTimeStopped()) ci.cancel();
        }

        @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/common/ForgeHooks;onLivingTick(Lnet/minecraft/world/entity/LivingEntity;)Z", remap = false))
        public void tick(CallbackInfo ci) {
            if (!(((LivingEntity) (Object) this).getTags().contains("unreal")))
                RealSword.Tick((LivingEntity) (Object) this);
        }

        @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/common/ForgeHooks;onLivingTick(Lnet/minecraft/world/entity/LivingEntity;)Z", shift = At.Shift.BEFORE, remap = false))
        public void tick_(CallbackInfo ci) {
            if (!(((LivingEntity) (Object) this).getTags().contains("unreal")))
                RealSword.Tick((LivingEntity) (Object) this);
        }

        @Inject(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/common/ForgeHooks;onLivingAttack(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/damagesource/DamageSource;F)Z", remap = false), cancellable = true)
        public void hurt(DamageSource p_21016_, float p_21017_, CallbackInfoReturnable<Boolean> cir) {
            if (RealSwordMod.REAL_ENTITIES.contains(((LivingEntity) (Object) this).getUUID()))
                cir.setReturnValue(Boolean.FALSE);
        }

        @Inject(method = "canBeAffected", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/event/entity/living/MobEffectEvent$Applicable;<init>(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/effect/MobEffectInstance;)V"), cancellable = true)
        public void canBeAffected(MobEffectInstance p_21197_, CallbackInfoReturnable<Boolean> cir) {
            if (RealSwordMod.REAL_ENTITIES.contains(((LivingEntity) (Object) this).getUUID()))
                cir.setReturnValue(Boolean.FALSE);
        }

        @Redirect(method = "releaseUsingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;releaseUsing(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)V"))
        public void releaseUsingItem(@NotNull ItemStack instance, Level level, LivingEntity user, int i) {
            if (instance.getItem() == RealSwordMod.REAL_SWORD.get()) {
                if (!level.isClientSide()) {
                    TimeDataHandler handler = TimeDataHandler.get();
                    Minecraft mc = Minecraft.getInstance();
                    if (!handler.isTimeMultiplier(user)) handler.setTimeManipulator(user);
                    mc.getSoundManager().resume();
                    handler.stopTime = 0;
                    handler.setTimeStopped(false);
                    handler.syncData(level);
                }
                RealSword.KillAllEntities(user.level() instanceof ServerLevel ? (ServerLevel) user.level() : Objects.requireNonNull(Minecraft.getInstance().getSingleplayerServer()).overworld());
            } else instance.releaseUsing(level, user, i);
        }
    }

    @Mixin(MinecraftServer.class)
    public static abstract class MinecraftServerMixin {
        @Inject(method = "tickServer", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/event/ForgeEventFactory;onPostServerTick(Ljava/util/function/BooleanSupplier;Lnet/minecraft/server/MinecraftServer;)V", remap = false, shift = At.Shift.BEFORE))
        public void tickServer(BooleanSupplier p_129871_, CallbackInfo ci) {
            List<AbstractMap.SimpleEntry<Runnable, Integer>> actions = new ArrayList<>();
            workQueue.forEach(work -> {
                work.setValue(work.getValue() - 1);
                if (work.getValue() == 0) actions.add(work);
            });
            actions.forEach(e -> e.getKey().run());
            workQueue.removeAll(actions);
        }
    }

    @Mixin(Player.class)
    public static abstract class PlayerMixin {
        @Inject(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/common/ForgeHooks;onPlayerAttackTarget(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;)Z", remap = false), cancellable = true)
        public void attack(Entity e, CallbackInfo ci) {
            if (((Player) (Object) (this)).getMainHandItem().getItem() == RealSwordMod.REAL_SWORD.get())
                RealSword.AttackEntity((Player) (Object) (this), e);
            else if (RealSwordMod.REAL_ENTITIES.contains(e.getUUID())) {
                RealSword.AttackEntity(e instanceof Player player ? player : Minecraft.getInstance().player, (Player) (Object) (this));
                ci.cancel();
            }
        }

        @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/event/ForgeEventFactory;onPlayerPostTick(Lnet/minecraft/world/entity/player/Player;)V", remap = false))
        public void tick(CallbackInfo ci) {
            RealSword.Tick((Player) (Object) this);
        }

        @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/event/ForgeEventFactory;onPlayerPostTick(Lnet/minecraft/world/entity/player/Player;)V", shift = At.Shift.BEFORE, remap = false))
        public void tick_(CallbackInfo ci) {
            RealSword.Tick((Player) (Object) this);
        }
    }

    @Mixin(Minecraft.class)
    public interface MinecraftProxyMixin {
        @Accessor("fontManager")
        FontManager getfontManager();
    }

    @Mixin(Font.class)
    public interface FontProxyMixin {
        @Accessor("fonts")
        Function<ResourceLocation, FontSet> getfonts();
    }

    @Mixin(Minecraft.class)
    public static abstract class MinecraftMixin {
        @Shadow
        @Nullable
        public abstract Entity getCameraEntity();

        @Shadow
        protected abstract String createTitle();

        @Shadow
        @Nullable
        public ClientLevel level;

        @Shadow
        private static int fps;

        @Shadow
        public abstract Window getWindow();

        @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/event/ForgeEventFactory;onPreClientTick()V", shift = At.Shift.BEFORE))
        public void tick(CallbackInfo ci) {
            RealFont.tick += 0.8F;
            if (RealFont.tick >= 720.0F) RealFont.tick = 0.0F;
        }

        @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/event/ForgeEventFactory;onRenderTickEnd(F)V", shift = At.Shift.BEFORE, remap = false))
        public void renderTick(boolean p_91384_, CallbackInfo ci) {
            Player player = getCameraEntity() instanceof Player CameraPlayer ? CameraPlayer : null;
            String Health = level != null && player != null ? String.valueOf(player.getEntityData().get(((LivingEntityProxyMixin) player).getDATA_HEALTH_ID())) : "null";
            String screen = ((Minecraft) (Object) this).screen != null ? Objects.requireNonNull(((Minecraft) (Object) this).screen).getClass().getSimpleName() : "null";
            getWindow().setTitle(createTitle() + " Health: " + Health + " mc.screen: " + screen + " FPS: " + fps);
        }
    }
}
