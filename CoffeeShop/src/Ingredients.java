import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class Ingredients {
    private Connection connection; // Store the database connection

    public Ingredients(Connection connection) {
        this.connection = connection;
    }

    // Method to check if an ingredient is in stock
    public boolean isInStock(String ingredientName) {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT instock FROM user.ingredients WHERE ingredient = ?")) {
            statement.setString(1, ingredientName);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("inStock") == 1;
                }
            }
        } catch (SQLException e) {
            // e.printStackTrace();
            // Handle the exception appropriately (e.g., log it, show a message)
        }
        return false; // Or throw an exception if ingredient not found is an error
    }

    // Method to add a new ingredient to the list
    public void addIngredient(String ingredientName, boolean inStock) {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO user.ingredients (ingredient, inStock) VALUES (?, ?)")) {
            statement.setString(1, ingredientName);
            statement.setInt(2, inStock ? 1 : 0);
            statement.executeUpdate();
        } catch (SQLException e) {
            // e.printStackTrace();
            // Handle the exception appropriately (e.g., log it, show a message)
        }
    }

    // Method to update the stock status of an ingredient
    public void updateIngredientStock(String ingredientName, boolean inStock) {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE user.ingredients SET inStock = ? WHERE ingredient = ?")) {
            statement.setInt(1, inStock ? 1 : 0);
            statement.setString(2, ingredientName);
            statement.executeUpdate();
        } catch (SQLException e) {
            // e.printStackTrace();
            // Handle the exception appropriately (e.g., log it, show a message)
        }
    }

    // Method to get all ingredients and their stock status
    public Map<String, Boolean> getIngredientList() {
        Map<String, Boolean> ingredientList = new HashMap<>();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM user.ingredients")) {
            while (resultSet.next()) {
                String name = resultSet.getString("ingredient");
                boolean inStock = resultSet.getInt("inStock") == 1;
                ingredientList.put(name, inStock);
            }
        } catch (SQLException e) {
            // e.printStackTrace();
            // Handle the exception appropriately (e.g., log it, show a message)
        }
        return ingredientList;
    }
}
