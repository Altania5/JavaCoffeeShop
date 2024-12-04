import javax.swing.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;



public class App {

    private CoffeeShopWindow window;
    private JFrame loginFrame;
    private JCheckBox rememberMeCheckBox;

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final int HASHING_ITERATIONS = 65536;
    private static final int HASHING_KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;
    private static final byte[] SALT = new byte[SALT_LENGTH];
    public static final String DATABASE_URL = "jdbc:mysql://45.62.14.35:3306/user";
    public static final String DATABASE_USERNAME = "altan";
    public static final String DATABASE_PASSWORD = "Pickles5-_";

    public static void main(String[] args) {
        SecureRandom random = new SecureRandom();
        random.nextBytes(SALT);
        App app = new App();
        app.showLogin(); 
    }

    private void showLogin() {

        this.loginFrame = new JFrame("Login");

        loginFrame.setSize(800, 600);
        loginFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
        loginFrame.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        JButton autoLoginButton = new JButton("Auto-Login");
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2; 
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 11, 10); 
        loginFrame.add(autoLoginButton, gbc);
        

        JLabel titleLabel = new JLabel("Coffee Shop Login");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2; 
        loginFrame.add(titleLabel, gbc);

        JLabel usernameLabel = new JLabel("Username:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1; 
        loginFrame.add(usernameLabel, gbc);

        JTextField usernameField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 1;
        loginFrame.add(usernameField, gbc);

        JLabel passwordLabel = new JLabel("Password:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        loginFrame.add(passwordLabel, gbc);

        JPasswordField passwordField = new JPasswordField(20);
        gbc.gridx = 1;
        gbc.gridy = 2;
        loginFrame.add(passwordField, gbc);

        JButton loginButton = new JButton("Login");
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2; 
        gbc.fill = GridBagConstraints.HORIZONTAL; 
        loginFrame.add(loginButton, gbc);

        JButton forgotPasswordButton = new JButton("Forgot Password?");
        forgotPasswordButton.setContentAreaFilled(false); 
        forgotPasswordButton.setBorderPainted(false); 
        forgotPasswordButton.setFocusPainted(false); 
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST; 
        loginFrame.add(forgotPasswordButton, gbc);

        JButton createAccountButton = new JButton("Create Account");
        createAccountButton.setContentAreaFilled(false);
        createAccountButton.setBorderPainted(false);
        createAccountButton.setFocusPainted(false);
        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.EAST; 
        loginFrame.add(createAccountButton, gbc);

        rememberMeCheckBox = new JCheckBox("Remember Me");
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST; 
        loginFrame.add(rememberMeCheckBox, gbc);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());

                if (authenticate(username, password)) {
                    loginFrame.dispose();

                    if (rememberMeCheckBox.isSelected()) {
                        storeCredentials(username, password);
                    } else {
                        clearStoredCredentials(username); 
                    }

                    showCoffeeShopWindow();
                } else {
                    JOptionPane.showMessageDialog(loginFrame, "Invalid username or password", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        autoLoginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                attemptAutoLogin();
            }
        });

        forgotPasswordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showForgotPasswordDialog(loginFrame);
            }
        });

        createAccountButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showCreateAccountDialog(loginFrame);
            }
        });

        loginFrame.setLocationRelativeTo(null); 
        loginFrame.setVisible(true);
    }

    private void showCreateAccountDialog(JFrame parentFrame) {
        JFrame createAccountFrame = new JFrame("Create Account");
        createAccountFrame.setSize(400, 250); 
        createAccountFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        createAccountFrame.setLayout(new GridLayout(4, 2, 10, 10)); 

        JLabel usernameLabel = new JLabel("Username (with email):");
        JTextField usernameField = new JTextField();
        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordField = new JPasswordField();
        JButton createAccountButton = new JButton("Create Account");

        createAccountButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());

                if (isValidEmailUsername(username)) {
                    if (createNewAccount(username, password)) {
                        JOptionPane.showMessageDialog(createAccountFrame, "Account created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        createAccountFrame.dispose();
                    } else {
                        JOptionPane.showMessageDialog(createAccountFrame, "Error creating account. Username might be taken.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(createAccountFrame, "Invalid username. Please use an email format.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        createAccountFrame.add(usernameLabel);
        createAccountFrame.add(usernameField);
        createAccountFrame.add(passwordLabel);
        createAccountFrame.add(passwordField);
        createAccountFrame.add(new JLabel()); 
        createAccountFrame.add(createAccountButton);

        createAccountFrame.setLocationRelativeTo(parentFrame); 
        createAccountFrame.setVisible(true);
    }

    private boolean isValidEmailUsername(String username) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(username);
        return matcher.matches();
    }

    private boolean createNewAccount(String username, String password) {

        try (Connection connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD)) {
            String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, username);
            statement.setString(2, password);

            int rowsInserted = statement.executeUpdate();
            return rowsInserted > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean authenticate(String username, String password) {

        try (Connection connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD)) {
            String sql = "SELECT * FROM users WHERE username=? AND password=?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, username);
            statement.setString(2, password);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void showCoffeeShopWindow() {
        JFrame loadingFrame = new JFrame("Loading...");
        loadingFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        JLabel loadingLabel = new JLabel("Loading application, please wait...", SwingConstants.CENTER);
        loadingFrame.add(loadingLabel);
        loadingFrame.setSize(300, 150);
        loadingFrame.setLocationRelativeTo(null);
        loadingFrame.setVisible(true);
    
    
        SwingWorker<CoffeeShopWindow, Void> worker = new SwingWorker<>() { // Return CoffeeShopWindow
            @Override
            protected CoffeeShopWindow doInBackground() throws Exception {  // Correct return type
                return new CoffeeShopWindow(); // Create and RETURN the window
            }
    
            @Override
            protected void done() {
                loadingFrame.dispose();
                try {
                    window = get(); // Get the created window  ***KEY CHANGE***
                    window.setVisible(true); // Now window is initialized
                    startClock(window);
                } catch (Exception e) { // Handle potential exceptions
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Error initializing application", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
    

    private void startClock(JFrame window) {
        Timer timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                LocalTime currentTime = LocalTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                String formattedTime = currentTime.format(formatter);
    
                window.setTitle("Coffee Shop - " + formattedTime);
            }
        });
        timer.start();
    }
}

