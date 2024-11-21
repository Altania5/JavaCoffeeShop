
import java.util.HashMap;
import java.util.Map;

public class VanillaLatte extends Drinks {
    private Ingredients ingredients;

    public VanillaLatte(double price, Ingredients ingredients) {
        super("Vanilla Latte",
                createIngredients(),
                "Brew espresso, steam milk, add vanilla syrup, pour...",
                price,
                4,
                4,
                DrinkType.ESPRESSO);
        this.ingredients = ingredients;
    }

    private static Map<String, Double> createIngredients() {
        Map<String, Double> ingredients = new HashMap<>();
        ingredients.put("Espresso", 0.088); // 50ml espresso
        ingredients.put("Dairy Milk", 0.2);     // 200ml milk
        ingredients.put("Vanilla Syrup", 0.02); // 20ml vanilla syrup
        return ingredients;
    }

}
