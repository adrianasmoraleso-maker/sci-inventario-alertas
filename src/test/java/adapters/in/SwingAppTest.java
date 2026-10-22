package adapters.in;

import application.InventoryUseCase;
import domain.Alert;
import domain.ClockPort;
import domain.Product;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

class SwingAppTest {
    private static final String[] EXPECTED_TABS = {
            "Inicio", "Productos", "Lotes", "Salidas", "Existencias", "Alertas"
    };

    @Test
    void providesAllSixNavigationSections() throws Exception {
        runWhenGraphicsAreAvailable(() -> {
            SwingApp app = createApp();
            try {
                JTabbedPane tabs = getTabs(app);
                assertEquals(EXPECTED_TABS.length, tabs.getTabCount());
                for (int index = 0; index < EXPECTED_TABS.length; index++) {
                    assertEquals(EXPECTED_TABS[index], tabs.getTitleAt(index));
                    assertNotNull(tabs.getComponentAt(index));
                }
            } finally {
                app.dispose();
            }
        });
    }

    @Test
    void productSectionShowsExpectedColumnsAndCreateAndDeleteActions() throws Exception {
        runWhenGraphicsAreAvailable(() -> {
            SwingApp app = createApp();
            try {
                JPanel productsPanel = (JPanel) getTabs(app).getComponentAt(1);
                JTable table = findComponent(productsPanel, JTable.class);
                assertNotNull(table);
                assertArrayEquals(new Object[]{"Código", "Nombre", "Min Stock", "Umbral", "Disponible"},
                        columnNames(table));
                assertTrue(hasButton(productsPanel, "Crear"));
                assertTrue(hasButton(productsPanel, "Eliminar"));
            } finally {
                app.dispose();
            }
        });
    }

    @Test
    void homeQuickActionsNavigateToProductsAndExits() throws Exception {
        runWhenGraphicsAreAvailable(() -> {
            SwingApp app = createApp();
            try {
                JTabbedPane tabs = getTabs(app);
                JButton registerProduct = findButton((Container) tabs.getComponentAt(0), "Registrar Producto");
                assertNotNull(registerProduct);
                registerProduct.doClick();
                assertEquals(1, tabs.getSelectedIndex());

                JButton registerExit = findButton((Container) tabs.getComponentAt(0), "Registrar Salida");
                assertNotNull(registerExit);
                registerExit.doClick();
                assertEquals(3, tabs.getSelectedIndex());
            } finally {
                app.dispose();
            }
        });
    }

    private static void runWhenGraphicsAreAvailable(ThrowingRunnable test) throws Exception {
        AtomicReference<Throwable> failure = new AtomicReference<>();
        assumeFalse(GraphicsEnvironment.isHeadless(), "La prueba requiere un entorno gráfico");
        SwingUtilities.invokeAndWait(() -> {
            try {
                test.run();
            } catch (Throwable throwable) {
                failure.set(throwable);
            }
        });
        if (failure.get() != null) {
            if (failure.get() instanceof Exception exception) {
                throw exception;
            }
            if (failure.get() instanceof Error error) {
                throw error;
            }
            throw new InvocationTargetException(failure.get());
        }
    }

    private static SwingApp createApp() {
        InventoryUseCase useCase = new EmptyInventoryUseCase();
        ClockPort clock = () -> LocalDate.of(2026, 10, 22);
        return new SwingApp(useCase, clock);
    }

    private static JTabbedPane getTabs(SwingApp app) {
        return (JTabbedPane) app.getContentPane().getComponent(0);
    }

    private static Object[] columnNames(JTable table) {
        Object[] columns = new Object[table.getColumnCount()];
        for (int index = 0; index < columns.length; index++) {
            columns[index] = table.getColumnName(index);
        }
        return columns;
    }

    private static boolean hasButton(Container parent, String text) {
        return findButton(parent, text) != null;
    }

    private static JButton findButton(Container parent, String text) {
        for (Component component : parent.getComponents()) {
            if (component instanceof JButton button && text.equals(button.getText())) {
                return button;
            }
            if (component instanceof Container child) {
                JButton button = findButton(child, text);
                if (button != null) {
                    return button;
                }
            }
        }
        return null;
    }

    private static <T extends Component> T findComponent(Container parent, Class<T> componentType) {
        for (Component component : parent.getComponents()) {
            if (componentType.isInstance(component)) {
                return componentType.cast(component);
            }
            if (component instanceof Container child) {
                T found = findComponent(child, componentType);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    private static final class EmptyInventoryUseCase implements InventoryUseCase {
        @Override public void createProduct(String code, String name, int minStock, int alertThreshold) { }
        @Override public List<Product> listProducts() { return List.of(); }
        @Override public void editProduct(String code, String name, int minStock, int alertThreshold) { }
        @Override public void deleteProduct(String code) { }
        @Override public void registerLot(String productCode, String lotCode, LocalDate dueDate, int quantity) { }
        @Override public void registerEntry(String productCode, String lotCode, int quantity) { }
        @Override public void registerExit(String productCode, int quantity) { }
        @Override public List<Alert> getAlerts() { return List.of(); }
        @Override public void deleteExpiredLot(String productCode, String lotCode) { }
    }
}
