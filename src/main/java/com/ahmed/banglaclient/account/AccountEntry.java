package com.ahmed.banglaclient.account;

import java.util.UUID;

/**
 * Represents a stored account metadata entry in the BANGLA CLIENT Account Switcher.
 * Never stores Microsoft passwords or raw credentials.
 */
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

    public AccountEntry(String username, UUID uuid, AccountType type) {
        this.username = username;
        this.uuid = uuid;
        this.type = type;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public UUID getUuid() { return uuid; }
    public AccountType getType() { return type; }
}
