import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class Ingredients {
    private Connection connection;

    public Ingredients(Connection connection) {
        this.connection = connection;
    }

    public boolean isInStock(String ingredientName) {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT instock FROM user.ingredients WHERE ingredients = ?")) {
            statement.setString(1, ingredientName);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("inStock") == 1;
                }
            }
        } catch (SQLException e) {
        }
        return false;
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

    public void updateIngredientStock(String ingredientName, boolean inStock) {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE user.ingredients SET inStock = ? WHERE ingredients = ?")) {
            statement.setInt(1, inStock ? 1 : 0);
            statement.setString(2, ingredientName);
            statement.executeUpdate();
        } catch (SQLException e) {
        }
    }

    public Map<String, Boolean> getIngredientList() {
        Map<String, Boolean> ingredientList = new HashMap<>();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM user.ingredients")) {
            while (resultSet.next()) {
                String name = resultSet.getString("ingredients");
                boolean inStock = resultSet.getInt("inStock") == 1;
                ingredientList.put(name, inStock);
            }
        } catch (SQLException e) {
        }
        return ingredientList;
    }
}
