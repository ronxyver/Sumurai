// OWNER: Franchesca Denise B. Manila
package com.sumurai.gui;

import com.sumurai.model.UserProfile;
import com.sumurai.storage.UserRepository;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public final class LoginPanel extends JPanel {
    private final JTextField usernameField = new JTextField(22);
    private final JPasswordField passwordField = new JPasswordField(22);
    private final UserRepository userRepository;
    private final Consumer<UserProfile> onAuthenticated;

    public LoginPanel(UserRepository users, Consumer<UserProfile> onAuthenticated) {
        this.userRepository = users;
        this.onAuthenticated = onAuthenticated;
        setBackground(ThemeManager.backgroundColor());
        setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        constraints.insets = new Insets(10, 12, 18, 12);
        JLabel title = new JLabel("S U M U R A I");
        title.setForeground(ThemeManager.textColor());
        title.setFont(ThemeManager.headingFont(38));
        add(title, constraints);

        constraints.gridwidth = 1;
        constraints.gridy++;
        constraints.anchor = GridBagConstraints.LINE_END;
        constraints.insets = new Insets(7, 12, 7, 8);
        add(new JLabel("Username"), constraints);
        constraints.gridx = 1;
        constraints.anchor = GridBagConstraints.LINE_START;
        add(usernameField, constraints);

        constraints.gridx = 0;
        constraints.gridy++;
        constraints.anchor = GridBagConstraints.LINE_END;
        add(new JLabel("Password"), constraints);
        constraints.gridx = 1;
        constraints.anchor = GridBagConstraints.LINE_START;
        add(passwordField, constraints);

        JButton loginButton = new JButton("LOG IN");
        JButton registerButton = new JButton("SIGN UP");
        JButton guestButton = new JButton("PLAY AS GUEST");
        style(loginButton);
        style(registerButton);
        style(guestButton);

        constraints.gridx = 0;
        constraints.gridy++;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.insets = new Insets(18, 8, 8, 8);
        add(loginButton, constraints);
        constraints.gridy++;
        add(registerButton, constraints);
        constraints.gridy++;
        add(guestButton, constraints);

        loginButton.addActionListener(event -> authenticate(false));
        registerButton.addActionListener(event -> authenticate(true));
        guestButton.addActionListener(event -> onAuthenticated.accept(UserProfile.guest()));
        passwordField.addActionListener(event -> authenticate(false));
    }

    public void clearFields() {
        usernameField.setText("");
        passwordField.setText("");
    }

    private void authenticate(boolean register) {
        String username = usernameField.getText().trim();
        char[] password = passwordField.getPassword();
        if (username.isEmpty() || password.length == 0) {
            JOptionPane.showMessageDialog(this, "Enter a username and password.");
            Arrays.fill(password, '\0');
            return;
        }

        setControlsEnabled(false);
        SwingWorker<UserProfile, Void> worker = new SwingWorker<UserProfile, Void>() {
            @Override
            protected UserProfile doInBackground() throws Exception {
                if (register) {
                    return userRepository.register(username, password);
                }
                Optional<UserProfile> profile = userRepository.authenticate(username, password);
                return profile.orElse(null);
            }

            @Override
            protected void done() {
                setControlsEnabled(true);
                try {
                    UserProfile profile = get();
                    if (profile == null) {
                        JOptionPane.showMessageDialog(LoginPanel.this,
                                "Account not found or password is incorrect. Sign up or play as a guest.");
                        return;
                    }
                    passwordField.setText("");
                    onAuthenticated.accept(profile);
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    showError("Account request was interrupted.");
                } catch (ExecutionException exception) {
                    Throwable cause = exception.getCause();
                    showError(cause == null ? exception.getMessage() : cause.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void setControlsEnabled(boolean enabled) {
        usernameField.setEnabled(enabled);
        passwordField.setEnabled(enabled);
        for (java.awt.Component component : getComponents()) {
            if (component instanceof JButton) {
                component.setEnabled(enabled);
            }
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Account error", JOptionPane.ERROR_MESSAGE);
    }

    private void style(JButton button) {
        button.setBackground(ThemeManager.accentColor());
        button.setForeground(ThemeManager.textColor());
        button.setFocusPainted(false);
        button.setPreferredSize(new java.awt.Dimension(220, 42));
    }
}
