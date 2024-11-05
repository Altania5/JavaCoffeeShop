
import java.util.HashMap;
import java.util.Map;

public class Bose extends Drinks {
    private Ingredients ingredients;

    public Bose(double price, Ingredients ingredients) {
        super("Brown Sugar Oat Milk Shaken Espresso",
                createIngredients(),
                "Brew espresso, steam milk, add vanilla syrup, pour...",
                price,
                5,
                5,
                DrinkType.ESPRESSO);
        this.ingredients = ingredients;
    }

    private static Map<String, Double> createIngredients() {
        Map<String, Double> ingredients = new HashMap<>();
        ingredients.put("Espresso", 0.244); // 50ml espresso
        ingredients.put("Oat Milk", 0.2);     // 200ml milk
        ingredients.put("Brown Sugar Syrup", 0.02); // 20ml vanilla syrup
        ingredients.put("Cinnamon", 0.01); // 20ml vanilla syrup
        return ingredients;
    }

}
