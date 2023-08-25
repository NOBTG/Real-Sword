package com.nobtg;

import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * This Project contains code from both GPL-licensed and MIT-licensed components.
 * <p>
 * For the GPL-licensed component:
 * <p>
 *  Original author: ji_GGO (ClassicSwordBlocking Mod)
 * <p>
 *  This code is distributed under the terms of the GNU General Public License.
 *  You should have received a copy of the GNU General Public License along with this code.
 *  If not, see <a href="https://www.gnu.org/licenses/gpl-3.0.html">https://www.gnu.org/licenses/gpl-3.0.html</a>.
 * <p>
 *  Project link: <a href="https://github.com/jiGGO1/ClassicSwordBlocking/tree/master">https://github.com/jiGGO1/ClassicSwordBlocking/tree/master</a>
 * <p>
 *  Mod link: <a href="https://www.curseforge.com/minecraft/mc-mods/classic-sword-blocking">https://www.curseforge.com/minecraft/mc-mods/classic-sword-blocking</a>
 * <p>
 * For the MIT-licensed component:
 * <p>
 *  Original author: Suel_ki (Time Stop Clock Mod)
 * <p>
 *  This code is distributed under the terms of the MIT License.
 *  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
 *  to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 *  and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p>
 *  The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * <p>
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *  WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * <p>
 *  Mod link: <a href="https://modrinth.com/mod/time-stop-clock-mod">https://modrinth.com/mod/time-stop-clock-mod</a>
 * <p>
 * <p>
 * 本项目包含了使用 GPL 许可证和 MIT 许可证的代码。
 * <p>
 * 对于使用 GPL 许可证的部分：
 * <p>
 *  原作者：ji_GGO（ClassicSwordBlocking Mod）
 * <p>
 *  本代码根据 GNU General Public License 的条款分发。
 *  您应该已经收到了 GNU General Public License 的副本，与此代码一起。
 *  如果没有，访问 <a href="https://www.gnu.org/licenses/gpl-3.0.html">https://www.gnu.org/licenses/gpl-3.0.html</a>。
 * <p>
 *  项目链接：<a href="https://github.com/jiGGO1/ClassicSwordBlocking/tree/master">https://github.com/jiGGO1/ClassicSwordBlocking/tree/master</a>
 * <p>
 *  Mod 链接：<a href="https://www.curseforge.com/minecraft/mc-mods/classic-sword-blocking">https://www.curseforge.com/minecraft/mc-mods/classic-sword-blocking</a>
 * <p>
 * 对于使用 MIT 许可证的部分：
 * <p>
 *  原作者：Suel_ki（Time Stop Clock Mod）
 * <p>
 *  本代码根据 MIT License 的条款分发。
 *  根据以下条件，任何获得本软件及相关文档文件的人可以免费使用本软件：
 *  - 在所有副本或实质性部分中都包含了上述版权声明和本许可声明。
 * <p>
 *  本软件按“原样”提供，不附带任何形式的担保，无论是明示的还是暗示的，包括但不限于对适销性、特定用途适用性和非侵权的保证。在任何情况下，作者或版权持有人均不承担任何索赔、损害赔偿或其他责任的责任，
 * 无论是在合同诉讼、侵权行为还是其他方面，由软件或使用或其他方式引起的，或与软件或使用或其他方式有关的索赔、损害赔偿或其他责任。
 * <p>
 *  Mod 链接：<a href="https://modrinth.com/mod/time-stop-clock-mod">https://modrinth.com/mod/time-stop-clock-mod</a>
 */

@Mod(RealSwordMod.MOD_ID)
public class RealSwordMod {
    public static final Set<UUID> REAL_ENTITIES = new HashSet<>();
    public static final Set<UUID> DEAD_ENTITIES = new HashSet<>();
    public static final Set<String> NO_SPAWN_ENTITIES = new HashSet<>();
    public static final String MOD_ID = "real_sword";
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    public static final RegistryObject<Item> REAL_SWORD = ITEMS.register("real_sword", RealSword::new);

    public RealSwordMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::setup);
        ITEMS.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.addListener(this::registerCommand);
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                TimeDataHandler.TimeUtil.update();
            }
        }, 1L, 1L);
    }

    private void setup(FMLCommonSetupEvent event) {
        TimeDataHandler.NetworkHandler.registerMessages();
    }

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel PACKET_HANDLER = NetworkRegistry.newSimpleChannel(new ResourceLocation(MOD_ID, MOD_ID), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
    private static int messageID = 0;

    public static <T> void addNetworkMessage(Class<T> messageType, BiConsumer<T, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, T> decoder, BiConsumer<T, Supplier<NetworkEvent.Context>> messageConsumer) {
        PACKET_HANDLER.registerMessage(messageID, messageType, encoder, decoder, messageConsumer);
        messageID++;
    }

    public static final Collection<AbstractMap.SimpleEntry<Runnable, Integer>> workQueue = new ConcurrentLinkedQueue<>();

    public static void queueServerWork(int tick, Runnable action) {
        workQueue.add(new AbstractMap.SimpleEntry<>(action, tick));
    }

    public void registerCommand(@NotNull RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal(".kill")
                .then(Commands.argument("targets", EntityArgument.entities()).executes(arguments -> {
                    for (Entity entity : EntityArgument.getEntities(arguments, "targets")) {
                        RealSword.AttackEntity(arguments.getSource().getPlayer(), entity);
                    }
                    return 0;
                })));
        event.getDispatcher().register(Commands.literal(".unreal")
                .then(Commands.argument("targets", EntityArgument.entities()).executes(arguments -> {
                    for (Entity entity : EntityArgument.getEntities(arguments, "targets")) {
                        RealSword.UnReal(entity);
                    }
                    return 0;
                })));
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModVariables {
        @SubscribeEvent
        public static void init(FMLCommonSetupEvent event) {
            addNetworkMessage(SavedDataSyncMessage.class, SavedDataSyncMessage::buffer, SavedDataSyncMessage::new, SavedDataSyncMessage::handler);
        }

        public static class WorldVariables extends SavedData {
            public static final String DATA_NAME = MOD_ID;

            public static @NotNull RealSwordMod.ModVariables.WorldVariables load(CompoundTag tag) {
                WorldVariables data = new WorldVariables();
                data.read(tag);
                return data;
            }

            public void read(CompoundTag nbt) {
            }

            @Override
            public @NotNull CompoundTag save(@NotNull CompoundTag nbt) {
                return nbt;
            }

            static WorldVariables clientSide = new WorldVariables();

            public static WorldVariables get(LevelAccessor world) {
                if (world instanceof ServerLevel level) {
                    return level.getDataStorage().computeIfAbsent(WorldVariables::load, ModVariables.WorldVariables::new, DATA_NAME);
                } else {
                    return clientSide;
                }
            }
        }

        public static class MapVariables extends SavedData {
            public static final String DATA_NAME = MOD_ID;
            public boolean isNoRespawn = false;

            public static @NotNull RealSwordMod.ModVariables.MapVariables load(CompoundTag tag) {
                MapVariables data = new MapVariables();
                data.read(tag);
                return data;
            }

            public void read(@NotNull CompoundTag nbt) {
                isNoRespawn = nbt.getBoolean("isNoRespawn");
            }

            @Override
            public @NotNull CompoundTag save(@NotNull CompoundTag nbt) {
                nbt.putBoolean("isNoRespawn", isNoRespawn);
                return nbt;
            }

            public void syncData(LevelAccessor world) {
                this.setDirty();
                if (world instanceof Level && !world.isClientSide())
                    PACKET_HANDLER.send(PacketDistributor.ALL.noArg(), new SavedDataSyncMessage(0, this));
            }

            static MapVariables clientSide = new MapVariables();

            public static MapVariables get(LevelAccessor world) {
                if (world instanceof ServerLevelAccessor serverLevelAcc) {
                    return Objects.requireNonNull(serverLevelAcc.getLevel().getServer().getLevel(Level.OVERWORLD)).getDataStorage().computeIfAbsent(MapVariables::load, ModVariables.MapVariables::new, DATA_NAME);
                } else {
                    return clientSide;
                }
            }
        }

        public static class SavedDataSyncMessage {
            public int type;
            public SavedData data;

            public SavedDataSyncMessage(@NotNull FriendlyByteBuf buffer) {
                this.type = buffer.readInt();
                this.data = this.type == 0 ? new MapVariables() : new WorldVariables();
                if (this.data instanceof MapVariables _mapvars)
                    _mapvars.read(Objects.requireNonNull(buffer.readNbt()));
                else {
                    WorldVariables _worldvars = (WorldVariables) this.data;
                    _worldvars.read(buffer.readNbt());
                }
            }

            public SavedDataSyncMessage(int type, SavedData data) {
                this.type = type;
                this.data = data;
            }

            public static void buffer(@NotNull RealSwordMod.ModVariables.SavedDataSyncMessage message, @NotNull FriendlyByteBuf buffer) {
                buffer.writeInt(message.type);
                buffer.writeNbt(message.data.save(new CompoundTag()));
            }

            public static void handler(SavedDataSyncMessage message, @NotNull Supplier<NetworkEvent.Context> contextSupplier) {
                NetworkEvent.Context context = contextSupplier.get();
                context.enqueueWork(() -> {
                    if (!context.getDirection().getReceptionSide().isServer()) {
                        if (message.type == 0)
                            MapVariables.clientSide = (MapVariables) message.data;
                        else
                            WorldVariables.clientSide = (WorldVariables) message.data;
                    }
                });
                context.setPacketHandled(true);
            }
        }
    }
}
