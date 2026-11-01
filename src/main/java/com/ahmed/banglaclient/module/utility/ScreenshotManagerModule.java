package com.ahmed.banglaclient.module.utility;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.Theme;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Util;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Module 110: Screenshot Manager
 * Scans the Minecraft `screenshots/` directory, displays the latest captured screenshot filename and total count
 * on the HUD, and optionally opens the screenshots folder or copies the latest file path to the clipboard.
 */
public final class ScreenshotManagerModule extends HudModule {
    private final BooleanSetting copyLatestPathOnTriggerSetting;
    private final BooleanSetting openFolderNowSetting;

    private int screenshotCount = 0;
    private String latestFileName = "None";
    private Path latestFilePath = null;
    private int scanTickCounter = 0;

    public ScreenshotManagerModule() {
        super(
                "screenshot_manager",
                "Screenshot Manager",
                "Displays recent screenshot info and provides quick folder/path conveniences.",
                ModuleCategory.UTILITY,
                10.0f,
                380.0f,
                156.0f,
                32.0f,
                false
        );
        this.copyLatestPathOnTriggerSetting = addSetting(new BooleanSetting(
                "copy_latest_path", "Copy Latest Path on New Capture", "Automatically copy the file path of newly taken screenshots", true
        ));
        this.openFolderNowSetting = addSetting(new BooleanSetting(
                "open_folder_action", "Action: Open Screenshots Folder", "Toggle ON to open the Minecraft screenshots folder in your OS file explorer", false
        ));
        this.openFolderNowSetting.onChange(enabled -> {
            if (Boolean.TRUE.equals(enabled)) {
                openScreenshotsDirectory();
                openFolderNowSetting.setValue(false);
            }
        });
        addAliases("screenshots", "gallery", "captures", "f2");
    }

    @Override
    protected void onEnable() {
        scanScreenshotsFolder(false);
    }

    @Override
    public void onTick() {
        scanTickCounter++;
        if (scanTickCounter >= 40) { // Scan every 2 seconds
            scanTickCounter = 0;
            scanScreenshotsFolder(true);
        }
    }

    public void openScreenshotsDirectory() {
        if (client == null || client.runDirectory == null) {
            return;
        }
        Path dir = client.runDirectory.toPath().resolve("screenshots");
        try {
            Files.createDirectories(dir);
            Util.getOperatingSystem().open(dir.toFile());
        } catch (IOException e) {
            BanglaClient.LOGGER.warn("Unable to open screenshots directory {}", dir, e);
        }
    }

    private void scanScreenshotsFolder(boolean notifyIfNew) {
        if (client == null || client.runDirectory == null) {
            return;
        }
        Path dir = client.runDirectory.toPath().resolve("screenshots");
        if (!Files.exists(dir)) {
            screenshotCount = 0;
            latestFileName = "None";
            return;
        }

        int count = 0;
        Path newest = null;
        long newestTime = -1L;

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.png")) {
            for (Path entry : stream) {
                count++;
                long modified = Files.getLastModifiedTime(entry).toMillis();
                if (modified > newestTime) {
                    newestTime = modified;
                    newest = entry;
                }
            }
        } catch (IOException ignored) {
        }

        if (notifyIfNew && newest != null && latestFilePath != null && !newest.equals(latestFilePath)) {
            if (copyLatestPathOnTriggerSetting.isEnabled() && client.keyboard != null) {
                client.keyboard.setClipboard(newest.toAbsolutePath().toString());
            }
            if (BanglaClient.getInstance() != null && BanglaClient.getInstance().getNotificationManager() != null) {
                BanglaClient.getInstance().getNotificationManager().pushSuccess(
                        "Screenshot Captured",
                        newest.getFileName().toString()
                );
            }
        }

        this.screenshotCount = count;
        this.latestFilePath = newest;
        this.latestFileName = newest != null ? newest.getFileName().toString() : "None";
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        String line1 = "Screenshots: " + screenshotCount;
        String line2 = "Latest: " + client.textRenderer.trimToWidth(latestFileName, 120);

        int pad = paddingSetting.getIntValue();
        int boxW = Math.max(client.textRenderer.getWidth(line1), client.textRenderer.getWidth(line2)) + pad * 2;
        int boxH = 22 + pad * 2;

        setWidth(boxW);
        setHeight(boxH);

        int x = Math.round(getPosX());
        int y = Math.round(getPosY());
        Theme theme = GlassRenderer.getTheme();

        if (showBackgroundSetting.isEnabled()) {
            GlassRenderer.drawCustomGlassPanel(
                    context, x, y, boxW, boxH,
                    backgroundColorSetting.getArgb(), false, false, showBorderSetting.isEnabled()
            );
        }

        context.drawTextWithShadow(client.textRenderer, line1, x + pad, y + pad + 1, textColorSetting.getArgb());
        context.drawTextWithShadow(client.textRenderer, line2, x + pad, y + pad + 12, theme.getTextSecondary());
    }
}
