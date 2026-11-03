package com.ahmed.banglaclient.account;

import java.util.UUID;

public final class AccountEntry {
    public enum AccountType {
        MICROSOFT("Microsoft Account"),
        OFFLINE("Offline Account");

        private final String label;
        AccountType(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    private String username;
    private final UUID uuid;
    private final AccountType type;
    private long lastUsed;
    private int useCount;

    public AccountEntry(String username, UUID uuid, AccountType type, long lastUsed, int useCount) {
        this.username = username;
        this.uuid = uuid;
        this.type = type;
        this.lastUsed = lastUsed;
        this.useCount = Math.max(1, useCount);
    }

    public AccountEntry(String username, UUID uuid, AccountType type) {
        this(username, uuid, type, System.currentTimeMillis(), 1);
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public UUID getUuid() { return uuid; }
    public AccountType getType() { return type; }
    public long getLastUsed() { return lastUsed; }
    public void setLastUsed(long lastUsed) { this.lastUsed = lastUsed; }
    public int getUseCount() { return useCount; }
    public void incrementUseCount() { this.useCount++; }
}
