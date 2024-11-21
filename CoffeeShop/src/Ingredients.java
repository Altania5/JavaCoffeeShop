import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class Ingredients {
    private Connection connection;
    private Map<String, IngredientData> ingredientMap;

    public static class IngredientData {
        public boolean inStock;
        public String type;

        public IngredientData(boolean inStock, String type) { // Constructor!
            this.inStock = inStock;
            this.type = type;
        }

        public boolean isInStock() { // or omit public for package-private
            return inStock;
        }

        public String getType() {  // or omit public for package-private
            return type;
        }
    }

    public Ingredients(Connection connection) {
        this.connection = connection;
        this.ingredientMap = new HashMap<>();
        loadIngredientsFromDatabase();
    }

    private void loadIngredientsFromDatabase() {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT ingredients, type, inStock FROM user.ingredients")) {

            while (resultSet.next()) {
                String name = resultSet.getString("ingredients");
                boolean inStock = resultSet.getInt("inStock") == 1;
                String type = resultSet.getString("type");
                ingredientMap.put(name, new IngredientData(inStock, type)); // Use the member variable
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception appropriately (log it, show a message, etc.)
        }
    }

    public boolean isInStock(String ingredientName) {
        IngredientData data = ingredientMap.get(ingredientName);
        return data != null && data.isInStock();
    }

    public void addIngredient(String ingredientName, boolean inStock) {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO user.ingredients (ingredients, inStock) VALUES (?, ?)")) {
            statement.setString(1, ingredientName);
            statement.setInt(2, inStock ? 1 : 0);
            statement.executeUpdate();
        } catch (SQLException e) {
        }
    }

    public void updateIngredientStock(String ingredientName, boolean inStock, String type) { // Add type parameter
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE user.ingredients SET inStock = ?, type = ? WHERE ingredients = ?")) {
            statement.setInt(1, inStock ? 1 : 0);
            statement.setString(2, type); // Update type
            statement.setString(3, ingredientName);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        ingredientMap.get(ingredientName).inStock = inStock;
    }

    public void addIngredient(String ingredientName, boolean inStock, String type) { // Add type parameter
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO user.ingredients (ingredients, inStock, type) VALUES (?, ?, ?)")) {
            statement.setString(1, ingredientName);
            statement.setInt(2, inStock ? 1 : 0);
            statement.setString(3, type); // Set the type
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace(); // Always handle exceptions!
        }
    }

    public Map<String, IngredientData> getIngredientList() { // Return Map<String, IngredientData>
        Map<String, IngredientData> ingredientMap = new HashMap<>();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM user.ingredients")) {
            while (resultSet.next()) {
                String name = resultSet.getString("ingredients");
                boolean inStock = resultSet.getInt("inStock") == 1;
                String type = resultSet.getString("type");
                ingredientMap.put(name, new IngredientData(inStock, type)); // Create IngredientData object
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ingredientMap;
    }
}