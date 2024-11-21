import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class CoffeeShopWindow extends JFrame {

    private Connection connection;
    private MenuPanel menuPanel;
    private Ingredients ingredients;
    public Map<Drinks, JPanel> drinkPanelMap;
    private JTable ingredientTable;
    private JScrollPane ingredientTableScrollPane;
    private IngredientTableModel ingredientTableModel;
    private List<Drinks> drinksList;
    

    public CoffeeShopWindow() {
        try {
            this.connection = DriverManager.getConnection(App.DATABASE_URL, App.DATABASE_USERNAME, App.DATABASE_PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        ingredients = new Ingredients(connection);

        ingredientTable = new JTable(); // Create JTable instance only once
        ingredientTableScrollPane = new JScrollPane(ingredientTable);
        
        ingredients = new Ingredients(connection);
        ingredientTableModel = new IngredientTableModel(ingredients.getIngredientList()); // Initialize HERE
        ingredientTable.setModel(ingredientTableModel);
        IngredientCellRenderer cellRenderer = new IngredientCellRenderer(ingredientTableModel);
        ingredientTable.setDefaultRenderer(Object.class, cellRenderer);

        loadInventory();

        menuPanel = new MenuPanel(ingredients, this);
        drinkPanelMap = new HashMap<>();
        drinksList = new ArrayList<>();
        drinksList = loadDrinksFromDatabase();

        Drinks.allDrinks.clear();
        Drinks.allDrinks.addAll(drinksList);
        addDrinksToMenu();
        updateMenu();

        addDrinksToDatabase();

        JTabbedPane tabbedPane = new JTabbedPane();
        JPanel inventoryPanel = createInventoryPanel();
        JPanel drinksPanel = createDrinksPanel();
    
        setTitle("Coffee Shop");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
        tabbedPane.addTab("Menu", menuPanel);
        tabbedPane.addTab("Inventory", inventoryPanel);
        tabbedPane.addTab("Drinks", drinksPanel);
        add(tabbedPane); // Add tabbedPane to the JFrame
    
        try {
            Image icon = ImageIO.read(new File("C:\\Users\\altan\\Desktop\\Java code\\Coffee Shop\\CoffeeShop\\res\\GDAS.png")); // Replace with your image file
            setIconImage(icon);
        } catch (IOException e) {
            System.err.println("Error loading icon image: " + e.getMessage());
        }
        ingredientTable.setDefaultRenderer(Object.class, cellRenderer);
    }

    // CoffeeShopWindow.java

public Ingredients getIngredients() {  // Add this getter
    return ingredients;
}


    private void addDrinksToMenu() {
        for (Drinks drink : drinksList) { //Add drinks from the database
            menuPanel.addDrink(drink);
        }
    }

    private List<Drinks> loadDrinksFromDatabase() {
        List<Drinks> drinks = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(App.DATABASE_URL, App.DATABASE_USERNAME, App.DATABASE_PASSWORD);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM drinks")) {

            while (resultSet.next()) {
                int drinkId = resultSet.getInt("id");
                String name = resultSet.getString("name");
                double price = resultSet.getDouble("price");
                int rating = resultSet.getInt("rating");
                int sweetness = resultSet.getInt("sweetness");
                Drinks.DrinkType type = Drinks.DrinkType.valueOf(resultSet.getString("drink_type"));

                // Load ingredients for this drink
                Map<String, Double> ingredients = loadDrinkIngredients(connection, drinkId);

                Drinks drink = new Drinks(name, ingredients, "", price, rating, sweetness, type);
                drinks.add(drink);
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception appropriately
        }

        return drinks;
    }

    private Map<String, Double> loadDrinkIngredients(Connection connection, int drinkId) throws SQLException {
        Map<String, Double> ingredients = new HashMap<>();

        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT ingredient_name, quantity FROM drink_ingredients WHERE drink_id = ?")) {

            statement.setInt(1, drinkId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String ingredientName = resultSet.getString("ingredient_name");
                    double quantity = resultSet.getDouble("quantity");
                    ingredients.put(ingredientName, quantity);
                }
            }
        }
        return ingredients;
    }

    private JPanel createInventoryPanel() {
        JPanel inventoryPanel = new JPanel(new BorderLayout());
        inventoryPanel.add(ingredientTableScrollPane, BorderLayout.CENTER);

        // Create buttons
        JButton togglebutton = new JButton("Toggle Ingredient");
        JButton removeButton = new JButton("Remove Ingredient");
        JButton modifyButton = new JButton("Modify Ingredient");
        JButton createButton = new JButton("Create Ingredient");

        // Add button listeners (implement these methods)
        togglebutton.addActionListener(new ActionListener() {
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
        buttonPanel.add(togglebutton);
        buttonPanel.add(removeButton);
        buttonPanel.add(modifyButton);
        buttonPanel.add(createButton);

        // Add button panel to the south of inventory panel
        inventoryPanel.add(buttonPanel, BorderLayout.SOUTH);

        return inventoryPanel;
    }

    private void showToggleInStockDialog() {
        int selectedRow = ingredientTable.getSelectedRow();
        int selectedColumn = ingredientTable.getSelectedColumn();

        if (selectedRow == -1 || selectedColumn == -1) {
            JOptionPane.showMessageDialog(this, "Please select an ingredient to toggle.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
    
        String ingredientName = (String) ingredientTable.getValueAt(selectedRow, selectedColumn);
    
        if (ingredientName == null || ingredientName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a valid ingredient.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean currentInStock = ingredients.isInStock(ingredientName);
        boolean newInStock = !currentInStock;
    
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE user.ingredients SET inStock = ? WHERE ingredients = ?")) {
    
            statement.setInt(1, newInStock ? 1 : 0);
            statement.setString(2, ingredientName);
            statement.executeUpdate();
    
            String ingredientType = ingredients.getIngredientList().get(ingredientName).getType();
            ingredients.updateIngredientStock(ingredientName, newInStock, ingredientType);
    
            updateInventoryTable(); // This is correct and important!
            ingredientTable.repaint();
    
            updateMenu(); // Refresh the menu
    
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    

    private void showRemoveIngredientDialog() {
        int selectedRow = ingredientTable.getSelectedRow();
        int selectedColumn = ingredientTable.getSelectedColumn(); // Get the selected column
    
        if (selectedRow == -1 || selectedColumn == -1) { // Check both row and column selection
            JOptionPane.showMessageDialog(this, "Please select an ingredient to remove.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
    
        String ingredientName = (String) ingredientTable.getValueAt(selectedRow, selectedColumn); // Get value from the correct cell
    
    
        if (ingredientName == null || ingredientName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a valid ingredient.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
    
        int option = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to remove " + ingredientName + "?",
                "Confirm Removal",
                JOptionPane.YES_NO_OPTION);
    
        if (option == JOptionPane.YES_OPTION) {
            String ingredientType = ingredients.getIngredientList().get(ingredientName).getType();
            int ingredientsOfTypeCount = 0;
            for (Ingredients.IngredientData data : ingredients.getIngredientList().values()) {
                if (data.getType().equals(ingredientType)) {
                    ingredientsOfTypeCount++;
                }
            }

            if (ingredientsOfTypeCount == 1) {
                JOptionPane.showMessageDialog(this, "Cannot remove the last ingredient of this type.", "Error", JOptionPane.ERROR_MESSAGE);
                return; // Don't proceed with removal
            }

            try (PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM user.ingredients WHERE ingredients = ?")) {
                statement.setString(1, ingredientName);
                statement.executeUpdate();
    
                ingredients.getIngredientList().remove(ingredientName); // Update Ingredients object
                updateInventoryTable();
                updateMenu();
    
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void showModifyIngredientDialog() {
        int selectedRow = ingredientTable.getSelectedRow();
        int selectedColumn = ingredientTable.getSelectedColumn();
    
        if (selectedRow == -1 || selectedColumn == -1) {
            JOptionPane.showMessageDialog(this, "Please select an ingredient to modify.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
    
        String oldIngredientName = (String) ingredientTable.getValueAt(selectedRow, selectedColumn);
    
        if (oldIngredientName == null || oldIngredientName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a valid ingredient.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
    
        String[] ingredientTypes = new String[ingredientTableModel.getColumnCount()];
        for (int i = 0; i < ingredientTypes.length; i++) {
            ingredientTypes[i] = ingredientTableModel.getColumnName(i);
        }

        JTextField ingredientNameField = new JTextField(oldIngredientName, 20);  // Pre-fill with old name
        String oldType = ingredients.getIngredientList().get(oldIngredientName).getType();
        JComboBox<String> typeComboBox = new JComboBox<>(ingredientTypes);
        typeComboBox.setSelectedItem(oldType);
        
    
        Object[] message = {
            "New Ingredient Name:", ingredientNameField,
            "Type:", typeComboBox  // Include type selection
        };
    
        int option = JOptionPane.showConfirmDialog(this, message, "Modify Ingredient", JOptionPane.OK_CANCEL_OPTION);
    
        if (option == JOptionPane.OK_OPTION) {
            String newIngredientName = ingredientNameField.getText();
            String newType = (String) typeComboBox.getSelectedItem();
    
            int ingredientsOfOldTypeCount = 0;
            for (Ingredients.IngredientData data : ingredients.getIngredientList().values()) {
                if (data.getType().equals(oldType)) {
                    ingredientsOfOldTypeCount++;
                }
            }

            if (ingredientsOfOldTypeCount == 1 && !newType.equals(oldType)) {
                JOptionPane.showMessageDialog(this, "Cannot change the type of the last ingredient of this type.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
    
            if (newIngredientName.isEmpty() || newType == null) {
                JOptionPane.showMessageDialog(this, "Ingredient name cannot be empty and type must be selected.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
    
            try {
                // 1. Update the database
                try (PreparedStatement statement = connection.prepareStatement(
                        "UPDATE user.ingredients SET ingredients = ?, type = ? WHERE ingredients = ?")) {
                    statement.setString(1, newIngredientName);
                    statement.setString(2, newType);
                    statement.setString(3, oldIngredientName);
                    statement.executeUpdate();
                }
    
                // 2. Update the Ingredients object (Important: Use the *new* type!)
                Ingredients.IngredientData ingredientData = ingredients.getIngredientList().get(oldIngredientName);
                if (ingredientData != null) { // check if ingredient exists in ingredientMap before updating its values
                    ingredients.getIngredientList().remove(oldIngredientName);
                    ingredientData.type = newType;
                    ingredients.getIngredientList().put(newIngredientName, ingredientData); 
                } else {
                    //Handle the error as necessary, the ingredient does not exist in the map for some reason
                }
    
    
    
                // 3. Update the table model
                updateInventoryTable();
    
                // 4. Update the menu (important!)
                updateMenu();
    
    
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void showCreateIngredientDialog() {
        JTextField ingredientNameField = new JTextField(20);
        JCheckBox inStockCheckBox = new JCheckBox("In Stock");
    
        String[] ingredientTypes = new String[ingredientTableModel.getColumnCount()];
    for (int i = 0; i < ingredientTypes.length; i++) {
        ingredientTypes[i] = ingredientTableModel.getColumnName(i);
    }
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

    private void showCreateDrinkDialog() {
        JTextField nameField = new JTextField(20);
        JTextField priceField = new JTextField(10);
        JSpinner ratingSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 5, 1)); // Rating 1-5
        JSlider sweetnessSlider = new JSlider(JSlider.HORIZONTAL, 1, 5, 3); // Sweetness 1-5, default 3
        JComboBox<Drinks.DrinkType> typeComboBox = new JComboBox<>(Drinks.DrinkType.values());
        DefaultListModel<String> ingredientListModel = new DefaultListModel<>();
        JList<String> ingredientList = new JList<>(ingredientListModel);
        JTextField quantityField = new JTextField(5);
        JButton addIngredientButton = new JButton("Add Ingredient");
        JButton removeIngredientButton = new JButton("Remove Ingredient");

        Map<String, Double> selectedIngredients = new HashMap<>();

        // Populate ingredientList with available ingredients from the database
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT ingredients FROM user.ingredients")) {
            while (resultSet.next()) {
                ingredientListModel.addElement(resultSet.getString("ingredients"));
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception
        }

        addIngredientButton.addActionListener(e -> {
            String selectedIngredient = ingredientList.getSelectedValue();
            try {
                double quantity = Double.parseDouble(quantityField.getText());
                if (selectedIngredient != null) {
                    selectedIngredients.put(selectedIngredient, quantity);
                    // Update a display area (e.g., a text area) to show selected ingredients
                    // ...
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid quantity. Please enter a number.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });


        removeIngredientButton.addActionListener(e -> {
            String selectedIngredient = ingredientList.getSelectedValue();
            if (selectedIngredient != null) {
                selectedIngredients.remove(selectedIngredient);
                // Update the display area to reflect removal
                // ...
            }
        });



        Object[] message = {
                "Name:", nameField,
                "Price:", priceField,
                "Rating (1-5):", ratingSpinner,
                "Sweetness (1-5):", sweetnessSlider,
                "Type:", typeComboBox,
                "Ingredients:", new JScrollPane(ingredientList),
                "Quantity:", quantityField,
                addIngredientButton, removeIngredientButton
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Create New Drink", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            try {
                String name = nameField.getText();
                double price = Double.parseDouble(priceField.getText());
                int rating = (int) ratingSpinner.getValue();
                int sweetness = sweetnessSlider.getValue();
                Drinks.DrinkType type = (Drinks.DrinkType) typeComboBox.getSelectedItem();

                if (name.isEmpty() || selectedIngredients.isEmpty()) {  // Validation
                    JOptionPane.showMessageDialog(this, "Drink name and ingredients cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Drinks newDrink = new Drinks(name, selectedIngredients, "", price, rating, sweetness, type); // Simplified constructor

                // Add drink to the database
                addDrinkToDatabase(connection, newDrink);


                drinksList.add(newDrink); //Update the DrinksList
                Drinks.allDrinks.add(newDrink);

                updateMenu(); //Important for updating the menu

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid price. Please enter a number.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void addDrinksToDatabase() {
        try (Connection connection = DriverManager.getConnection(App.DATABASE_URL, App.DATABASE_USERNAME, App.DATABASE_PASSWORD)) {
            for (Drinks drink : drinksList) {
                addDrinkToDatabase(connection, drink);
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle exception appropriately
        }
    }

    private void addDrinkIngredientsToDatabase(Connection connection, int drinkId, Map<String, Double> ingredients) throws SQLException {
        String insertIngredientsSQL = "INSERT INTO drink_ingredients (drink_id, ingredient_name, quantity) VALUES (?, ?, ?)";
        try (PreparedStatement ingredientStatement = connection.prepareStatement(insertIngredientsSQL)) {
            for (Map.Entry<String, Double> ingredientEntry : ingredients.entrySet()) {
                ingredientStatement.setInt(1, drinkId);
                ingredientStatement.setString(2, ingredientEntry.getKey());
                ingredientStatement.setDouble(3, ingredientEntry.getValue());
                ingredientStatement.executeUpdate();
            }
        } // The try-with-resources automatically closes ingredientStatement
    }

    private void addDrinkToDatabase(Connection connection, Drinks drink) throws SQLException {
        try {
            connection.setAutoCommit(false); // Start transaction

            // 1. Insert into drinks table
            String insertDrinkSQL = "INSERT INTO drinks (name, price, rating, sweetness, drink_type) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(insertDrinkSQL, Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, drink.getName());
                statement.setDouble(2, drink.getPrice());
                statement.setInt(3, drink.getRating());
                statement.setInt(4, drink.getSweetness());
                statement.setString(5, drink.getType().toString());
                statement.executeUpdate();

                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int drinkId = generatedKeys.getInt(1);
                        addDrinkIngredientsToDatabase(connection, drinkId, drink.getIngredients()); // Call like this
                    }
                }
            }

            connection.commit(); // Commit transaction
        } catch (SQLException e) {
            connection.rollback(); // Rollback on error
            throw e; // Re-throw the exception to be handled by the caller
        } finally {
            connection.setAutoCommit(true); // Restore auto-commit
        }
    }

    private JPanel createDrinksPanel(){
        JPanel createDrinkPanel = new JPanel(new BorderLayout());
        JButton createDrinkButton = new JButton("Create New Drink");
        createDrinkButton.addActionListener(e -> showCreateDrinkDialog());
        createDrinkPanel.add(createDrinkButton, BorderLayout.SOUTH);
        return createDrinkPanel;

    }



    private void loadInventory() {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT ingredients, type, inStock FROM user.ingredients")) { // Include inStock
    
            Map<String, Ingredients.IngredientData> dbIngredients = new HashMap<>();
            while (resultSet.next()) {
                String name = resultSet.getString("ingredients");
                String type = resultSet.getString("type");
                boolean inStock = resultSet.getInt("inStock") == 1; // Get inStock value from DB
                dbIngredients.put(name, new Ingredients.IngredientData(inStock, type));
            }
    
            // Add or update ingredients from the database to the Ingredients object
            for (Map.Entry<String, Ingredients.IngredientData> entry : dbIngredients.entrySet()) {
                String ingredientName = entry.getKey();
                Ingredients.IngredientData data = entry.getValue();
                ingredients.getIngredientList().put(ingredientName, data); // Use put to add/update correctly
            }
    
            // Now handle ingredients from drinks that might not be in the database
            for (Drinks drink : Drinks.getAllDrinks()) {
                for (Map.Entry<String, Double> entry : drink.getIngredients().entrySet()) {
                    String ingredientName = entry.getKey();
                    if (!ingredients.getIngredientList().containsKey(ingredientName)) { 
                        String type = getIngredientTypeFromDrink(drink, ingredientName); //Still needed if ingredient wasn't loaded from the database initially
                        ingredients.addIngredient(ingredientName, true, type); //Or false, depending on your intended default.
                    }
                }
            }

            updateInventoryTable(); // Update the table model after loading all ingredients.
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
    
    private void updateInventoryTable() {
        // Get the updated ingredients map
        Map<String, Ingredients.IngredientData> ingredientMap = ingredients.getIngredientList();
        //Update both the table data AND the stock map of the model.
        ingredientTableModel.updateIngredients(ingredientMap);

        ingredientTable.repaint(); // Needed to refresh the display
    }

    private void updateMenu() { 
        menuPanel.updateMenu(ingredients); // Call MenuPanel's updateMenu
    }
    
}