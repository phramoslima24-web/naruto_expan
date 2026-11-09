package net.narutotoexpansion.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.narutotoexpansion.NarutotoExpansion;
import net.narutotoexpansion.client.ClientSync;
import net.narutotoexpansion.core.BombService;
import net.narutotoexpansion.core.FormService;
import net.narutotoexpansion.data.ExpansionCapability;

import java.util.function.Supplier;

/** Own network channel (separate from Narutoto's). Client actions are always validated on the server. */
public final class ExpansionNetwork {
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(NarutotoExpansion.MODID, "main"),
            () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    public enum Action {
        TOGGLE_FORM, SELECT_FORM, CYCLE_MODE, BOMB_START, BOMB_RELEASE
    }

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, ActionPacket.class, ActionPacket::encode, ActionPacket::decode, ActionPacket::handle);
        CHANNEL.registerMessage(id++, SyncPacket.class, SyncPacket::encode, SyncPacket::decode, SyncPacket::handle);
    }

    public static void sendToServer(Action action) {
        CHANNEL.sendToServer(new ActionPacket(action));
    }

    /** Sends the full add-on state of a player to that player (used by the HUD). */
    public static void sync(ServerPlayer player) {
        CompoundTag tag = ExpansionCapability.get(player).saveForSync();
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SyncPacket(tag));
    }

    /** Client -> server. */
    public static final class ActionPacket {
        private final Action action;

        public ActionPacket(Action action) {
            this.action = action;
        }

        public static void encode(ActionPacket msg, FriendlyByteBuf buf) {
            buf.writeEnum(msg.action);
        }

        public static ActionPacket decode(FriendlyByteBuf buf) {
            return new ActionPacket(buf.readEnum(Action.class));
        }

        public static void handle(ActionPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
            NetworkEvent.Context ctx = ctxSupplier.get();
            ctx.enqueueWork(() -> {
                ServerPlayer sender = ctx.getSender();
                if (sender == null) return;
                switch (msg.action) {
                    case TOGGLE_FORM -> FormService.toggle(sender);
                    case SELECT_FORM -> FormService.cycleSelectedForm(sender);
                    case CYCLE_MODE -> FormService.cycleMode(sender);
                    case BOMB_START -> BombService.startCharge(sender);
                    case BOMB_RELEASE -> BombService.release(sender);
                }
            });
            ctx.setPacketHandled(true);
        }
    }

    /** Server -> client. */
    public static final class SyncPacket {
        private final CompoundTag tag;

        public SyncPacket(CompoundTag tag) {
            this.tag = tag;
        }

        public static void encode(SyncPacket msg, FriendlyByteBuf buf) {
            buf.writeNbt(msg.tag);
        }

        public static SyncPacket decode(FriendlyByteBuf buf) {
            CompoundTag t = buf.readNbt();
            return new SyncPacket(t == null ? new CompoundTag() : t);
        }

        public static void handle(SyncPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
            NetworkEvent.Context ctx = ctxSupplier.get();
            ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientSync.apply(msg.tag)));
            ctx.setPacketHandled(true);
        }
    }

    private ExpansionNetwork() {}
}
