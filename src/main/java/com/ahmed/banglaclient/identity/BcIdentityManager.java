package com.ahmed.banglaclient.identity;

import com.ahmed.banglaclient.BanglaClient;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages BANGLA CLIENT identity detection and rendering across Nametags, Tab List, and Chat.
 * Uses a safe, versioned custom payload channel ("banglaclient:identity_v1") that exchanges
 * only protocolVersion and player UUID—never credentials or tokens.
 */
public final class BcIdentityManager {
    public static final int PROTOCOL_VERSION = 1;
    public static final Identifier CHANNEL_ID = Identifier.of(BanglaClient.MOD_ID, "identity_v1");

    public record BcIdentityPayload(int version, UUID playerUuid) implements CustomPayload {
        public static final CustomPayload.Id<BcIdentityPayload> ID = new CustomPayload.Id<>(CHANNEL_ID);
        public static final PacketCodec<PacketByteBuf, BcIdentityPayload> CODEC = PacketCodec.of(
                (value, buf) -> {
                    buf.writeVarInt(value.version());
                    buf.writeUuid(value.playerUuid());
                },
                buf -> new BcIdentityPayload(buf.readVarInt(), buf.readUuid())
        );

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    private static final Set<UUID> verifiedBcUuids = ConcurrentHashMap.newKeySet();
    private static final Set<String> verifiedBcNames = ConcurrentHashMap.newKeySet();
    private static boolean initialized = false;

    private BcIdentityManager() {}

    public static synchronized void initialize() {
        if (initialized) return;
        initialized = true;

        try {
            PayloadTypeRegistry.playC2S().register(BcIdentityPayload.ID, BcIdentityPayload.CODEC);
            PayloadTypeRegistry.playS2C().register(BcIdentityPayload.ID, BcIdentityPayload.CODEC);

            ClientPlayNetworking.registerGlobalReceiver(BcIdentityPayload.ID, (payload, context) -> {
                if (payload != null && payload.version() == PROTOCOL_VERSION && payload.playerUuid() != null) {
                    verifiedBcUuids.add(payload.playerUuid());
                    MinecraftClient mc = context.client();
                    if (mc != null && mc.getNetworkHandler() != null) {
                        var entry = mc.getNetworkHandler().getPlayerListEntry(payload.playerUuid());
                        if (entry != null && entry.getProfile() != null && entry.getProfile().getName() != null) {
                            verifiedBcNames.add(entry.getProfile().getName().toLowerCase());
                        }
                    }
                }
            });
        } catch (Throwable t) {
            BanglaClient.LOGGER.debug("Identity payload registration note: {}", t.getMessage());
        }

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            verifiedBcUuids.clear();
            verifiedBcNames.clear();
            markLocalPlayerVerified(client);
            if (client.player != null && ClientPlayNetworking.canSend(BcIdentityPayload.ID)) {
                ClientPlayNetworking.send(new BcIdentityPayload(PROTOCOL_VERSION, client.player.getUuid()));
            }
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            verifiedBcUuids.clear();
            verifiedBcNames.clear();
        });
    }

    private static void markLocalPlayerVerified(MinecraftClient client) {
        if (client == null) return;
        if (client.player != null) {
            verifiedBcUuids.add(client.player.getUuid());
        }
        if (client.getSession() != null && client.getSession().getUsername() != null) {
            verifiedBcNames.add(client.getSession().getUsername().toLowerCase());
            if (client.getSession().getUuidOrNull() != null) {
                verifiedBcUuids.add(client.getSession().getUuidOrNull());
            }
        }
    }

    public static boolean isVerifiedBcUser(UUID uuid) {
        if (uuid == null) return false;
        MinecraftClient mc = MinecraftClient.getInstance();
        markLocalPlayerVerified(mc);
        return verifiedBcUuids.contains(uuid);
    }

    public static boolean isVerifiedBcUserByName(String username) {
        if (username == null || username.isBlank()) return false;
        MinecraftClient mc = MinecraftClient.getInstance();
        markLocalPlayerVerified(mc);
        return verifiedBcNames.contains(username.trim().toLowerCase());
    }

    /**
     * Renders the recognizable BANGLA CLIENT vector emblem (Red rounded badge + Golden Crown + BC mark).
     */
    public static void drawBcBadge(DrawContext context, int x, int y, int size) {
        if (context == null || size < 6) return;
        context.fill(x, y, x + size, y + size, 0xFFDC2626);
        context.fill(x + 1, y + 1, x + size - 1, y + size - 1, 0xFF991B1B);
        // Top golden crown pixels
        context.fill(x + 1, y, x + size - 1, y + 2, 0xFFFBBF24);
        // Inner white BC pixel mark
        int mid = y + size / 2;
        context.fill(x + 2, y + 3, x + 4, y + size - 2, 0xFFFFFFFF);
        context.fill(x + size - 4, y + 3, x + size - 2, mid, 0xFFFFFFFF);
        context.fill(x + size - 4, mid + 1, x + size - 2, y + size - 2, 0xFFFFFFFF);
    }

    /**
     * Creates a styled [BC] prefix badge for Nametags, Tab List, and Chat.
     */
    public static MutableText createBcPrefixBadge() {
        MutableText left = Text.literal("[").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFBBF24)));
        MutableText bc = Text.literal("BC").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xEF4444)).withBold(true));
        MutableText right = Text.literal("] ").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFBBF24)).withBold(false));
        return left.append(bc).append(right);
    }

    public static Set<UUID> getVerifiedBcUuids() {
        return Collections.unmodifiableSet(verifiedBcUuids);
    }
}
