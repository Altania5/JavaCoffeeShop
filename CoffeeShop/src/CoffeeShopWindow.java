import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

class CoffeeShopWindow extends JFrame {

    private Connection connection;
    private MenuPanel menuPanel;
    private Ingredients ingredients;
    public Map<Drinks, JPanel> drinkPanelMap;
    private JTable ingredientTable;
    private JScrollPane ingredientTableScrollPane;

    public CoffeeShopWindow() {

        try {
            this.connection = DriverManager.getConnection(App.DATABASE_URL, App.DATABASE_USERNAME, App.DATABASE_PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        ingredients = new Ingredients(connection);

        ingredientTable = new JTable(); // Create JTable instance only once
        ingredientTableScrollPane = new JScrollPane(ingredientTable);
        
        loadInventory();
        
        IngredientTableModel tableModel = new IngredientTableModel(ingredients.getIngredientList());
        ingredientTable.setModel(tableModel); // Set the model to the existing JTable
        IngredientCellRenderer cellRenderer = new IngredientCellRenderer(tableModel.getInStockMap());
        ingredientTable.setDefaultRenderer(Object.class, cellRenderer);

        menuPanel = new MenuPanel(ingredients);
        drinkPanelMap = new HashMap<>();
        addDrinksToMenu();

        JTabbedPane tabbedPane = new JTabbedPane();
        JPanel inventoryPanel = createInventoryPanel();
    
        setTitle("Coffee Shop");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
        tabbedPane.addTab("Menu", menuPanel);
        tabbedPane.addTab("Inventory", inventoryPanel);
        add(tabbedPane); // Add tabbedPane to the JFrame
    
        try {
            Image icon = ImageIO.read(new File("C:\\Users\\altan\\Desktop\\Java code\\Coffee Shop\\CoffeeShop\\res\\GDAS.png")); // Replace with your image file
            setIconImage(icon);
        } catch (IOException e) {
            System.err.println("Error loading icon image: " + e.getMessage());
        }
        ingredientTable.setDefaultRenderer(Object.class, cellRenderer);
        updateMenu();
    }

    private JPanel createInventoryPanel() {
        JPanel inventoryPanel = new JPanel(new BorderLayout());
        inventoryPanel.add(ingredientTableScrollPane, BorderLayout.CENTER);

        // Create buttons
        JButton toggle = new JButton("Add Ingredient");
        JButton removeButton = new JButton("Remove Ingredient");
        JButton modifyButton = new JButton("Modify Ingredient");
        JButton createButton = new JButton("Create Ingredient");

        // Add button listeners (implement these methods)
        toggle.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showToggleInStockDialog();
            }
        });

        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showRemoveIngredientDialog();
            }
        });

        modifyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showModifyIngredientDialog();
            }
        });

        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showCreateIngredientDialog();
            }
        });

        // Add buttons to a panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(toggle);
        buttonPanel.add(removeButton);
        buttonPanel.add(modifyButton);
        buttonPanel.add(createButton);

        // Add button panel to the south of inventory panel
        inventoryPanel.add(buttonPanel, BorderLayout.SOUTH);

        return inventoryPanel;
    }

    private void showToggleInStockDialog() {
        // Get the selected ingredient from the table
        int selectedRow = ingredientTable.getSelectedRow();
        int selectedColumn = ingredientTable.getSelectedColumn();
        if (selectedRow == -1 || selectedColumn == -1) {
            JOptionPane.showMessageDialog(this, "Please select an ingredient to toggle.", 
                                          "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
    
        // Get the ingredient name (assuming it's in the first column)
        String ingredientName = (String) ingredientTable.getValueAt(selectedRow, selectedColumn);

        if (ingredientName == null || ingredientName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a valid ingredient.", 
                                          "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
    
        // Get the current in-stock status from the Ingredients object
        boolean currentInStock = ingredients.isInStock(ingredientName);
    
        // Toggle the in-stock status
        boolean newInStock = !currentInStock;
    
        // Update the in-stock status in the database
        try (PreparedStatement statement = connection.prepareStatement(
            "UPDATE user.ingredients SET inStock = ? WHERE ingredients = ?")) {

        statement.setInt(1, newInStock ? 1 : 0);
        statement.setString(2, ingredientName);
        statement.executeUpdate();

        // Update the Ingredients object (call the correct method with the type)
        ingredients.updateIngredientStock(ingredientName, newInStock, 
                                         ingredients.getIngredientList().get(ingredientName).getType()); 

        // ... (your existing code to update the table model) ...
    } catch (SQLException e) {
        // ... (your existing exception handling) ...
    }
    }

    private void showRemoveIngredientDialog() {
        // Get the selected ingredient from the table
        int selectedRow = ingredientTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an ingredient to remove.", 
                                          "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
    
        String ingredientName = (String) ingredientTable.getValueAt(selectedRow, 0); // Assuming name is in the first column
    
        // Confirm removal
        int option = JOptionPane.showConfirmDialog(this, 
                                                  "Are you sure you want to remove " + ingredientName + "?", 
                                                  "Confirm Removal", 
                                                  JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            // Remove the ingredient from the database and update the table
            try (PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM user.ingredients WHERE ingredients = ?")) {
                statement.setString(1, ingredientName);
                statement.executeUpdate();
                updateInventoryTable();
                updateMenu();
            } catch (SQLException e) {
                e.printStackTrace();
                // Handle the exception appropriately (e.g., log, show error message)
            }
        }
    }

    private void showModifyIngredientDialog() {

    }

    private void showCreateIngredientDialog() {
        JTextField ingredientNameField = new JTextField(20);
        JCheckBox inStockCheckBox = new JCheckBox("In Stock");
    
        String[] ingredientTypes = {"Espresso Bean", "Milk", "Syrup", "Other"}; 
        JComboBox<String> typeComboBox = new JComboBox<>(ingredientTypes);
    
        Object[] message = {
            "Ingredient Name:", ingredientNameField,
            "In Stock:", inStockCheckBox,
            "Type:", typeComboBox
        };
    
        int option = JOptionPane.showConfirmDialog(this, message, "Create New Ingredient", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String ingredientName = ingredientNameField.getText();
            boolean inStock = inStockCheckBox.isSelected();
            String type = (String) typeComboBox.getSelectedItem(); 
    
            if (!ingredientName.isEmpty() && type != null) { // Check for null type
                ingredients.addIngredient(ingredientName, inStock, type);
                updateInventoryTable(); // Update the JTable
                updateMenu(); 
            } else {
                JOptionPane.showMessageDialog(this, "Ingredient name cannot be empty and a type must be selected.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadInventory() {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT ingredients, type FROM user.ingredients")) {  //Retrieve type as well
            
            Map<String, String> existingIngredients = new HashMap<>();  // Store both name AND existing type
            while (resultSet.next()) {
                existingIngredients.put(resultSet.getString("ingredients"), resultSet.getString("type"));
            }
    
    
            for (Drinks drink : Drinks.getAllDrinks()) {
                for (Map.Entry<String, Double> entry : drink.getIngredients().entrySet()) {
                    String ingredientName = entry.getKey();
    
    
                    String existingType = existingIngredients.get(ingredientName);
                    if (existingType != null) {
                        // Ingredient exists: Use the type from the database
                        ingredients.addIngredient(ingredientName, true, existingType);  // Correctly setting type from DB
    
                    } else {
                        // Ingredient is new: Add it to the database with its proper type
                        try (PreparedStatement insertStatement = connection.prepareStatement(
                                "INSERT INTO user.ingredients (ingredients, inStock, amount, type) VALUES (?, ?, ?, ?)")) {
    
                            String type = getIngredientTypeFromDrink(drink, ingredientName);  // Get type from method
    
                            insertStatement.setString(1, ingredientName);
                            // ... (rest of the insertStatement parameters)
    
                            existingIngredients.put(ingredientName, type); // No need to re-query, more efficient
                            ingredients.addIngredient(ingredientName, true, type); // Add it to our in-memory list
    
                        } catch (SQLException e) {
                            // ... error handling
                        }
                    }
                }
            }
            updateInventoryTable();
        } catch (SQLException e) {
           // ... error handling
        }
    }
    
    private String getIngredientTypeFromDrink(Drinks drink, String ingredientName) {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT type FROM user.ingredients WHERE ingredients = ?")) {
    
            statement.setString(1, ingredientName);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String type = resultSet.getString("type");
                    return (type != null) ? type : "Other"; // Return type from database or "Other" if null
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting ingredient type from database: " + e.getMessage());
            // Handle the exception appropriately (e.g., log, show error message)
        }
        return "Other"; // Default to "Other" if there's an error or ingredient not found
    }
    
    private void saveInventory() {
        File inventoryFile = new File("inventory.txt");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(inventoryFile))) {
            for (Map.Entry<String, Ingredients.IngredientData> entry : ingredients.getIngredientList().entrySet()) {
                writer.write(entry.getKey() + "," + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving inventory: " + e.getMessage());
        }
    }

    public void addDrinksToMenu() {
        menuPanel.addDrink(new VanillaLatte(4.75, ingredients));
        menuPanel.addDrink(new Bose(6.50, ingredients));
        menuPanel.addDrink(new SCCB(4.50, ingredients));
        menuPanel.addDrink(new PSL(4.75, ingredients));
        menuPanel.addDrink(new PEGTL(2.50, ingredients));
    }
    
    private void updateInventoryTable() {
        ingredientTable.setModel(new IngredientTableModel(ingredients.getIngredientList())); 
     }

    private void updateMenu() {
        menuPanel.removeAll(); // Clear existing menu items
    
        // Add ALL drinks back to the menu, letting the canMake logic handle display
        for (Drinks drink : Drinks.getAllDrinks()) { 
            menuPanel.addDrink(drink); 
        }
    
        menuPanel.revalidate();
        menuPanel.repaint();
        saveInventory();
    }
    
}