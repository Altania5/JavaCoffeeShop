import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class MenuPanel extends JPanel implements DrinkListListener{
    private Map<Drinks, JPanel> drinkPanelMap; // Added to store drink panels
    private CoffeeShopWindow coffeeShopWindow;

    public MenuPanel(Ingredients ingredients, CoffeeShopWindow window) {
        this.drinkPanelMap = new HashMap<>(); //
        this.coffeeShopWindow = window;

        // Set layout (we'll use GridLayout for simplicity)
        setLayout(new GridLayout(0, 2, 10, 10)); // 2 columns, variable rows, 10px spacing
        window.addDrinkListListener(this);
    }

    public void addDrink(Drinks drink) {  // Modified to prevent duplicates
        if (!drinkPanelMap.containsKey(drink)) {  // Only create a new panel if one doesn't exist
            JPanel drinkPanel = new JPanel(new BorderLayout());
            drinkPanelMap.put(drink, drinkPanel);
            add(drinkPanel);
            drinkPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        }

        JPanel drinkPanel = drinkPanelMap.get(drink); // Get the existing panel

        Drinks.CheckResult result = drink.canMake(coffeeShopWindow.getIngredients());

        JLabel nameLabel = new JLabel(drink.getName()); // Add name label
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD));
        drinkPanel.add(nameLabel, BorderLayout.NORTH);

        JPanel priceRatingPanel = new JPanel(new GridLayout(2, 1));
        JLabel priceLabel = new JLabel("$" + String.format("%.2f", drink.getPrice())); // Add price
        priceRatingPanel.add(priceLabel);
    
        JLabel ratingLabel = new JLabel("Rating: " + drink.getRating() + "/5"); // Add rating
        priceRatingPanel.add(ratingLabel);
    
        drinkPanel.add(priceRatingPanel, BorderLayout.CENTER);

        if (!result.canMake) {
            JLabel unavailableLabel = new JLabel("Sold Out: " + result.missingIngredient); // Add sold out status
            unavailableLabel.setForeground(Color.RED);
            drinkPanel.add(unavailableLabel, BorderLayout.SOUTH);
        }

        revalidate();
        repaint();
    }

    public void updateMenu(Ingredients ingredients) { 
        // This method now only refreshes the availability status of existing drinks
        for (Drinks drink : drinkPanelMap.keySet()) {
            Drinks.CheckResult result = drink.canMake(ingredients);
            JPanel drinkPanel = drinkPanelMap.get(drink);

            // Find and update the "Sold Out" label if it exists
            for (Component component : drinkPanel.getComponents()) {
                if (component instanceof JLabel && ((JLabel) component).getText().startsWith("Sold Out: ")) {
                    if (!result.canMake) {
                        ((JLabel) component).setText("Sold Out: " + result.missingIngredient);
                    } else {
                        drinkPanel.remove(component); // Remove the label if the drink is available
                    }
                    break; 
                }
            }

            drinkPanel.revalidate();
            drinkPanel.repaint();
        }
        revalidate();
        repaint();
    }

    @Override
    public void onDrinkListUpdated() {
    try (Connection connection = DriverManager.getConnection(App.DATABASE_URL, App.DATABASE_USERNAME, App.DATABASE_PASSWORD)) {
        this.removeAll();
        List<Drinks> updatedDrinksList = Drinks.getDrinksFromDatabase(connection); // Get from DB
        for (Drinks drink : updatedDrinksList) {
            this.addDrink(drink);
        }
        this.revalidate();
        this.repaint();
        } catch (SQLException e) {
            e.printStackTrace(); //Handle appropriately
            JOptionPane.showMessageDialog(this, "Error loading drinks from database", "Error", JOptionPane.ERROR_MESSAGE);
        }
}
}