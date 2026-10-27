package adapters.in;

import domain.ExitAllocation;
import domain.Product;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/** Salidas: formulario de salida y asignación FEFO por lote (RF07, RF-UI05). */
class SalidasScreen extends ScreenPanel {
    private final StyledCombo<ProductItem> productCombo = new StyledCombo<>();
    private final JLabel productError = new JLabel(" ");
    private final FormField quantity = new FormField("field-quantity", "Cantidad", 10);
    private final AppButton submit = new AppButton("Registrar salida", AppButton.Style.PRIMARY);
    private final JLabel resultTitle = cardTitle("Asignación FEFO");
    private final JLabel lowStockWarning = new JLabel(" ");
    private final DataTable table = new DataTable("salidas-table", new DataTable.Column[]{
            new DataTable.Column("Lote", DataTable.Kind.TEXT, 200),
            new DataTable.Column("Vencimiento", DataTable.Kind.TEXT, 160),
            new DataTable.Column("Cantidad", DataTable.Kind.TEXT, 120)});
    private boolean updating;

    SalidasScreen(AppShell shell) {
        super(shell, "screen-salidas");
        productCombo.setName("salidas-product");
        productError.setName("salidas-product-error");
        productError.setFont(Theme.FONT_SMALL);
        productError.setForeground(Theme.ERROR);
        quantity.input().addActionListener(e -> submit());
        productCombo.addActionListener(e -> { if (!updating) productError.setText(" "); });
        submit.setName("salidas-submit");
        submit.addActionListener(e -> submit());

        JLabel productLabel = new JLabel("Producto");
        productLabel.setFont(Theme.FONT_SMALL_BOLD);
        productLabel.setForeground(Theme.TEXT);
        JPanel productBox = new JPanel();
        productBox.setOpaque(false);
        productBox.setLayout(new BoxLayout(productBox, BoxLayout.Y_AXIS));
        for (JComponent c : new JComponent[]{productLabel, productCombo, productError}) c.setAlignmentX(LEFT_ALIGNMENT);
        productBox.add(productLabel);
        productBox.add(Box.createVerticalStrut(Theme.SP_XS + 2));
        productBox.add(productCombo);
        productBox.add(Box.createVerticalStrut(Theme.SP_XS));
        productBox.add(productError);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = 0;
        gc.anchor = GridBagConstraints.NORTHWEST;
        gc.insets = new Insets(0, 0, 0, Theme.SP_L);
        gc.gridx = 0;
        form.add(productBox, gc);
        gc.gridx = 1;
        quantity.setPreferredSize(new Dimension(170, quantity.getPreferredSize().height));
        form.add(quantity, gc);
        gc.gridx = 2;
        gc.insets = new Insets(Theme.SP_L + Theme.SP_XS + 2, 0, 0, 0);
        form.add(submit, gc);
        gc.gridx = 3;
        gc.weightx = 1;
        form.add(Box.createHorizontalGlue(), gc);

        Card formCard = new Card(new BorderLayout(0, Theme.SP_M));
        formCard.add(cardTitle("Registrar salida"), BorderLayout.NORTH);
        formCard.add(form, BorderLayout.CENTER);

        lowStockWarning.setName("salidas-low-stock");
        lowStockWarning.setFont(Theme.FONT_BODY_BOLD);
        lowStockWarning.setForeground(Theme.Tone.AMBER.fg);
        lowStockWarning.setVisible(false);
        JPanel resultHeader = new JPanel(new BorderLayout(0, Theme.SP_XS));
        resultHeader.setOpaque(false);
        resultHeader.add(resultTitle, BorderLayout.NORTH);
        resultHeader.add(lowStockWarning, BorderLayout.CENTER);
        resultHeader.setBorder(BorderFactory.createEmptyBorder(0, 0, Theme.SP_M, 0));

        Card resultCard = new Card(new BorderLayout());
        resultCard.add(resultHeader, BorderLayout.NORTH);
        resultCard.add(table, BorderLayout.CENTER);
        table.setEmptyMessage("Aún no hay una salida registrada",
                "Aquí verás de qué lotes salió la mercancía (primero vence, primero sale).");

        JPanel column = new JPanel(new BorderLayout(0, Theme.SP_S));
        column.setOpaque(false);
        column.add(formCard, BorderLayout.NORTH);
        column.add(resultCard, BorderLayout.CENTER);
        add(column, BorderLayout.CENTER);
    }

    /** Valida, pide confirmación y registra la salida (también usable desde pruebas con el diálogo simulado). */
    void submit() {
        productError.setText(" ");
        String code = selectedCode(productCombo);
        boolean ok = true;
        if (code == null) {
            productError.setText("Selecciona un producto.");
            ok = false;
        }
        String qtyError = Validation.integerError(quantity.text(), 1);
        quantity.setError(qtyError);
        if (qtyError != null) ok = false;
        if (!ok) return;

        int qty = Validation.parseInt(quantity.text());
        if (!Dialogs.confirm(shell.window(), "Confirmar salida",
                "¿Registrar la salida de " + qty + " unidades de " + productCombo.getSelectedItem() + "?",
                "Registrar salida")) {
            return;
        }
        try {
            List<ExitAllocation> allocations = shell.useCase().registerExit(code, qty);
            showResult(code, qty, allocations);
            quantity.input().setText("");
            shell.notifySuccess("Salida registrada");
        } catch (Exception ex) {
            Dialogs.error(shell.window(), Messages.of(ex));
        }
    }

    private void showResult(String code, int qty, List<ExitAllocation> allocations) {
        List<Object[]> rows = new ArrayList<>();
        for (ExitAllocation a : allocations) {
            rows.add(new Object[]{a.lotCode(), a.dueDate().format(Validation.DATE_FORMAT), a.quantity()});
        }
        table.setRows(rows);
        resultTitle.setText("Asignación FEFO — salida de " + qty + " unidades de " + code);

        Product p = find(shell.useCase().listProducts(), code);
        if (p != null && p.isLowStock(shell.clock().today())) {
            lowStockWarning.setText("Aviso: el producto quedó con stock bajo (disponible "
                    + p.getAvailableStock(shell.clock().today()) + ", mínimo " + p.getMinStock() + ").");
            lowStockWarning.setVisible(true);
        } else {
            lowStockWarning.setVisible(false);
        }
    }

    @Override
    void refresh() {
        updating = true;
        try {
            fillProducts(productCombo, shell.useCase().listProducts(), null);
        } finally {
            updating = false;
        }
        submit.setEnabled(productCombo.getItemCount() > 0);
    }
}
