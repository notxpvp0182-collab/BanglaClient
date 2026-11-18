package com.ahmed.banglaclient.module.performance;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class MemoryCleanupModule extends Module {
    private final BooleanSetting smartAutoCleanupSetting;
    private final NumberSetting thresholdPctSetting;
    private final BooleanSetting cleanNowActionSetting;
    private int tickTimer = 0;

    public MemoryCleanupModule() {
        super("memory_cleanup", "Smart Memory Cleanup",
                "Monitors JVM heap pressure and performs safe memory cleanup on threshold or keybind.",
                ModuleCategory.PERFORMANCE);
        this.smartAutoCleanupSetting = addSetting(new BooleanSetting("auto_clean", "Smart Auto Cleanup", "Automatically trigger cleanup when heap exceeds threshold", true));
        this.thresholdPctSetting = addSetting(new NumberSetting("threshold", "RAM Threshold", "Heap usage percentage that triggers cleanup", 85.0, 60.0, 95.0, 5.0, "%"));
        this.cleanNowActionSetting = addSetting(new BooleanSetting("clean_now", "Action: Clean Memory Now", "Click to immediately run memory cleanup", false));

        this.cleanNowActionSetting.onChange(val -> {
            if (Boolean.TRUE.equals(val)) {
                runCleanup(true);
                cleanNowActionSetting.setValue(false);
            }
        });
        addAliases("ramcleanup", "ramoptimizer", "smartmemorycleanup", "manualmemorycleanup");
    }

    @Override
    public void onTick() {
        tickTimer++;
        if (smartAutoCleanupSetting.isEnabled() && tickTimer >= 1200) {
            tickTimer = 0;
            Runtime rt = Runtime.getRuntime();
            long usedPct = ((rt.totalMemory() - rt.freeMemory()) * 100L) / Math.max(1L, rt.maxMemory());
            if (usedPct >= thresholdPctSetting.getIntValue()) {
                runCleanup(false);
            }
        }
    }

    public void runCleanup(boolean notify) {
        Runtime rt = Runtime.getRuntime();
        long beforeMb = (rt.totalMemory() - rt.freeMemory()) / (1024L * 1024L);
        System.gc();
        long afterMb = (rt.totalMemory() - rt.freeMemory()) / (1024L * 1024L);
        long freed = Math.max(0L, beforeMb - afterMb);
        if (notify && BanglaClient.getInstance().getNotificationManager() != null) {
            BanglaClient.getInstance().getNotificationManager().pushInfo("Memory Cleanup", "Freed ~" + freed + " MB of heap memory");
        }
    }
}
