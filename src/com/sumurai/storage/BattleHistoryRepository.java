// OWNER: John Efrain V. Manalo
package com.sumurai.storage;

import com.sumurai.model.BattleOutcome;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class BattleHistoryRepository {
    private final Path dataFile;

    public BattleHistoryRepository(Path dataFile) throws IOException {
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

    public synchronized void appendResult(String username, BattleOutcome outcome, int turns, Instant completedAt)
            throws IOException {
        validateUsername(username);
        if (outcome == null || outcome == BattleOutcome.IN_PROGRESS || turns < 0 || completedAt == null) {
            throw new IllegalArgumentException("A completed battle record is required.");
        }
        String line = username + "\t" + outcome.name() + "\t" + turns + "\t" + completedAt;
        Files.write(dataFile, Collections.singletonList(line), StandardCharsets.UTF_8,
                java.nio.file.StandardOpenOption.APPEND);
    }

    public synchronized List<String> findRecent(String username, int limit) throws IOException {
        validateUsername(username);
        if (limit < 1) {
            throw new IllegalArgumentException("History limit must be positive.");
        }
        List<String> results = new ArrayList<String>();
        List<String> lines = Files.readAllLines(dataFile, StandardCharsets.UTF_8);
        for (int index = lines.size() - 1; index >= 0 && results.size() < limit; index--) {
            String[] fields = lines.get(index).split("\\t", -1);
            if (fields.length != 4) {
                throw new IOException("Invalid battle history record at " + dataFile + ":" + (index + 1));
            }
            try {
                BattleOutcome outcome = BattleOutcome.valueOf(fields[1]);
                int turns = Integer.parseInt(fields[2]);
                if (outcome == BattleOutcome.IN_PROGRESS || turns < 0) {
                    throw new IllegalArgumentException("Invalid completed battle values.");
                }
                Instant.parse(fields[3]);
            } catch (IllegalArgumentException exception) {
                throw new IOException("Invalid battle history record at " + dataFile + ":" + (index + 1), exception);
            }
            if (username.equals(fields[0])) {
                results.add(fields[1] + " — " + fields[2] + " turns — " + fields[3]);
            }
        }
        return results;
    }

    private void validateUsername(String username) {
        if (username == null || username.trim().isEmpty() || username.indexOf('\t') >= 0
                || username.indexOf('\n') >= 0 || username.indexOf('\r') >= 0) {
            throw new IllegalArgumentException("A valid username is required.");
        }
    }
}
