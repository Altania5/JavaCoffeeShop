import javax.swing.*;
import java.awt.*;

public class MenuPanel extends JPanel {
    private Ingredients ingredients;

    public MenuPanel(Ingredients ingredients) {
        this.ingredients = ingredients;

        // Set layout (we'll use GridLayout for simplicity)
        setLayout(new GridLayout(0, 2, 10, 10)); // 2 columns, variable rows, 10px spacing

    }

    public void addDrink(Drinks drink) {
        JPanel drinkPanel = new JPanel(new BorderLayout());
        drinkPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        JLabel nameLabel = new JLabel(drink.getName());
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD));
        drinkPanel.add(nameLabel, BorderLayout.NORTH);

        // Price and Rating
        JPanel priceRatingPanel = new JPanel(new GridLayout(2, 1));
        JLabel priceLabel = new JLabel("$" + String.format("%.2f", drink.getPrice()));
        priceRatingPanel.add(priceLabel);

        JLabel ratingLabel = new JLabel("Rating: " + drink.getRating() + "/5");
        priceRatingPanel.add(ratingLabel);

        drinkPanel.add(priceRatingPanel, BorderLayout.CENTER);

        // Check if the drink can be made
        Drinks.CheckResult result = drink.canMake(ingredients);
        if (!result.canMake) {
            JLabel unavailableLabel = new JLabel("Sold Out: " + result.missingIngredient);
            unavailableLabel.setForeground(Color.RED);
            drinkPanel.add(unavailableLabel, BorderLayout.SOUTH);
        }

        add(drinkPanel);
    }
}
