package com.nobtg;

import com.google.common.collect.Lists;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

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

public class TimeDataHandler {
    private float timeMultiplier = 1.0F;

    private float pausePartialTick;

    private boolean timestop;

    private UUID timeManipulator = UUID.randomUUID();

    private final List<UUID> stopWhitelist = Lists.newArrayList();

    public int stopTime;

    public int soundTick;

    public void setTimeManipulator(Entity entity) {
        this.timeManipulator = entity.getUUID();
    }

    public void removeTimeManipulator() {
        this.timeManipulator = UUID.randomUUID();
    }

    public boolean isInStopWhiteList(Entity entity) {
        return this.stopWhitelist.contains(entity.getUUID());
    }

    public void addAllToStopWhiteList(Collection<? extends UUID> collection) {
        for (UUID uuid : collection) {
            if (!this.stopWhitelist.contains(uuid)) {
                this.stopWhitelist.add(uuid);
                return;
            }
        }
    }

    public boolean canTimeStopped() {
        return this.timestop;
    }

    public void setTimeStopped(boolean stopped) {
        this.timestop = stopped;
    }

    public float getPausePartialTick() {
        return this.pausePartialTick;
    }

    public float getTimeMultiplier() {
        return this.timeMultiplier;
    }

    public boolean isTimeMultiplier(Entity entity) {
        return this.timeManipulator.equals(entity.getUUID());
    }

    public boolean canEntityBeStopped(Entity entity) {
        if (entity == null)
            return false;
        if (isInStopWhiteList(entity) && !this.timeManipulator.equals(entity.getUUID()))
            return false;
        if (entity instanceof net.minecraft.world.entity.player.Player && !this.timeManipulator.equals(entity.getUUID()))
            return false;
        return !this.timeManipulator.equals(entity.getUUID());
    }

    public void read(CompoundTag compoundTag) {
        if (compoundTag == null) {
            this.timestop = false;
            this.timeManipulator = UUID.randomUUID();
            this.stopTime = Integer.MAX_VALUE;
            this.pausePartialTick = 0.0F;
            this.timeMultiplier = 1.0F;
            this.soundTick = 0;
            return;
        }
        this.timestop = compoundTag.getBoolean("TimeStop");
        this.timeManipulator = compoundTag.getUUID("TimeManipulator");
        this.stopTime = compoundTag.getInt("StopTime");
        this.pausePartialTick = compoundTag.getFloat("PausePartialTick");
        this.timeMultiplier = compoundTag.getFloat("TimeMultiplier");
        this.soundTick = compoundTag.getInt("SoundTick");
        loadStopWhiteList(compoundTag.getList("EntitiesInTheStopWhitelist", 10));
    }

    private void loadStopWhiteList(ListTag listTag) {
        List<UUID> uuids = Lists.newArrayList();
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag compoundtag = listTag.getCompound(i);
            UUID uuid = compoundtag.getUUID("InStopWhiteList");
            if (!uuids.contains(uuid))
                uuids.add(uuid);
            addAllToStopWhiteList(uuids);
        }
    }

    public ListTag getInStopWhiteList() {
        ListTag listtag = new ListTag();
        for (UUID uuid : this.stopWhitelist) {
            if (uuid != null) {
                CompoundTag compoundtag = new CompoundTag();
                compoundtag.putUUID("InStopWhiteList", uuid);
                listtag.add(compoundtag);
            }
        }
        return listtag;
    }

    public CompoundTag save(CompoundTag compoundTag) {
        compoundTag.put("EntitiesInTheStopWhitelist", getInStopWhiteList());
        compoundTag.putBoolean("TimeStop", this.timestop);
        compoundTag.putUUID("TimeManipulator", this.timeManipulator);
        compoundTag.putInt("StopTime", this.stopTime);
        compoundTag.putFloat("PausePartialTick", this.pausePartialTick);
        compoundTag.putFloat("TimeMultiplier", this.timeMultiplier);
        compoundTag.putInt("SoundTick", this.soundTick);
        return compoundTag;
    }

    public static TimeDataHandler handler = new TimeDataHandler();

    public static TimeDataHandler get() {
        return handler;
    }

    public void syncData(Level level) {
        if (!level.isClientSide())
            NetworkHandler.sendToAllPlayer(level, new NetworkHandler.ChangeTimeDataPacket(this));
    }

    /**
     * by Suel_ki
     * by She's Time Stop Clock Mod
     * Thank Suel_ki
     */

    public static class TimeUtil {
        public static long timeMills;

        public static float nextTimeMills;

        public static void update() {
            nextTimeMills = get().getTimeMultiplier() + nextTimeMills;
            timeMills = (long)nextTimeMills;
        }
    }

    /**
     * by Suel_ki
     * by She's Time Stop Clock Mod
     * Thank Suel_ki
     */

    public static class ShaderHelper {
        public static final ResourceLocation DESATURATE = new ResourceLocation("shaders/post/desaturate.json");

        public static ResourceLocation getShader() {
            return DESATURATE;
        }
    }

    /**
     * by Suel_ki
     * by She's Time Stop Clock Mod
     * Thank Suel_ki
     */

    public static class NetworkHandler {

        private static int ID = 0;

        public static SimpleChannel INSTANCE;

        private static int nextID() {
            return ID++;
        }

        public static void send(PacketDistributor.PacketTarget target, Object object) {
            INSTANCE.send(target, object);
        }

        public static void sendToAllPlayer(Level level, Object object) {
            if (!level.isClientSide()) {
                MinecraftServer mcServer = ServerLifecycleHooks.getCurrentServer();
                if (mcServer != null && !mcServer.getPlayerList().getPlayers().isEmpty())
                    for (ServerPlayer serverPlayer : mcServer.getPlayerList().getPlayers())
                        send(PacketDistributor.PLAYER.with(() -> serverPlayer), object);
            }
        }

        public static void registerMessages() {
            INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation("timeclock", "timeclock"), () -> "1", "1"::equals, "1"::equals);
            INSTANCE.registerMessage(
                    nextID(), ChangeTimeDataPacket.class, ChangeTimeDataPacket::toBytes, ChangeTimeDataPacket::new, ChangeTimeDataPacket::handler);
        }

        /**
         * by Suel_ki
         * by She's Time Stop Clock Mod
         * Thank Suel_ki
         */

        public static class ChangeTimeDataPacket {
            public TimeDataHandler data;

            public ChangeTimeDataPacket(FriendlyByteBuf buffer) {
                this.data = new TimeDataHandler();
                this.data.read(buffer.readNbt());
            }

            public ChangeTimeDataPacket(TimeDataHandler data) {
                this.data = data;
            }

            public void toBytes(FriendlyByteBuf buffer) {
                buffer.writeNbt(this.data.save(new CompoundTag()));
            }

            public void handler(Supplier<NetworkEvent.Context> contextSupplier) {
                NetworkEvent.Context context = contextSupplier.get();
                context.enqueueWork(() -> handler = this.data);
                context.setPacketHandled(true);
            }
        }
    }
}