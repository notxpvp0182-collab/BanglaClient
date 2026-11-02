package com.ahmed.banglaclient.module.utility;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.ColorSetting;
import com.ahmed.banglaclient.module.setting.EnumSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.util.ColorUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Module 113: Custom Scoreboard (Upgraded to HudModule for full HUD Editor drag, scale & right-click config).
 * Detects and renders the actual active server scoreboard locally without modifying server data.
 */
public final class CustomScoreboardModule extends HudModule {

    public enum ScoreboardTheme {
        RED_WHITE_BANGLA("Red & White BANGLA Theme"),
        LIQUID_GLASS("Liquid Glass Theme"),
        CUSTOM_COLORS("Custom User Colors");

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

    private Text lastCachedTitle = Text.literal("BANGLA CLIENT");
    private final List<RenderedLine> lastCachedLines = new ArrayList<>();

    public CustomScoreboardModule() {
        super(
                "custom_scoreboard",
                "Custom Scoreboard",
                "Customizes the live server scoreboard in the HUD Editor with Liquid Glass & Red/White themes.",
                ModuleCategory.UTILITY,
                510.0f,
                96.0f,
                118.0f,
                110.0f,
                true
        );
        this.showScoreboardSetting = addSetting(new BooleanSetting(
                "show_scoreboard", "Show Scoreboard", "Toggle visibility of the scoreboard on screen", true
        ));
        this.themeModeSetting = addSetting(new EnumSetting<>(
                "sb_theme", "Scoreboard Theme", "Visual preset for the scoreboard panel", ScoreboardTheme.RED_WHITE_BANGLA
        ));
        this.hideScoreNumbersSetting = addSetting(new BooleanSetting(
                "hide_numbers", "Hide Red Score Numbers", "Hide the red numeric values on the right side", true
        ));
        this.backgroundOpacitySetting = addSetting(new NumberSetting(
                "bg_opacity", "Background Opacity", "Transparency level of the scoreboard glass panel",
                65.0, 0.0, 100.0, 5.0, "%"
        ));
        this.borderColorSetting = addSetting(new ColorSetting(
                "border_color", "Border Color", "Custom border color for the scoreboard", 0xFFEF4444, true
        ));
        this.borderThicknessSetting = addSetting(new NumberSetting(
                "border_thickness", "Border Thickness", "Thickness of the scoreboard border in pixels",
                1.0, 1.0, 4.0, 1.0, "px"
        ));
        this.lineSpacingSetting = addSetting(new NumberSetting(
                "line_spacing", "Text Line Spacing", "Vertical height per scoreboard line in pixels",
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
                resetPosition();
                fineOffsetXSetting.resetToDefault();
                fineOffsetYSetting.resetToDefault();
                resetPositionActionSetting.setValue(false);
            }
        });
        this.resetAppearanceActionSetting = addSetting(new BooleanSetting(
                "reset_style_action", "Action: Reset Appearance", "Toggle ON to restore default scoreboard colors and spacing", false
        ));
        this.resetAppearanceActionSetting.onChange(val -> {
            if (Boolean.TRUE.equals(val)) {
                resetSettingsToDefault();
                resetAppearanceActionSetting.setValue(false);
            }
        });
        addAliases("scoreboard", "sidebar", "serverboard", "scoreboardeditor");
    }

    public boolean shouldHideEntirely() {
        return isEnabled() && !showScoreboardSetting.isEnabled();
    }

    public boolean shouldHideScoreNumbers() {
        return isEnabled() && hideScoreNumbersSetting.isEnabled();
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        if (!showScoreboardSetting.isEnabled() && !preview) {
            return;
        }

        ScoreboardObjective objective = null;
        Scoreboard scoreboard = null;
        if (client != null && client.world != null) {
            scoreboard = client.world.getScoreboard();
            if (scoreboard != null) {
                objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
            }
        }

        Text titleText = null;
        List<RenderedLine> lines = new ArrayList<>();

        if (objective != null && scoreboard != null) {
            titleText = objective.getDisplayName();
            Collection<ScoreboardEntry> rawEntries = scoreboard.getScoreboardEntries(objective);
            List<ScoreboardEntry> sorted = new ArrayList<>();
            for (ScoreboardEntry e : rawEntries) {
                if (e != null && !e.hidden()) {
                    sorted.add(e);
                }
            }
            sorted.sort(ENTRY_COMPARATOR);
            int limit = Math.min(15, sorted.size());
            for (int i = 0; i < limit; i++) {
                ScoreboardEntry e = sorted.get(i);
                Team team = scoreboard.getScoreHolderTeam(e.owner());
                Text decorated = e.name() != null
                        ? e.name()
                        : Team.decorateName(team, Text.literal(e.owner()));
                lines.add(new RenderedLine(decorated, String.valueOf(e.value())));
            }
            this.lastCachedTitle = titleText;
            this.lastCachedLines.clear();
            this.lastCachedLines.addAll(lines);
        } else if (preview) {
            if (!lastCachedLines.isEmpty()) {
                titleText = lastCachedTitle;
                lines.addAll(lastCachedLines);
            } else {
                titleText = Text.literal("BANGLA SMP");
                lines.add(new RenderedLine(Text.literal("Profile: " + (client != null && client.getSession() != null ? client.getSession().getUsername() : "Player")), "1"));
                lines.add(new RenderedLine(Text.literal("Rank: Member"), "2"));
                lines.add(new RenderedLine(Text.literal("Ping: 32 ms"), "3"));
                lines.add(new RenderedLine(Text.literal("play.banglasmp.net"), "4"));
            }
        } else {
            return;
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

        int x = Math.round(getPosX() + fineOffsetXSetting.getFloatValue());
        int y = Math.round(getPosY() + fineOffsetYSetting.getFloatValue());

        ScoreboardTheme sbTheme = themeModeSetting.getValue();
        float alphaNorm = backgroundOpacitySetting.getFloatValue() / 100.0f;

        if (showBackgroundSetting.isEnabled() && alphaNorm > 0.01f) {
            int baseBg;
            int headerTint;
            if (sbTheme == ScoreboardTheme.RED_WHITE_BANGLA) {
                baseBg = ColorUtils.withAlpha(0x120A0E, alphaNorm);
                headerTint = ColorUtils.withAlpha(0xEF4444, Math.min(0.85f, alphaNorm + 0.18f));
            } else if (sbTheme == ScoreboardTheme.LIQUID_GLASS) {
                baseBg = ColorUtils.withAlpha(0x0E131D, alphaNorm);
                headerTint = ColorUtils.withAlpha(0xFFFFFF, alphaNorm * 0.22f);
            } else {
                baseBg = ColorUtils.withAlpha(backgroundColorSetting.getArgb(), alphaNorm);
                headerTint = ColorUtils.withAlpha(borderColorSetting.getArgb(), alphaNorm * 0.35f);
            }

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

        // Render Centered Title
        int titleW = client.textRenderer.getWidth(titleText);
        int txtCol = textColorSetting.getArgb();
        context.drawTextWithShadow(client.textRenderer, titleText, x + (boxW - titleW) / 2, y + pad, txtCol);
        GlassRenderer.drawHorizontalDivider(context, x + 4, y + pad + 11, boxW - 8);

        // Render Scoreboard Entries
        int drawY = y + pad + headerH;
        for (RenderedLine rl : lines) {
            context.drawTextWithShadow(client.textRenderer, rl.formattedText(), x + pad, drawY, txtCol);
            if (!hideNums) {
                int sw = client.textRenderer.getWidth(rl.scoreStr());
                context.drawTextWithShadow(client.textRenderer, rl.scoreStr(), x + boxW - pad - sw, drawY, 0xFFF87171);
            }
            drawY += lineStep;
        }
    }
}
