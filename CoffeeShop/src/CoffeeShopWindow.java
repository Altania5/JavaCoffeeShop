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
    private MenuPanel menuPanel; // Store reference to MenuPanel
    private Ingredients ingredients;
    public Map<Drinks, JPanel> drinkPanelMap;
    private DefaultListModel<String> ingredientListModel;

    public CoffeeShopWindow() {

                try {
            this.connection = DriverManager.getConnection("jdbc:mysql://45.62.14.188:3306/users", "altan", "Pickles5-_");
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle connection errors appropriately (e.g., display an error message)
        }

        // Now create the Ingredients object, passing the connection
        ingredients = new Ingredients(connection); 

        JTabbedPane tabbedPane = new JTabbedPane();
        ingredientListModel = new DefaultListModel<>();
        JPanel inventoryPanel = createInventoryPanel(ingredientListModel);
        loadInventory();
        menuPanel = new MenuPanel(ingredients);
        drinkPanelMap = new HashMap<>(); // Initialize the map
    
        setTitle("Coffee Shop");
        setSize(800, 600);
        setLocationRelativeTo(null); // Center the window
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
        // *** Create and add the tabbed pane FIRST ***
        tabbedPane.addTab("Menu", menuPanel);
        tabbedPane.addTab("Inventory", inventoryPanel);
        add(tabbedPane); // Add tabbedPane to the JFrame
    
        try {
            Image icon = ImageIO.read(new File("C:\\Users\\altan\\Desktop\\Java code\\Coffee Shop\\CoffeeShop\\res\\GDAS.png")); // Replace with your image file
            setIconImage(icon);
        } catch (IOException e) {
            System.err.println("Error loading icon image: " + e.getMessage());
            // Handle the error appropriately (e.g., log it, show a message)
        }
    }

    private void loadInventory() {
        File inventoryFile = new File("inventory.txt"); // File to store inventory

        if (inventoryFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(inventoryFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length == 2) {
                        String ingredientName = parts[0];
                        boolean inStock = Boolean.parseBoolean(parts[1]);
                        ingredients.addIngredient(ingredientName, inStock);
                    }
                }
            } catch (IOException e) {
                // System.err.println("Error loading inventory: " + e.getMessage());
                // Handle the error appropriately (e.g., log it, show a message)
            }
        }

        // Update the ingredient list in the UI after loading
        updateIngredientList(ingredientListModel); 
    }

    private void saveInventory() {
        File inventoryFile = new File("inventory.txt");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(inventoryFile))) {
            for (Map.Entry<String, Boolean> entry : ingredients.getIngredientList().entrySet()) {
                writer.write(entry.getKey() + "," + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving inventory: " + e.getMessage());
            // Handle the error appropriately (e.g., log it, show a message)
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

        // Create a list to display ingredients
        for (Map.Entry<String, Boolean> entry : ingredients.getIngredientList().entrySet()) {
            String ingredientStatus = entry.getKey() + " - " + (entry.getValue() ? "In Stock" : "Out of Stock");
            ingredientListModel.addElement(ingredientStatus);
        }
        JList<String> ingredientList = new JList<>(ingredientListModel);
        JScrollPane ingredientListScrollPane = new JScrollPane(ingredientList);
        inventoryPanel.add(ingredientListScrollPane, BorderLayout.CENTER);

        // Create buttons for adding and removing ingredients
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addButton = new JButton("Add Ingredient");
        JButton removeButton = new JButton("Remove Ingredient");
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        inventoryPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Add action listeners to buttons
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newIngredient = ingredientList.getSelectedValue();
                if (newIngredient != null) {
                    String ingredientNames = newIngredient.split(" - ")[0];
                    ingredients.updateIngredientStock(ingredientNames, true); // Add as in stock by default
                    updateIngredientList(ingredientListModel);
                    updateMenu();
                }
            }
        });

        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedIngredient = ingredientList.getSelectedValue();
                if (selectedIngredient != null) {
                    String ingredientName = selectedIngredient.split(" - ")[0];
                    ingredients.updateIngredientStock(ingredientName, false); // Set to out of stock
                    updateIngredientList(ingredientListModel);
                    updateMenu();
                }
            }
        });

        return inventoryPanel;
    }

    private void updateIngredientList(DefaultListModel<String> model) {
        model.clear();
        for (Map.Entry<String, Boolean> entry : ingredients.getIngredientList().entrySet()) {
            String ingredientStatus = entry.getKey() + " - " + (entry.getValue() ? "In Stock" : "Out of Stock");
            model.addElement(ingredientStatus);
        }
    }

    private void updateMenu() {
        menuPanel.removeAll(); // Remove all existing drink panels
        // Add back drinks that can be made
        for (Drinks drink : Drinks.getAllDrinks()) {
            Drinks.CheckResult result = drink.canMake(ingredients);
            if (result.canMake) {
                menuPanel.addDrink(drink); 
            }
        }
        menuPanel.revalidate();
        menuPanel.repaint();
        saveInventory();
    }
}