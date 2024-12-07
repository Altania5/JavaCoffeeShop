import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import java.util.ArrayList;
import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

class EditDrinkDialog extends JDialog {

    private JTextField nameField;
    private JTextField priceField;
    private JSpinner ratingSpinner;
    private JSlider sweetnessSlider;
    private JComboBox<Drinks.DrinkType> typeComboBox;
    private DefaultListModel<String> ingredientListModel;
    private JList<String> ingredientList;
    private JTextField quantityField;
    private JTextArea ingredientsDisplay;
    private Map<String, Double> selectedIngredients;
    private Connection connection;
    private Drinks drink;
    private DefaultListModel<String> selectedIngredientListModel;
    private JList<String> selectedIngredientList;

    public EditDrinkDialog(CoffeeShopWindow parent, Drinks drink, Connection connection) {
        super(parent, "Edit Drink", true);
        this.connection = connection;
        this.drink = drink; // Store the drink

        setLayout(new BorderLayout());
        selectedIngredients = new HashMap<>(drink.getIngredients());

        // Initialize UI components
        nameField = new JTextField(drink.getName(), 20);
        priceField = new JTextField(String.valueOf(drink.getPrice()), 10);
        ratingSpinner = new JSpinner(new SpinnerNumberModel(drink.getRating(), 1, 5, 1));
        sweetnessSlider = new JSlider(JSlider.HORIZONTAL, 1, 5, drink.getSweetness());
        typeComboBox = new JComboBox<>(Drinks.DrinkType.values());
        typeComboBox.setSelectedItem(drink.getType());
        ingredientListModel = new DefaultListModel<>();
        ingredientList = new JList<>(ingredientListModel);
        quantityField = new JTextField(5);
        ingredientsDisplay = new JTextArea(10, 20);
        selectedIngredientListModel = new DefaultListModel<>();
        selectedIngredientList = new JList<>(selectedIngredientListModel);

        // Populate ingredientList and ingredientsDisplay
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT ingredients FROM user.ingredients")) {
            while (resultSet.next()) {
                String ingredientName = resultSet.getString("ingredients");
                if (!selectedIngredients.containsKey(ingredientName)) { // Check if already selected
                    ingredientListModel.addElement(ingredientName);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        for (Map.Entry<String, Double> entry : selectedIngredients.entrySet()) {
            selectedIngredientListModel.addElement(entry.getKey() + ": " + entry.getValue());
        }

        // Create and add panels
        setLayout(new BorderLayout(10, 10));
        add(createInputPanel(), BorderLayout.NORTH);
        add(createIngredientPanel(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);

        ((JPanel) getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));

        pack();
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }

    private JPanel createInputPanel() {
        JPanel inputPanel = new JPanel(new GridBagLayout()); // Use GridBagLayout
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Add spacing around components

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        inputPanel.add(new JLabel("Name:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        inputPanel.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        inputPanel.add(new JLabel("Price:"), gbc);

        gbc.gridx = 1;
        inputPanel.add(priceField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        inputPanel.add(new JLabel("Rating (1-5):"), gbc);

        gbc.gridx = 1;
        inputPanel.add(ratingSpinner, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        inputPanel.add(new JLabel("Sweetness (1-5):"), gbc);

        gbc.gridx = 1;
        inputPanel.add(sweetnessSlider, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        inputPanel.add(new JLabel("Type:"), gbc);

        gbc.gridx = 1;
        inputPanel.add(typeComboBox, gbc);

        return inputPanel;
    }

    private JPanel createIngredientPanel() {
        JPanel ingredientPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        ingredientPanel.add(new JLabel("Selected Ingredients:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2; 
        gbc.fill = GridBagConstraints.HORIZONTAL;
        ingredientPanel.add(new JScrollPane(selectedIngredientList), gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        ingredientPanel.add(new JLabel("Ingredient:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        ingredientPanel.add(new JScrollPane(ingredientList), gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        ingredientPanel.add(new JLabel("Quantity:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 1;
        ingredientPanel.add(quantityField, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        ingredientPanel.add(createAddIngredientButton(), gbc);

        gbc.gridx = 2;
        gbc.gridy = 3;
        ingredientPanel.add(createRemoveSelectedIngredientButton(), gbc); 

        return ingredientPanel;
    }

    private JButton createRemoveSelectedIngredientButton() {
        JButton removeIngredientButton = new JButton("Remove Ingredient");
        removeIngredientButton.addActionListener(e -> {
            int selectedIndex = selectedIngredientList.getSelectedIndex();
            if (selectedIndex != -1) {
                String selectedValue = selectedIngredientList.getSelectedValue();
                String[] parts = selectedValue.split(": ");
                String ingredientToRemove = parts[0];

                selectedIngredientListModel.remove(selectedIndex);
                selectedIngredients.remove(ingredientToRemove);

                // Move ingredient back to ingredientList
                ingredientListModel.addElement(ingredientToRemove); 
            }
        });
        return removeIngredientButton;
    }

    private JButton createAddIngredientButton() {
        JButton addIngredientButton = new JButton("Add Ingredient");
        addIngredientButton.addActionListener(e -> {
            String selectedIngredient = ingredientList.getSelectedValue();
            try {
                double quantity = Double.parseDouble(quantityField.getText());
                if (selectedIngredient != null) {
                    selectedIngredients.put(selectedIngredient, quantity);
                    updateIngredientsDisplay(); 
                    quantityField.setText("");

                    // Move ingredient from ingredientList to selectedIngredientList
                    ingredientListModel.removeElement(selectedIngredient);
                    selectedIngredientListModel.addElement(selectedIngredient + ": " + quantity);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid quantity. Please enter a number.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        return addIngredientButton;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            try {
                saveDrinkChanges();
                dispose(); // Close the dialog
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        buttonPanel.add(saveButton);
        return buttonPanel;
    }

    private void updateIngredientsDisplay() {
        selectedIngredientListModel.clear();

        // Use a TreeMap to store selected ingredients in alphabetical order
        Map<String, Double> sortedIngredients = new TreeMap<>(selectedIngredients);

        for (Map.Entry<String, Double> entry : sortedIngredients.entrySet()) {
            selectedIngredientListModel.addElement(entry.getKey() + ": " + entry.getValue());
        }

        updateIngredientList(); // This is the important call
    }

    private void updateIngredientList() {
        ArrayList<String> tempList = new ArrayList<>();

        // Get all ingredients from the database and add them to tempList
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT ingredients FROM user.ingredients")) {
            while (resultSet.next()) {
                tempList.add(resultSet.getString("ingredients"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Sort tempList alphabetically
        Collections.sort(tempList);

        ingredientListModel.clear();

        for (String ingredient : tempList) {
            if (!selectedIngredients.containsKey(ingredient)) {
                ingredientListModel.addElement(ingredient);
            }
        }
    }

    private void saveDrinkChanges() throws SQLException {
        String name = nameField.getText();
        double price = Double.parseDouble(priceField.getText());
        int rating = (int) ratingSpinner.getValue();
        int sweetness = sweetnessSlider.getValue();
        Drinks.DrinkType type = (Drinks.DrinkType) typeComboBox.getSelectedItem();

        if (name.isEmpty() || selectedIngredients.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Drink name and ingredients cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Update the drink object
        drink.setName(name);
        drink.setPrice(price);
        drink.setRating(rating);
        drink.setSweetness(sweetness);
        drink.setType(type);
        drink.setIngredients(selectedIngredients);

        // Update the drink in the database
        updateDrinkInDatabase(drink.getId(), name, price, rating, sweetness, type, selectedIngredients);
    }

    private void addDrinkIngredientsToDatabase(Connection connection, int drinkId, Map<String, Double> ingredients) throws SQLException {
        String insertIngredientsSQL = "INSERT INTO drink_ingredients (drink_id, ingredient_name, quantity) VALUES (?, ?, ?)";
        try (PreparedStatement ingredientStatement = connection.prepareStatement(insertIngredientsSQL)) {
            for (Map.Entry<String, Double> ingredientEntry : ingredients.entrySet()) {
                ingredientStatement.setInt(1, drinkId);
                ingredientStatement.setString(2, ingredientEntry.getKey());  // Check this
                ingredientStatement.setDouble(3, ingredientEntry.getValue()); // Check this
                ingredientStatement.executeUpdate();
            }
        }
    }

    private void updateDrinkInDatabase(int drinkId, String name, double price, int rating, int sweetness, Drinks.DrinkType type, Map<String, Double> ingredients) throws SQLException {
        try {
            connection.setAutoCommit(false); // Start transaction
    
            // 1. Update the drinks table
            String updateDrinkSQL = "UPDATE drinks SET name = ?, price = ?, rating = ?, sweetness = ?, drink_type = ? WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(updateDrinkSQL)) {
                statement.setString(1, name);
                statement.setDouble(2, price);
                statement.setInt(3, rating);
                statement.setInt(4, sweetness);
                statement.setString(5, type.toString());
                statement.setInt(6, drinkId);
                statement.executeUpdate();
            }
    
            // 2. Delete and re-insert ingredients (simplest approach for updates)
            String deleteIngredientsSQL = "DELETE FROM drink_ingredients WHERE drink_id = ?";
            try (PreparedStatement deleteStatement = connection.prepareStatement(deleteIngredientsSQL)) {
                deleteStatement.setInt(1, drinkId);
                deleteStatement.executeUpdate();
            }
    
            // 3. Insert new ingredients
            addDrinkIngredientsToDatabase(connection, drinkId, ingredients);
    
            connection.commit(); // Commit transaction
        } catch (SQLException e) {
            connection.rollback(); // Rollback on error
            throw e; // Re-throw the exception
        } finally {
            connection.setAutoCommit(true); // Restore auto-commit
        }
    }
}