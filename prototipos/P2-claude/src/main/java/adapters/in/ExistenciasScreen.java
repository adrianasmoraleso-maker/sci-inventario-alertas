package adapters.in;

import domain.Lot;
import domain.LotStatus;
import domain.Product;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Existencias de un producto: indicadores y lotes en orden FEFO (RF08, RF-UI06). */
class ExistenciasScreen extends ScreenPanel {
    private final StyledCombo<ProductItem> productCombo = new StyledCombo<>();
    private final StatCard available = new StatCard("stat-available", Icons.CHECK, Theme.Tone.GREEN, "Stock disponible");
    private final StatCard expired = new StatCard("stat-expired-stock", Icons.BLOCK, Theme.Tone.RED, "Stock caducado");
    private final StatCard minimum = new StatCard("stat-minimum", Icons.TARGET, Theme.Tone.BLUE, "Stock mínimo");
    private final DataTable table = new DataTable("existencias-table", new DataTable.Column[]{
            new DataTable.Column("Código de lote", DataTable.Kind.TEXT, 150),
            new DataTable.Column("Vencimiento", DataTable.Kind.TEXT, 130),
            new DataTable.Column("Saldo", DataTable.Kind.TEXT, 100),
            new DataTable.Column("Días restantes", DataTable.Kind.TEXT, 120),
            new DataTable.Column("Estado", DataTable.Kind.PILL, 160)});
    private boolean updating;

    ExistenciasScreen(AppShell shell) {
        super(shell, "screen-existencias");
        productCombo.setName("existencias-product");
        productCombo.addActionListener(e -> { if (!updating) refresh(); });

        JPanel selector = toolbar(new JComponent[]{toolbarLabel("Producto:"), productCombo});
        selector.setBorder(BorderFactory.createEmptyBorder(0, Theme.SHADOW_SIZE, Theme.SP_XS, 0));

        JPanel stats = new JPanel(new GridLayout(1, 3, Theme.SP_XS, 0));
        stats.setOpaque(false);
        stats.add(available);
        stats.add(expired);
        stats.add(minimum);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(selector, BorderLayout.NORTH);
        top.add(stats, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);

        Card card = new Card(new BorderLayout());
        JLabel title = cardTitle("Lotes en orden FEFO");
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, Theme.SP_M, 0));
        card.add(title, BorderLayout.NORTH);
        card.add(table, BorderLayout.CENTER);
        add(card, BorderLayout.CENTER);
    }

    @Override
    void refresh() {
        List<Product> products = shell.useCase().listProducts();
        updating = true;
        try {
            fillProducts(productCombo, products, null);
        } finally {
            updating = false;
        }
        Product p = find(products, selectedCode(productCombo));
        LocalDate today = shell.clock().today();
        List<Object[]> rows = new ArrayList<>();
        if (p == null) {
            available.setValue("0");
            expired.setValue("0");
            minimum.setValue("0");
            table.setEmptyMessage("Aún no hay productos", "Registra un producto en la sección Productos.");
        } else {
            available.setValue(String.valueOf(p.getAvailableStock(today)));
            expired.setValue(String.valueOf(p.getExpiredStock(today)));
            minimum.setValue(String.valueOf(p.getMinStock()));
            for (Lot l : p.getLotsInFefoOrder(today)) {
                LotStatus st = p.getLotStatus(l, today);
                rows.add(new Object[]{l.getCode(), l.getDueDate().format(Validation.DATE_FORMAT), l.getBalance(),
                        st == LotStatus.CADUCADO ? "—" : String.valueOf(l.daysRemaining(today)),
                        new DataTable.Badge(st.getLabel(), Theme.toneOf(st))});
            }
            table.setEmptyMessage("Este producto aún no tiene lotes",
                    "Registra un lote en la sección Lotes para ver sus existencias.");
        }
        table.setRows(rows);
    }
}
