import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

class CoffeeLogPanel extends JPanel {

    private Connection connection;
    private JPanel logArea; // Changed to JPanel to hold individual log entries

    public CoffeeLogPanel(Connection connection) {
        this.connection = connection;
        setLayout(new BorderLayout());

        logArea = new JPanel();
        logArea.setLayout(new BoxLayout(logArea, BoxLayout.Y_AXIS)); // Vertical arrangement of logs
        JScrollPane scrollPane = new JScrollPane(logArea);
        add(scrollPane, BorderLayout.CENTER);
        loadCoffeeLog();
    }

    public void logCoffee(String drinkName, String espressoType, double inWeight, int grindSize, int extractionTime, double outWeight) {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO coffee_log (drink_name, espresso_type, in_weight, grind_size, extraction_time, out_weight) VALUES (?, ?, ?, ?, ?, ?)")) {
            statement.setString(1, drinkName);
            statement.setString(2, espressoType);
            statement.setDouble(3, inWeight);
            statement.setInt(4, grindSize);
            statement.setInt(5, extractionTime);
            statement.setDouble(6, outWeight);
            statement.executeUpdate();

            loadCoffeeLog();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error logging coffee to database", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadCoffeeLog() {
        logArea.removeAll(); // Clear previous logs
        logArea.revalidate();
        logArea.repaint();

        try (Statement statement = connection.prepareStatement("SELECT * FROM coffee_log");
             ResultSet resultSet = statement.executeQuery("SELECT * FROM coffee_log")) {

            while (resultSet.next()) {
                String drinkName = resultSet.getString("drink_name");
                Timestamp logTime = resultSet.getTimestamp("log_time");
                String espressoType = resultSet.getString("espresso_type");
                double inWeight = resultSet.getDouble("in_weight");
                int grindSize = resultSet.getInt("grind_size");
                int extractionTime = resultSet.getInt("extraction_time");
                double outWeight = resultSet.getDouble("out_weight");

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String formattedLogTime = dateFormat.format(new Date(logTime.getTime()));

                // Create a panel for each log entry
                JPanel logEntryPanel = new JPanel();
                logEntryPanel.setLayout(new BoxLayout(logEntryPanel, BoxLayout.Y_AXIS));
                logEntryPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                logEntryPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100)); // Limit height

                JLabel drinkNameLabel = new JLabel(drinkName);
                drinkNameLabel.setFont(new Font("Arial", Font.BOLD, 16));
                drinkNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                logEntryPanel.add(drinkNameLabel);

                JLabel logTimeLabel = new JLabel(formattedLogTime);
                logTimeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                logEntryPanel.add(logTimeLabel);

                JLabel detailsLabel = new JLabel(String.format("Espresso: %s | In: %.2f | Grind: %d | Time: %d | Out: %.2f",
                        espressoType, inWeight, grindSize, extractionTime, outWeight));
                detailsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                logEntryPanel.add(detailsLabel);

                logArea.add(logEntryPanel);
                logArea.add(Box.createRigidArea(new Dimension(0, 5))); // Spacing between logs
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Or handle the exception appropriately
        }
    }
}