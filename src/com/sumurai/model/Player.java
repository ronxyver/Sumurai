// OWNER: Ron Xyver Atienza
package com.sumurai.model;

import java.util.Objects;

public final class Player extends GameCharacter {
    private final UserProfile profile;

    public Player(UserProfile profile, int maxHealth) {
        super(Objects.requireNonNull(profile, "profile").getUsername(), maxHealth);
        this.profile = profile;
    }

    public UserProfile getProfile() {
        return profile;
    }
}
