// OWNER: John Efrain V. Manalo
package com.sumurai.storage;

import com.sumurai.model.BattleStats;
import com.sumurai.model.UserProfile;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class UserRepository {
    private static final int HASH_ITERATIONS = 120000;
    private static final int HASH_BITS = 256;
    private static final int SALT_BYTES = 16;

    private final Path dataFile;
    private final SecureRandom secureRandom = new SecureRandom();

    public UserRepository(Path dataFile) throws IOException {
        if (dataFile == null) {
            throw new IllegalArgumentException("Data file path is required.");
        }
        this.dataFile = dataFile;
        Path parent = dataFile.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        if (Files.notExists(dataFile)) {
            try {
                Files.createFile(dataFile);
            } catch (FileAlreadyExistsException ignored) {
                // Another application instance created the file first.
            }
        }
    }

    public synchronized Optional<UserProfile> authenticate(String username, char[] password) throws IOException {
        if (password == null) {
            throw new IllegalArgumentException("Password is required.");
        }
        try {
            validateUsername(username);
            UserProfile profile = readProfiles().get(username);
            if (profile == null || !passwordMatches(password, profile.getPasswordHash())) {
                return Optional.empty();
            }
            return Optional.of(profile);
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    public synchronized UserProfile register(String username, char[] password) throws IOException {
        if (password == null) {
            throw new IllegalArgumentException("Password is required.");
        }
        try {
            validateUsername(username);
            if (password.length < 4) {
                throw new IllegalArgumentException("Password must contain at least four characters.");
            }
            Map<String, UserProfile> profiles = readProfiles();
            if (profiles.containsKey(username)) {
                throw new IllegalArgumentException("That username is already registered.");
            }
            UserProfile profile = new UserProfile(username, hashPassword(password), 1, 0, new BattleStats());
            profiles.put(username, profile);
            writeProfiles(profiles);
            return profile;
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    public synchronized void save(UserProfile profile) throws IOException {
        if (profile == null) {
            throw new IllegalArgumentException("Profile is required.");
        }
        if (profile.isGuest()) {
            return;
        }
        Map<String, UserProfile> profiles = readProfiles();
        if (!profiles.containsKey(profile.getUsername())) {
            throw new IOException("Cannot update an account that is not registered: " + profile.getUsername());
        }
        profiles.put(profile.getUsername(), profile);
        writeProfiles(profiles);
    }

    public synchronized List<UserProfile> findLeaderboard(int limit) throws IOException {
        if (limit < 1) {
            throw new IllegalArgumentException("Leaderboard limit must be positive.");
        }
        List<UserProfile> profiles = new ArrayList<UserProfile>(readProfiles().values());
        profiles.sort(Comparator
                .comparingInt((UserProfile profile) -> profile.getStats().getWins()).reversed()
                .thenComparing(Comparator.comparingLong(UserProfile::getExperience).reversed()));
        if (profiles.size() > limit) {
            return new ArrayList<UserProfile>(profiles.subList(0, limit));
        }
        return profiles;
    }

    public synchronized List<UserProfile> findAllProfiles() throws IOException {
        return new ArrayList<UserProfile>(readProfiles().values());
    }

    private Map<String, UserProfile> readProfiles() throws IOException {
        Map<String, UserProfile> profiles = new LinkedHashMap<String, UserProfile>();
        List<String> lines = Files.readAllLines(dataFile, StandardCharsets.UTF_8);
        for (int index = 0; index < lines.size(); index++) {
            String line = lines.get(index);
            if (line.trim().isEmpty()) {
                continue;
            }
            String[] fields = line.split("\\t", -1);
            try {
<<<<<<< HEAD
                if (fields.length != 8 && fields.length != 9) {
                    throw new IllegalArgumentException("Expected 8 or 9 tab-separated fields.");
                }
                BattleStats stats = new BattleStats(
                        Integer.parseInt(fields[4]),
                        Integer.parseInt(fields[5]),
                        Integer.parseInt(fields[6]),
                        Integer.parseInt(fields[7]));
                int savedLevel = Integer.parseInt(fields[2]);
=======
                if (fields.length != 8 && fields.length != 11) {
                    throw new IllegalArgumentException("Expected 8 or 11 tab-separated fields.");
                }
                BattleStats stats;
                if (fields.length == 8) {
                    stats = new BattleStats(
                            Integer.parseInt(fields[4]),
                            Integer.parseInt(fields[5]),
                            Integer.parseInt(fields[6]),
                            Integer.parseInt(fields[7]));
                } else {
                    stats = new BattleStats(
                            Integer.parseInt(fields[4]),
                            Integer.parseInt(fields[5]),
                            Integer.parseInt(fields[6]),
                            Integer.parseInt(fields[7]),
                            Integer.parseInt(fields[8]),
                            Integer.parseInt(fields[9]),
                            Long.parseLong(fields[10]));
                }
>>>>>>> Leaderboards/added-UI
                UserProfile profile = new UserProfile(
                        fields[0], fields[1],
                        Math.min(savedLevel, UserProfile.MAX_PLAYER_LEVEL), Long.parseLong(fields[3]),
                        stats, fields.length == 9 ? Integer.parseInt(fields[8]) : 1);
                if (profiles.put(profile.getUsername(), profile) != null) {
                    throw new IllegalArgumentException("Duplicate username.");
                }
            } catch (IllegalArgumentException exception) {
                throw new IOException("Invalid user record at " + dataFile + ":" + (index + 1), exception);
            }
        }
        return profiles;
    }

    private void writeProfiles(Map<String, UserProfile> profiles) throws IOException {
        List<String> lines = new ArrayList<String>();
        for (UserProfile profile : profiles.values()) {
            BattleStats stats = profile.getStats();
            lines.add(profile.getUsername() + "\t" + profile.getPasswordHash() + "\t"
                    + profile.getLevel() + "\t" + profile.getExperience() + "\t"
                    + stats.getBattlesPlayed() + "\t" + stats.getWins() + "\t"
                    + stats.getLosses() + "\t" + stats.getCorrectAnswers() + "\t"
<<<<<<< HEAD
                    + profile.getHighestUnlockedStage());
=======
                    + stats.getMeasuredAnswers() + "\t" + stats.getMeasuredCorrectAnswers() + "\t"
                    + stats.getTotalAnswerTimeMillis());
>>>>>>> Leaderboards/added-UI
        }
        Files.write(dataFile, lines, StandardCharsets.UTF_8);
    }

    private String hashPassword(char[] password) throws IOException {
        byte[] salt = new byte[SALT_BYTES];
        secureRandom.nextBytes(salt);
        byte[] hash = deriveHash(password, salt);
        return HASH_ITERATIONS + ":" + Base64.getEncoder().encodeToString(salt)
                + ":" + Base64.getEncoder().encodeToString(hash);
    }

    private boolean passwordMatches(char[] password, String encoded) throws IOException {
        String[] parts = encoded.split(":", -1);
        if (parts.length != 3) {
            throw new IOException("Invalid password hash in " + dataFile);
        }
        try {
            int iterations = Integer.parseInt(parts[0]);
            if (iterations < 1 || iterations > 1000000) {
                throw new IOException("Invalid password hash parameters in " + dataFile);
            }
            byte[] salt = Base64.getDecoder().decode(parts[1]);
            byte[] expected = Base64.getDecoder().decode(parts[2]);
            return MessageDigest.isEqual(expected, deriveHash(password, salt, iterations));
        } catch (IllegalArgumentException exception) {
            throw new IOException("Invalid password hash in " + dataFile, exception);
        }
    }

    private byte[] deriveHash(char[] password, byte[] salt) throws IOException {
        return deriveHash(password, salt, HASH_ITERATIONS);
    }

    private byte[] deriveHash(char[] password, byte[] salt, int iterations) throws IOException {
        PBEKeySpec keySpec = new PBEKeySpec(password, salt, iterations, HASH_BITS);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(keySpec).getEncoded();
        } catch (GeneralSecurityException exception) {
            throw new IOException("Unable to hash account password.", exception);
        } finally {
            keySpec.clearPassword();
        }
    }

    private void validateUsername(String username) {
        if (username == null || username.trim().isEmpty() || username.length() > 32
                || username.indexOf('\t') >= 0 || username.indexOf('\n') >= 0
                || username.indexOf('\r') >= 0) {
            throw new IllegalArgumentException("Username must be 1-32 characters without tabs or newlines.");
        }
    }
}
