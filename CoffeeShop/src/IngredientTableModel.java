import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class IngredientTableModel extends AbstractTableModel{
    private List<String> columnNames;
    private List<List<String>> data;
    private Map<String, Boolean> inStockMap; // To store in-stock status

    public IngredientTableModel(Map<String, Ingredients.IngredientData> ingredientMap) {
        columnNames = new ArrayList<>();
        data = new ArrayList<>();
        inStockMap = new HashMap<>(); // Initialize inStockMap
        Map<String, List<String>> ingredientsByType = new HashMap<>();

        // Collect all ingredient types 
        for (Ingredients.IngredientData ingredientData : ingredientMap.values()) {
            String type = ingredientData.getType();
            if (!ingredientsByType.containsKey(type)) {
                ingredientsByType.put(type, new ArrayList<>());
                columnNames.add(type); 
            }
        }

        // Add ingredient names to the corresponding type list
        for (Map.Entry<String, Ingredients.IngredientData> entry : ingredientMap.entrySet()) {
            String ingredientName = entry.getKey();
            String ingredientType = entry.getValue().getType();
            boolean isInStock = entry.getValue().isInStock();

            if (ingredientsByType.containsKey(ingredientType)) {
                ingredientsByType.get(ingredientType).add(ingredientName);
                inStockMap.put(ingredientName, isInStock); // Store in-stock status
            }
        }

        // Transpose data for the table model
        int maxRows = 0;
        for (List<String> ingredients : ingredientsByType.values()) {
            maxRows = Math.max(maxRows, ingredients.size());
        }

        for (int i = 0; i < maxRows; i++) {
            List<String> rowData = new ArrayList<>();
            for (String type : columnNames) {
                List<String> ingredientsOfType = ingredientsByType.get(type);
                rowData.add(i < ingredientsOfType.size() ? ingredientsOfType.get(i) : ""); 
            }
            data.add(rowData);
        }
    }

    public Map<String, Boolean> getInStockMap() {
        return inStockMap;
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        JLabel cellComponent = new JLabel();

        if (value != null) {
            cellComponent.setText(value.toString()); 
        }

        if (value != null && !value.equals("")) {
            String ingredientName = (String) value;
            if (inStockMap.containsKey(ingredientName) && !inStockMap.get(ingredientName)) {
                cellComponent.setForeground(Color.RED); 
            } 
        }

        return cellComponent;
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return data.get(rowIndex).get(columnIndex);
    }

    @Override
    public String getColumnName(int column) {
        return columnNames.get(column);
    }
}
