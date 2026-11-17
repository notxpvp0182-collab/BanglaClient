package com.ahmed.banglaclient.gui;

import com.ahmed.banglaclient.animation.Animation;
import com.ahmed.banglaclient.animation.Easing;
import com.ahmed.banglaclient.cosmetics.ServerBannerManager;
import com.ahmed.banglaclient.gui.components.SmoothScrollContainer;
import com.ahmed.banglaclient.rendering.BlurRenderer;
import com.ahmed.banglaclient.rendering.ClipRenderer;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.ThemeTokens;
import com.ahmed.banglaclient.util.MathUtils;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.joml.Matrix3x2fStack;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Allows selecting any added Multiplayer Server and assigning a custom Background Banner image to it.
 */
public final class ServerBannerScreen extends Screen {
    private final Screen parent;
    private final ServerBannerManager manager = ServerBannerManager.getInstance();
    private final SmoothScrollContainer serverScroll = new SmoothScrollContainer();
    private final SmoothScrollContainer bannerScroll = new SmoothScrollContainer();
    private final Animation openAnim = new Animation(0.0f, 170L, Easing.EASE_OUT_CUBIC);
    private boolean closing = false;

    private final List<ServerInfo> servers = new ArrayList<>();
    private ServerInfo selectedServer = null;
    private String highlightedBannerFile = null;

    private int winX, winY, winW, winH;

    public ServerBannerScreen(Screen parent) {
        super(Text.literal("Server Banners"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.winW = Math.min(430, Math.max(270, (int) (this.width * 0.76f)));
        this.winH = Math.min(240, Math.max(170, (int) (this.height * 0.74f)));
        this.winX = (this.width - this.winW) / 2;
        this.winY = (this.height - this.winH) / 2;
        this.closing = false;
        this.openAnim.snapTo(0.0f);
        this.openAnim.animateTo(1.0f);

        manager.refreshBanners();
        servers.clear();
        if (this.client != null) {
            ServerList list = new ServerList(this.client);
            list.loadFile();
            for (int i = 0; i < list.size(); i++) {
                servers.add(list.get(i));
            }
            if (!servers.isEmpty() && selectedServer == null) {
                selectedServer = servers.get(0);
            }
        }
    }

    @Override
    public void onFilesDropped(List<Path> paths) {
        if (paths == null) return;
        for (Path p : paths) {
            try {
                String name = p.getFileName().toString().replaceAll("[^a-zA-Z0-9._-]", "_");
                Files.copy(p, manager.getBannersDir().resolve(name), StandardCopyOption.REPLACE_EXISTING);
            } catch (Throwable ignored) {}
        }
        manager.refreshBanners();
        GlassRenderer.playClickSound();
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        float anim = openAnim.getValue();
        if (closing && anim <= 0.02f) {
            if (this.client != null) this.client.setScreen(parent);
            return;
        }

        BlurRenderer.renderScreenBackdrop(ctx, this.width, this.height, anim);

        Matrix3x2fStack matrices = ctx.getMatrices();
        float scale = 0.94f + 0.06f * anim;
        matrices.pushMatrix();
        matrices.translate(this.width * 0.5f, this.height * 0.5f);
        matrices.scale(scale, scale);
        matrices.translate(-this.width * 0.5f, -this.height * 0.5f);

        try {
            GlassRenderer.drawGlassPanel(ctx, winX, winY, winW, winH, false, false, true);

            // Header: [← Back] | Server Banner Customizer | [Folder] [Reload]
            GlassRenderer.drawPillBadge(ctx, winX + 8, winY + 7, 46, 16, 0xFFFFFFFF, MathUtils.isHovered(mouseX, mouseY, winX + 8, winY + 7, 46, 16));
            GlassRenderer.drawSmallCapsText(ctx, textRenderer, "← Back", winX + 14, winY + 11, 0xFFFFFFFF);
            GlassRenderer.drawSmallCapsText(ctx, textRenderer, "Server Banners", winX + 62, winY + 11, 0xFFFFFFFF);

            int folX = winX + winW - 96;
            GlassRenderer.drawPillBadge(ctx, folX, winY + 7, 44, 16, 0xFFFFFFFF, MathUtils.isHovered(mouseX, mouseY, folX, winY + 7, 44, 16));
            GlassRenderer.drawSmallCapsText(ctx, textRenderer, "Folder", folX + 8, winY + 11, 0xFFFFFFFF);

            int relX = winX + winW - 48;
            GlassRenderer.drawPillBadge(ctx, relX, winY + 7, 40, 16, 0xFFFFFFFF, MathUtils.isHovered(mouseX, mouseY, relX, winY + 7, 40, 16));
            GlassRenderer.drawSmallCapsText(ctx, textRenderer, "Reload", relX + 6, winY + 11, 0xFFFFFFFF);

            GlassRenderer.drawHorizontalDivider(ctx, winX + 8, winY + 27, winW - 16);

            // Left Column: Added Multiplayer Servers
            int leftW = 136;
            int bodyY = winY + 32;
            int bodyH = winH - 64;
            GlassRenderer.drawGlassPanel(ctx, winX + 8, bodyY, leftW, bodyH, true, false, false);
            GlassRenderer.drawSmallCapsText(ctx, textRenderer, "1. Select Server", winX + 14, bodyY + 5, 0xFF94A3B8);

            serverScroll.tickAndInterpolate();
            serverScroll.updateDimensions(bodyH - 18, servers.size() * 24 + 4);
            ClipRenderer.renderClipped(ctx, winX + 10, bodyY + 16, leftW - 4, bodyH - 18, () -> {
                int sy = Math.round(bodyY + 16 - serverScroll.getScrollOffset()) + 2;
                for (ServerInfo srv : servers) {
                    boolean sel = (srv == selectedServer);
                    boolean hov = MathUtils.isHovered(mouseX, mouseY, winX + 12, sy, leftW - 8, 21);
                    GlassRenderer.drawGlassPanel(ctx, winX + 12, sy, leftW - 8, 21, false, hov, sel);
                    ctx.drawTextWithShadow(textRenderer, textRenderer.trimToWidth(srv.name, leftW - 16), winX + 16, sy + 6, sel ? 0xFFFFFFFF : 0xFF94A3B8);
                    sy += 24;
                }
            });

            // Right Column: Banner Images from banglaclient/banners/
            int rightX = winX + leftW + 18;
            int rightW = winW - leftW - 26;
            GlassRenderer.drawGlassPanel(ctx, rightX, bodyY, rightW, bodyH, true, false, false);
            GlassRenderer.drawSmallCapsText(ctx, textRenderer, "2. Choose Banner Image", rightX + 8, bodyY + 5, 0xFF94A3B8);

            List<Map.Entry<String, Identifier>> banners = new ArrayList<>(manager.getBannerTextures().entrySet());
            bannerScroll.tickAndInterpolate();
            bannerScroll.updateDimensions(bodyH - 18, banners.size() * 38 + 6);
            ClipRenderer.renderClipped(ctx, rightX + 4, bodyY + 16, rightW - 8, bodyH - 18, () -> {
                int by = Math.round(bodyY + 16 - bannerScroll.getScrollOffset()) + 2;
                if (banners.isEmpty()) {
                    GlassRenderer.drawSmallCapsText(ctx, textRenderer, "Drop PNG/JPG banners here or click Folder!", rightX + 10, by + 12, 0xFF94A3B8);
                }
                for (var entry : banners) {
                    boolean sel = entry.getKey().equals(highlightedBannerFile);
                    boolean hov = MathUtils.isHovered(mouseX, mouseY, rightX + 6, by, rightW - 12, 34);
                    GlassRenderer.drawGlassPanel(ctx, rightX + 6, by, rightW - 12, 34, true, hov, sel);
                    try {
                        ctx.drawTexture(RenderPipelines.GUI_TEXTURED, entry.getValue(), rightX + 8, by + 2, 0.0f, 0.0f, rightW - 16, 30, rightW - 16, 30);
                    } catch (Throwable ignored) {}
                    by += 38;
                }
            });

            // Bottom Action Bar: [Apply Banner] [Clear Banner]
            int botY = winY + winH - 24;
            int btnW = (winW - 22) / 2;
            GlassRenderer.drawPillBadge(ctx, winX + 8, botY, btnW, 18, 0xFFFFFFFF, true);
            String appLbl = GlassRenderer.toSmallCaps("Apply Banner");
            ctx.drawTextWithShadow(textRenderer, appLbl, winX + 8 + (btnW - textRenderer.getWidth(appLbl)) / 2, botY + 5, 0xFFFFFFFF);

            int clrX = winX + 14 + btnW;
            GlassRenderer.drawPillBadge(ctx, clrX, botY, btnW, 18, ThemeTokens.STATUS_ERROR, false);
            String clrLbl = GlassRenderer.toSmallCaps("Remove Banner");
            ctx.drawTextWithShadow(textRenderer, clrLbl, clrX + (btnW - textRenderer.getWidth(clrLbl)) / 2, botY + 5, 0xFFFFFFFF);
        } finally {
            matrices.popMatrix();
        }
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mx = click.x(), my = click.y();
        if (click.button() != 0 || closing) return super.mouseClicked(click, doubled);

        if (MathUtils.isHovered(mx, my, winX + 8, winY + 7, 46, 16)) { GlassRenderer.playClickSound(); close(); return true; }
        if (MathUtils.isHovered(mx, my, winX + winW - 96, winY + 7, 44, 16)) {
            GlassRenderer.playClickSound();
            try { Util.getOperatingSystem().open(manager.getBannersDir().toFile()); } catch (Throwable ignored) {}
            return true;
        }
        if (MathUtils.isHovered(mx, my, winX + winW - 48, winY + 7, 40, 16)) {
            GlassRenderer.playClickSound();
            manager.refreshBanners();
            return true;
        }

        int leftW = 136, bodyY = winY + 32, bodyH = winH - 64;
        if (MathUtils.isHovered(mx, my, winX + 10, bodyY + 16, leftW - 4, bodyH - 18)) {
            int sy = Math.round(bodyY + 16 - serverScroll.getScrollOffset()) + 2;
            for (ServerInfo srv : servers) {
                if (MathUtils.isHovered(mx, my, winX + 12, sy, leftW - 8, 21)) {
                    selectedServer = srv;
                    GlassRenderer.playClickSound();
                    return true;
                }
                sy += 24;
            }
        }

        int rightX = winX + leftW + 18, rightW = winW - leftW - 26;
        if (MathUtils.isHovered(mx, my, rightX + 4, bodyY + 16, rightW - 8, bodyH - 18)) {
            int by = Math.round(bodyY + 16 - bannerScroll.getScrollOffset()) + 2;
            for (var entry : manager.getBannerTextures().entrySet()) {
                if (MathUtils.isHovered(mx, my, rightX + 6, by, rightW - 12, 34)) {
                    highlightedBannerFile = entry.getKey();
                    GlassRenderer.playClickSound();
                    return true;
                }
                by += 38;
            }
        }

        int botY = winY + winH - 24, btnW = (winW - 22) / 2;
        if (MathUtils.isHovered(mx, my, winX + 8, botY, btnW, 18)) {
            if (selectedServer != null && highlightedBannerFile != null) {
                manager.setBannerForServer(selectedServer.address, highlightedBannerFile);
                GlassRenderer.playClickSound();
            }
            return true;
        }
        if (MathUtils.isHovered(mx, my, winX + 14 + btnW, botY, btnW, 18)) {
            if (selectedServer != null) {
                manager.setBannerForServer(selectedServer.address, null);
                GlassRenderer.playClickSound();
            }
            return true;
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        serverScroll.onMouseScrolled(mouseX, mouseY, verticalAmount, winX + 8, winY + 32, 136, winH - 64);
        bannerScroll.onMouseScrolled(mouseX, mouseY, verticalAmount, winX + 154, winY + 32, winW - 162, winH - 64);
        return true;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (input.key() == GLFW.GLFW_KEY_ESCAPE) { close(); return true; }
        return super.keyPressed(input);
    }

    @Override
    public void close() {
        if (!closing) { closing = true; openAnim.animateTo(0.0f); }
    }
}
