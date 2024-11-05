import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Drinks {
    private String name;
    private Map<String, Double> ingredients; // Ingredient name, quantity
    private String recipe; // Instructions or description of preparation
    private double price;
    private int rating; // Out of 5, for example
    private int sweetness; // Scale of 1 to 5 (or any range you prefer)
    private DrinkType type; // Enum for Drip, Espresso, Tea
    

    public static List<Drinks> allDrinks = new ArrayList<>(); 

    // Constructor
    public Drinks(String name, Map<String, Double> ingredients, String recipe, double price,
                 int rating, int sweetness, DrinkType type) {
        this.name = name;
        this.ingredients = ingredients;
        this.recipe = recipe;
        this.price = price;
        this.rating = rating;
        this.sweetness = sweetness;
        this.type = type;

        allDrinks.add(this); 
    }

    // Getters and Setters (add as needed)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public int getRating() {
        return rating;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Map<String, Double> getIngredients() {
        return ingredients;
    }

    public void setIngredients(Map<String, Double> ingredients) {
        this.ingredients = ingredients;
    }

    // ... (Add getters and setters for other attributes)

    // Enum for drink types
    public enum DrinkType {
        DRIP, ESPRESSO, TEA
    }

    public static List<Drinks> getAllDrinks() {
        return allDrinks;
    }

    public static void addDrink(Drinks drink) {
        allDrinks.add(drink);
    }

    public CheckResult canMake(Ingredients allIngredients) {
        for (String ingredient : getIngredients().keySet()) {
            if (!allIngredients.isInStock(ingredient)) {
                return new CheckResult(false, ingredient);
            }
        }
        return new CheckResult(true, null);
    }

    // Inner class to hold check result
    public static class CheckResult {
        public boolean canMake;
        public String missingIngredient;

        public CheckResult(boolean canMake, String missingIngredient) {
            this.canMake = canMake;
            this.missingIngredient = missingIngredient;
        }
    }
}
