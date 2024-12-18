import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataItem;

class AdvancedStatsPanel extends JPanel {

    private Connection connection;

    public AdvancedStatsPanel(Connection connection) {
        this.connection = connection;
        setLayout(new BorderLayout());
        loadAdvancedStats();
    }

    private void loadAdvancedStats() {
        removeAll(); // Clear existing components

        JTabbedPane tabbedPane = new JTabbedPane();

        // Create datasets for the charts
        DefaultCategoryDataset barChartDataset = new DefaultCategoryDataset();
        DefaultPieDataset pieChartDataset = new DefaultPieDataset();
        HistogramDataset histogramDataset = new HistogramDataset();
        XYSeriesCollection scatterPlotDataset = new XYSeriesCollection();

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM coffee_log")) {

            XYSeries extractionTimeSeries = new XYSeries("Extraction Time vs. Grind Size");
            XYSeries outWeightSeries = new XYSeries("Out Weight vs. Grind Size");

            while (resultSet.next()) {
                String drinkName = resultSet.getString("drink_name");
                String espressoType = resultSet.getString("espresso_type");
                double inWeight = resultSet.getDouble("in_weight");
                int grindSize = resultSet.getInt("grind_size");
                int extractionTime = resultSet.getInt("extraction_time");
                double outWeight = resultSet.getDouble("out_weight");

                // Add data to datasets
                barChartDataset.addValue(extractionTime, "Extraction Time", drinkName);
                if (pieChartDataset.getKeys().contains(drinkName)) { // Check if it exists.
                    pieChartDataset.setValue(drinkName, pieChartDataset.getValue(drinkName).intValue() + 1);
                } else {
                    pieChartDataset.setValue(drinkName, 1); // Initialize to 1 if it's the first encounter.
                }
                histogramDataset.addSeries(espressoType, new double[]{inWeight}, 10); // 10 bins for histogram
                
                // Add data point only if it doesn't already exist to avoid duplicates
                boolean pointExists = false;
                for (int i = 0; i < extractionTimeSeries.getItemCount(); i++) {
                    XYDataItem existingItem = extractionTimeSeries.getDataItem(i);
                    if (existingItem.getX().intValue() == grindSize && existingItem.getY().intValue() == extractionTime) {
                        pointExists = true;
                        break;
                    }
                }

                if (!pointExists) {
                    extractionTimeSeries.add(grindSize, extractionTime);
                    outWeightSeries.add(grindSize, outWeight);
                }
            }

            scatterPlotDataset.addSeries(extractionTimeSeries);
            scatterPlotDataset.addSeries(outWeightSeries);
            

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading advanced stats", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Create charts
        JFreeChart barChart = ChartFactory.createBarChart(
                "Extraction Time by Drink", "Drink Name", "Extraction Time", barChartDataset,
                PlotOrientation.VERTICAL, true, true, false);

        JFreeChart pieChart = ChartFactory.createPieChart(
                "Drink Popularity", pieChartDataset, true, true, false);

        JFreeChart histogram = ChartFactory.createHistogram(
                "In Weight Distribution", "In Weight", "Frequency", histogramDataset,
                PlotOrientation.VERTICAL, true, true, false);

        // Scatter plot with lines
        XYPlot scatterPlot = new XYPlot();
        scatterPlot.setDataset(scatterPlotDataset);
        
        // Create a renderer with lines and small visible shapes
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesLinesVisible(0, true); // Make lines visible
        renderer.setSeriesShapesVisible(0, true); // Make shapes visible
        renderer.setSeriesLinesVisible(1, true); 
        renderer.setSeriesShapesVisible(1, true);
        scatterPlot.setRenderer(renderer);

        // Scatter plot with lines
        scatterPlot.setDataset(scatterPlotDataset);

        // Set the domain (X) and range (Y) axes *BEFORE* getting the range axis.
        NumberAxis rangeAxis = new NumberAxis("Value"); // Or a more descriptive name
        scatterPlot.setRangeAxis(rangeAxis); // Set range axis
        NumberAxis domainAxis = new NumberAxis("Grind Size");
        scatterPlot.setDomainAxis(domainAxis); // Set domain axis


        // Create a renderer with lines and small visible shapes
        renderer.setSeriesLinesVisible(0, true); // Make lines visible
        renderer.setSeriesShapesVisible(0, true); // Make shapes visible
        renderer.setSeriesLinesVisible(1, true);
        renderer.setSeriesShapesVisible(1, true);
        scatterPlot.setRenderer(renderer);

        // NOW you can safely customize the range axis
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        
        scatterPlot.setDomainAxis(new NumberAxis("Grind Size"));
        scatterPlot.setRangeAxis(rangeAxis);

        JFreeChart scatterPlotChart = new JFreeChart("Extraction Time and Out Weight vs. Grind Size", scatterPlot);

        // Add charts to tabs
        tabbedPane.addTab("Bar Chart", new ChartPanel(barChart));
        tabbedPane.addTab("Pie Chart", new ChartPanel(pieChart));
        tabbedPane.addTab("Histogram", new ChartPanel(histogram));
        tabbedPane.addTab("Scatter Plot", new ChartPanel(scatterPlotChart));

        add(tabbedPane, BorderLayout.CENTER);

        revalidate();
        repaint();
    }
}