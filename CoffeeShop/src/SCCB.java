
import java.util.HashMap;
import java.util.Map;

public class SCCB extends Drinks {
    private Ingredients ingredients;

    public SCCB(double price, Ingredients ingredients) {
        super("Salted Caramel Cold Brew",
                createIngredients(),
                "Brew espresso, steam milk, add vanilla syrup, pour...",
                price,
                3,
                2,
                DrinkType.DRIP);
        this.ingredients = ingredients;
    }

    private static Map<String, Double> createIngredients() {
        Map<String, Double> ingredients = new HashMap<>();
        ingredients.put("Cold Brew", 0.3); // 50ml espresso
        ingredients.put("Dairy Milk", 0.02);     // 200ml milk
        ingredients.put("Heavy Cream", 0.05);     // 200ml milk
        ingredients.put("Salted Caramel Syrup", 0.02); // 20ml vanilla syrup
        return ingredients;
    }

}
