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
    private DefaultListModel<String> ingredientListModel;

    public CoffeeShopWindow() {

                try {
                    this.connection = DriverManager.getConnection(App.DATABASE_URL, App.DATABASE_USERNAME, App.DATABASE_PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        ingredients = new Ingredients(connection); 

        JTabbedPane tabbedPane = new JTabbedPane();
        ingredientListModel = new DefaultListModel<>();
        JPanel inventoryPanel = createInventoryPanel(ingredientListModel);
        loadInventory();
        menuPanel = new MenuPanel(ingredients);
        drinkPanelMap = new HashMap<>();
    
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
    }

    private void loadInventory() {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM user.ingredients")) {
            while (resultSet.next()) {
                String ingredientName = resultSet.getString("ingredients"); // Corrected column name
                boolean inStock = resultSet.getInt("inStock") == 1;
                ingredients.addIngredient(ingredientName, inStock);
            }
        } catch (SQLException e) {
            System.err.println("Error loading inventory from database: " + e.getMessage());
        }
        updateIngredientList(ingredientListModel);
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

    private JPanel createInventoryPanel(DefaultListModel<String> ingredientListModel) {
        JPanel inventoryPanel = new JPanel(new BorderLayout());
    
        updateIngredientList(ingredientListModel); // Load initial data
    
        JList<String> ingredientList = new JList<>(ingredientListModel);
        JScrollPane ingredientListScrollPane = new JScrollPane(ingredientList);
        inventoryPanel.add(ingredientListScrollPane, BorderLayout.CENTER);

        loadInventory();

        menuPanel = new MenuPanel(ingredients);
        drinkPanelMap = new HashMap<>();
    
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addButton = new JButton("Add Ingredient");
        JButton removeButton = new JButton("Remove Ingredient");
        JButton createNewButton = new JButton("Create New Ingredient");
    
    
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedIngredient = ingredientList.getSelectedValue();
                if (selectedIngredient != null) {
                    String ingredientName = selectedIngredient.split(" - ")[0];
                    ingredients.updateIngredientStock(ingredientName, true, ingredients.getType()); // Update in DB
                    updateIngredientList(ingredientListModel); // Refresh the list
                    updateMenu(); // Update menu based on inventory
                }
            }
        });
    
        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedIngredient = ingredientList.getSelectedValue();
                if (selectedIngredient != null) {
                    String ingredientName = selectedIngredient.split(" - ")[0];
                    ingredients.updateIngredientStock(ingredientName, false, ingredients.getType()); // Update in DB
                    updateIngredientList(ingredientListModel);  // Refresh the list
                    updateMenu();  // Update menu based on inventory
                }
            }
        });

        createNewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showCreateIngredientDialog();
            }
        });
    
    
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(createNewButton);
        inventoryPanel.add(buttonPanel, BorderLayout.SOUTH);
    
        return inventoryPanel;
    }
    
    private void showCreateIngredientDialog() {
        JTextField ingredientNameField = new JTextField(20);
        JCheckBox inStockCheckBox = new JCheckBox("In Stock");
        //Ingredient Type Combo box
        String[] ingredientTypes = {"Espresso Bean", "Milk", "Syrup", "Other"}; // Replace with your types
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
            String type = (String) typeComboBox.getSelectedItem(); // Get selected type
    
            if (!ingredientName.isEmpty()) {
                ingredients.addIngredient(ingredientName, inStock, type); // Add type to addIngredient call
                updateIngredientList(ingredientListModel);
                updateMenu(); // Important!
            } else {
                // ... (Error message for empty name)
            }
        }
    }
    

    private void updateIngredientList(DefaultListModel<String> model) {
        model.clear();
        for (Map.Entry<String, Ingredients.IngredientData> entry : ingredients.getIngredientList().entrySet()) {
            String ingredientStatus = String.format("%s - %s - %s", 
                                                    entry.getKey(), 
                                                    entry.getValue().isInStock() ? "In Stock" : "Out of Stock",  // Use getter
                                                    entry.getValue().getType());  // Use getter
            model.addElement(ingredientStatus);
        }
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