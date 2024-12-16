import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.ArrayList;
import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

class CreateDrinkDialog extends JDialog {

    private JTextField nameField;
    private JTextField priceField;
    private JSpinner ratingSpinner;
    private JSlider sweetnessSlider;
    private JComboBox<Drinks.DrinkType> typeComboBox;
    private DefaultListModel<String> ingredientListModel;
    private JList<String> ingredientList;
    private JTextField quantityField;
    private Map<String, Double> selectedIngredients;
    private Connection connection;
    private DefaultListModel<String> selectedIngredientListModel;
    private JList<String> selectedIngredientList;

    public CreateDrinkDialog(CoffeeShopWindow parent, Connection connection) {
        super(parent, "Create Drink", true);
        this.connection = connection;

        setLayout(new BorderLayout());
        selectedIngredients = new HashMap<>();

        // Initialize UI components
        nameField = new JTextField(20);
        priceField = new JTextField(10);
        ratingSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 5, 1));
        sweetnessSlider = new JSlider(JSlider.HORIZONTAL, 1, 5, 3);
        typeComboBox = new JComboBox<>(Drinks.DrinkType.values());
        ingredientListModel = new DefaultListModel<>();
        ingredientList = new JList<>(ingredientListModel);
        quantityField = new JTextField(5);
        selectedIngredientListModel = new DefaultListModel<>();
        selectedIngredientList = new JList<>(selectedIngredientListModel);

        // Populate ingredientList
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT ingredients FROM user.ingredients")) {
            while (resultSet.next()) {
                String ingredientName = resultSet.getString("ingredients");
                ingredientListModel.addElement(ingredientName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
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

    // ... [createInputPanel(), createIngredientPanel(), createButtonPanel()] ...

    private JPanel createInputPanel() {
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
    
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

    private JButton createAddIngredientButton() {
        JButton addIngredientButton = new JButton("Add Ingredient");
        addIngredientButton.addActionListener(e -> {
            String selectedIngredient = ingredientList.getSelectedValue();
            try {
                double quantity = Double.parseDouble(quantityField.getText());
    
                // --- The fix is here ---
                if (selectedIngredient != null) {
                    if (selectedIngredients.containsKey(selectedIngredient)) {
                        // If the ingredient is already in the map, update its quantity
                        double currentQuantity = selectedIngredients.get(selectedIngredient);
                        selectedIngredients.put(selectedIngredient, currentQuantity + quantity);
    
                        // Update the selectedIngredientList
                        updateIngredientsDisplay(); // This will refresh the list with the new quantity
                    } else {
                        // If the ingredient is not in the map, add it
                        selectedIngredients.put(selectedIngredient, quantity);
    
                        // Move ingredient from ingredientList to selectedIngredientList
                        ingredientListModel.removeElement(selectedIngredient);
                        selectedIngredientListModel.addElement(selectedIngredient + ": " + quantity);
                    }
    
                    quantityField.setText("");
                }
                // --- End of fix ---
    
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid quantity. Please enter a number.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        return addIngredientButton;
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
        try {
            double price = Double.parseDouble(priceField.getText()); // This line might throw NumberFormatException
            int rating = (int) ratingSpinner.getValue();
            int sweetness = sweetnessSlider.getValue();
            Drinks.DrinkType type = (Drinks.DrinkType) typeComboBox.getSelectedItem();
    
            if (name.isEmpty() || selectedIngredients.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Drink name and ingredients cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
    
            Drinks newDrink = new Drinks(0, name, selectedIngredients, "", price, rating, sweetness, type); 
    
            // Get the parent window (CoffeeShopWindow)
            CoffeeShopWindow parent = (CoffeeShopWindow) getParent(); 
    
            // Add drink to the database
            parent.addDrinkToDatabase(parent.connection, newDrink); 
    
            // Update the drinks list and refresh the UI
            parent.drinksList.add(newDrink);
            parent.updateMenu();
            parent.refreshDrinksPanel(); 
            parent.notifyDrinkListListeners();
    
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid price. Please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}