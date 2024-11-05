
import java.util.HashMap;
import java.util.Map;

public class PEGTL extends Drinks {
    private Ingredients ingredients;

    public PEGTL(double price, Ingredients ingredients) {
        super("Peach Green Tea Lemonade",
                createIngredients(),
                "Brew espresso, steam milk, add vanilla syrup, pour...",
                price,
                4,
                4,
                DrinkType.TEA);
        this.ingredients = ingredients;
    }

    private static Map<String, Double> createIngredients() {
        Map<String, Double> ingredients = new HashMap<>();
        ingredients.put("Peach Consentrate", 0.075); // 50ml espresso
        ingredients.put("Green Tea", 0.2);     // 200ml milk
        ingredients.put("Lemonade", 0.2);     // 200ml milk
        return ingredients;
    }

}
