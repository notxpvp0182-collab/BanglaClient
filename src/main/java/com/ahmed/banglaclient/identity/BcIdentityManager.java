package com.ahmed.banglaclient.identity;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages official BANGLA CLIENT identity detection, icon texture loading,
 * and container watermarks.
 */
public final class BcIdentityManager {
    public static final int PROTOCOL_VERSION = 1;
    public static final Identifier CHANNEL_ID = Identifier.of(BanglaClient.MOD_ID, "identity_v1");

    // Exact Image 1 BANGLA CLIENT Identity Icon Asset Path
    public static final Identifier IDENTITY_ICON = Identifier.of(BanglaClient.MOD_ID, "textures/gui/bangla_client_identity.png");

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
                        if (entry != null && entry.getProfile() != null && entry.getProfile().name() != null) {
                            verifiedBcNames.add(entry.getProfile().name().toLowerCase());
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
        markLocalPlayerVerified(MinecraftClient.getInstance());
        return verifiedBcUuids.contains(uuid);
    }

    public static boolean isVerifiedBcUserByName(String username) {
        if (username == null || username.isBlank()) return false;
        markLocalPlayerVerified(MinecraftClient.getInstance());
        return verifiedBcNames.contains(username.trim().toLowerCase());
    }

    /**
     * Renders the exact Image 1 BANGLA CLIENT Identity Icon with transparency and proper aspect ratio.
     */
    public static void drawIdentityIcon(DrawContext context, int x, int y, int width, int height) {
        if (context == null || width <= 0 || height <= 0) return;
        try {
            context.drawTexture(RenderPipelines.GUI_TEXTURED, IDENTITY_ICON, x, y, 0.0f, 0.0f, width, height, width, height);
        } catch (Throwable t) {
            context.fill(x + 1, y + height - 3, x + width, y + height, 0xFFEF4444);
            context.fill(x + 2, y + 1, x + width - 2, y + 4, 0xFFFBBF24);
        }
    }

    /**
     * Renders the subtle BANGLA CLIENT watermark at the bottom-right of Inventory & Container screens.
     * Appearance: [Exact identity icon] BANGLA CLIENT
     */
    public static void drawWatermark(DrawContext context, int screenWidth, int screenHeight, TextRenderer textRenderer) {
        if (context == null || textRenderer == null || screenWidth <= 0 || screenHeight <= 0) return;

        String title = BanglaClient.CLIENT_NAME;
        int textW = textRenderer.getWidth(title);
        int iconSize = 9;
        int totalW = iconSize + 4 + textW + 8;
        int totalH = 14;
        int x = screenWidth - totalW - 6;
        int y = screenHeight - totalH - 5;

        // Subtle dark translucent pill background with faint white specular outline
        context.fill(x + 1, y, x + totalW - 1, y + totalH, 0x33000000);
        context.fill(x, y + 1, x + 1, y + totalH - 1, 0x33000000);
        context.fill(x + totalW - 1, y + 1, x + totalW, y + totalH - 1, 0x33000000);
        GlassRenderer.drawOutlineBorder(context, x, y, totalW, totalH, 0x1EFFFFFF);

        // Draw identity icon
        drawIdentityIcon(context, x + 4, y + 2, iconSize, iconSize);

        // Draw text
        context.drawTextWithShadow(textRenderer, title, x + 4 + iconSize + 3, y + 3, 0xDDF4F7FA);
    }

    public static MutableText createBcPrefixBadge() {
        return Text.literal("✦ ").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xEF4444)).withBold(true));
    }

    public static Set<UUID> getVerifiedBcUuids() {
        return Collections.unmodifiableSet(verifiedBcUuids);
    }
}
