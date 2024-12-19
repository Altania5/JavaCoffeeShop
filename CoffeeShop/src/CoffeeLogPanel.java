import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

class CoffeeLogPanel extends JPanel {

    private Connection connection;
    private JPanel logArea; // Changed to JPanel to hold individual log entries
    private JPanel statsPanel;
    private List<CoffeeLoggedListener> coffeeLoggedListeners = new ArrayList<>();

    public CoffeeLogPanel(Connection connection) {
        this.connection = connection;
        this.statsPanel = new JPanel(new GridLayout(0, 3, 10, 10));
        setLayout(new BorderLayout());

        statsPanel = new JPanel(new GridLayout(0, 3, 10, 10));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Overall Stats
        statsPanel.add(new JLabel("Overall Stats"));
        statsPanel.add(new JLabel("Dark Espresso"));
        statsPanel.add(new JLabel("Blonde Espresso"));

        statsPanel.add(new JLabel("Favorite Drink: -"));
        statsPanel.add(new JLabel("Avg Extraction Time: -"));
        statsPanel.add(new JLabel("Avg Extraction Time: -"));

        statsPanel.add(new JLabel("Total Logs: 0"));
        statsPanel.add(new JLabel("Avg Out Weight: -"));
        statsPanel.add(new JLabel("Avg Out Weight: -"));

        statsPanel.add(new JLabel("Avg Time of Day: -"));
        statsPanel.add(new JLabel("Avg Grind Size: -"));
        statsPanel.add(new JLabel("Avg Grind Size: -"));

        add(statsPanel, BorderLayout.NORTH);

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

            for (CoffeeLoggedListener listener : coffeeLoggedListeners) {
                listener.onCoffeeLogged();
            }

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

                Map<String, Integer> drinkCounts = new HashMap<>();
                String avgTimeofDay = "-";
                int totalLogs = 0;
                String favoriteDrink = "-";
                int totalExtractionTime = 0; // For calculating average time of day
                int darkEspressoCount = 0;
                int blondeEspressoCount = 0;
                int darkEspressoExtractionTime = 0;
                double darkEspressoOutWeight = 0;
                int darkEspressoGrindSize = 0;
                int blondeEspressoExtractionTime = 0;
                double blondeEspressoOutWeight = 0;
                int blondeEspressoGrindSize = 0;

            while (resultSet.next()) {
                
                String espressoType = resultSet.getString("espresso_type");

                if (!espressoType.equalsIgnoreCase("None")) {
                totalLogs++;
                

                String drinkName = resultSet.getString("drink_name");
                drinkCounts.put(drinkName, drinkCounts.getOrDefault(drinkName, 0) + 1);
                Timestamp logTime = resultSet.getTimestamp("log_time");
                double inWeight = resultSet.getDouble("in_weight");
                int grindSize = resultSet.getInt("grind_size");
                int extractionTime = resultSet.getInt("extraction_time");
                double outWeight = resultSet.getDouble("out_weight");

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String formattedLogTime = dateFormat.format(new Date(logTime.getTime()));

                totalExtractionTime += logTime.toLocalDateTime().getHour() * 60 + logTime.toLocalDateTime().getMinute();

                if (espressoType.equalsIgnoreCase("Dark")) {
                    darkEspressoCount++;
                    darkEspressoExtractionTime += extractionTime;
                    darkEspressoOutWeight += outWeight;
                    darkEspressoGrindSize += grindSize;
                } else if (espressoType.equalsIgnoreCase("Blonde")) {
                    blondeEspressoCount++;
                    blondeEspressoExtractionTime += extractionTime;
                    blondeEspressoOutWeight += outWeight;
                    blondeEspressoGrindSize += grindSize;
                }

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

                JLabel detailsLabel = new JLabel(String.format("Espresso: %s | In: %.2f | Grind: %d | Time: %d | Out: %.2f",espressoType, inWeight, grindSize, extractionTime, outWeight));
                detailsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                logEntryPanel.add(detailsLabel);

                logArea.add(logEntryPanel);
                logArea.add(Box.createRigidArea(new Dimension(0, 5))); // Spacing between logs

                favoriteDrink = drinkCounts.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("-");

                if (totalLogs > 0) {
                    int avgTimeOfDayMinutes = totalExtractionTime / totalLogs;
                    int avgTimeOfDayHours = avgTimeOfDayMinutes / 60;
                    int avgTimeOfDayRemainderMinutes = avgTimeOfDayMinutes % 60;
    
                    LocalTime avgTimeOfDay = LocalTime.of(avgTimeOfDayHours, avgTimeOfDayRemainderMinutes);
                    avgTimeofDay = avgTimeOfDay.format(DateTimeFormatter.ofPattern("HH:mm")); 
                }
    
                // Update stat labels
                statsPanel.removeAll(); // Clear existing labels
    
                // Overall Stats
                statsPanel.add(new JLabel("Overall Stats"));
                statsPanel.add(new JLabel("Dark Espresso"));
                statsPanel.add(new JLabel("Blonde Espresso"));
    
                statsPanel.add(new JLabel("Favorite Drink: " + favoriteDrink));
    
                // Calculate and add Avg Extraction Time labels
                double darkAvgExtractionTime = darkEspressoCount > 0 ? (double) darkEspressoExtractionTime / darkEspressoCount : -1;
                double blondeAvgExtractionTime = blondeEspressoCount > 0 ? (double) blondeEspressoExtractionTime / blondeEspressoCount : -1;
                statsPanel.add(new JLabel("Avg Extraction Time: " + (darkAvgExtractionTime >= 0 ? String.format("%.2f", darkAvgExtractionTime) : "-")));
                statsPanel.add(new JLabel("Avg Extraction Time: " + (blondeAvgExtractionTime >= 0 ? String.format("%.2f", blondeAvgExtractionTime) : "-")));
    
                statsPanel.add(new JLabel("Total Logs: " + totalLogs));
    
                // Calculate and add Avg Out Weight labels
                double darkAvgOutWeight = darkEspressoCount > 0 ? darkEspressoOutWeight / darkEspressoCount : -1;
                double blondeAvgOutWeight = blondeEspressoCount > 0 ? blondeEspressoOutWeight / blondeEspressoCount : -1;
                statsPanel.add(new JLabel("Avg Out Weight: " + (darkAvgOutWeight >= 0 ? String.format("%.2f", darkAvgOutWeight) : "-")));
                statsPanel.add(new JLabel("Avg Out Weight: " + (blondeAvgOutWeight >= 0 ? String.format("%.2f", blondeAvgOutWeight) : "-")));
    
                statsPanel.add(new JLabel("Avg Time of Day: " + avgTimeofDay));
    
                // Calculate and add Avg Grind Size labels
                double darkAvgGrindSize = darkEspressoCount > 0 ? (double) darkEspressoGrindSize / darkEspressoCount : -1;
                double blondeAvgGrindSize = blondeEspressoCount > 0 ? (double) blondeEspressoGrindSize / blondeEspressoCount : -1;
                statsPanel.add(new JLabel("Avg Grind Size: " + (darkAvgGrindSize >= 0 ? String.format("%.2f", darkAvgGrindSize) : "-")));
                statsPanel.add(new JLabel("Avg Grind Size: " + (blondeAvgGrindSize >= 0 ? String.format("%.2f", blondeAvgGrindSize) : "-")));
    
                statsPanel.revalidate();
                statsPanel.repaint();
            }
        }

        } catch (SQLException e) {
            e.printStackTrace(); // Or handle the exception appropriately
        }
    }

    public interface CoffeeLoggedListener {
        void onCoffeeLogged();
    }

    public void addCoffeeLoggedListener(CoffeeLoggedListener listener) {
        coffeeLoggedListeners.add(listener);
    }

    public void removeCoffeeLoggedListener(CoffeeLoggedListener listener) {
        coffeeLoggedListeners.remove(listener);
    }
}