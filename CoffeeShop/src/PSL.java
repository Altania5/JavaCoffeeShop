
import java.util.HashMap;
import java.util.Map;

public class PSL extends Drinks {
    private Ingredients ingredients;

    public PSL(double price, Ingredients ingredients) {
        super("Pumpkin Spice Latte",
                createIngredients(),
                "Brew espresso, steam milk, add vanilla syrup, pour...",
                price,
                5,
                5,
                DrinkType.DRIP);
        this.ingredients = ingredients;
    }

    private static Map<String, Double> createIngredients() {
        Map<String, Double> ingredients = new HashMap<>();
        ingredients.put("Espresso", 0.075); // 50ml espresso
        ingredients.put("Milk", 0.02);     // 200ml milk
        ingredients.put("Pumpkin Spice Syrup", 0.05);     // 200ml milk
        return ingredients;
    }

}
