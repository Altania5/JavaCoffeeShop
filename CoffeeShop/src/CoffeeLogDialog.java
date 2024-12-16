import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CoffeeLogDialog extends JDialog {

    private JTextField espressoTypeField;
    private JTextField inWeightField;
    private JTextField grindSizeField;
    private JTextField extractionTimeField;
    private JTextField outWeightField;
    private CoffeeLogPanel coffeeLogPanel;
    private String drinkName;
    private Ingredients ingredients;

    public CoffeeLogDialog(JFrame parent, Ingredients ingredients, String drinkName) {
        super(parent, "Log Coffee", true);
        this.ingredients = ingredients;
        this.drinkName = drinkName;


        // ... other existing code ...

        setLayout(new BorderLayout());

        // Input fields
        espressoTypeField = new JTextField(ingredients.getEspressoType(), 15); //Gets the espresso type from ingredients
        inWeightField = new JTextField(10);
        grindSizeField = new JTextField(10);
        extractionTimeField = new JTextField(10);
        outWeightField = new JTextField(10);

        // Create and add panels
        add(createInputPanel(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(parent); // Centers on the parent window
    }

    private JPanel createInputPanel() {
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        inputPanel.add(new JLabel("Espresso Type:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(espressoTypeField, gbc);

        // ... (similarly add other input fields with labels: inWeight, grindSize, extractionTime, outWeight)

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        inputPanel.add(new JLabel("In Weight:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(inWeightField, gbc);


        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        inputPanel.add(new JLabel("Grind Size:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(grindSizeField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        inputPanel.add(new JLabel("Extraction Time:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(extractionTimeField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        inputPanel.add(new JLabel("Out Weight:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(outWeightField, gbc);



        return inputPanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    // Retrieve values from input fields, handling potential NumberFormatExceptions
                    String espressoType = espressoTypeField.getText();
                    double inWeight = Double.parseDouble(inWeightField.getText());
                    int grindSize = Integer.parseInt(grindSizeField.getText());
                    int extractionTime = Integer.parseInt(extractionTimeField.getText());
                    double outWeight = Double.parseDouble(outWeightField.getText());


                    // Access and call the logCoffee() method of the CoffeeLogPanel
                    CoffeeShopWindow parent = (CoffeeShopWindow) getParent();
                    coffeeLogPanel = parent.getCoffeeLogPanel();
                    coffeeLogPanel.logCoffee(drinkName, espressoType, inWeight, grindSize, extractionTime, outWeight);

                    dispose(); // Close the dialog

                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(CoffeeLogDialog.this, "Invalid input. Please enter valid numbers.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        return buttonPanel;
    }
}
