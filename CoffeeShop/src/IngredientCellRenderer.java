import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.Map;

public class IngredientCellRenderer extends DefaultTableCellRenderer {

    private Map<String, Boolean> inStockMap;

    public IngredientCellRenderer(Map<String, Boolean> inStockMap) {
        this.inStockMap = inStockMap;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component cellComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        // Set text color based on in-stock status
        if (value != null && !value.equals("")) {
            String ingredientName = (String) value;
            if (inStockMap.containsKey(ingredientName)) {
                if (inStockMap.get(ingredientName)) {
                    cellComponent.setForeground(Color.BLACK); // In stock
                } else {
                    cellComponent.setForeground(Color.RED); // Out of stock
                }
            }
        }
        return cellComponent;
    }
}