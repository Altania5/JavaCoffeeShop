import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.Map;

public class IngredientCellRenderer extends DefaultTableCellRenderer {

    private Map<String, Boolean> inStockMap;
    private IngredientTableModel tableModel;

    public IngredientCellRenderer(IngredientTableModel tableModel) {  // Constructor changed!
        this.tableModel = tableModel;
    }
    

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component cellComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        // Access the inStockMap DIRECTLY from the tableModel
        if (value != null && !value.equals("")) {
            String ingredientName = (String) value;
            if (tableModel.getInStockMap().containsKey(ingredientName)) { // Accessing via tableModel
                if (tableModel.getInStockMap().get(ingredientName)) {
                    cellComponent.setForeground(Color.BLACK);
                } else {
                    cellComponent.setForeground(Color.RED);
                }
            }
        }
        return cellComponent;
    }
}