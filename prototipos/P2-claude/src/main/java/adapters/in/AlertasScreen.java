package adapters.in;

import domain.Alert;
import domain.Product;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Alertas calculadas al consultar, con filtros por tipo y por producto (RF09, RF-UI07). */
class AlertasScreen extends ScreenPanel {
    private static final String ALL = "Todas";
    private static final Map<String, String> TYPE_LABELS = Map.of(
            "STOCK_BAJO", "Stock bajo",
            "PROXIMO_A_CADUCAR", "Próximo a caducar",
            "CADUCADO", "Caducado");

    private final StyledCombo<String> typeFilter = new StyledCombo<>(new String[]{
            ALL, "Stock bajo", "Próximo a caducar", "Caducado"});
    private final StyledCombo<ProductItem> productFilter = new StyledCombo<>();
    private final DataTable table = new DataTable("alertas-table", new DataTable.Column[]{
            new DataTable.Column("Tipo", DataTable.Kind.PILL, 150),
            new DataTable.Column("Producto", DataTable.Kind.TEXT, 270),
            new DataTable.Column("Lote", DataTable.Kind.TEXT, 100),
            new DataTable.Column("Mensaje", DataTable.Kind.TEXT, 400)});
    private boolean updating;

    AlertasScreen(AppShell shell) {
        super(shell, "screen-alertas");
        typeFilter.setName("alertas-type-filter");
        typeFilter.addActionListener(e -> { if (!updating) refresh(); });
        productFilter.setName("alertas-product-filter");
        productFilter.addActionListener(e -> { if (!updating) refresh(); });

        Card card = new Card(new BorderLayout());
        card.add(toolbar(new JComponent[]{toolbarLabel("Tipo:"), typeFilter, toolbarLabel("Producto:"), productFilter}),
                BorderLayout.NORTH);
        card.add(table, BorderLayout.CENTER);
        add(card, BorderLayout.CENTER);
    }

    private static Theme.Tone toneOf(String type) {
        return "CADUCADO".equals(type) ? Theme.Tone.RED : Theme.Tone.AMBER;
    }

    @Override
    void refresh() {
        List<Product> products = shell.useCase().listProducts();
        updating = true;
        try {
            fillProducts(productFilter, products, ProductItem.all("Todos"));
        } finally {
            updating = false;
        }
        Map<String, String> names = new HashMap<>();
        for (Product p : products) names.put(p.getCode(), p.getCode() + " — " + p.getName());

        String typeLabel = (String) typeFilter.getSelectedItem();
        String productCode = selectedCode(productFilter);
        List<Alert> all = shell.useCase().getAlerts();
        List<Object[]> rows = new ArrayList<>();
        for (Alert a : all) {
            String label = TYPE_LABELS.getOrDefault(a.getType(), a.getType());
            if (typeLabel != null && !ALL.equals(typeLabel) && !label.equals(typeLabel)) continue;
            if (productCode != null && !productCode.equals(a.getProductCode())) continue;
            rows.add(new Object[]{new DataTable.Badge(label, toneOf(a.getType())),
                    names.getOrDefault(a.getProductCode(), a.getProductCode()),
                    a.getLotCode() == null ? "—" : a.getLotCode(), a.getMessage()});
        }
        if (all.isEmpty()) {
            table.setEmptyMessage("Sin alertas", "El inventario está en orden: no hay stock bajo ni lotes por caducar.");
        } else {
            table.setEmptyMessage("Ninguna alerta coincide con el filtro", "Cambia el tipo o el producto.");
        }
        table.setRows(rows);
    }
}
