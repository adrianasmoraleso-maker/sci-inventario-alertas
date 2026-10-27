package adapters.in;

import domain.Lot;
import domain.LotStatus;
import domain.Product;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Lotes por producto: tabla, filtro de estado y acciones Entrada / Eliminar caducado (RF05, RF06, RF10). */
class LotesScreen extends ScreenPanel {
    private static final String ALL = "Todos";

    private final StyledCombo<ProductItem> productCombo = new StyledCombo<>();
    private final StyledCombo<String> statusFilter = new StyledCombo<>(new String[]{
            ALL, LotStatus.VIGENTE.getLabel(), LotStatus.PROXIMO_A_CADUCAR.getLabel(), LotStatus.CADUCADO.getLabel()});
    private final AppButton registerButton = new AppButton("Registrar lote", AppButton.Style.PRIMARY);
    private final DataTable table = new DataTable("lotes-table", new DataTable.Column[]{
            new DataTable.Column("Código de lote", DataTable.Kind.TEXT, 140),
            new DataTable.Column("Vencimiento", DataTable.Kind.TEXT, 120),
            new DataTable.Column("Saldo", DataTable.Kind.TEXT, 90),
            new DataTable.Column("Días restantes", DataTable.Kind.TEXT, 110),
            new DataTable.Column("Estado", DataTable.Kind.PILL, 150),
            new DataTable.Column("Acciones", DataTable.Kind.ACTIONS, 0)},
            "Entrada", "Eliminar");

    private boolean updating;
    private Product shownProduct;
    private List<Lot> shownLots = new ArrayList<>();
    private List<LotStatus> shownStatuses = new ArrayList<>();

    LotesScreen(AppShell shell) {
        super(shell, "screen-lotes");
        productCombo.setName("lotes-product");
        productCombo.addActionListener(e -> { if (!updating) refresh(); });
        statusFilter.setName("lotes-status-filter");
        statusFilter.addActionListener(e -> { if (!updating) refresh(); });

        registerButton.setName("lotes-register");
        registerButton.addActionListener(e -> {
            if (shownProduct == null) return;
            Product p = shownProduct;
            Dialogs.showForm(shell.window(), "Registrar lote",
                    close -> new LotForm(shell.useCase(), shell.clock(), p.getCode(),
                            p.getCode() + " — " + p.getName(), shell::notifySuccess, close));
        });

        table.setActionHandler(this::onAction);
        // Eliminar solo está habilitado en lotes caducados
        table.setActionEnabled((row, action) ->
                action != 1 || (row >= 0 && row < shownStatuses.size() && shownStatuses.get(row) == LotStatus.CADUCADO));

        Card card = new Card(new BorderLayout());
        card.add(toolbar(new JComponent[]{toolbarLabel("Producto:"), productCombo, toolbarLabel("Estado:"), statusFilter},
                registerButton), BorderLayout.NORTH);
        card.add(table, BorderLayout.CENTER);
        add(card, BorderLayout.CENTER);
    }

    /** Selecciona un producto (usado por «Ver lotes» en Productos). */
    void selectProduct(String code) {
        updating = true;
        try {
            for (int i = 0; i < productCombo.getItemCount(); i++) {
                if (code != null && code.equals(productCombo.getItemAt(i).code())) {
                    productCombo.setSelectedIndex(i);
                }
            }
            statusFilter.setSelectedIndex(0);
        } finally {
            updating = false;
        }
        refresh();
    }

    private void onAction(int row, int action) {
        if (shownProduct == null || row < 0 || row >= shownLots.size()) return;
        Product p = shownProduct;
        Lot lot = shownLots.get(row);
        if (action == 0) {
            Dialogs.showForm(shell.window(), "Registrar entrada",
                    close -> new EntryForm(shell.useCase(), p.getCode(), lot.getCode(), shell::notifySuccess, close));
        } else if (action == 1 && shownStatuses.get(row) == LotStatus.CADUCADO) {
            if (Dialogs.confirm(shell.window(), "Eliminar lote caducado",
                    "¿Eliminar el lote " + lot.getCode() + "? Se descartará su saldo de " + lot.getBalance()
                            + " unidades.", "Eliminar")) {
                try {
                    shell.useCase().deleteExpiredLot(p.getCode(), lot.getCode());
                    shell.notifySuccess("Lote caducado eliminado");
                } catch (Exception ex) {
                    Dialogs.error(shell.window(), Messages.of(ex));
                }
            }
        }
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
        LocalDate today = shell.clock().today();
        shownProduct = find(products, selectedCode(productCombo));
        shownLots = new ArrayList<>();
        shownStatuses = new ArrayList<>();
        registerButton.setEnabled(shownProduct != null);

        List<Object[]> rows = new ArrayList<>();
        String filter = (String) statusFilter.getSelectedItem();
        if (shownProduct == null) {
            table.setEmptyMessage("Aún no hay productos",
                    "Primero registra un producto en la sección Productos.");
        } else {
            for (Lot l : shownProduct.getLots()) {
                LotStatus st = shownProduct.getLotStatus(l, today);
                if (filter != null && !ALL.equals(filter) && !st.getLabel().equals(filter)) continue;
                shownLots.add(l);
                shownStatuses.add(st);
                rows.add(new Object[]{l.getCode(), l.getDueDate().format(Validation.DATE_FORMAT), l.getBalance(),
                        st == LotStatus.CADUCADO ? "—" : String.valueOf(l.daysRemaining(today)),
                        new DataTable.Badge(st.getLabel(), Theme.toneOf(st)), null});
            }
            if (shownProduct.getLots().isEmpty()) {
                table.setEmptyMessage("Este producto aún no tiene lotes",
                        "Usa el botón «Registrar lote» para agregar el primero.");
            } else {
                table.setEmptyMessage("Ningún lote coincide con el filtro", "Prueba con otro estado.");
            }
        }
        table.setRows(rows);
    }
}
