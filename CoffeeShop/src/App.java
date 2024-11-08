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
import java.security.SecureRandom;
import java.util.Base64;

public class App {

    private CoffeeShopWindow window;
    private JFrame loginFrame;
    private JCheckBox rememberMeCheckBox;

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final int ITERATIONS = 65536;
    private static final int KEY_SIZE = 256; // Use 128, 192, or 256
    private static final int SALT_LENGTH = 16; // Salt length in bytes
    private static final byte[] SALT = new byte[SALT_LENGTH]; // Declare as byte[]
    private static final String DATABASE_URL = "jdbc:mysql://45.62.14.188:3306/user";
    private static final String DATABASE_USERNAME = "altan";
    private static final String DATABASE_PASSWORD = "Pickles5-_";

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
        gbc.gridy = 5; // Adjust gridy as needed
        gbc.gridwidth = 2; 
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 11, 10); 
        loginFrame.add(autoLoginButton, gbc);
        

        // Title
        JLabel titleLabel = new JLabel("Coffee Shop Login");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2; 
        loginFrame.add(titleLabel, gbc);

        // Username
        JLabel usernameLabel = new JLabel("Username:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1; 
        loginFrame.add(usernameLabel, gbc);

        JTextField usernameField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 1;
        loginFrame.add(usernameField, gbc);

        // Password
        JLabel passwordLabel = new JLabel("Password:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        loginFrame.add(passwordLabel, gbc);

        JPasswordField passwordField = new JPasswordField(20);
        gbc.gridx = 1;
        gbc.gridy = 2;
        loginFrame.add(passwordField, gbc);

        // Login Button
        JButton loginButton = new JButton("Login");
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2; 
        gbc.fill = GridBagConstraints.HORIZONTAL; 
        loginFrame.add(loginButton, gbc);

        // Forgot Password Link
        JButton forgotPasswordButton = new JButton("Forgot Password?");
        forgotPasswordButton.setContentAreaFilled(false); 
        forgotPasswordButton.setBorderPainted(false); 
        forgotPasswordButton.setFocusPainted(false); 
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST; 
        loginFrame.add(forgotPasswordButton, gbc);

        // Create Account Link
        JButton createAccountButton = new JButton("Create Account");
        createAccountButton.setContentAreaFilled(false);
        createAccountButton.setBorderPainted(false);
        createAccountButton.setFocusPainted(false);
        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.EAST; 
        loginFrame.add(createAccountButton, gbc);

        // Remember Me
        rememberMeCheckBox = new JCheckBox("Remember Me");
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST; 
        loginFrame.add(rememberMeCheckBox, gbc);

        // Action Listeners
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());

                if (authenticate(username, password)) {
                    loginFrame.dispose();

                    // Store credentials if "Remember Me" is checked
                    if (rememberMeCheckBox.isSelected()) {
                        storeCredentials(username, password);
                    } else {
                        // If "Remember Me" is unchecked, clear any existing stored credentials
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

    private void attemptAutoLogin() {
        try {
            String[] credentials = loadCredentials();
            if (credentials != null) {
                String username = credentials[0];
                String encryptedPassword = credentials[1];
                String storedHashedPassword = getStoredHashedPassword(username);

                // Fetch salt and IV from the database based on username
                String[] saltAndIV = getSaltAndIV(username);
                if (saltAndIV != null) {
                    String base64Salt = saltAndIV[0];
                    String base64IV = saltAndIV[1];

                    // Decrypt the password

                    String decryptedPassword = decrypt(encryptedPassword, base64IV, base64Salt, storedHashedPassword);

                    // Authenticate using the decrypted password
                    if (authenticate(username, decryptedPassword)) {
                        // Auto-login successful
                        showCoffeeShopWindow();
                        loginFrame.dispose(); 
                    } else {
                        // Invalid credentials - clear stored data and show login
                        clearStoredCredentials(username); 
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Handle errors (e.g., credentials not found, decryption error)
        }
    }

    private String getStoredHashedPassword(String username) {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD)) {
            String sql = "SELECT password FROM users WHERE username = ?"; // Assuming 'password' column stores the hash
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, username);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getString("password"); 
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void storeCredentials(String username, String password) {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD)) {
            // 1. Generate a secure random salt
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);
            String base64Salt = Base64.getEncoder().encodeToString(salt);

            // 2. Derive the encryption key from the password and salt using PBKDF2
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_SIZE);
            SecretKey secretKey = factory.generateSecret(spec);
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getEncoded(), "AES");

            // 3. Generate a secure random IV
            byte[] iv = new byte[16];
            random.nextBytes(iv);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            String base64IV = Base64.getEncoder().encodeToString(iv);

            // 4. Encrypt the password
            String encryptedPassword = encrypt(password, keySpec, ivParameterSpec);
            
            System.out.println("Password used for encryption: " + password);

            // 5. Store encrypted password, IV, and salt in the database
            String sql = "UPDATE users SET encrypted_password = ?, salt = ?, iv = ? WHERE username = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, encryptedPassword);
                statement.setString(2, base64Salt);
                statement.setString(3, base64IV);
                statement.setString(4, username);
                statement.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Handle errors appropriately
        }
    }

    private void clearStoredCredentials(String username) {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD)) {
            String sql = "UPDATE users SET encrypted_password = NULL, salt = NULL, iv = NULL WHERE username = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, username);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle errors appropriately
        }
    }
    
    private String[] loadCredentials() {
        // In a real application, you should use a more secure storage mechanism
        // such as KeyStore or platform-specific secure storage.
        try (Connection connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD)) {
            // Assuming you have a way to identify the user for auto-login (e.g., stored in a file)
            String usernameForAutoLogin = getStoredUsername(); // Implement this method to get the username
    
            if (usernameForAutoLogin != null) {
                String sql = "SELECT encrypted_password FROM users WHERE username = ?";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, usernameForAutoLogin); 
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (resultSet.next()) {
                            String encryptedPassword = resultSet.getString("encrypted_password");
                            if (encryptedPassword != null) {
                                return new String[]{usernameForAutoLogin, encryptedPassword}; 
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getStoredUsername() {
    try (BufferedReader reader = new BufferedReader(new FileReader("remembered_user.txt"))) {
        return reader.readLine();
    } catch (IOException e) {
        e.printStackTrace();
        return null;
    }
}

    private String[] getSaltAndIV(String username) {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD)) {
            String sql = "SELECT salt, iv FROM users WHERE username = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, username);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        String base64Salt = resultSet.getString("salt");
                        String base64IV = resultSet.getString("iv");
                        return new String[]{base64Salt, base64IV};
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle errors appropriately
        }
        return null;
    }

    private String encrypt(String value, SecretKeySpec keySpec, IvParameterSpec iv) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv);
        byte[] encrypted = cipher.doFinal(value.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    private String decrypt(String encrypted, String base64IV, String base64Salt, String storedHashedPassword) throws Exception {
        // 1. Decode the IV and salt
        byte[] iv = Base64.getDecoder().decode(base64IV);
        byte[] salt = Base64.getDecoder().decode(base64Salt);
    
        // 2. Derive the encryption key 
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        PBEKeySpec spec = new PBEKeySpec(storedHashedPassword.toCharArray(), salt, ITERATIONS, KEY_SIZE);
        SecretKey secretKey = factory.generateSecret(spec);
        SecretKeySpec keySpec = new SecretKeySpec(secretKey.getEncoded(), "AES");
    
        // 3. Decrypt 
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(iv));
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encrypted));
        return new String(decryptedBytes); 
    }

    private void showForgotPasswordDialog(JFrame parentFrame) {
        // ... (Implementation for Forgot Password dialog) ...
        JFrame forgotPasswordFrame = new JFrame("Forgot Password");
        forgotPasswordFrame.setSize(400, 200);
        forgotPasswordFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        forgotPasswordFrame.setLayout(new GridLayout(3, 2, 10, 10));

        JLabel usernameLabel = new JLabel("Enter your username:");
        JTextField usernameField = new JTextField();
        JLabel newPasswordLabel = new JLabel("New password:");
        JPasswordField newPasswordField = new JPasswordField();
        JButton updatePasswordButton = new JButton("Update Password");

        updatePasswordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String newPassword = new String(newPasswordField.getPassword());

                if (updatePassword(username, newPassword)) {
                    JOptionPane.showMessageDialog(forgotPasswordFrame, "Password updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    forgotPasswordFrame.dispose();
                } else {
                    JOptionPane.showMessageDialog(forgotPasswordFrame, "Error updating password. Please check your username.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        forgotPasswordFrame.add(usernameLabel);
        forgotPasswordFrame.add(usernameField);
        forgotPasswordFrame.add(newPasswordLabel);
        forgotPasswordFrame.add(newPasswordField);
        forgotPasswordFrame.add(new JLabel()); // Empty label for spacing
        forgotPasswordFrame.add(updatePasswordButton);

        forgotPasswordFrame.setLocationRelativeTo(parentFrame);
        forgotPasswordFrame.setVisible(true);
    }

    private boolean updatePassword(String username, String newPassword) {
        String url = "jdbc:mysql://45.62.14.188:3306/user";
        String dbUsername = "altan";
        String dbPassword = "Pickles5-_";

        try (Connection connection = DriverManager.getConnection(url, dbUsername, dbPassword)) {
            String sql = "UPDATE users SET password=? WHERE username=?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, newPassword);
            statement.setString(2, username);

            int rowsUpdated = statement.executeUpdate();
            return rowsUpdated > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
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
        // You can use a regular expression to validate email format
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(username);
        return matcher.matches();
    }

    private boolean createNewAccount(String username, String password) {
        String url = "jdbc:mysql://45.62.14.188:3306/user";
        String dbUsername = "altan";
        String dbPassword = "Pickles5-_";

        try (Connection connection = DriverManager.getConnection(url, dbUsername, dbPassword)) {
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
        // ... (Your existing authentication logic) ...
        String url = "jdbc:mysql://45.62.14.188:3306/user"; 
        String dbUsername = "altan"; 
        String dbPassword = "Pickles5-_"; 

        try (Connection connection = DriverManager.getConnection(url, dbUsername, dbPassword)) {
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

    public void showCoffeeShopWindow() {
        // ... (Your existing code to show the main window) ...
        window = new CoffeeShopWindow();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setVisible(true);
        window.addDrinksToMenu();

        // Use a Timer instead of a while loop
        Timer timer = new Timer(1000, new ActionListener() { // Update every 1000 milliseconds (1 second)
            @Override
            public void actionPerformed(ActionEvent e) {
                // Get current time and format it
                LocalTime currentTime = LocalTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                String formattedTime = currentTime.format(formatter);

                // Update the window title with the time
                window.setTitle("Coffee Shop - " + formattedTime);
            }
        });
        timer.start(); 
    }
}

