import javax.swing.*;
import java.awt.*;

public class LoginUI extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginUI() {

        setTitle("SUMURAI");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        ImageIcon icon = new ImageIcon(getClass().getResource("images/katana.png"));
        setIconImage(icon.getImage());

        Font yoruKazeFont;
        try {
            yoruKazeFont = Font.createFont(
                Font.TRUETYPE_FONT,
                new java.io.File("src/fonts/YoruKaze-WpYa9.otf")
            ).deriveFont(Font.PLAIN, 42f);

        } catch (Exception e) {
            e.printStackTrace();
            yoruKazeFont = new Font("Times New Roman", Font.PLAIN, 42);
        }

        // Main panel - centers everything on screen
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(25, 25, 45));

        // Center container
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(new Color(25, 25, 45));
        container.setPreferredSize(new Dimension(400, 500));

        // Title
        JLabel title = new JLabel("S U M U R A I");
        title.setFont(yoruKazeFont);
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        container.add(title);
        container.add(Box.createVerticalStrut(10));

        // Username label
        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 15));
        usernameLabel.setForeground(Color.WHITE);
        usernameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        container.add(usernameLabel);
        container.add(Box.createVerticalStrut(8));

        // Username field
        usernameField = new JTextField();
        usernameField.setFont(new Font("Arial", Font.PLAIN, 16));
        usernameField.setMaximumSize(new Dimension(350, 40));
        usernameField.setPreferredSize(new Dimension(350, 40));
        usernameField.setAlignmentX(Component.CENTER_ALIGNMENT);
        usernameField.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        container.add(usernameField);
        container.add(Box.createVerticalStrut(20));

        // Password label
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 15));
        passwordLabel.setForeground(Color.WHITE);
        passwordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        container.add(passwordLabel);
        container.add(Box.createVerticalStrut(8));

        // Password field
        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Arial", Font.PLAIN, 16));
        passwordField.setMaximumSize(new Dimension(350, 40));
        passwordField.setPreferredSize(new Dimension(350, 40));
        passwordField.setAlignmentX(Component.CENTER_ALIGNMENT);
        passwordField.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        container.add(passwordField);
        container.add(Box.createVerticalStrut(30));

        // Login button
        JButton loginButton = new JButton("LOGIN");
        loginButton.setFont(new Font("Arial", Font.BOLD, 16));
        loginButton.setBackground(new Color(65, 105, 225));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setMaximumSize(new Dimension(350, 45));
        loginButton.setPreferredSize(new Dimension(350, 45));
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        container.add(loginButton);
        container.add(Box.createVerticalStrut(15));

        // Login functionality
        loginButton.addActionListener(e -> {

            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            if (username.isEmpty() || password.isEmpty()) {

                JOptionPane.showMessageDialog(
                    this,
                    "Please enter your username and password."
                );

            } else {

                JOptionPane.showMessageDialog(
                    this,
                    "Welcome, " + username + "!"
                );

            }
        });

        // Signup button
        JButton signupButton = new JButton("SIGN UP");
        signupButton.setFont(new Font("Arial", Font.BOLD, 14));
        signupButton.setBackground(new Color(46, 139, 87));
        signupButton.setForeground(Color.WHITE);
        signupButton.setFocusPainted(false);
        signupButton.setPreferredSize(new Dimension(165, 40));

        // Guest button
        JButton guestButton = new JButton("PLAY AS GUEST");
        guestButton.setFont(new Font("Arial", Font.BOLD, 14));
        guestButton.setBackground(new Color(90, 90, 110));
        guestButton.setForeground(Color.WHITE);
        guestButton.setFocusPainted(false);
        guestButton.setPreferredSize(new Dimension(165, 40));

        // Signup and guest button panel
        JPanel buttonPanel = new JPanel(
            new FlowLayout(FlowLayout.CENTER, 15, 0)
        );

        buttonPanel.setBackground(new Color(25, 25, 45));
        buttonPanel.add(signupButton);
        buttonPanel.add(guestButton);

        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        container.add(buttonPanel);

        // Signup functionality
        signupButton.addActionListener(e -> {

            JOptionPane.showMessageDialog(
                this,
                "Sign Up Page Coming Soon!"
            );

        });

        // Guest functionality
        guestButton.addActionListener(e -> {

            JOptionPane.showMessageDialog(
                this,
                "Welcome, Guest! Enjoy SUMURAI!"
            );

        });

        // Add container to center of screen
        panel.add(container);

        add(panel);

    }
}