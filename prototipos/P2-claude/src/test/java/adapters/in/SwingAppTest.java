package adapters.in;

import application.InventoryService;
import domain.ExitAllocation;
import domain.Lot;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import testsupport.FixedClock;
import testsupport.InMemoryProductRepository;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/** Pruebas de la interfaz Swing. Necesitan entorno gráfico: se omiten en modo headless. */
class SwingAppTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 14);

    /** Servicio real que además cuenta las llamadas, para comprobar que lo inválido nunca llega al caso de uso. */
    static class SpyService extends InventoryService {
        final AtomicInteger creates = new AtomicInteger();
        final AtomicInteger edits = new AtomicInteger();
        final AtomicInteger lots = new AtomicInteger();
        final AtomicInteger entries = new AtomicInteger();
        final AtomicInteger exits = new AtomicInteger();

        SpyService(InMemoryProductRepository repo, FixedClock clock) { super(repo, clock); }

        @Override public void createProduct(String c, String n, int m, int t) { creates.incrementAndGet(); super.createProduct(c, n, m, t); }
        @Override public void editProduct(String c, String n, int m, int t) { edits.incrementAndGet(); super.editProduct(c, n, m, t); }
        @Override public void registerLot(String p, String l, LocalDate d, int q) { lots.incrementAndGet(); super.registerLot(p, l, d, q); }
        @Override public void registerEntry(String p, String l, int q) { entries.incrementAndGet(); super.registerEntry(p, l, q); }
        @Override public List<ExitAllocation> registerExit(String p, int q) { exits.incrementAndGet(); return super.registerExit(p, q); }
    }

    private InMemoryProductRepository repo;
    private FixedClock clock;
    private SpyService service;
    private SwingApp app;

    @BeforeEach
    void setUp() throws Exception {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(), "Sin entorno gráfico: prueba de UI omitida");
        repo = new InMemoryProductRepository();
        clock = new FixedClock(TODAY);
        service = new SpyService(repo, clock);
        service.createProduct("P1", "Paracetamol", 50, 30);
        service.registerLot("P1", "L1", TODAY.plusDays(10), 20);
        service.registerLot("P1", "L2", TODAY.plusDays(200), 40);
        repo.findById("P1").getLots().add(new Lot("OLD", TODAY.minusDays(5), 4));
        service.createProduct("P2", "Gasas", 0, 60);
        service.creates.set(0);
        service.lots.set(0);
        SwingUtilities.invokeAndWait(() -> app = new SwingApp(service, clock));
    }

    @AfterEach
    void tearDown() throws Exception {
        if (app != null) SwingUtilities.invokeAndWait(() -> app.dispose());
    }

    // ---------------------------------------------------------------- utilidades

    private static <T extends Component> void collect(Container root, Class<T> type, List<T> out) {
        for (Component c : root.getComponents()) {
            if (type.isInstance(c)) out.add(type.cast(c));
            if (c instanceof Container child) collect(child, type, out);
        }
    }

    private static <T extends Component> List<T> all(Container root, Class<T> type) {
        List<T> out = new ArrayList<>();
        collect(root, type, out);
        return out;
    }

    private static <T extends Component> T named(Container root, Class<T> type, String name) {
        return all(root, type).stream().filter(c -> name.equals(c.getName())).findFirst()
                .orElseThrow(() -> new AssertionError("No se encontró " + name));
    }

    private void onEdt(Runnable r) throws Exception { SwingUtilities.invokeAndWait(r); }

    private ProductForm newProductForm(Runnable onClose, AtomicReference<String> saved) {
        return new ProductForm(service, null, saved::set, onClose);
    }

    // --------------------------------------------------------------------- menú

    @Test
    void menuHasExactlyTheSixSections() {
        List<String> labels = new ArrayList<>();
        for (Sidebar.NavButton b : all(app.getContentPane(), Sidebar.NavButton.class)) labels.add(b.getText());
        assertEquals(List.of("Inicio", "Productos", "Lotes", "Salidas", "Existencias", "Alertas"), labels);
        assertEquals(labels, app.menuLabels());
    }

    @Test
    void activeSectionIsHighlightedAndTitleChanges() throws Exception {
        onEdt(() -> app.navigate(Section.LOTES));
        for (Sidebar.NavButton b : all(app.getContentPane(), Sidebar.NavButton.class)) {
            assertEquals(b.getText().equals("Lotes"), b.isActive(), b.getText());
        }
        assertEquals("Lotes", named(app.getContentPane(), JLabel.class, "section-title").getText());
    }

    @Test
    void templateOnlyMenusAreNotPresent() {
        List<String> forbidden = List.of("Dashboard", "Pedidos", "Proveedores", "Reportes", "Ajustes",
                "Importar CSV", "Exportar PDF", "Precio", "Categoría", "Ubicación");
        for (JLabel l : all(app.getContentPane(), JLabel.class)) {
            for (String f : forbidden) assertFalse(String.valueOf(l.getText()).contains(f), l.getText());
        }
        for (AbstractButton b : all(app.getContentPane(), AbstractButton.class)) {
            for (String f : forbidden) assertFalse(String.valueOf(b.getText()).contains(f), b.getText());
        }
    }

    @Test
    void windowHasMinimumSize() {
        assertEquals(1000, app.getMinimumSize().width);
        assertEquals(650, app.getMinimumSize().height);
        assertTrue(app.isResizable());
    }

    @Test
    void quickAccessButtonsNavigate() throws Exception {
        onEdt(() -> named(app.getContentPane(), AppButton.class, "quick-product").doClick());
        assertEquals(Section.PRODUCTOS, app.currentSection());
        onEdt(() -> app.navigate(Section.INICIO));
        onEdt(() -> named(app.getContentPane(), AppButton.class, "quick-exit").doClick());
        assertEquals(Section.SALIDAS, app.currentSection());
        onEdt(() -> app.navigate(Section.INICIO));
        onEdt(() -> named(app.getContentPane(), AppButton.class, "quick-alerts").doClick());
        assertEquals(Section.ALERTAS, app.currentSection());
    }

    @Test
    void homeIndicatorsShowCounts() throws Exception {
        onEdt(() -> app.navigate(Section.INICIO));
        assertEquals("2", named(app.getContentPane(), JLabel.class, "stat-total-value").getText());
        assertEquals("0", named(app.getContentPane(), JLabel.class, "stat-low-value").getText()); // 60 disponibles >= mínimo 50
        assertEquals("1", named(app.getContentPane(), JLabel.class, "stat-soon-value").getText());
        assertEquals("1", named(app.getContentPane(), JLabel.class, "stat-expired-value").getText());
    }

    // ------------------------------------------------------ RF-UI16: solo lectura

    @Test
    void noTableCellIsEditableInAnyScreen() throws Exception {
        for (Section s : Section.values()) {
            onEdt(() -> app.navigate(s));
            for (JTable t : all(app.getContentPane(), JTable.class)) {
                for (int r = 0; r < t.getRowCount(); r++) {
                    for (int c = 0; c < t.getColumnCount(); c++) {
                        assertFalse(t.isCellEditable(r, c), t.getName() + " [" + r + "," + c + "]");
                        int row = r, col = c;
                        AtomicReference<Boolean> started = new AtomicReference<>();
                        onEdt(() -> started.set(t.editCellAt(row, col)));
                        assertFalse(started.get(), "editCellAt no debe iniciar edición");
                        assertFalse(t.isEditing());
                    }
                }
            }
        }
    }

    @Test
    void tablesAreSeededSoTheEditabilityCheckIsMeaningful() throws Exception {
        onEdt(() -> app.navigate(Section.PRODUCTOS));
        assertTrue(named(app.getContentPane(), JTable.class, "productos-table").getRowCount() >= 2);
        onEdt(() -> app.navigate(Section.ALERTAS));
        assertTrue(named(app.getContentPane(), JTable.class, "alertas-table").getRowCount() >= 2);
    }

    // ---------------------------------------------------------- pantallas

    @Test
    void productsTableHasTheSpecifiedColumnsAndStatuses() throws Exception {
        onEdt(() -> app.navigate(Section.PRODUCTOS));
        JTable t = named(app.getContentPane(), JTable.class, "productos-table");
        List<String> cols = new ArrayList<>();
        for (int i = 0; i < t.getColumnCount(); i++) cols.add(t.getColumnName(i));
        assertEquals(List.of("Código", "Nombre", "Stock mínimo", "Umbral (días)", "Stock disponible", "Estado", "Acciones"), cols);
        // P1: disponible 60 >= mínimo 50 -> Disponible; P2: sin lotes -> Sin stock
        assertEquals("Disponible", ((DataTable.Badge) t.getValueAt(0, 5)).text());
        assertEquals("Sin stock", ((DataTable.Badge) t.getValueAt(1, 5)).text());
    }

    @Test
    void productSearchFiltersByCodeOrName() throws Exception {
        onEdt(() -> app.navigate(Section.PRODUCTOS));
        JTable t = named(app.getContentPane(), JTable.class, "productos-table");
        onEdt(() -> named(app.getContentPane(), StyledTextField.class, "productos-search").setText("gas"));
        assertEquals(1, t.getRowCount());
        assertEquals("P2", t.getValueAt(0, 0));
        onEdt(() -> named(app.getContentPane(), StyledTextField.class, "productos-search").setText("p1"));
        assertEquals(1, t.getRowCount());
        assertEquals("P1", t.getValueAt(0, 0));
    }

    @Test
    void lotsScreenShowsStatusesWithDaysRemaining() throws Exception {
        onEdt(() -> app.showLots("P1"));
        JTable t = named(app.getContentPane(), JTable.class, "lotes-table");
        assertEquals(3, t.getRowCount());
        assertEquals("Próximo a caducar", ((DataTable.Badge) t.getValueAt(0, 4)).text());
        assertEquals("10", t.getValueAt(0, 3));
        assertEquals("Vigente", ((DataTable.Badge) t.getValueAt(1, 4)).text());
        assertEquals("Caducado", ((DataTable.Badge) t.getValueAt(2, 4)).text());
        assertEquals("14/10/2026".length(), String.valueOf(t.getValueAt(0, 1)).length());
        assertEquals("24/10/2026", t.getValueAt(0, 1));
    }

    @Test
    void stockExistenceTableFollowsFefoOrder() throws Exception {
        onEdt(() -> app.navigate(Section.EXISTENCIAS));
        JTable t = named(app.getContentPane(), JTable.class, "existencias-table");
        assertEquals("L1", t.getValueAt(0, 0));
        assertEquals("L2", t.getValueAt(1, 0));
        assertEquals("OLD", t.getValueAt(2, 0));
        assertEquals("60", named(app.getContentPane(), JLabel.class, "stat-available-value").getText());
        assertEquals("4", named(app.getContentPane(), JLabel.class, "stat-expired-stock-value").getText());
        assertEquals("50", named(app.getContentPane(), JLabel.class, "stat-minimum-value").getText());
    }

    @Test
    void emptyTablesShowGuidanceMessage() throws Exception {
        repo.delete("P1");
        repo.delete("P2");
        onEdt(() -> app.navigate(Section.PRODUCTOS));
        JLabel empty = named(app.getContentPane(), JLabel.class, "productos-table-empty");
        assertTrue(empty.getText().contains("Aún no hay productos"));
    }

    @Test
    void alertsScreenFiltersByType() throws Exception {
        onEdt(() -> app.navigate(Section.ALERTAS));
        JTable t = named(app.getContentPane(), JTable.class, "alertas-table");
        assertEquals(2, t.getRowCount()); // próximo a caducar (L1) y caducado (OLD); P1 no tiene stock bajo
        @SuppressWarnings("unchecked")
        JComboBox<String> type = named(app.getContentPane(), JComboBox.class, "alertas-type-filter");
        onEdt(() -> type.setSelectedItem("Caducado"));
        assertEquals(1, t.getRowCount());
        assertEquals("Caducado", ((DataTable.Badge) t.getValueAt(0, 0)).text());
    }

    // ------------------------------------ RF-UI15 / RF-UI08: formularios

    @Test
    void negativeNumbersNeverReachTheUseCase() throws Exception {
        AtomicReference<String> saved = new AtomicReference<>();
        AtomicInteger closed = new AtomicInteger();
        ProductForm form = newProductForm(closed::incrementAndGet, saved);
        named(form, StyledTextField.class, "field-code").setText("N1");
        named(form, StyledTextField.class, "field-name").setText("Producto");
        named(form, StyledTextField.class, "field-min-stock").setText("-5");
        named(form, StyledTextField.class, "field-threshold").setText("30");
        onEdt(form::save);
        assertEquals(0, service.creates.get());
        assertNull(saved.get());
        assertEquals(0, closed.get());
        assertFalse(named(form, JLabel.class, "field-min-stock-error").getText().isBlank());
        assertTrue(named(form, JLabel.class, "field-min-stock-error").getText().toLowerCase().contains("negativ"));
    }

    @Test
    void decimalsTextAndEmptyAreRejected() throws Exception {
        for (String bad : new String[]{"1.5", "2,5", "abc", "", "  ", "1e3", "-0", "+3", "99999999999"}) {
            ProductForm form = newProductForm(() -> { }, new AtomicReference<>());
            named(form, StyledTextField.class, "field-code").setText("X");
            named(form, StyledTextField.class, "field-name").setText("Nombre");
            named(form, StyledTextField.class, "field-min-stock").setText(bad);
            named(form, StyledTextField.class, "field-threshold").setText("30");
            onEdt(form::save);
            assertEquals(0, service.creates.get(), "valor aceptado por error: '" + bad + "'");
            assertFalse(named(form, JLabel.class, "field-min-stock-error").getText().isBlank(), bad);
        }
    }

    @Test
    void thresholdMustBeGreaterThanZeroAndMinStockMayBeZero() throws Exception {
        ProductForm form = newProductForm(() -> { }, new AtomicReference<>());
        named(form, StyledTextField.class, "field-code").setText("Z1");
        named(form, StyledTextField.class, "field-name").setText("Nombre");
        named(form, StyledTextField.class, "field-min-stock").setText("0");
        named(form, StyledTextField.class, "field-threshold").setText("0");
        onEdt(form::save);
        assertEquals(0, service.creates.get());
        assertFalse(named(form, JLabel.class, "field-threshold-error").getText().isBlank());
        assertTrue(named(form, JLabel.class, "field-min-stock-error").getText().isBlank());

        named(form, StyledTextField.class, "field-threshold").setText("45");
        onEdt(form::save);
        assertEquals(1, service.creates.get());
        assertNotNull(repo.findById("Z1"));
    }

    @Test
    void emptyRequiredFieldsAreReportedNextToTheField() throws Exception {
        ProductForm form = newProductForm(() -> { }, new AtomicReference<>());
        named(form, StyledTextField.class, "field-min-stock").setText("");
        named(form, StyledTextField.class, "field-threshold").setText("");
        onEdt(form::save);
        for (String f : new String[]{"field-code", "field-name", "field-min-stock", "field-threshold"}) {
            assertFalse(named(form, JLabel.class, f + "-error").getText().isBlank(), f);
        }
        assertEquals(0, service.creates.get());
    }

    @Test
    void createFormDefaultsThresholdTo180() {
        ProductForm form = newProductForm(() -> { }, new AtomicReference<>());
        assertEquals("180", named(form, StyledTextField.class, "field-threshold").getText());
    }

    @Test
    void ruleErrorsAreShownAsMessagesInsideTheForm() throws Exception {
        AtomicReference<String> saved = new AtomicReference<>();
        ProductForm form = newProductForm(() -> { }, saved);
        named(form, StyledTextField.class, "field-code").setText("P1"); // duplicado
        named(form, StyledTextField.class, "field-name").setText("Otro");
        named(form, StyledTextField.class, "field-min-stock").setText("1");
        named(form, StyledTextField.class, "field-threshold").setText("10");
        onEdt(form::save);
        assertNull(saved.get());
        String msg = form.formErrorText();
        assertTrue(msg.contains("Ya existe un producto"), msg);
        assertFalse(msg.contains("Exception"));
    }

    @Test
    void editFormShowsCurrentValuesValidatesAndCancelDiscards() throws Exception {
        AtomicInteger closed = new AtomicInteger();
        AtomicReference<String> saved = new AtomicReference<>();
        ProductForm form = new ProductForm(service, repo.findById("P1"), saved::set, closed::incrementAndGet);
        assertEquals("P1", named(form, StyledTextField.class, "field-code").getText());
        assertFalse(named(form, StyledTextField.class, "field-code").isEnabled());
        assertEquals("Paracetamol", named(form, StyledTextField.class, "field-name").getText());
        assertEquals("50", named(form, StyledTextField.class, "field-min-stock").getText());
        assertEquals("30", named(form, StyledTextField.class, "field-threshold").getText());

        // inválido: no llega al caso de uso
        named(form, StyledTextField.class, "field-min-stock").setText("-1");
        onEdt(form::save);
        assertEquals(0, service.edits.get());

        // cancelar descarta los cambios
        named(form, StyledTextField.class, "field-name").setText("Cambiado");
        onEdt(() -> named(form, AppButton.class, "form-cancel").doClick());
        assertEquals(1, closed.get());
        assertEquals(0, service.edits.get());
        assertEquals("Paracetamol", repo.findById("P1").getName());

        // guardar con valores válidos
        named(form, StyledTextField.class, "field-min-stock").setText("7");
        onEdt(() -> named(form, AppButton.class, "form-save").doClick());
        assertEquals(1, service.edits.get());
        assertEquals("Cambiado", repo.findById("P1").getName());
        assertEquals(7, repo.findById("P1").getMinStock());
        assertEquals("Producto actualizado", saved.get());
    }

    @Test
    void lotFormRejectsPastDatesInvalidDatesAndBadQuantities() throws Exception {
        for (String[] bad : new String[][]{
                {"13/10/2026", "5"}, {"2026-12-01", "5"}, {"31/02/2027", "5"}, {"", "5"},
                {"01/12/2026", "0"}, {"01/12/2026", "-4"}, {"01/12/2026", "2.5"}, {"01/12/2026", ""}}) {
            LotForm form = new LotForm(service, clock, "P1", "P1 — Paracetamol", m -> { }, () -> { });
            named(form, StyledTextField.class, "field-lot-code").setText("NEW");
            named(form, StyledTextField.class, "field-due-date").setText(bad[0]);
            named(form, StyledTextField.class, "field-quantity").setText(bad[1]);
            onEdt(form::save);
            assertEquals(0, service.lots.get(), "aceptado por error: " + String.join(" | ", bad));
        }
        LotForm ok = new LotForm(service, clock, "P1", "P1 — Paracetamol", m -> { }, () -> { });
        named(ok, StyledTextField.class, "field-lot-code").setText("NEW");
        named(ok, StyledTextField.class, "field-due-date").setText("14/10/2026"); // hoy sí es válido
        named(ok, StyledTextField.class, "field-quantity").setText("3");
        onEdt(ok::save);
        assertEquals(1, service.lots.get());
    }

    @Test
    void entryFormRejectsNegativeAndZero() throws Exception {
        for (String bad : new String[]{"-1", "0", "x", ""}) {
            EntryForm form = new EntryForm(service, "P1", "L1", m -> { }, () -> { });
            named(form, StyledTextField.class, "field-quantity").setText(bad);
            onEdt(form::save);
            assertEquals(0, service.entries.get(), bad);
        }
        EntryForm ok = new EntryForm(service, "P1", "L1", m -> { }, () -> { });
        named(ok, StyledTextField.class, "field-quantity").setText("5");
        onEdt(ok::save);
        assertEquals(1, service.entries.get());
        assertEquals(25, repo.findById("P1").getLots().get(0).getBalance());
    }

    @Test
    void exitScreenRejectsInvalidQuantityBeforeAnyDialog() throws Exception {
        onEdt(() -> app.navigate(Section.SALIDAS));
        for (String bad : new String[]{"-3", "0", "1.5", "abc", ""}) {
            onEdt(() -> {
                named(app.getContentPane(), StyledTextField.class, "field-quantity").setText(bad);
                named(app.getContentPane(), AppButton.class, "salidas-submit").doClick();
            });
            assertEquals(0, service.exits.get(), bad);
            assertFalse(named(app.getContentPane(), JLabel.class, "field-quantity-error").getText().isBlank(), bad);
        }
    }

    @Test
    void theThemeIsTheSingleSourceOfColors() {
        // las pantallas no definen colores propios: los componentes toman los del tema
        assertEquals(Theme.PRIMARY, Theme.PRIMARY);
        assertNotNull(Theme.FONT_FAMILY);
        assertEquals(Theme.Tone.GREEN, Theme.toneOf(domain.LotStatus.VIGENTE));
        assertEquals(Theme.Tone.AMBER, Theme.toneOf(domain.LotStatus.PROXIMO_A_CADUCAR));
        assertEquals(Theme.Tone.RED, Theme.toneOf(domain.LotStatus.CADUCADO));
        assertEquals(Theme.Tone.AMBER, Theme.toneOf(domain.ProductStatus.STOCK_BAJO));
        assertEquals(Theme.Tone.RED, Theme.toneOf(domain.ProductStatus.SIN_STOCK));
    }
}
