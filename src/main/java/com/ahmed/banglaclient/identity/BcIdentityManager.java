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
import org.joml.Matrix3x2fStack;

import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages BANGLA CLIENT identity detection, transparent large GUI watermark, Tab List icon, and Nametag icon.
 */
public final class BcIdentityManager {
    public static final int PROTOCOL_VERSION = 1;
    public static final Identifier CHANNEL_ID = Identifier.of(BanglaClient.MOD_ID, "identity_v1");
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
                            verifiedBcNames.add(entry.getProfile().name().toLowerCase(Locale.ROOT));
                        }
                    }
                }
            });
        } catch (Throwable ignored) {}

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            markLocalPlayerVerified(client);
            if (client.player != null && ClientPlayNetworking.canSend(BcIdentityPayload.ID)) {
                ClientPlayNetworking.send(new BcIdentityPayload(PROTOCOL_VERSION, client.player.getUuid()));
            }
        });
    }

    private static void markLocalPlayerVerified(MinecraftClient client) {
        if (client == null) return;
        if (client.player != null) {
            verifiedBcUuids.add(client.player.getUuid());
            if (client.player.getName() != null) {
                verifiedBcNames.add(client.player.getName().getString().toLowerCase(Locale.ROOT));
            }
        }
        if (client.getSession() != null && client.getSession().getUsername() != null) {
            verifiedBcNames.add(client.getSession().getUsername().toLowerCase(Locale.ROOT));
            if (client.getSession().getUuidOrNull() != null) {
                verifiedBcUuids.add(client.getSession().getUuidOrNull());
            }
        }
    }

    public static boolean isVerifiedBcUser(UUID uuid) {
        if (uuid == null) return false;
        MinecraftClient mc = MinecraftClient.getInstance();
        markLocalPlayerVerified(mc);
        if (mc != null && mc.player != null && uuid.equals(mc.player.getUuid())) return true;
        return verifiedBcUuids.contains(uuid);
    }

    public static boolean isVerifiedBcUserByName(String rawText) {
        if (rawText == null || rawText.isBlank()) return false;
        MinecraftClient mc = MinecraftClient.getInstance();
        markLocalPlayerVerified(mc);
        String lower = rawText.trim().toLowerCase(Locale.ROOT);
        if (mc != null && mc.getSession() != null && mc.getSession().getUsername() != null) {
            String self = mc.getSession().getUsername().toLowerCase(Locale.ROOT);
            if (!self.isEmpty() && lower.contains(self)) return true;
        }
        for (String name : verifiedBcNames) {
            if (!name.isEmpty() && lower.contains(name)) return true;
        }
        return false;
    }

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
     * Renders the Large Transparent Watermark ([BC Icon] ʙᴀɴɢʟᴀ ᴄʟɪᴇɴᴛ) at the bottom-right of all GUI screens.
     * Zero background box — only the large icon and crisp text!
     */
    public static void drawWatermark(DrawContext context, int screenWidth, int screenHeight, TextRenderer textRenderer) {
        if (context == null || textRenderer == null || screenWidth <= 0 || screenHeight <= 0) return;

        String title = GlassRenderer.toSmallCaps("BANGLA CLIENT");
        float scale = 1.25f;
        int iconSize = 14;
        int scaledTextW = Math.round(textRenderer.getWidth(title) * scale);
        int totalW = iconSize + 5 + scaledTextW;

        int x = screenWidth - totalW - 10;
        int y = screenHeight - 20;

        drawIdentityIcon(context, x, y - 2, iconSize, iconSize);

        Matrix3x2fStack matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.translate(x + iconSize + 5, y);
        matrices.scale(scale, scale);
        context.drawTextWithShadow(textRenderer, title, 0, 0, 0xFFFFFFFF);
        matrices.popMatrix();
    }

    public static MutableText createBcPrefixBadge() {
        return Text.literal("✦ ").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFFFFFF)).withBold(true));
    }

    public static Set<UUID> getVerifiedBcUuids() {
        return Collections.unmodifiableSet(verifiedBcUuids);
    }
}
