package com.ahmed.banglaclient.module.inventory;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.ThemeTokens;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * Module 55: Item Pickup Notifications
 * Tracks positive inventory diffs on client tick and displays recent item pickups in a HUD feed
 * and optional client toast notifications.
 */
public final class ItemPickupNotificationsModule extends HudModule {
    private record PickupEntry(String itemName, int countAdded, long timestampMillis) {
    }

    private final NumberSetting displayDurationSecSetting;
    private final BooleanSetting pushClientToastsSetting;

    private final Map<Item, Integer> previousCounts = new HashMap<>();
    private final Deque<PickupEntry> recentPickups = new ArrayDeque<>();
    private boolean initializedSnapshot = false;

    public ItemPickupNotificationsModule() {
        super(
                "item_pickup_notifications",
                "Item Pickup Notifications",
                "Shows live HUD notifications when the player picks up items.",
                ModuleCategory.INVENTORY,
                590.0f,
                182.0f,
                128.0f,
                40.0f,
                false
        );
        this.displayDurationSecSetting = addSetting(new NumberSetting(
                "duration", "Display Duration", "How long pickup entries stay on screen",
                3.5, 1.5, 10.0, 0.5, "s"
        ));
        this.pushClientToastsSetting = addSetting(new BooleanSetting(
                "toast_notify", "Also Push Client Toast", "Send pickup alerts to the bottom-right Notification Manager", false
        ));
        addAliases("pickup", "lootfeed", "itemlog");
    }

    @Override
    protected void onDisable() {
        previousCounts.clear();
        recentPickups.clear();
        initializedSnapshot = false;
    }

    @Override
    public void onTick() {
        if (client.player == null || client.currentScreen != null) {
            // Avoid false pickup triggers while manually rearranging items inside a container screen
            snapshotCurrentInventory(false);
            return;
        }
        snapshotCurrentInventory(initializedSnapshot);
        initializedSnapshot = true;

        long expiryMs = (long) (displayDurationSecSetting.getValue() * 1000.0);
        long now = System.currentTimeMillis();
        while (!recentPickups.isEmpty() && (now - recentPickups.peekFirst().timestampMillis()) > expiryMs) {
            recentPickups.pollFirst();
        }
    }

    private void snapshotCurrentInventory(boolean recordDiffs) {
        if (client.player == null) {
            return;
        }
        Map<Item, Integer> currentCounts = new HashMap<>();
        Map<Item, String> displayNames = new HashMap<>();
        var inv = client.player.getInventory();

        for (int i = 0; i < inv.size(); i++) {
            ItemStack s = inv.getStack(i);
            if (!s.isEmpty()) {
                Item item = s.getItem();
                currentCounts.merge(item, s.getCount(), Integer::sum);
                displayNames.putIfAbsent(item, s.getName().getString());
            }
        }

        if (recordDiffs) {
            long now = System.currentTimeMillis();
            for (Map.Entry<Item, Integer> entry : currentCounts.entrySet()) {
                int oldCount = previousCounts.getOrDefault(entry.getKey(), 0);
                int diff = entry.getValue() - oldCount;
                if (diff > 0) {
                    String name = displayNames.getOrDefault(entry.getKey(), "Item");
                    while (recentPickups.size() >= 5) {
                        recentPickups.pollFirst();
                    }
                    recentPickups.addLast(new PickupEntry(name, diff, now));
                    if (pushClientToastsSetting.isEnabled() && BanglaClient.getInstance() != null) {
                        BanglaClient.getInstance().getNotificationManager().pushInfo("Item Pickup", "+" + diff + " " + name);
                    }
                }
            }
        }

        previousCounts.clear();
        previousCounts.putAll(currentCounts);
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        if (recentPickups.isEmpty() && !preview) {
            return;
        }

        int pad = paddingSetting.getIntValue();
        int x = Math.round(getPosX());
        int y = Math.round(getPosY());

        if (recentPickups.isEmpty()) {
            String sample = "+16 Diamond";
            int w = client.textRenderer.getWidth(sample) + pad * 2;
            int h = client.textRenderer.fontHeight + pad * 2;
            setWidth(w);
            setHeight(h);
            if (showBackgroundSetting.isEnabled()) {
                GlassRenderer.drawCustomGlassPanel(context, x, y, w, h, backgroundColorSetting.getArgb(), false, false, showBorderSetting.isEnabled());
            }
            context.drawTextWithShadow(client.textRenderer, sample, x + pad, y + pad + 1, ThemeTokens.STATUS_SUCCESS);
            return;
        }

        int maxW = 80;
        for (PickupEntry entry : recentPickups) {
            String line = "+" + entry.countAdded() + " " + entry.itemName();
            maxW = Math.max(maxW, client.textRenderer.getWidth(line));
        }

        int boxW = maxW + pad * 2;
        int boxH = recentPickups.size() * 12 + pad * 2 - 2;
        setWidth(boxW);
        setHeight(boxH);

        if (showBackgroundSetting.isEnabled()) {
            GlassRenderer.drawCustomGlassPanel(
                    context, x, y, boxW, boxH,
                    backgroundColorSetting.getArgb(), false, false, showBorderSetting.isEnabled()
            );
        }

        int drawY = y + pad + 1;
        for (PickupEntry entry : recentPickups) {
            String line = "+" + entry.countAdded() + " " + entry.itemName();
            context.drawTextWithShadow(client.textRenderer, line, x + pad, drawY, ThemeTokens.STATUS_SUCCESS);
            drawY += 12;
        }
    }
}
