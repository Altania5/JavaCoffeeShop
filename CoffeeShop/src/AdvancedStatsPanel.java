import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
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

    public double[] analyzeExtractionTime() {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM coffee_log WHERE espresso_type <> 'None'")) {

            List<Double> extractionTimes = new ArrayList<>();
            while (resultSet.next()) {
                int extractionTime = resultSet.getInt("extraction_time");
                extractionTimes.add((double) extractionTime);
            }

            // Calculate average extraction time
            double averageExtractionTime = calculateAverage(extractionTimes);
            System.out.println("Average Extraction Time: " + averageExtractionTime);

            // Calculate extraction time variance
            double extractionTimeVariance = calculateVariance(extractionTimes);
            System.out.println("Extraction Time Variance: " + extractionTimeVariance);

            // TODO: Add analysis for correlation with grind size and peak hours

            return new double[]{averageExtractionTime, extractionTimeVariance};
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error analyzing extraction time", "Error", JOptionPane.ERROR_MESSAGE);
            return new double[]{0, 0};
        }
    }

    public double[] analyzeEspressoYield() {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM coffee_log WHERE espresso_type <> 'None'")) {

            List<Double> yieldRatios = new ArrayList<>();
            while (resultSet.next()) {
                double inWeight = resultSet.getDouble("in_weight");
                double outWeight = resultSet.getDouble("out_weight");
                double yieldRatio = outWeight / inWeight;
                yieldRatios.add(yieldRatio);
            }

            // Calculate average yield ratio
            double averageYieldRatio = calculateAverage(yieldRatios);
            System.out.println("Average Yield Ratio: " + averageYieldRatio);

            // Calculate yield ratio variance
            double yieldRatioVariance = calculateVariance(yieldRatios);
            System.out.println("Yield Ratio Variance: " + yieldRatioVariance);

            // TODO: Add analysis for correlation with grind size and waste reduction

            return new double[]{averageYieldRatio, yieldRatioVariance};
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error analyzing espresso yield", "Error", JOptionPane.ERROR_MESSAGE);
            return new double[]{0, 0};
        }
    }

    private double calculateAverage(List<Double> data) {
        double sum = 0;
        for (double value : data) {
            sum += value;
        }
        return sum / data.size();
    }

    private double calculateVariance(List<Double> data) {
        double average = calculateAverage(data);
        double sumOfSquaredDifferences = 0;
        for (double value : data) {
            sumOfSquaredDifferences += Math.pow(value - average, 2);
        }
        return sumOfSquaredDifferences / data.size();
    }

    public void loadAdvancedStats() {
        removeAll(); // Clear existing components

        HistogramDataset extractionTimeHistogramDataset = new HistogramDataset();
        HistogramDataset yieldRatioHistogramDataset = new HistogramDataset();

        JTabbedPane tabbedPane = new JTabbedPane();

        // Create datasets for the charts
        DefaultCategoryDataset barChartDataset = new DefaultCategoryDataset();
        DefaultPieDataset pieChartDataset = new DefaultPieDataset();
        HistogramDataset histogramDataset = new HistogramDataset();
        XYSeriesCollection scatterPlotDataset = new XYSeriesCollection();

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM coffee_log WHERE espresso_type <> 'None'")) {

            List<Double> extractionTimes = new ArrayList<>();
            List<Double> yieldRatios = new ArrayList<>();

            XYSeries extractionTimeSeries = new XYSeries("Extraction Time vs. Grind Size");
            XYSeries outWeightSeries = new XYSeries("Out Weight vs. Grind Size");

            while (resultSet.next()) {
                String drinkName = resultSet.getString("drink_name");
                String espressoType = resultSet.getString("espresso_type");
                int grindSize = resultSet.getInt("grind_size");
                double inWeight = resultSet.getDouble("in_weight");
                double outWeight = resultSet.getDouble("out_weight");
                int extractionTime = resultSet.getInt("extraction_time");
                extractionTimes.add((double) extractionTime);
                double yieldRatio = outWeight / inWeight;
                yieldRatios.add(yieldRatio);

                // Add data to datasets
                barChartDataset.addValue(extractionTime, "Extraction Time", drinkName);
                if (pieChartDataset.getKeys().contains(drinkName)) { // Check if it exists.
                    pieChartDataset.setValue(drinkName, pieChartDataset.getValue(drinkName).intValue() + 1);
                } else {
                    pieChartDataset.setValue(drinkName, 1); // Initialize to 1 if it's the first encounter.
                }
                histogramDataset.addSeries(espressoType, new double[]{outWeight}, 10); // 10 bins for histogram
                
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

            double[] extractionTimeArray = extractionTimes.stream().mapToDouble(Double::doubleValue).toArray();
            double[] yieldRatioArray = yieldRatios.stream().mapToDouble(Double::doubleValue).toArray();

            extractionTimeHistogramDataset.addSeries("Extraction Time", extractionTimeArray, 10); // 10 bins
            yieldRatioHistogramDataset.addSeries("Yield Ratio", yieldRatioArray, 10);

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
                "Out Weight Distribution", "Out Weight", "Frequency", histogramDataset,
                PlotOrientation.VERTICAL, true, true, false);

        JFreeChart extractionTimeHistogram = ChartFactory.createHistogram(
            "Extraction Time Distribution", "Extraction Time", "Frequency", extractionTimeHistogramDataset,
            PlotOrientation.VERTICAL, true, true, false
        );

        JFreeChart yieldRatioHistogram = ChartFactory.createHistogram(
            "Yield Ratio Distribution", "Yield Ratio", "Frequency", yieldRatioHistogramDataset,
            PlotOrientation.VERTICAL, true, true, false
        );

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
        tabbedPane.addTab("Extraction Time Analysis", new ChartPanel(extractionTimeHistogram));
        tabbedPane.addTab("Yield Analysis", new ChartPanel(yieldRatioHistogram));

        add(tabbedPane, BorderLayout.CENTER);

        revalidate();
        repaint();
    }
}