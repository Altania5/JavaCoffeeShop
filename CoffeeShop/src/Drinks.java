import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Drinks {
    private int id;
    private String name;
    private Map<String, Double> ingredients; // Ingredient name, quantity
    private String recipe; // Instructions or description of preparation
    private double price;
    private int rating; // Out of 5, for example
    private int sweetness; // Scale of 1 to 5 (or any range you prefer)
    private DrinkType type; // Enum for Drip, Espresso, Tea

    // Constructor
    public Drinks(int id, String name, Map<String, Double> ingredients, String recipe, double price, int rating, int sweetness, DrinkType type) {
        this.id = id;
        this.name = name;
        this.ingredients = ingredients;
        this.recipe = recipe;
        this.price = price;
        this.rating = rating;
        this.sweetness = sweetness;
        this.type = type;
    }

    public int getId() {
        return id;
    }

     public void setId(int i) {
        id = i;
     }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public int getSweetness() {
        return sweetness;
    }

    public void setSweetness(int sweetness) {
        this.sweetness = sweetness;
    }

    public Map<String, Double> getIngredients() {
        return ingredients;
    }

    public void setIngredients(Map<String, Double> ingredients) {
        this.ingredients = ingredients;
    }

    public String getRecipe() {
        return recipe;
    }

    public void setRecipe(String recipe) {
        this.recipe = recipe;
    }

    public DrinkType getType() {
        return type;
    }

    public void setType(DrinkType type) {
        this.type = type;
    }

    public static List<Drinks> getDrinksFromDatabase(Connection connection) throws SQLException {
        List<Drinks> drinks = new ArrayList<>();
        String sql = "SELECT id, name, price, rating, sweetness, drink_type FROM drinks"; // Adjust query as needed
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                double price = resultSet.getDouble("price");
                int rating = resultSet.getInt("rating");
                int sweetness = resultSet.getInt("sweetness");
                String typeStr = resultSet.getString("drink_type");
                DrinkType type = DrinkType.valueOf(typeStr);

                Map<String, Double> ingredients = getDrinkIngredientsFromDatabase(connection, id);
                drinks.add(new Drinks(id, name, ingredients, "", price, rating, sweetness, type)); // Add ingredients
            }
        }
        return drinks;
    }

    private static Map<String, Double> getDrinkIngredientsFromDatabase(Connection connection, int drinkId) throws SQLException {
        Map<String, Double> ingredients = new HashMap<>();
        String sql = "SELECT ingredient_name, quantity FROM drink_ingredients WHERE drink_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, drinkId);
            try(ResultSet resultSet = statement.executeQuery()){
                while(resultSet.next()){
                    ingredients.put(resultSet.getString("ingredient_name"), resultSet.getDouble("quantity"));
                }
            }
        }
        return ingredients;
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

    // Enum for drink types
    public enum DrinkType {
        DRIP, ESPRESSO, TEA
    }
}
