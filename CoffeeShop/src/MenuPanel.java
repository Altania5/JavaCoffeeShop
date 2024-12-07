import javax.swing.*;
import java.awt.*;
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
            drinkPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            drinkPanelMap.put(drink, drinkPanel);
            add(drinkPanel);
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
        for (Drinks drink : drinkPanelMap.keySet()) { 
            Drinks.CheckResult result = drink.canMake(ingredients);
    
            JPanel drinkPanel = drinkPanelMap.get(drink);
            drinkPanel.removeAll(); 
    
            // *** Re-add components to the drink panel ***
    
            JLabel nameLabel = new JLabel(drink.getName()); 
            nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD));
            drinkPanel.add(nameLabel, BorderLayout.NORTH);
    
            JPanel priceRatingPanel = new JPanel(new GridLayout(2, 1));
            JLabel priceLabel = new JLabel("$" + String.format("%.2f", drink.getPrice())); 
            priceRatingPanel.add(priceLabel);
    
            JLabel ratingLabel = new JLabel("Rating: " + drink.getRating() + "/5"); 
            priceRatingPanel.add(ratingLabel);
    
            drinkPanel.add(priceRatingPanel, BorderLayout.CENTER);
    
            if (!result.canMake) {
                JLabel unavailableLabel = new JLabel("Sold Out: " + result.missingIngredient);
                unavailableLabel.setForeground(Color.RED);
                drinkPanel.add(unavailableLabel, BorderLayout.SOUTH);
            }
    
            drinkPanel.revalidate();
            drinkPanel.repaint();
        }
        revalidate();  
        repaint();   
    }

    @Override
    public void onDrinkListUpdated() {
        // 1. Clear existing drinks from the panel
        this.removeAll(); 

        // 2. Get the updated drinks list from CoffeeShopWindow
        System.out.println("onDrinkListUpdated() called!");
        List<Drinks> updatedDrinksList = Drinks.getAllDrinks(); // Or a similar method
        System.out.println("Updated drinks list size: " + updatedDrinksList.size());

        // 3. Add the updated drinks to the panel
        for (Drinks drink : updatedDrinksList) {
            this.addDrink(drink);
        }

        // 4. Revalidate and repaint the panel
        this.revalidate();
        this.repaint();
    }
}
