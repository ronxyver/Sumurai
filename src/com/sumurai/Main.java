// OWNER: Franchesca Denise B. Manila
package com.sumurai;

import com.sumurai.gui.GameWindow;
import com.sumurai.storage.BattleHistoryRepository;
import com.sumurai.storage.UserRepository;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.io.IOException;
import java.nio.file.Paths;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        try {
            UserRepository users = new UserRepository(Paths.get("data", "users.txt"));
            BattleHistoryRepository history = new BattleHistoryRepository(Paths.get("data", "history.txt"));
            SwingUtilities.invokeLater(() -> {
                GameWindow window = new GameWindow(users, history);
                window.setVisible(true);
            });
        } catch (IOException exception) {
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                    null,
                    "Unable to initialize game data: " + exception.getMessage(),
                    "Sumurai startup error",
                    JOptionPane.ERROR_MESSAGE));
        }
    }
}