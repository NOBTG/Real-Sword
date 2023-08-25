package com.nobtg.Mixins;

import com.mojang.blaze3d.systems.RenderSystem;
import com.nobtg.TimeDataHandler;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.tutorial.Tutorial;
import net.minecraft.core.BlockPos;
import net.minecraft.server.ServerFunctionManager;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raids;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.ticks.LevelTicks;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;
import java.util.function.BooleanSupplier;

/**
 * by Suel_ki (She's Time Stop Clock Mod) decompile
 * <p>
 * Thank Suel_ki
 * <p>
 * Copyright (c) 2023 Suel_ki
 * <p>
 * MIT License
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * <p>
 * <p>
 * 由 Suel_ki (She's Time Stop Clock Mod) 反编译而成
 * <p>
 * 感谢 Suel_ki
 * <p>
 * 版权所有 (c) 2023 Suel_ki
 * <p>
 * MIT 许可证
 * <p>
 * 在此特此授予任何获得本软件及相关文档文件副本的人免费使用的权限，无需支付费用，
 * 可以无限制地处理本软件，包括但不限于使用、复制、修改、合并、出版、分发、再许可和/或销售本软件的副本，
 * 并允许在本软件所提供的情况下向其提供软件的人员这样做，但须符合以下条件：
 * <p>
 * 上述版权声明和本许可声明应包含在所有副本或实质性部分中。
 * <p>
 * 本软件按"原样"提供，无任何明示或暗示的担保，包括但不限于适销性、特定用途适用性和非侵权性的担保。
 * 作者或版权持有人在任何情况下均不承担任何索赔、损害赔偿或其他责任的责任，无论是在合同诉讼、侵权行为还是其他方面，
 * 由软件或使用或其他方式引起的，或与软件或使用或其他方式有关的索赔、损害赔偿或其他责任。
 */

public class Mixins_Time_Stop {
    @Mixin({ServerLevel.class})
    public static class ServerLevelMixin {
        @Inject(method = {"blockUpdated"}, at = {@At("HEAD")}, cancellable = true)
        private void blockUpdated(BlockPos p_8743_, Block p_8744_, CallbackInfo ci) {
            ClientLevel level = (Minecraft.getInstance()).level;
            if (level == null) return;
            if (TimeDataHandler.get().canTimeStopped()) ci.cancel();
        }

        @Inject(method = {"tickBlock"}, at = {@At("HEAD")}, cancellable = true)
        private void tickBlock(BlockPos p_184113_, Block p_184114_, CallbackInfo ci) {
            ClientLevel level = (Minecraft.getInstance()).level;
            if (level == null) return;
            if (TimeDataHandler.get().canTimeStopped()) ci.cancel();
        }

        @Inject(method = {"tickChunk"}, at = {@At("HEAD")}, cancellable = true)
        private void tickChunk(LevelChunk levelChunk, int p_8716_, CallbackInfo ci) {
            ClientLevel level = (Minecraft.getInstance()).level;
            if (level == null) return;
            if (TimeDataHandler.get().canTimeStopped()) ci.cancel();
        }

        @Inject(method = {"advanceWeatherCycle"}, at = {@At("HEAD")}, cancellable = true)
        private void advanceWeatherCycle(CallbackInfo ci) {
            ClientLevel level = (Minecraft.getInstance()).level;
            if (level == null) return;
            if (TimeDataHandler.get().canTimeStopped()) ci.cancel();
        }

        @Inject(method = {"tickNonPassenger"}, at = {@At("HEAD")}, cancellable = true)
        private void tickNonPassenger(Entity entity, CallbackInfo ci) {
            ClientLevel level = (Minecraft.getInstance()).level;
            if (level == null) return;
            TimeDataHandler handler = TimeDataHandler.get();
            if (handler.canTimeStopped() && handler.canEntityBeStopped(entity)) ci.cancel();
        }

        @Inject(method = {"tickPassenger"}, at = {@At("HEAD")}, cancellable = true)
        private void tickPassenger(Entity vehicle, Entity passenger, CallbackInfo ci) {
            ClientLevel level = (Minecraft.getInstance()).level;
            if (level == null) return;
            TimeDataHandler handler = TimeDataHandler.get();
            if (handler.canTimeStopped() && handler.canEntityBeStopped(passenger)) ci.cancel();
        }

        @Inject(method = {"tickFluid"}, at = {@At("HEAD")}, cancellable = true)
        private void tickFluid(BlockPos p_184077_, Fluid p_184078_, CallbackInfo ci) {
            ClientLevel level = (Minecraft.getInstance()).level;
            if (level == null) return;
            if (TimeDataHandler.get().canTimeStopped()) ci.cancel();
        }

        @Inject(method = {"tickTime"}, at = {@At("HEAD")}, cancellable = true)
        private void tickTime(CallbackInfo ci) {
            ClientLevel level = (Minecraft.getInstance()).level;
            if (level == null) return;
            if (TimeDataHandler.get().canTimeStopped()) ci.cancel();
        }

        @Inject(method = {"tickCustomSpawners"}, at = {@At("HEAD")}, cancellable = true)
        private void tickCustomSpawners(boolean p_8800_, boolean p_8801_, CallbackInfo ci) {
            ClientLevel level = (Minecraft.getInstance()).level;
            if (level == null) return;
            if (TimeDataHandler.get().canTimeStopped()) ci.cancel();
        }

        @Inject(method = {"tickChunk"}, at = {@At("HEAD")}, cancellable = true)
        private void tickChunk(CallbackInfo ci) {
            ClientLevel level = (Minecraft.getInstance()).level;
            if (level == null) return;
            if (TimeDataHandler.get().canTimeStopped()) ci.cancel();
        }

        @Inject(method = {"runBlockEvents"}, at = {@At("HEAD")}, cancellable = true)
        private void runBlockEvents(CallbackInfo ci) {
            ClientLevel level = (Minecraft.getInstance()).level;
            if (level == null) return;
            if (TimeDataHandler.get().canTimeStopped()) ci.cancel();
        }
    }

    @Mixin(value = {RenderLevelStageEvent.class}, remap = false)
    public static class RenderLevelStageEventMixin {
        @Inject(method = {"getPartialTick"}, at = {@At("HEAD")}, cancellable = true)
        private void getPartialTick(CallbackInfoReturnable<Float> cir) {
            ClientLevel level = (Minecraft.getInstance()).level;
            if (level == null) return;
            TimeDataHandler handler = TimeDataHandler.get();
            if (handler.canTimeStopped()) cir.setReturnValue(0.0F);
        }
    }

    @Mixin(ExperienceOrb.class)
    public static class ExperienceOrbMixin {
        @Inject(method = {"playerTouch"}, at = {@At("HEAD")}, cancellable = true)
        private void playerTouch(Player p_20792_, CallbackInfo ci) {
            ClientLevel level = (Minecraft.getInstance()).level;
            if (level == null) return;
            TimeDataHandler handler = TimeDataHandler.get();
            if (handler.canTimeStopped()) ci.cancel();
        }
    }

    @Mixin(ItemEntity.class)
    public static class ItemEntityMixin {
        @Inject(method = {"playerTouch"}, at = {@At("HEAD")}, cancellable = true)
        private void playerTouch(Player p_20792_, CallbackInfo ci) {
            ClientLevel level = (Minecraft.getInstance()).level;
            if (level == null) return;
            TimeDataHandler handler = TimeDataHandler.get();
            if (handler.canTimeStopped()) ci.cancel();
        }
    }

    @Mixin(value = {ForgeEventFactory.class}, remap = false)
    public static class ForgeEventFactoryMixin {
        @Unique
        private static boolean nOBTG$timestop;

        @Inject(method = {"onRenderTickStart"}, at = {@At("HEAD")}, cancellable = true)
        private static void onRenderTickStart(CallbackInfo ci) {
            ClientLevel level = (Minecraft.getInstance()).level;
            if (level == null) return;
            if (TimeDataHandler.get().canTimeStopped()) ci.cancel();
        }

        @Inject(method = {"onRenderTickEnd"}, at = {@At("HEAD")}, cancellable = true)
        private static void onRenderTickEnd(CallbackInfo ci) {
            ClientLevel level = (Minecraft.getInstance()).level;
            if (level == null) return;
            Minecraft mc = Minecraft.getInstance();
            if (TimeDataHandler.get().canTimeStopped()) {
                nOBTG$timestop = true;
                if (mc.gameRenderer.currentEffect() == null)
                    mc.gameRenderer.loadEffect(TimeDataHandler.ShaderHelper.getShader());
                ci.cancel();
            } else if (nOBTG$timestop) {
                nOBTG$timestop = false;
                mc.gameRenderer.checkEntityPostEffect(mc.getCameraEntity());
            }
        }

        @Inject(method = {"onPreClientTick"}, at = {@At("HEAD")}, cancellable = true)
        private static void onPreClientTick(CallbackInfo ci) {
            ClientLevel level = (Minecraft.getInstance()).level;
            if (level == null) return;
            if (TimeDataHandler.get().canTimeStopped()) ci.cancel();
        }

        @Inject(method = {"onPostClientTick"}, at = {@At("HEAD")}, cancellable = true)
        private static void onPostClientTick(CallbackInfo ci) {
            ClientLevel level = (Minecraft.getInstance()).level;
            if (level == null) return;
            if (TimeDataHandler.get().canTimeStopped()) ci.cancel();
        }

        @Inject(method = {"onPreServerTick"}, at = {@At("HEAD")}, cancellable = true)
        private static void onPreServerTick(CallbackInfo ci) {
            ClientLevel level = (Minecraft.getInstance()).level;
            if (level == null) return;
            if (TimeDataHandler.get().canTimeStopped()) ci.cancel();
        }

        @Inject(method = {"onPostServerTick"}, at = {@At("HEAD")}, cancellable = true)
        private static void onPostServerTick(CallbackInfo ci) {
            ClientLevel level = (Minecraft.getInstance()).level;
            if (level == null) return;
            if (TimeDataHandler.get().canTimeStopped()) ci.cancel();
        }

        @Inject(method = {"onPreLevelTick"}, at = {@At("HEAD")}, cancellable = true)
        private static void onPreLevelTick(Level level, BooleanSupplier haveTime, CallbackInfo ci) {
            ClientLevel clientLevel = (Minecraft.getInstance()).level;
            if (clientLevel == null) return;
            TimeDataHandler handler = TimeDataHandler.get();
            if (handler.canTimeStopped()) {
                ci.cancel();
                if (handler.stopTime > 0) {
                    handler.stopTime--;
                } else {
                    handler.setTimeStopped(false);
                    handler.removeTimeManipulator();
                    handler.syncData(level);
                }
            }
        }

        @Inject(method = {"onPostLevelTick"}, at = {@At("HEAD")}, cancellable = true)
        private static void onPostLevelTick(CallbackInfo ci) {
            ClientLevel level = (Minecraft.getInstance()).level;
            if (level == null) return;
            if (TimeDataHandler.get().canTimeStopped()) ci.cancel();
        }
    }

    @Mixin({ClientLevel.class})
    public static class ClientLevelMixin {
        @Inject(method = {"tick"}, at = {@At("HEAD")}, cancellable = true)
        private void tick(CallbackInfo ci) {
            ClientLevel level = (Minecraft.getInstance()).level;
            if (level == null) return;
            if (TimeDataHandler.get().canTimeStopped()) ci.cancel();
        }

        @Inject(method = {"doAnimateTick"}, at = {@At("HEAD")}, cancellable = true)
        private void animateTick(CallbackInfo ci) {
            ClientLevel level = (Minecraft.getInstance()).level;
            if (level == null) return;
            if (TimeDataHandler.get().canTimeStopped()) ci.cancel();
        }

        @Inject(method = {"tickNonPassenger"}, at = {@At("HEAD")}, cancellable = true)
        private void tickNonPassenger(Entity entity, CallbackInfo ci) {
            ClientLevel level = (Minecraft.getInstance()).level;
            if (level == null) return;
            TimeDataHandler handler = TimeDataHandler.get();
            if (handler.canTimeStopped() && handler.canEntityBeStopped(entity)) ci.cancel();
        }

        @Inject(method = {"tickPassenger"}, at = {@At("HEAD")}, cancellable = true)
        private void tickPassenger(Entity vehicle, Entity passenger, CallbackInfo ci) {
            ClientLevel level = (Minecraft.getInstance()).level;
            if (level == null) return;
            TimeDataHandler handler = TimeDataHandler.get();
            if (handler.canTimeStopped() && handler.canEntityBeStopped(passenger)) ci.cancel();
        }

        @Inject(method = {"tickTime"}, at = {@At("HEAD")}, cancellable = true)
        private void tickTime(CallbackInfo ci) {
            ClientLevel level = (Minecraft.getInstance()).level;
            if (level == null) return;
            if (TimeDataHandler.get().canTimeStopped()) ci.cancel();
        }

        @Inject(method = {"pollLightUpdates"}, at = {@At("HEAD")}, cancellable = true)
        private void pollLightUpdates(CallbackInfo ci) {
            ClientLevel level = (Minecraft.getInstance()).level;
            if (level == null) return;
            if (TimeDataHandler.get().canTimeStopped()) ci.cancel();
        }
    }

    @Mixin({GameRenderer.class})
    public static class GameRendererMixin {
        @Shadow
        private int tick;

        @Shadow
        @Final
        Minecraft minecraft;

        @Shadow
        @Final
        private Camera mainCamera;

        @Shadow
        private int itemActivationTicks;

        @Shadow
        private float darkenWorldAmount;

        @Shadow
        private boolean effectActive;

        @Shadow
        @Nullable PostChain postEffect;

        @Redirect(method = {"render"}, at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/GameRenderer;effectActive:Z"))
        private boolean effectActive(GameRenderer gameRenderer) {
            ClientLevel level = (Minecraft.getInstance()).level;
            if (level == null) return this.effectActive;
            if (TimeDataHandler.get().canTimeStopped() && this.postEffect != null && this.postEffect.getName().equals(TimeDataHandler.ShaderHelper.getShader().toString()))
                return true;
            return this.effectActive;
        }

        @Inject(method = {"tick"}, at = {@At("HEAD")})
        private void tick(CallbackInfo ci) {
            ClientLevel level = (Minecraft.getInstance()).level;
            if (level == null) return;
            if (TimeDataHandler.get().canTimeStopped()) {
                this.tick--;
                if (this.minecraft.gui.getBossOverlay().shouldDarkenScreen()) {
                    this.darkenWorldAmount -= 0.05F;
                } else if (this.darkenWorldAmount > 0.0F) {
                    this.darkenWorldAmount += 0.0125F;
                }
                if (this.itemActivationTicks > 0) this.itemActivationTicks++;
            }
        }

        @Inject(method = {"checkEntityPostEffect"}, at = {@At("HEAD")}, cancellable = true)
        private void checkEntityPostEffect(CallbackInfo ci) {
            ClientLevel level = (Minecraft.getInstance()).level;
            if (level == null) return;
            if (TimeDataHandler.get().canTimeStopped()) ci.cancel();
        }

        @Inject(method = {"tickFov"}, at = {@At("HEAD")}, cancellable = true)
        private void tickFov(CallbackInfo ci) {
            ClientLevel level = (Minecraft.getInstance()).level;
            if (level == null) return;
            TimeDataHandler handler = TimeDataHandler.get();
            if (handler.canTimeStopped() && handler.canEntityBeStopped(Objects.requireNonNull(this.minecraft.getCameraEntity())))
                ci.cancel();
        }

        @ModifyArg(method = {"renderLevel"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;getFov(Lnet/minecraft/client/Camera;FZ)D"), index = 1)
        private float getFov(float original) {
            ClientLevel level = (Minecraft.getInstance()).level;
            if (level == null) return original;
            TimeDataHandler handler = TimeDataHandler.get();
            Entity entity = this.mainCamera.getEntity();
            if (handler.canTimeStopped() && !handler.canEntityBeStopped(entity))
                return Minecraft.getInstance().getPartialTick();
            return original;
        }

        @ModifyArg(method = {"renderLevel"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;bobHurt(Lcom/mojang/blaze3d/vertex/PoseStack;F)V"), index = 1)
        private float bobHurt(float original) {
            return nOBTG$PartialTick(original);
        }

        @ModifyArg(method = {"renderLevel"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setup(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/world/entity/Entity;ZZF)V"), index = 4)
        private float setup(float original) {
            return nOBTG$PartialTick(original);
        }

        @ModifyArg(method = {"renderLevel"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;bobView(Lcom/mojang/blaze3d/vertex/PoseStack;F)V"), index = 1)
        private float bobView(float original) {
            return nOBTG$PartialTick(original);
        }

        @Unique
        private float nOBTG$PartialTick(float original) {
            ClientLevel level = (Minecraft.getInstance()).level;
            if (level == null) return original;
            Camera camera = this.mainCamera;
            TimeDataHandler handler = TimeDataHandler.get();
            if (handler.canTimeStopped() && !handler.canEntityBeStopped(camera.getEntity()))
                return Minecraft.getInstance().getPartialTick();
            return original;
        }

        @ModifyArg(method = {"renderLevel"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;renderItemInHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/Camera;F)V"), index = 2)
        private float renderItemInHand(float original) {
            return nOBTG$PartialTick(original);
        }
    }

    @Mixin({LevelRenderer.class})
    public static class LevelRendererMixin {
        @Unique
        private long nOBTG$millis = Util.getMillis();

        @Redirect(method = {"renderWorldBorder"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/Util;getMillis()J"))
        private long getMillis() {
            ClientLevel level = (Minecraft.getInstance()).level;
            if (level == null) return Util.getMillis();
            if (TimeDataHandler.get().canTimeStopped()) return this.nOBTG$millis;
            this.nOBTG$millis = Util.getMillis();
            return Util.getMillis();
        }

        @Inject(method = {"tick"}, at = {@At("HEAD")}, cancellable = true)
        private void tick(CallbackInfo ci) {
            ClientLevel level = (Minecraft.getInstance()).level;
            if (level == null) return;
            if (TimeDataHandler.get().canTimeStopped()) ci.cancel();
        }

        @Inject(method = {"tickRain"}, at = {@At("HEAD")}, cancellable = true)
        private void tickRain(CallbackInfo ci) {
            ClientLevel level = (Minecraft.getInstance()).level;
            if (level == null) return;
            if (TimeDataHandler.get().canTimeStopped()) ci.cancel();
        }
    }

    @Mixin({Minecraft.class})
    public static class MinecraftMixin {

        @ModifyArg(method = {"runTick"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;render(FJZ)V"), index = 0)
        private float partialTick(float original) {
            ClientLevel level = (Minecraft.getInstance()).level;
            if (level == null) return original;
            TimeDataHandler handler = TimeDataHandler.get();
            if (handler.canTimeStopped()) return handler.getPausePartialTick();
            return original;
        }

        @Redirect(method = {"handleKeybinds"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;consumeClick()Z", ordinal = 2))
        private boolean handleKeybinds(KeyMapping keyMapping) {
            return nOBTG$get(keyMapping);
        }

        @Unique
        private boolean nOBTG$get(KeyMapping keyMapping) {
            ClientLevel level = (Minecraft.getInstance()).level;
            if (level == null) return keyMapping.consumeClick();
            TimeDataHandler handler = TimeDataHandler.get();
            if (handler.canTimeStopped() && handler.canEntityBeStopped(((Minecraft) (Object) this).player))
                return false;
            return keyMapping.consumeClick();
        }

        @Redirect(method = {"handleKeybinds"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;consumeClick()Z", ordinal = 15))
        private boolean handleKeybinds2(KeyMapping keyMapping) {
            return nOBTG$get(keyMapping);
        }
    }


    @Mixin(RenderStateShard.class)
    public static class RenderStateShardMixin {
        @Inject(method = "setupGlintTexturing", at = @At("HEAD"), cancellable = true)
        private static void setupGlintTexturing(float p_110187_, CallbackInfo ci) {
            if (TimeDataHandler.get().canTimeStopped()) {
                long i = (long) ((double) Util.getMillis() * Minecraft.getInstance().options.glintSpeed().get() * 8.0D);
                float f = (float) (i % 110000L) / 110000.0F;
                float f1 = (float) (i % 30000L) / 30000.0F;
                Matrix4f matrix4f = (new Matrix4f()).translation(-f, f1, 0.0F);
                matrix4f.rotateZ(0.17453292F).scale(p_110187_);
                RenderSystem.setTextureMatrix(matrix4f);
                ci.cancel();
            }
        }
    }

    @Mixin({TextureManager.class})
    public abstract static class TextureManagerMixin {
        @Inject(method = {"tick"}, at = {@At("HEAD")}, cancellable = true)
        private void tick(CallbackInfo ci) {
            ClientLevel level = Minecraft.getInstance().level;
            if (level == null) return;
            if (TimeDataHandler.get().canTimeStopped()) ci.cancel();
        }
    }

    @Mixin({LevelChunk.class})
    public static class LevelChunkMixin {
        @Inject(method = {"isTicking"}, at = {@At("HEAD")}, cancellable = true)
        private void isTicking(BlockPos p_156411_, CallbackInfoReturnable<Boolean> cir) {
            ClientLevel level = (Minecraft.getInstance()).level;
            if (level == null) return;
            if (TimeDataHandler.get().canTimeStopped()) cir.setReturnValue(Boolean.FALSE);
        }
    }


    @Mixin({Level.class})
    public static class LevelMixin {
        @Inject(method = {"tickBlockEntities"}, at = {@At("HEAD")}, cancellable = true)
        private void tickBlockEntities(CallbackInfo ci) {
            ClientLevel level = (Minecraft.getInstance()).level;
            if (level == null) return;
            if (TimeDataHandler.get().canTimeStopped()) ci.cancel();
        }

        @Inject(method = {"updateSkyBrightness"}, at = {@At("HEAD")}, cancellable = true)
        private void updateSkyBrightness(CallbackInfo ci) {
            ClientLevel level = (Minecraft.getInstance()).level;
            if (level == null) return;
            if (TimeDataHandler.get().canTimeStopped()) ci.cancel();
        }
    }


    @Mixin({LevelTicks.class})
    public static class LevelTicksMixin {
        @Inject(method = {"tick"}, at = {@At("HEAD")}, cancellable = true)
        private void tick(CallbackInfo ci) {
            ClientLevel level = (Minecraft.getInstance()).level;
            if (level == null) return;
            if (TimeDataHandler.get().canTimeStopped()) ci.cancel();
        }
    }

    @Mixin({ServerChunkCache.class})
    public static class ServerChunkCacheMixin {
        @Inject(method = {"tick(Ljava/util/function/BooleanSupplier;Z)V"}, at = {@At("HEAD")}, cancellable = true)
        private void tick(BooleanSupplier p_201913_, boolean p_201914_, CallbackInfo ci) {
            ClientLevel level = (Minecraft.getInstance()).level;
            if (level == null) return;
            if (TimeDataHandler.get().canTimeStopped()) ci.cancel();
        }
    }

    @Mixin({ServerFunctionManager.class})
    public static class ServerFunctionManagerMixin {
        @Inject(method = {"tick"}, at = {@At("HEAD")}, cancellable = true)
        private void tick(CallbackInfo ci) {
            ClientLevel level = (Minecraft.getInstance()).level;
            if (level == null) return;
            if (TimeDataHandler.get().canTimeStopped()) ci.cancel();
        }
    }

    @Mixin({Util.class})
    public static class UtilMixin {
        @Inject(method = {"getMillis"}, at = {@At("HEAD")}, cancellable = true)
        private static void getMillis(@NotNull CallbackInfoReturnable<Long> cir) {
            cir.setReturnValue(TimeDataHandler.TimeUtil.timeMills);
        }
    }

    @Mixin({ParticleEngine.class})
    public static class ParticleEngineMixin {
        @Inject(method = {"tickParticle"}, at = {@At("HEAD")}, cancellable = true)
        private void tickParticle(Particle particle, CallbackInfo ci) {
            ClientLevel level = (Minecraft.getInstance()).level;
            if (level == null) return;
            if (TimeDataHandler.get().canTimeStopped()) ci.cancel();
        }
    }

    @Mixin({Camera.class})
    public static class CameraMixin {
        @Shadow
        private Entity entity;

        @Inject(method = {"tick"}, at = {@At("HEAD")}, cancellable = true)
        private void tick(CallbackInfo ci) {
            ClientLevel level = (Minecraft.getInstance()).level;
            if (level == null) return;
            TimeDataHandler handler = TimeDataHandler.get();
            if (handler.canTimeStopped() && handler.canEntityBeStopped(this.entity)) ci.cancel();
        }
    }

    @Mixin({ChatListener.class})
    public static class ChatListenerMixin {
        @Inject(method = {"tick"}, at = {@At("HEAD")}, cancellable = true)
        private void tick(CallbackInfo ci) {
            ClientLevel level = (Minecraft.getInstance()).level;
            if (level == null) return;
            if (TimeDataHandler.get().canTimeStopped()) ci.cancel();
        }
    }

    @Mixin({AbstractContainerScreen.class})
    public static class AbstractContainerScreenMixin {
        @Inject(method = {"slotClicked"}, at = {@At("HEAD")}, cancellable = true)
        private void slotClicked(CallbackInfo ci) {
            ClientLevel level = (Minecraft.getInstance()).level;
            if (level == null) return;
            TimeDataHandler handler = TimeDataHandler.get();
            if (handler.canTimeStopped() && handler.canEntityBeStopped((Minecraft.getInstance()).player)) ci.cancel();
        }
    }

    @Mixin({CreativeModeInventoryScreen.class})
    public static class CreativeModeInventoryScreenMixin {
        @Inject(method = {"slotClicked"}, at = {@At("HEAD")}, cancellable = true)
        private void slotClicked(CallbackInfo ci) {
            ClientLevel level = (Minecraft.getInstance()).level;
            if (level == null) return;
            TimeDataHandler handler = TimeDataHandler.get();
            if (handler.canTimeStopped() && handler.canEntityBeStopped((Minecraft.getInstance()).player)) ci.cancel();
        }
    }

    @Mixin({Gui.class})
    public static class GuiMixin {
        @Inject(method = {"tick*"}, at = {@At("HEAD")}, cancellable = true)
        private void tick(CallbackInfo ci) {
            ClientLevel level = (Minecraft.getInstance()).level;
            if (level == null) return;
            if (TimeDataHandler.get().canTimeStopped()) ci.cancel();
        }
    }

    @Mixin({ItemInHandRenderer.class})
    public static class ItemInHandRendererMixin {
        @Shadow
        @Final
        private Minecraft minecraft;

        @Inject(method = {"tick"}, at = {@At("HEAD")}, cancellable = true)
        private void tick(CallbackInfo ci) {
            ClientLevel level = (Minecraft.getInstance()).level;
            LocalPlayer clientPlayer = this.minecraft.player;
            if (level == null) return;
            if (clientPlayer == null) return;
            TimeDataHandler handler = TimeDataHandler.get();
            if (handler.canTimeStopped() && handler.canEntityBeStopped(clientPlayer)) ci.cancel();
        }
    }


    @Mixin({LightTexture.class})
    public static class LightTextureMixin {
        @Inject(method = {"tick"}, at = {@At("HEAD")}, cancellable = true)
        private void tick(CallbackInfo ci) {
            ClientLevel level = (Minecraft.getInstance()).level;
            if (level == null) return;
            if (TimeDataHandler.get().canTimeStopped()) ci.cancel();
        }
    }


    @Mixin({MusicManager.class})
    public static class MusicManagerMixin {
        @Inject(method = {"tick"}, at = {@At("HEAD")}, cancellable = true)
        private void tick(CallbackInfo ci) {
            ClientLevel level = (Minecraft.getInstance()).level;
            if (level == null) return;
            if (TimeDataHandler.get().canTimeStopped()) ci.cancel();
        }
    }


    @Mixin({SoundManager.class})
    public static class SoundManagerMixin {
        @ModifyArg(method = {"tick"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/SoundEngine;tick(Z)V"))
        private boolean tick(boolean paused) {
            ClientLevel level = (Minecraft.getInstance()).level;
            if (level == null) return paused;
            if (TimeDataHandler.get().canTimeStopped()) return true;
            return paused;
        }
    }

    @Mixin({Tutorial.class})
    public static class TutorialMixin {
        @Inject(method = {"tick"}, at = {@At("HEAD")}, cancellable = true)
        private void tick(CallbackInfo ci) {
            ClientLevel level = (Minecraft.getInstance()).level;
            if (level == null) return;
            if (TimeDataHandler.get().canTimeStopped()) ci.cancel();
        }
    }

    @Mixin({EndDragonFight.class})
    public static class EndDragonFightMixin {
        @Inject(method = {"tick"}, at = {@At("HEAD")}, cancellable = true)
        private void tick(CallbackInfo ci) {
            ClientLevel level = (Minecraft.getInstance()).level;
            if (level == null) return;
            if (TimeDataHandler.get().canTimeStopped()) ci.cancel();
        }
    }

    @Mixin({Inventory.class})
    public static class InventoryMixin {
        @Shadow
        @Final
        public Player player;

        @Inject(method = {"swapPaint"}, at = {@At("HEAD")}, cancellable = true)
        private void swapPaint(double i, CallbackInfo ci) {
            ClientLevel level = (Minecraft.getInstance()).level;
            if (level == null) return;
            TimeDataHandler handler = TimeDataHandler.get();
            if (handler.canTimeStopped() && handler.canEntityBeStopped(this.player)) ci.cancel();
        }
    }

    @Mixin({PersistentEntitySectionManager.class})
    public static class PersistentEntitySectionManagerMixin {
        @Inject(method = {"tick"}, at = {@At("HEAD")}, cancellable = true)
        private void tick(CallbackInfo ci) {
            ClientLevel level = (Minecraft.getInstance()).level;
            if (level == null) return;
            if (TimeDataHandler.get().canTimeStopped()) ci.cancel();
        }
    }

    @Mixin({Raids.class})
    public static class RaidsMixin {
        @Inject(method = {"tick"}, at = {@At("HEAD")}, cancellable = true)
        private void tick(CallbackInfo ci) {
            ClientLevel level = (Minecraft.getInstance()).level;
            if (level == null) return;
            if (TimeDataHandler.get().canTimeStopped()) ci.cancel();
        }
    }

    @Mixin({WorldBorder.class})
    public static class WorldBorderMixin {
        @Inject(method = {"tick"}, at = {@At("HEAD")}, cancellable = true)
        private void tick(CallbackInfo ci) {
            ClientLevel level = (Minecraft.getInstance()).level;
            if (level == null) return;
            if (TimeDataHandler.get().canTimeStopped()) ci.cancel();
        }
    }
}
