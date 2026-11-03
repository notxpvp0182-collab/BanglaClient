package com.ahmed.banglaclient.module.utility;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.ColorSetting;
import com.ahmed.banglaclient.module.setting.EnumSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.ThemeTokens;
import com.ahmed.banglaclient.util.ColorUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.scoreboard.*;
import net.minecraft.text.Text;
import org.joml.Matrix3x2fStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Module 113: Custom Scoreboard.
 * Customizes the actual multiplayer server scoreboard locally without modifying server data or inventing fake content.
 */
public final class CustomScoreboardModule extends HudModule {

    public enum ScoreboardTheme {
        RED_WHITE_BANGLA("Red & White BANGLA Theme"),
        LIQUID_GLASS("Liquid Glass Theme"),
        CUSTOM_COLORS("Custom Colors");

        private final String label;
        ScoreboardTheme(String label) { this.label = label; }
        @Override public String toString() { return label; }
    }

    private record RenderedLine(Text formattedText, String scoreStr) {}

    private static final Comparator<ScoreboardEntry> ENTRY_COMPARATOR =
            Comparator.comparingInt(ScoreboardEntry::value)
                    .reversed()
                    .thenComparing(ScoreboardEntry::owner, String.CASE_INSENSITIVE_ORDER);

    private final BooleanSetting showScoreboardSetting;
    private final EnumSetting<ScoreboardTheme> themeModeSetting;
    private final BooleanSetting hideScoreNumbersSetting;
    private final NumberSetting backgroundOpacitySetting;
    private final ColorSetting borderColorSetting;
    private final NumberSetting borderThicknessSetting;
    private final NumberSetting lineSpacingSetting;
    private final NumberSetting minWidthSetting;
    private final NumberSetting fineOffsetXSetting;
    private final NumberSetting fineOffsetYSetting;
    private final BooleanSetting resetPositionActionSetting;
    private final BooleanSetting resetAppearanceActionSetting;

    public CustomScoreboardModule() {
        super(
                "custom_scoreboard",
                "Custom Scoreboard",
                "Customizes the actual server scoreboard appearance, position, and styling.",
                ModuleCategory.UTILITY,
                480.0f,
                80.0f,
                118.0f,
                110.0f,
                true
        );
        this.showScoreboardSetting = addSetting(new BooleanSetting(
                "show_scoreboard", "Show Scoreboard", "Master visibility switch for the sidebar scoreboard", true
        ));
        this.themeModeSetting = addSetting(new EnumSetting<>(
                "sb_theme", "Scoreboard Theme", "Visual styling preset for the server scoreboard", ScoreboardTheme.RED_WHITE_BANGLA
        ));
        this.hideScoreNumbersSetting = addSetting(new BooleanSetting(
                "hide_numbers", "Hide Red Score Numbers", "Hide the red numeric points on the right side", true
        ));
        this.backgroundOpacitySetting = addSetting(new NumberSetting(
                "bg_opacity", "Background Opacity", "Transparency level of the scoreboard glass panel",
                65.0, 0.0, 100.0, 5.0, "%"
        ));
        this.borderColorSetting = addSetting(new ColorSetting(
                "border_color", "Border Color", "Custom border color for the scoreboard panel", 0xFFEF4444, true
        ));
        this.borderThicknessSetting = addSetting(new NumberSetting(
                "border_thickness", "Border Thickness", "Thickness of the scoreboard border in pixels",
                1.0, 1.0, 4.0, 1.0, "px"
        ));
        this.lineSpacingSetting = addSetting(new NumberSetting(
                "line_spacing", "Line Spacing", "Vertical height per entry line in pixels",
                11.0, 9.0, 16.0, 1.0, "px"
        ));
        this.minWidthSetting = addSetting(new NumberSetting(
                "min_width", "Minimum Width", "Minimum width of the scoreboard panel in pixels",
                104.0, 70.0, 240.0, 4.0, "px"
        ));
        this.fineOffsetXSetting = addSetting(new NumberSetting(
                "offset_x", "Fine Move Left/Right", "Fine horizontal position adjustment",
                0.0, -100.0, 100.0, 1.0, "px"
        ));
        this.fineOffsetYSetting = addSetting(new NumberSetting(
                "offset_y", "Fine Move Up/Down", "Fine vertical position adjustment",
                0.0, -100.0, 100.0, 1.0, "px"
        ));
        this.resetPositionActionSetting = addSetting(new BooleanSetting(
                "reset_pos_action", "Action: Reset Position", "Toggle ON to reset scoreboard position to right edge", false
        ));
        this.resetPositionActionSetting.onChange(val -> {
            if (Boolean.TRUE.equals(val)) {
                resetToDefaultPosition();
                fineOffsetXSetting.resetToDefault();
                fineOffsetYSetting.resetToDefault();
                resetPositionActionSetting.setValue(false);
            }
        });
        this.resetAppearanceActionSetting = addSetting(new BooleanSetting(
                "reset_style_action", "Action: Reset Appearance", "Toggle ON to restore default scoreboard styling", false
        ));
        this.resetAppearanceActionSetting.onChange(val -> {
            if (Boolean.TRUE.equals(val)) {
                resetSettingsToDefault();
                resetAppearanceActionSetting.setValue(false);
            }
        });
        addAliases("scoreboard", "sidebar", "serverboard", "customscoreboard");
    }

    private void resetToDefaultPosition() {
        if (client != null && client.getWindow() != null) {
            setPosX(client.getWindow().getScaledWidth() - getScaledWidth() - 4.0f);
            setPosY(Math.max(20.0f, (client.getWindow().getScaledHeight() - getScaledHeight()) / 2.0f));
        } else {
            resetPosition();
        }
    }

    public boolean shouldHideEntirely() {
        return isEnabled() && !showScoreboardSetting.isEnabled();
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        // Outside the HUD Editor, InGameHudMixin renders the active server objective directly.
        // renderHud executes strictly inside the interactive HUD Editor (preview == true).
        if (!preview) return;

        Scoreboard scoreboard = client != null && client.world != null ? client.world.getScoreboard() : null;
        ScoreboardObjective objective = scoreboard != null ? scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR) : null;

        if (objective != null) {
            renderLiveScoreboard(context, objective);
        } else {
            // Display honest HUD Editor position outline with ZERO fake/dummy entries
            int pad = paddingSetting.getIntValue();
            int boxW = minWidthSetting.getIntValue();
            int boxH = 58;
            setWidth(boxW);
            setHeight(boxH);
            int x = Math.round(getPosX() + fineOffsetXSetting.getFloatValue());
            int y = Math.round(getPosY() + fineOffsetYSetting.getFloatValue());

            GlassRenderer.drawCustomGlassPanel(context, x, y, boxW, boxH, 0x44080A10, false, true, true);
            context.drawTextWithShadow(client.textRenderer, "Server Scoreboard", x + pad, y + pad, ThemeTokens.DEFAULT_ACCENT_PRIMARY);
            context.drawTextWithShadow(client.textRenderer, "(No active sidebar)", x + pad, y + pad + 14, 0xFFA5AFBC);
            context.drawTextWithShadow(client.textRenderer, "Right-click to configure", x + pad, y + pad + 28, 0xFF64748B);
        }
    }

    public void renderLiveScoreboard(DrawContext context, ScoreboardObjective objective) {
        if (objective == null || client == null) return;
        Scoreboard scoreboard = objective.getScoreboard();
        if (scoreboard == null) return;

        Text titleText = objective.getDisplayName();
        Collection<ScoreboardEntry> rawEntries = scoreboard.getScoreboardEntries(objective);
        List<ScoreboardEntry> sorted = new ArrayList<>();
        for (ScoreboardEntry e : rawEntries) {
            if (e != null && !e.hidden()) {
                sorted.add(e);
            }
        }
        sorted.sort(ENTRY_COMPARATOR);
        int limit = Math.min(15, sorted.size());

        List<RenderedLine> lines = new ArrayList<>(limit);
        for (int i = 0; i < limit; i++) {
            ScoreboardEntry e = sorted.get(i);
            Team team = scoreboard.getScoreHolderTeam(e.owner());
            Text decorated = e.name() != null
                    ? e.name()
                    : Team.decorateName(team, Text.literal(e.owner()));
            lines.add(new RenderedLine(decorated, String.valueOf(e.value())));
        }

        int pad = paddingSetting.getIntValue();
        int lineStep = lineSpacingSetting.getIntValue();
        boolean hideNums = hideScoreNumbersSetting.isEnabled();

        int maxW = Math.max(minWidthSetting.getIntValue(), client.textRenderer.getWidth(titleText) + 12);
        for (RenderedLine rl : lines) {
            int lw = client.textRenderer.getWidth(rl.formattedText());
            if (!hideNums) {
                lw += 8 + client.textRenderer.getWidth(rl.scoreStr());
            }
            maxW = Math.max(maxW, lw);
        }

        int boxW = maxW + pad * 2;
        int headerH = 14;
        int boxH = headerH + (lines.size() * lineStep) + pad * 2;

        setWidth(boxW);
        setHeight(boxH);

        // Pinned default initialization if coordinates unset
        if (getPosX() <= 0.0f && client.getWindow() != null) {
            setPosX(client.getWindow().getScaledWidth() - boxW - 4.0f);
            setPosY(Math.max(20.0f, (client.getWindow().getScaledHeight() - boxH) / 2.0f));
        }

        int x = Math.round(getPosX() + fineOffsetXSetting.getFloatValue());
        int y = Math.round(getPosY() + fineOffsetYSetting.getFloatValue());
        float scale = getScale();

        Matrix3x2fStack matrices = context.getMatrices();
        boolean needsScale = Math.abs(scale - 1.0f) > 0.001f;
        if (needsScale) {
            matrices.pushMatrix();
            matrices.translate(x, y);
            matrices.scale(scale, scale);
            matrices.translate(-x, -y);
        }

        try {
            ScoreboardTheme sbTheme = themeModeSetting.getValue();
            float alphaNorm = backgroundOpacitySetting.getFloatValue() / 100.0f;

            if (showBackgroundSetting.isEnabled() && alphaNorm > 0.01f) {
                int baseBg = sbTheme == ScoreboardTheme.RED_WHITE_BANGLA
                        ? ColorUtils.withAlpha(0x120A0E, alphaNorm)
                        : (sbTheme == ScoreboardTheme.LIQUID_GLASS
                        ? ColorUtils.withAlpha(0x0E131D, alphaNorm)
                        : ColorUtils.withAlpha(backgroundColorSetting.getArgb(), alphaNorm));
                int headerTint = sbTheme == ScoreboardTheme.RED_WHITE_BANGLA
                        ? ColorUtils.withAlpha(0xEF4444, Math.min(0.85f, alphaNorm + 0.18f))
                        : (sbTheme == ScoreboardTheme.LIQUID_GLASS
                        ? ColorUtils.withAlpha(0xFFFFFF, alphaNorm * 0.22f)
                        : ColorUtils.withAlpha(borderColorSetting.getArgb(), alphaNorm * 0.35f));

                GlassRenderer.drawCustomGlassPanel(context, x, y, boxW, boxH, baseBg, false, false, false);
                context.fillGradient(x + 1, y + 1, x + boxW - 1, y + headerH + 2, headerTint, 0x00000000);
            }

            if (showBorderSetting.isEnabled()) {
                int bCol = sbTheme == ScoreboardTheme.RED_WHITE_BANGLA
                        ? 0xCCEF4444
                        : (sbTheme == ScoreboardTheme.LIQUID_GLASS ? 0x66FFFFFF : borderColorSetting.getArgb());
                int thick = borderThicknessSetting.getIntValue();
                for (int t = 0; t < thick; t++) {
                    GlassRenderer.drawOutlineBorder(context, x + t, y + t, boxW - t * 2, boxH - t * 2, bCol);
                }
            }

            int titleW = client.textRenderer.getWidth(titleText);
            int txtCol = textColorSetting.getArgb();
            context.drawTextWithShadow(client.textRenderer, titleText, x + (boxW - titleW) / 2, y + pad, txtCol);
            GlassRenderer.drawHorizontalDivider(context, x + 4, y + pad + 11, boxW - 8);

            int drawY = y + pad + headerH;
            for (RenderedLine rl : lines) {
                context.drawTextWithShadow(client.textRenderer, rl.formattedText(), x + pad, drawY, txtCol);
                if (!hideNums) {
                    int sw = client.textRenderer.getWidth(rl.scoreStr());
                    context.drawTextWithShadow(client.textRenderer, rl.scoreStr(), x + boxW - pad - sw, drawY, 0xFFF87171);
                }
                drawY += lineStep;
            }
        } finally {
            if (needsScale) {
                matrices.popMatrix();
            }
        }
    }
}
