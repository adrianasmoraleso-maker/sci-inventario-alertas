package adapters.in;

import domain.Product;
import domain.ProductStatus;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Productos: búsqueda, filtro de estado, tabla y acciones Editar / Eliminar / Ver lotes (RF01 a RF04). */
class ProductosScreen extends ScreenPanel {
    private static final String ALL = "Todos";

    private final StyledTextField search = new StyledTextField(22);
    private final StyledCombo<String> statusFilter = new StyledCombo<>(new String[]{
            ALL, ProductStatus.DISPONIBLE.getLabel(), ProductStatus.STOCK_BAJO.getLabel(),
            ProductStatus.SIN_STOCK.getLabel()});
    private final DataTable table = new DataTable("productos-table", new DataTable.Column[]{
            new DataTable.Column("Código", DataTable.Kind.TEXT, 75),
            new DataTable.Column("Nombre", DataTable.Kind.TEXT, 240),
            new DataTable.Column("Stock mínimo", DataTable.Kind.TEXT, 100),
            new DataTable.Column("Umbral (días)", DataTable.Kind.TEXT, 105),
            new DataTable.Column("Stock disponible", DataTable.Kind.TEXT, 125),
            new DataTable.Column("Estado", DataTable.Kind.PILL, 110),
            new DataTable.Column("Acciones", DataTable.Kind.ACTIONS, 0)},
            "Editar", "Eliminar", "Ver lotes");
    private List<Product> shown = new ArrayList<>();

    ProductosScreen(AppShell shell) {
        super(shell, "screen-productos");
        search.setName("productos-search");
        search.setPlaceholder("Buscar por código o nombre");
        search.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { refresh(); }
            @Override public void removeUpdate(DocumentEvent e) { refresh(); }
            @Override public void changedUpdate(DocumentEvent e) { refresh(); }
        });
        statusFilter.setName("productos-status-filter");
        statusFilter.addActionListener(e -> refresh());

        AppButton newButton = new AppButton("Nuevo producto", AppButton.Style.PRIMARY);
        newButton.setName("productos-new");
        newButton.addActionListener(e -> Dialogs.showForm(shell.window(), "Nuevo producto",
                close -> new ProductForm(shell.useCase(), null, shell::notifySuccess, close)));

        table.setActionHandler(this::onAction);

        Card card = new Card(new BorderLayout());
        card.add(toolbar(new JComponent[]{search, toolbarLabel("Estado:"), statusFilter}, newButton), BorderLayout.NORTH);
        card.add(table, BorderLayout.CENTER);
        add(card, BorderLayout.CENTER);
    }

    private void onAction(int row, int action) {
        if (row < 0 || row >= shown.size()) return;
        Product p = shown.get(row);
        switch (action) {
            case 0 -> Dialogs.showForm(shell.window(), "Editar producto",
                    close -> new ProductForm(shell.useCase(), p, shell::notifySuccess, close));
            case 1 -> {
                if (Dialogs.confirm(shell.window(), "Eliminar producto",
                        "¿Eliminar el producto «" + p.getName() + "» (" + p.getCode() + ")? Esta acción no se puede deshacer.",
                        "Eliminar")) {
                    try {
                        shell.useCase().deleteProduct(p.getCode());
                        shell.notifySuccess("Producto eliminado");
                    } catch (Exception ex) {
                        Dialogs.error(shell.window(), Messages.of(ex));
                    }
                }
            }
            case 2 -> shell.showLots(p.getCode());
            default -> { }
        }
    }

    @Override
    void refresh() {
        List<Product> all = shell.useCase().listProducts();
        LocalDate today = shell.clock().today();
        String query = search.getText().trim().toLowerCase(Locale.ROOT);
        String status = (String) statusFilter.getSelectedItem();

        shown = new ArrayList<>();
        List<Object[]> rows = new ArrayList<>();
        for (Product p : all) {
            ProductStatus ps = p.getStockStatus(today);
            boolean matchesText = query.isEmpty() || p.getCode().toLowerCase(Locale.ROOT).contains(query)
                    || p.getName().toLowerCase(Locale.ROOT).contains(query);
            boolean matchesStatus = status == null || ALL.equals(status) || ps.getLabel().equals(status);
            if (!matchesText || !matchesStatus) continue;
            shown.add(p);
            rows.add(new Object[]{p.getCode(), p.getName(), p.getMinStock(), p.getAlertThresholdDays(),
                    p.getAvailableStock(today), new DataTable.Badge(ps.getLabel(), Theme.toneOf(ps)), null});
        }
        if (all.isEmpty()) {
            table.setEmptyMessage("Aún no hay productos",
                    "Usa el botón «Nuevo producto» para registrar el primero.");
        } else {
            table.setEmptyMessage("Ningún producto coincide con la búsqueda",
                    "Prueba con otro texto o cambia el filtro de estado.");
        }
        table.setRows(rows);
    }
}
