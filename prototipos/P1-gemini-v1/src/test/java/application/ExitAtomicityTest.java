package application;

import domain.ClockPort;
import domain.Lot;
import domain.Product;
import domain.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Bloque 1.1 - Atomicidad de las salidas (registerExit).
 *
 * Regla verificada: una salida se aplica completa o no se aplica en absoluto.
 * Si se rechaza, ningun lote cambia su saldo y no se persiste nada.
 */
class ExitAtomicityTest {

    private CountingProductRepository repository;
    private MutableClock clock;
    private InventoryService service;

    private final LocalDate today = LocalDate.of(2026, 10, 14);

    @BeforeEach
    void setUp() {
        repository = new CountingProductRepository();
        clock = new MutableClock(today);
        service = new InventoryService(repository, clock);
    }

    // ---------- Salidas que se aceptan ----------

    @Test
    void shouldLeaveAllValidLotsAtZeroWhenExitEqualsTotalAvailableStock() {
        prepareProductWithLots("P001",
                lotData("L001", today.plusDays(3), 4),
                lotData("L002", today.plusDays(9), 6));

        service.registerExit("P001", 10);

        assertEquals(0, balanceOf("P001", "L001"));
        assertEquals(0, balanceOf("P001", "L002"));
        assertEquals(0, repository.findById("P001").getAvailableStock(today));
    }

    @Test
    void shouldConsumeLotThatExpiresToday() {
        prepareProductWithLots("P001", lotData("L001", today, 5));

        service.registerExit("P001", 5);

        assertEquals(0, balanceOf("P001", "L001"));
    }

    @Test
    void shouldLeaveLaterLotsUntouchedWhenEarlierLotsCoverTheExit() {
        prepareProductWithLots("P001",
                lotData("L001", today.plusDays(1), 2),
                lotData("L002", today.plusDays(2), 2),
                lotData("L003", today.plusDays(3), 5));

        service.registerExit("P001", 3);

        assertEquals(0, balanceOf("P001", "L001"));
        assertEquals(1, balanceOf("P001", "L002"));
        assertEquals(5, balanceOf("P001", "L003"));
    }

    @Test
    void shouldPersistExactlyOnceWhenExitIsAccepted() {
        prepareProductWithLots("P001",
                lotData("L001", today.plusDays(3), 4),
                lotData("L002", today.plusDays(9), 6));
        repository.resetSaveCounter();

        service.registerExit("P001", 7);

        assertEquals(1, repository.saveCalls());
    }

    // ---------- Salidas que se rechazan: no debe quedar ningun efecto ----------

    @Test
    void shouldNotChangeNorPersistAnythingWhenExitExceedsStockAcrossSeveralLots() {
        prepareProductWithLots("P001",
                lotData("L001", today.plusDays(3), 4),
                lotData("L002", today.plusDays(9), 6));
        repository.resetSaveCounter();

        assertThrows(IllegalStateException.class,
                () -> service.registerExit("P001", 11));

        assertEquals(4, balanceOf("P001", "L001"));
        assertEquals(6, balanceOf("P001", "L002"));
        assertEquals(0, repository.saveCalls());
    }

    @Test
    void shouldNotCountExpiredStockWhenRejectingAnExitAndShouldKeepEveryLotUnchanged() {
        // L001 vence primero y luego caduca; L002 sigue vigente.
        prepareProductWithLots("P001",
                lotData("L001", today.plusDays(1), 10),
                lotData("L002", today.plusDays(10), 5));
        clock.setToday(today.plusDays(2));
        repository.resetSaveCounter();

        // Hay 15 unidades fisicas, pero solo 5 disponibles (vigentes).
        assertThrows(IllegalStateException.class,
                () -> service.registerExit("P001", 8));

        assertEquals(10, balanceOf("P001", "L001"));
        assertEquals(5, balanceOf("P001", "L002"));
        assertEquals(0, repository.saveCalls());
    }

    @Test
    void shouldRejectExitWhenAllLotsAreExpiredAndKeepTheirBalances() {
        prepareProductWithLots("P001",
                lotData("L001", today.plusDays(1), 6),
                lotData("L002", today.plusDays(2), 4));
        clock.setToday(today.plusDays(3));
        repository.resetSaveCounter();

        assertThrows(IllegalStateException.class,
                () -> service.registerExit("P001", 1));

        assertEquals(6, balanceOf("P001", "L001"));
        assertEquals(4, balanceOf("P001", "L002"));
        assertEquals(0, repository.saveCalls());
    }

    @Test
    void shouldRejectExitForProductWithoutLots() {
        service.createProduct("P001", "Arroz", 5, 180);
        repository.resetSaveCounter();

        assertThrows(IllegalStateException.class,
                () -> service.registerExit("P001", 1));

        assertTrue(repository.findById("P001").getLots().isEmpty());
        assertEquals(0, repository.saveCalls());
    }

    @Test
    void shouldNotPersistWhenQuantityIsInvalid() {
        prepareProductWithLots("P001", lotData("L001", today.plusDays(3), 5));
        repository.resetSaveCounter();

        assertThrows(IllegalArgumentException.class,
                () -> service.registerExit("P001", 0));
        assertThrows(IllegalArgumentException.class,
                () -> service.registerExit("P001", -4));

        assertEquals(5, balanceOf("P001", "L001"));
        assertEquals(0, repository.saveCalls());
    }

    @Test
    void shouldKeepBalancesOfPreviousExitsWhenALaterExitIsRejected() {
        prepareProductWithLots("P001",
                lotData("L001", today.plusDays(5), 3),
                lotData("L002", today.plusDays(10), 4));

        service.registerExit("P001", 2);   // L001 = 1, L002 = 4
        service.registerExit("P001", 4);   // L001 = 0, L002 = 1
        repository.resetSaveCounter();

        assertThrows(IllegalStateException.class,
                () -> service.registerExit("P001", 2));  // solo queda 1

        assertEquals(0, balanceOf("P001", "L001"));
        assertEquals(1, balanceOf("P001", "L002"));
        assertEquals(0, repository.saveCalls());
    }

    // ---------- Utilidades de la prueba ----------

    private static LotData lotData(String code, LocalDate dueDate, int quantity) {
        return new LotData(code, dueDate, quantity);
    }

    /** Crea el producto y registra sus lotes solo con la API publica del servicio. */
    private void prepareProductWithLots(String productCode, LotData... lots) {
        service.createProduct(productCode, "Producto de prueba", 0, 180);
        for (LotData lot : lots) {
            service.registerLot(productCode, lot.code, lot.dueDate, lot.quantity);
        }
    }

    private int balanceOf(String productCode, String lotCode) {
        Product product = repository.findById(productCode);
        for (Lot lot : product.getLots()) {
            if (lot.getCode().equals(lotCode)) {
                return lot.getBalance();
            }
        }
        throw new AssertionError("No existe el lote " + lotCode);
    }

    private static class LotData {
        private final String code;
        private final LocalDate dueDate;
        private final int quantity;

        private LotData(String code, LocalDate dueDate, int quantity) {
            this.code = code;
            this.dueDate = dueDate;
            this.quantity = quantity;
        }
    }

    // Repositorio en memoria que ademas cuenta cuantas veces se llamo a save().
    private static class CountingProductRepository implements ProductRepository {

        private final Map<String, Product> products = new HashMap<>();
        private int saveCalls = 0;

        int saveCalls() { return saveCalls; }

        void resetSaveCounter() { saveCalls = 0; }

        @Override
        public List<Product> findAll() { return new ArrayList<>(products.values()); }

        @Override
        public Product findById(String code) { return products.get(code); }

        @Override
        public void save(Product product) {
            saveCalls++;
            products.put(product.getCode(), product);
        }

        @Override
        public void delete(String code) { products.remove(code); }

        @Override
        public void saveAll(List<Product> products) {
            for (Product product : products) {
                save(product);
            }
        }
    }

    private static class MutableClock implements ClockPort {
        private LocalDate today;

        private MutableClock(LocalDate today) { this.today = today; }

        void setToday(LocalDate today) { this.today = today; }

        @Override
        public LocalDate today() { return today; }
    }
}