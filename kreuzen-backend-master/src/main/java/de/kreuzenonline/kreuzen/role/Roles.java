package de.kreuzenonline.kreuzen.role;

import lombok.Getter;

public enum Roles {
    USER("USER"),
    MODERATOR("MOD"),
    ADMIN("ADMIN"),
    SUDO("SUDO");

    @Getter
    private final String id;

    Roles(String id) {
        this.id = id;
    }
}
