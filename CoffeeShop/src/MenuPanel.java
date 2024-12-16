import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;

public class MenuPanel extends JPanel implements DrinkListListener{
    private Map<Drinks, JPanel> drinkPanelMap; // Added to store drink panels
    private CoffeeShopWindow coffeeShopWindow;
    private CoffeeLogPanel coffeeLogPanel;
    private JPanel menuItemsPanel;
    private JTabbedPane tabbedPane;
    private Ingredients ingredients;

    public MenuPanel(Ingredients ingredients, CoffeeShopWindow window, JTabbedPane tabbedPane, CoffeeLogPanel coffeeLogPanel) {
        this.drinkPanelMap = new HashMap<>();
        this.ingredients = ingredients;
        this.coffeeShopWindow = window;
        this.tabbedPane = tabbedPane;
        this.coffeeLogPanel = coffeeLogPanel;
        window.addDrinkListListener(this);

        // Set layout (we'll use GridLayout for simplicity)
        setLayout(new BorderLayout());

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("Altanian Coffee Shop");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel descriptionLabel = new JLabel("Welcome to the Altanian Coffee Shop app!");
        descriptionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        titlePanel.add(titleLabel);
        titlePanel.add(descriptionLabel);
        add(titlePanel, BorderLayout.NORTH);

        menuItemsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        menuItemsPanel.setBackground(new Color(240, 240, 240));
        JScrollPane scrollPane = new JScrollPane(menuItemsPanel);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void addDrink(Drinks drink) {
        if (!drinkPanelMap.containsKey(drink)) {
            JPanel drinkPanel = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Dimension arcs = new Dimension(20, 20);
                    int width = getWidth();
                    int height = getHeight();
                    Graphics2D graphics = (Graphics2D) g;
                    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    graphics.setColor(getBackground());
                    graphics.fillRoundRect(0, 0, width - 1, height - 1, arcs.width, arcs.height);

                    graphics.setColor(Color.LIGHT_GRAY); // Set border color here
                    graphics.setStroke(new BasicStroke(1)); // Set border thickness
                    graphics.drawRoundRect(0, 0, width - 1, height - 1, arcs.width, arcs.height); 
                }
            };
            drinkPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Remove the line border
            drinkPanel.setBackground(Color.WHITE);
            drinkPanelMap.put(drink, drinkPanel);

            JLabel nameLabel = new JLabel(drink.getName());
            nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 18)); // Increase font size
            drinkPanel.add(nameLabel, BorderLayout.NORTH);

            JPanel priceRatingPanel = new JPanel(new GridLayout(2, 1));
            JLabel priceLabel = new JLabel("$" + String.format("%.2f", drink.getPrice()));
            priceLabel.setFont(priceLabel.getFont().deriveFont(16f)); // Increase font size
            priceRatingPanel.add(priceLabel);

            JLabel ratingLabel = new JLabel("Rating: " + drink.getRating() + "/5");
            ratingLabel.setFont(ratingLabel.getFont().deriveFont(14f)); // Increase font size
            priceRatingPanel.add(ratingLabel);
            priceRatingPanel.setOpaque(false);

            drinkPanel.add(priceRatingPanel, BorderLayout.CENTER);

            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS)); // Stack buttons vertically
            buttonPanel.setOpaque(false);
            JButton logButton = new JButton("Log");
            logButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    tabbedPane.setSelectedIndex(3); 
                    CoffeeLogDialog dialog = new CoffeeLogDialog(coffeeShopWindow, ingredients, drink.getName());
                    dialog.setLocationRelativeTo(coffeeShopWindow);
                    dialog.setVisible(true);
                }
            });

            JButton buyButton = new JButton("Buy");

            buttonPanel.add(logButton);
            buttonPanel.add(buyButton);
            drinkPanel.add(buttonPanel, BorderLayout.EAST);

            // Add to menuItemsPanel using GridBagLayout
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            gbc.gridy = (drinkPanelMap.size() - 1) / 2; // Calculate row index (dividing by 2)
            gbc.gridx = (drinkPanelMap.size() - 1) % 2; // Calculate column index (modulo 2)
            menuItemsPanel.add(drinkPanel, gbc);
        }

        JPanel drinkPanel = drinkPanelMap.get(drink); // Get the existing panel
        Drinks.CheckResult result = drink.canMake(coffeeShopWindow.getIngredients());
        JLabel nameLabel = new JLabel(drink.getName()); // Add name label
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD));
        drinkPanel.add(nameLabel, BorderLayout.NORTH);

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

            // Find and update the "Sold Out" label
            for (Component component : drinkPanel.getComponents()) {  // Iterate through drinkPanel components
                if (component instanceof JLabel && ((JLabel) component).getText().startsWith("Sold Out: ")) {
                    if (!result.canMake) {
                        ((JLabel) component).setText("Sold Out: " + result.missingIngredient);
                    } else {
                        drinkPanel.remove(component);
                    }
                    break; // Exit the inner loop once the label is found/updated
                }
            }
            drinkPanel.revalidate();
            drinkPanel.repaint();
        }
        revalidate();  // These are still necessary for the main MenuPanel.
        repaint();
    }

    @Override
public void onDrinkListUpdated() {
    try (Connection connection = DriverManager.getConnection(App.DATABASE_URL, App.DATABASE_USERNAME, App.DATABASE_PASSWORD)) {
        menuItemsPanel.removeAll(); // Clear the menuItemsPanel, not the entire MenuPanel
        drinkPanelMap.clear(); // Clear the existing drink panels in the map
        List<Drinks> updatedDrinksList = Drinks.getDrinksFromDatabase(connection); // Get from DB
        for (Drinks drink : updatedDrinksList) {
            addDrink(drink); // This will re-add drinks to the menuItemsPanel
        }
        menuItemsPanel.revalidate();  // VERY IMPORTANT: Tell Swing to recalculate the layout
        menuItemsPanel.repaint();      // VERY IMPORTANT: Redraw the menuItemsPanel contents
        } catch (SQLException e) {
            e.printStackTrace(); //Handle appropriately
            JOptionPane.showMessageDialog(this, "Error loading drinks from database", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}