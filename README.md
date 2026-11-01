# BANGLA CLIENT (v1.0.0)

**Developed by Ahmed**  
**Target Minecraft Version:** Java Edition `1.21.11` (Fabric)  
**Design System:** Premium Dark Transparent Liquid Glass  

---

## 1. Overview
**BANGLA CLIENT** is a legitimate client-side utility, HUD telemetry, visual customization, audio control, and performance monitoring mod for **Minecraft Java Edition 1.21.11**. It contains **zero cheats, exploits, or combat automation**, and is designed to work seamlessly alongside popular Fabric optimization and QoL mods (Sodium, Lithium, Iris, Mod Menu, Replay Mod).

---

## 2. Key Features
- **Complete 120-Module Catalog:** Every module is implemented in its own standalone Java class across 10 organized categories (`HUD`, `PvP Information`, `Visual`, `Inventory`, `Movement`, `World`, `Chat`, `Audio`, `Performance`, and `Utility`).
- **3-Tier Hardware-Adaptive Liquid Glass Engine:**
  - `HIGH`: Native blur backdrop, ambient drop shadows, directional top-lit specular borders, and diagonal surface sheen.
  - `BALANCED` *(Default)*: Throttled screen blur with zero-allocation gradient glass quads.
  - `LOW` *(Low-End Hardware Mode)*: Disables framebuffer blur shaders completely, rendering high-contrast smoked charcoal glass with zero GPU copy overhead.
- **Application-Style Click GUI (`Right Shift`):**
  - Left Navigation Sidebar, Top Real-Time Search & Filter Bar (`Enabled`, `Favorites ★`, `Compact/Comfortable`), 4-Card KPI Telemetry Home Dashboard, and right-click / `[⚙ Options]` per-module settings panels.
- **Interactive HUD Editor (`H`):**
  - Drag-and-drop positioning, center/edge alignment snap guides, mouse-wheel scaling (`0.5x` to `2.5x`), and per-widget color/border customization.
- **Custom Liquid Glass Main Menu:**
  - Includes a corner Theme Switcher (`"Switch to Default Minecraft Theme"` / `"Switch to BANGLA CLIENT Theme"`) and `MainMenuButtonBridge` to automatically preserve third-party mod buttons.
- **14-Section Interactive Tutorial Center & Mod Menu Integration.**

---

## 3. Build & Packaging Instructions

### Prerequisites
- **Java Development Kit (JDK):** Java 21 (`JDK 21`)
- **Gradle:** `8.12+` (via `./gradlew`)

### Compile & Run Automated Unit Tests
```bash
./gradlew test compileJava processResources
