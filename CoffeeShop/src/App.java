import javax.swing.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
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
import java.util.Arrays;
import java.util.Base64;

public class App {

    private CoffeeShopWindow window;
    private JFrame loginFrame;
    private JCheckBox rememberMeCheckBox;

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final int ITERATIONS = 65536;
    private static final int KEY_SIZE = 256;
    private static final int SALT_LENGTH = 16;
    private static final byte[] SALT = new byte[SALT_LENGTH];
    public static final String DATABASE_URL = "jdbc:mysql://45.62.14.35:3306/user";
    public static final String DATABASE_USERNAME = "altan";
    public static final String DATABASE_PASSWORD = "Pickles5-_";
    private static final String CREDENTIALS_FILE = "credentials.txt"; // Store in the same directory as the .jar

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
                char[] passwordChars = passwordField.getPassword(); // Get char array
                String password = new String(passwordChars);      // Convert to String IMMEDIATELY
                Arrays.fill(passwordChars, '0');

                if (authenticate(username, password)) {

                    if (rememberMeCheckBox.isSelected()) {
                        storeCredentials(username, password);
                    }

                    loginFrame.dispose();

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

    private void storeCredentials(String username, String password) {

    try (Connection connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD)) {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);
    
            String hashedPassword = hashPassword(password); // Hash the raw password
    
            // ... (rest of the encryption process, using hashedPassword for key derivation)
    
            String sql = "UPDATE users SET password = ?, salt = ?, iv = ?, encrypted_password = ? WHERE username = ?"; // Correct SQL
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, hashedPassword); // Store the hashed password
                // ... (set other parameters: salt, iv, encryptedPassword)
                statement.executeUpdate();
            }
    
        } catch (SQLException e) { // Catch specific exceptions
            e.printStackTrace(); // Log for debugging
            JOptionPane.showMessageDialog(null, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) { // For other potential exceptions
            // ... handle other exceptions appropriately
        }

        try (FileWriter writer = new FileWriter(CREDENTIALS_FILE)) {
            writer.write(username); // Only store the username!

        } catch (IOException e) {
            e.printStackTrace();
            // Handle the error as appropriate (e.g., display an error message).
        }
    }

    private void attemptAutoLogin() {
        String username = null;
        String storedHashedPassword = null;
        String encryptedPassword = null;
        byte[] salt = null;
        byte[] iv = null;
    
        try (BufferedReader reader = new BufferedReader(new FileReader(CREDENTIALS_FILE))) {
            username = reader.readLine();
    
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the error appropriately (e.g., display an error message, return).
            return; //Important to prevent further errors
        }
    
        if (username != null) {
            try (Connection connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD)) {
    
    
                // Retrieve hashed password, salt, iv from database using the username.
                String sql = "SELECT password, salt, iv, encrypted_password FROM users WHERE username = ?";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, username);
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (resultSet.next()) {
                            storedHashedPassword = resultSet.getString("password");
                            encryptedPassword = resultSet.getString("encrypted_password");
                            salt = Base64.getDecoder().decode(resultSet.getString("salt"));
                            iv = Base64.getDecoder().decode(resultSet.getString("iv"));
                        }
                    }
                }
    
                if (encryptedPassword != null && storedHashedPassword != null) {  //Make sure they are present.
                   String decryptedPassword = decrypt(encryptedPassword, salt, iv, storedHashedPassword);
    
                   if(authenticate(username, decryptedPassword)) { //This is where the authentication takes place.
                    loginFrame.dispose();
                    showCoffeeShopWindow();
                   }
    
                }
            } catch (SQLException e) {
                e.printStackTrace();
                // Handle database errors appropriately.
            } catch (Exception e) {
                throw new RuntimeException(e); //Handle other exceptions
            }
        }
    }

    private boolean isValidEmailUsername(String username) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(username);
        return matcher.matches();
    }

    private boolean createNewAccount(String username, String password) {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD)) {
    
            // 1. Generate Salt
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);
    
            // 2. Hash the password
            String hashedPassword = hashPassword(password);
    
            // 3. Generate IV
            byte[] iv = new byte[16]; // AES block size
            random.nextBytes(iv);
    
            // 4. Key Derivation (from hashed password)
            PBEKeySpec keySpec = new PBEKeySpec(hashedPassword.toCharArray(), salt, ITERATIONS, KEY_SIZE);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            SecretKey key = keyFactory.generateSecret(keySpec);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getEncoded(), "AES");
    
            // 5. Encrypt
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new IvParameterSpec(iv));
            byte[] encryptedPasswordBytes = cipher.doFinal(password.getBytes());
            String encryptedPassword = Base64.getEncoder().encodeToString(encryptedPasswordBytes);
    
            // 6. Store in Database (Hashed password, salt, iv, encrypted password)
            String sql = "INSERT INTO users (username, password, salt, iv, encrypted_password) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, username);
                statement.setString(2, hashedPassword); // Store hashed password
                statement.setString(3, Base64.getEncoder().encodeToString(salt));
                statement.setString(4, Base64.getEncoder().encodeToString(iv));
                statement.setString(5, encryptedPassword);
                int rowsInserted = statement.executeUpdate();
                return rowsInserted > 0;
            }
    
        } catch (SQLException | NoSuchAlgorithmException | InvalidKeySpecException ex) {
            ex.printStackTrace();     // Log for debugging
            JOptionPane.showMessageDialog(loginFrame, "Error during account creation: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        } catch (Exception ex) { //For other exceptions
            throw new RuntimeException(ex);
        }
    }

    private String decrypt(String base64EncryptedPassword, byte[] salt, byte[] iv, String storedHashedPassword) throws Exception {

        byte[] encryptedPasswordBytes = Base64.getDecoder().decode(base64EncryptedPassword);
    
    
        // Key Derivation (from storedHashedPassword)
        PBEKeySpec keySpec = new PBEKeySpec(storedHashedPassword.toCharArray(), salt, ITERATIONS, KEY_SIZE);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        SecretKey key = keyFactory.generateSecret(keySpec);
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getEncoded(), "AES");
    
        // Decryption
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(iv));
        byte[] decryptedBytes = cipher.doFinal(encryptedPasswordBytes);
        return new String(decryptedBytes);
    }

    private String hashPassword(String password) {  // Method to hash password using PBKDF2
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);
        
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_SIZE); // Or KEY_LENGTH
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = skf.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean authenticate(String username, String enteredPassword) {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD)) {
            // 1. Retrieve Hashed Password, Salt, IV, and Encrypted Password
            String sql = "SELECT password, salt, iv, encrypted_password FROM users WHERE username = ?";
    
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, username);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        String storedHashedPassword = resultSet.getString("password");
                        String base64EncryptedPassword = resultSet.getString("encrypted_password");
                        byte[] salt = Base64.getDecoder().decode(resultSet.getString("salt"));
                        byte[] iv = Base64.getDecoder().decode(resultSet.getString("iv"));
    
                        // 2. Decrypt the password
                        String decryptedPassword = decrypt(base64EncryptedPassword, salt, iv, storedHashedPassword);
                        // 3. Verify entered password against the decryptedPassword
                        return enteredPassword.equals(decryptedPassword);
                    }
                }
            }
            return false; // Username not found
    
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        } catch (Exception e) { //Handle other exceptions from decrypt, for example
            throw new RuntimeException("Decryption error during login.", e); //Provide helpful info
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

    private void showForgotPasswordDialog(JFrame parentFrame) {
    JTextField usernameField = new JTextField(20);
    JPasswordField newPasswordField = new JPasswordField(20);
    JPasswordField confirmPasswordField = new JPasswordField(20);

    Object[] message = {
        "Username:", usernameField,
        "New Password:", newPasswordField,
        "Confirm Password:", confirmPasswordField
    };

    int option = JOptionPane.showConfirmDialog(parentFrame, message, "Forgot Password", JOptionPane.OK_CANCEL_OPTION);

    if (option == JOptionPane.OK_OPTION) {
        String username = usernameField.getText();
        char[] newPassword = newPasswordField.getPassword();
        char[] confirmPassword = confirmPasswordField.getPassword();

        if (!username.isEmpty() && newPassword.length > 0 && Arrays.equals(newPassword, confirmPassword)) {
            try (Connection connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD)) {

                //Check if the username exists
                if (!usernameExists(connection, username)) {
                    JOptionPane.showMessageDialog(parentFrame, "Username not found.", "Error", JOptionPane.ERROR_MESSAGE);
                    Arrays.fill(newPassword, '0');
                    Arrays.fill(confirmPassword, '0');
                    return;
                }



                String oldHashedPassword = getStoredHashedPassword(username);

                if (oldHashedPassword != null) {

                    String newHashedPassword = hashPassword(new String(newPassword));

                    byte[] newSalt = new byte[SALT_LENGTH];
                    new SecureRandom().nextBytes(newSalt);
                    byte[] newIV = new byte[16];
                    new SecureRandom().nextBytes(newIV);

                    String base64NewSalt = Base64.getEncoder().encodeToString(newSalt);
                    String base64NewIV = Base64.getEncoder().encodeToString(newIV);



                    // Encrypt the new password
                    PBEKeySpec keySpec = new PBEKeySpec(newHashedPassword.toCharArray(), newSalt, ITERATIONS, KEY_SIZE);
                    SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
                    SecretKey key = keyFactory.generateSecret(keySpec);
                    SecretKeySpec secretKeySpec = new SecretKeySpec(key.getEncoded(), "AES");
    
                    Cipher cipher = Cipher.getInstance(ALGORITHM);
                    cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new IvParameterSpec(newIV));
                    byte[] encryptedNewPasswordBytes = cipher.doFinal(new String(newPassword).getBytes()); //Use the new password, not old
                    String base64EncryptedNewPassword = Base64.getEncoder().encodeToString(encryptedNewPasswordBytes);




                    String sql = "UPDATE users SET password = ?, salt = ?, iv = ?, encrypted_password = ? WHERE username = ?";
                    try (PreparedStatement statement = connection.prepareStatement(sql)) {
                        statement.setString(1, newHashedPassword);
                        statement.setString(2, base64NewSalt); //Use new salt and IV
                        statement.setString(3, base64NewIV);
                        statement.setString(4, base64EncryptedNewPassword); //Use the encrypted *new* password
                        statement.setString(5, username);

                        int rowsUpdated = statement.executeUpdate();
                        if(rowsUpdated > 0) {
                            JOptionPane.showMessageDialog(parentFrame, "Password updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(parentFrame, "Failed to update password.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }


                }

            } catch (SQLException | NoSuchAlgorithmException | InvalidKeySpecException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(parentFrame, "Error updating password: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            } else {
                JOptionPane.showMessageDialog(parentFrame, "Please enter a valid username and matching passwords.", "Error", JOptionPane.ERROR_MESSAGE);
            }

            Arrays.fill(newPassword, '0'); // Clear password from memory
            Arrays.fill(confirmPassword, '0');
        }
    }

    private boolean usernameExists(Connection connection, String username) throws SQLException {
        String checkSQL = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (PreparedStatement checkStatement = connection.prepareStatement(checkSQL)) {
            checkStatement.setString(1, username);
            try (ResultSet rs = checkStatement.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    return count > 0;
                }
            }
        }
        return false;
    }

    private String getStoredHashedPassword(String username) {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD)) {
            String sql = "SELECT password FROM users WHERE username = ?";
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

