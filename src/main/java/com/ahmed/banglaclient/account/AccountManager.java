package com.ahmed.banglaclient.account;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.config.ConfigManager;
import com.ahmed.banglaclient.config.ConfigValidator;
import com.ahmed.banglaclient.mixin.MinecraftClientSessionAccessor;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Manages Microsoft and Offline accounts safely for BANGLA CLIENT.
 * Preserves the original authenticated launcher session and supports offline profile switching.
 */
public final class AccountManager {
    private final Path accountsFile;
    private final List<AccountEntry> accounts = new ArrayList<>();
    private Session initialLaunchSession;

    public AccountManager() {
        this.accountsFile = FabricLoader.getInstance().getGameDir().resolve("banglaclient").resolve("accounts.json");
        captureInitialSession();
        loadAccounts();
    }

    private void captureInitialSession() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc != null && mc.getSession() != null) {
            this.initialLaunchSession = mc.getSession();
            UUID id = initialLaunchSession.getUuidOrNull() != null
                    ? initialLaunchSession.getUuidOrNull()
                    : offlineUuid(initialLaunchSession.getUsername());
            AccountEntry.AccountType type = (initialLaunchSession.getAccessToken() != null && !initialLaunchSession.getAccessToken().equals("0") && !initialLaunchSession.getAccessToken().isBlank())
                    ? AccountEntry.AccountType.MICROSOFT
                    : AccountEntry.AccountType.OFFLINE;
            accounts.add(new AccountEntry(initialLaunchSession.getUsername(), id, type));
        }
    }

    public static boolean isValidMinecraftUsername(String name) {
        if (name == null) return false;
        String trimmed = name.trim();
        return trimmed.matches("^[a-zA-Z0-9_]{3,16}$");
    }

    public static UUID offlineUuid(String username) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(StandardCharsets.UTF_8));
    }

    public synchronized String addOfflineAccount(String rawName) {
        if (!isValidMinecraftUsername(rawName)) {
            return "Invalid username! Use 3-16 letters, numbers, or underscores.";
        }
        String clean = rawName.trim();
        for (AccountEntry existing : accounts) {
            if (existing.getUsername().equalsIgnoreCase(clean)) {
                switchToAccount(existing);
                return "Switched to existing account: " + existing.getUsername();
            }
        }
        AccountEntry entry = new AccountEntry(clean, offlineUuid(clean), AccountEntry.AccountType.OFFLINE);
        accounts.add(entry);
        saveAccounts();
        switchToAccount(entry);
        return "Added & activated Offline Account: " + clean;
    }

    public synchronized boolean editOfflineAccount(AccountEntry entry, String newName) {
        if (entry == null || entry.getType() != AccountEntry.AccountType.OFFLINE) return false;
        if (!isValidMinecraftUsername(newName)) return false;
        boolean wasActive = isAccountActive(entry);
        entry.setUsername(newName.trim());
        saveAccounts();
        if (wasActive) {
            switchToAccount(entry);
        }
        return true;
    }

    public synchronized void removeAccount(AccountEntry entry) {
        if (entry == null || accounts.size() <= 1) return;
        if (initialLaunchSession != null && entry.getUsername().equalsIgnoreCase(initialLaunchSession.getUsername())
                && entry.getType() == AccountEntry.AccountType.MICROSOFT) {
            return; // Keep original authenticated launcher session available
        }
        accounts.remove(entry);
        saveAccounts();
        if (isAccountActive(entry) && !accounts.isEmpty()) {
            switchToAccount(accounts.get(0));
        }
    }

    public synchronized void switchToAccount(AccountEntry entry) {
        if (entry == null) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null) return;

        if (initialLaunchSession != null
                && entry.getType() == AccountEntry.AccountType.MICROSOFT
                && entry.getUsername().equalsIgnoreCase(initialLaunchSession.getUsername())) {
            ((MinecraftClientSessionAccessor) mc).banglaclient$setSession(initialLaunchSession);
        } else {
            Session offlineSession = new Session(
                    entry.getUsername(),
                    offlineUuid(entry.getUsername()),
                    "0",
                    Optional.empty(),
                    Optional.empty()
            );
            ((MinecraftClientSessionAccessor) mc).banglaclient$setSession(offlineSession);
        }
        saveAccounts();
    }

    public boolean isAccountActive(AccountEntry entry) {
        if (entry == null) return false;
        return getActiveUsername().equalsIgnoreCase(entry.getUsername());
    }

    public String getActiveUsername() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc != null && mc.getSession() != null && mc.getSession().getUsername() != null) {
            return mc.getSession().getUsername();
        }
        return "Player";
    }

    public List<AccountEntry> getAccounts() {
        return Collections.unmodifiableList(accounts);
    }

    public void loadAccounts() {
        JsonObject root = ConfigValidator.readSafeJsonObject(accountsFile);
        if (root.has("accounts") && root.get("accounts").isJsonArray()) {
            for (JsonElement el : root.getAsJsonArray("accounts")) {
                if (!el.isJsonObject()) continue;
                JsonObject obj = el.getAsJsonObject();
                String name = obj.has("username") ? obj.get("username").getAsString() : "";
                if (!isValidMinecraftUsername(name)) continue;
                boolean duplicate = accounts.stream().anyMatch(a -> a.getUsername().equalsIgnoreCase(name));
                if (!duplicate) {
                    accounts.add(new AccountEntry(name, offlineUuid(name), AccountEntry.AccountType.OFFLINE));
                }
            }
        }
        if (root.has("activeOfflineUsername")) {
            String activeName = root.get("activeOfflineUsername").getAsString();
            for (AccountEntry a : accounts) {
                if (a.getUsername().equalsIgnoreCase(activeName)) {
                    switchToAccount(a);
                    break;
                }
            }
        }
    }

    public void saveAccounts() {
        JsonObject root = new JsonObject();
        JsonArray arr = new JsonArray();
        for (AccountEntry a : accounts) {
            if (a.getType() == AccountEntry.AccountType.OFFLINE) {
                JsonObject obj = new JsonObject();
                obj.addProperty("username", a.getUsername());
                obj.addProperty("uuid", a.getUuid().toString());
                arr.add(obj);
            }
        }
        root.add("accounts", arr);
        root.addProperty("activeOfflineUsername", getActiveUsername());
        ConfigManager.writeAtomically(accountsFile, root);
    }
}
