package com.ahmed.banglaclient.module.performance;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.Theme;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.resource.ResourcePackProfile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Module 109: Resource Pack Monitor
 * Displays active resource pack count and top enabled custom resource pack names on the HUD.
 */
public final class ResourcePackMonitorModule extends HudModule {
    private final BooleanSetting hideBuiltInPacksSetting;

    public ResourcePackMonitorModule() {
        super(
                "resource_pack_monitor",
                "Resource Pack Monitor",
                "Displays active resource pack names and enabled pack count.",
                ModuleCategory.PERFORMANCE,
                440.0f,
                400.0f,
                142.0f,
                32.0f,
                false
        );
        this.hideBuiltInPacksSetting = addSetting(new BooleanSetting(
                "hide_builtin", "Filter Built-in Mod Packs", "Only list user-selected resource packs and vanilla default", true
        ));
        addAliases("packdisplay", "resourcepacks", "texturepack", "packhud");
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        List<String> packNames = new ArrayList<>();
        if (client != null && client.getResourcePackManager() != null) {
            Collection<ResourcePackProfile> enabled = client.getResourcePackManager().getEnabledProfiles();
            for (ResourcePackProfile profile : enabled) {
                String id = profile.getId();
                if (hideBuiltInPacksSetting.isEnabled() && (id.startsWith("fabric") || id.equals("programmer_art") || id.equals("high_contrast"))) {
                    continue;
                }
                if (id.startsWith("file/")) {
                    packNames.add(id.substring(5));
                } else {
                    packNames.add(profile.getDisplayName().getString());
                }
            }
        }
        if (packNames.isEmpty()) {
            packNames.add("Default Vanilla");
        }

        String topPack = packNames.get(packNames.size() - 1);
        String line1 = "Pack: " + client.textRenderer.trimToWidth(topPack, 120);
        String line2 = "Active Packs: " + packNames.size();

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
