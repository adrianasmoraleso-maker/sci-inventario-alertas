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
import java.util.List;
import domain.Alert;

import static org.junit.jupiter.api.Assertions.*;

class InventoryServiceTest {

    private InMemoryProductRepository repository;
    private FixedClock clock;
    private InventoryService service;

    private final LocalDate today = LocalDate.of(2026, 10, 14);

    @BeforeEach
    void setUp() {
        repository = new InMemoryProductRepository();
        clock = new FixedClock(today);
        service = new InventoryService(repository, clock);
    }

    private Product createProductWithLot(
            String productCode,
            String lotCode,
            LocalDate dueDate,
            int quantity) {

        Product product = new Product(
                productCode, "Producto de prueba", 5, 180
        );

        product.addLot(
                new Lot(lotCode, dueDate, quantity),
                clock.today()
        );

        repository.save(product);
        return product;
    }

    @Test
    void shouldRegisterEntryAndIncreaseLotBalance() {
        createProductWithLot(
                "P001", "L001", today.plusDays(10), 10
        );

        service.registerEntry("P001", "L001", 5);

        Product product = repository.findById("P001");
        assertEquals(15, product.getLots().get(0).getBalance());
    }

    @Test
    void shouldRejectEntryWithZeroQuantity() {
        createProductWithLot(
                "P001", "L001", today.plusDays(10), 10
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.registerEntry("P001", "L001", 0)
        );

        assertEquals(
                10,
                repository.findById("P001")
                        .getLots().get(0).getBalance()
        );
    }

    @Test
    void shouldRejectEntryWithNegativeQuantity() {
        createProductWithLot(
                "P001", "L001", today.plusDays(10), 10
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.registerEntry("P001", "L001", -3)
        );

        assertEquals(
                10,
                repository.findById("P001")
                        .getLots().get(0).getBalance()
        );
    }

    @Test
    void shouldRejectEntryForExpiredLot() {
        createProductWithLot(
                "P001", "L001", today.plusDays(1), 10
        );

        // Avanzamos la fecha hasta después del vencimiento.
        clock.setToday(today.plusDays(2));

        assertThrows(
                IllegalStateException.class,
                () -> service.registerEntry("P001", "L001", 5)
        );

        assertEquals(
                10,
                repository.findById("P001")
                        .getLots().get(0).getBalance()
        );
    }

    @Test
    void shouldRejectEntryWhenProductDoesNotExist() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.registerEntry("P999", "L001", 5)
        );
    }

    @Test
    void shouldRejectEntryWhenLotDoesNotExist() {
        createProductWithLot(
                "P001", "L001", today.plusDays(10), 10
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.registerEntry("P001", "L999", 5)
        );
    }

    @Test
    void shouldAllowEntryOnExpirationDate() {
        createProductWithLot(
                "P001", "L001", today, 10
        );

        service.registerEntry("P001", "L001", 5);

        assertEquals(
                15,
                repository.findById("P001")
                        .getLots().get(0).getBalance()
        );

    }


    @Test
    void shouldRegisterExitUsingFEFO() {
        Product product = new Product("P001", "Producto de prueba", 5, 180);

        // El lote L001 vence después, pero se registra primero.
        product.addLot(
                new Lot("L001", today.plusDays(10), 5),
                today
        );

        // El lote L002 vence antes, por lo que debe consumirse primero.
        product.addLot(
                new Lot("L002", today.plusDays(3), 4),
                today
        );

        repository.save(product);

        service.registerExit("P001", 6);

        Product updatedProduct = repository.findById("P001");

        Lot lot1 = updatedProduct.getLots().stream()
                .filter(lot -> lot.getCode().equals("L001"))
                .findFirst()
                .orElseThrow();

        Lot lot2 = updatedProduct.getLots().stream()
                .filter(lot -> lot.getCode().equals("L002"))
                .findFirst()
                .orElseThrow();

        assertEquals(3, lot1.getBalance());
        assertEquals(0, lot2.getBalance());
        assertEquals(3, updatedProduct.getAvailableStock(today));
    }

    @Test
    void shouldRejectExitWhenAvailableStockIsInsufficient() {
        Product product = createProductWithLot(
                "P001", "L001", today.plusDays(10), 5
        );

        assertThrows(
                IllegalStateException.class,
                () -> service.registerExit("P001", 6)
        );

        assertEquals(5, product.getLots().get(0).getBalance());
    }

    @Test
    void shouldRejectExitWhenProductDoesNotExist() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.registerExit("P999", 5)
        );
    }

    @Test
    void shouldRejectExitWithZeroQuantity() {
        Product product = createProductWithLot(
                "P001", "L001", today.plusDays(10), 5
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.registerExit("P001", 0)
        );

        assertEquals(5, product.getLots().get(0).getBalance());
    }

    @Test
    void shouldRejectExitWithNegativeQuantity() {
        Product product = createProductWithLot(
                "P001", "L001", today.plusDays(10), 5
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.registerExit("P001", -2)
        );

        assertEquals(5, product.getLots().get(0).getBalance());
    }


    @Test
    void shouldCreateProductSuccessfully() {
        service.createProduct("P001", "Arroz", 10, 180);

        Product product = repository.findById("P001");

        assertNotNull(product);
        assertEquals("P001", product.getCode());
        assertEquals("Arroz", product.getName());
        assertEquals(10, product.getMinStock());
        assertEquals(180, product.getAlertThresholdDays());
    }

    @Test
    void shouldRejectDuplicateProductCode() {
        service.createProduct("P001", "Arroz", 10, 180);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.createProduct("P001", "Arroz integral", 5, 90)
        );

        assertEquals(1, service.listProducts().size());
        assertEquals("Arroz", repository.findById("P001").getName());
    }

    @Test
    void shouldListAllProducts() {
        service.createProduct("P001", "Arroz", 10, 180);
        service.createProduct("P002", "Lentejas", 5, 90);

        List<Product> products = service.listProducts();

        assertEquals(2, products.size());
        assertTrue(products.stream().anyMatch(
                product -> product.getCode().equals("P001")
        ));
        assertTrue(products.stream().anyMatch(
                product -> product.getCode().equals("P002")
        ));
    }

    @Test
    void shouldEditProductSuccessfully() {
        service.createProduct("P001", "Arroz", 10, 180);

        service.editProduct("P001", "Arroz integral", 15, 90);

        Product product = repository.findById("P001");

        assertEquals("Arroz integral", product.getName());
        assertEquals(15, product.getMinStock());
        assertEquals(90, product.getAlertThresholdDays());
    }

    @Test
    void shouldRejectEditingNonexistentProduct() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.editProduct("P999", "Arroz", 10, 180)
        );
    }

    @Test
    void shouldDeleteProductWithoutPositiveStock() {
        Product product = createProductWithLot(
                "P001", "L001", today.plusDays(10), 5
        );

        // Un lote con saldo cero no impide eliminar el producto.
        product.getLots().get(0).setBalance(0);

        service.deleteProduct("P001");

        assertNull(repository.findById("P001"));
        assertTrue(service.listProducts().isEmpty());
    }

    @Test
    void shouldRejectDeletingProductWithPositiveStock() {
        createProductWithLot(
                "P001", "L001", today.plusDays(10), 5
        );

        assertThrows(
                IllegalStateException.class,
                () -> service.deleteProduct("P001")
        );

        assertNotNull(repository.findById("P001"));
        assertEquals(
                5,
                repository.findById("P001").getLots().get(0).getBalance()
        );
    }


    @Test
    void shouldRegisterLotSuccessfully() {
        service.createProduct("P001", "Arroz", 10, 180);

        service.registerLot(
                "P001", "L001", today.plusDays(30), 20
        );

        Product product = repository.findById("P001");

        assertEquals(1, product.getLots().size());
        assertEquals("L001", product.getLots().get(0).getCode());
        assertEquals(today.plusDays(30), product.getLots().get(0).getDueDate());
        assertEquals(20, product.getLots().get(0).getBalance());
    }

    @Test
    void shouldRejectLotWithZeroInitialQuantity() {
        service.createProduct("P001", "Arroz", 10, 180);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.registerLot(
                        "P001", "L001", today.plusDays(30), 0
                )
        );

        assertTrue(repository.findById("P001").getLots().isEmpty());
    }

    @Test
    void shouldRejectLotWithNegativeInitialQuantity() {
        service.createProduct("P001", "Arroz", 10, 180);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.registerLot(
                        "P001", "L001", today.plusDays(30), -5
                )
        );

        assertTrue(repository.findById("P001").getLots().isEmpty());
    }

    @Test
    void shouldRejectLotWithPastExpirationDate() {
        service.createProduct("P001", "Arroz", 10, 180);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.registerLot(
                        "P001", "L001", today.minusDays(1), 20
                )
        );

        assertTrue(repository.findById("P001").getLots().isEmpty());
    }

    @Test
    void shouldRejectDuplicateLotCodeWithinProduct() {
        service.createProduct("P001", "Arroz", 10, 180);

        service.registerLot(
                "P001", "L001", today.plusDays(30), 20
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.registerLot(
                        "P001", "L001", today.plusDays(60), 15
                )
        );

        Product product = repository.findById("P001");

        assertEquals(1, product.getLots().size());
        assertEquals(20, product.getLots().get(0).getBalance());
    }

    @Test
    void shouldRejectLotWhenProductDoesNotExist() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.registerLot(
                        "P999", "L001", today.plusDays(30), 20
                )
        );
    }



    @Test
    void shouldDeleteExpiredLotSuccessfully() {
        Product product = createProductWithLot(
                "P001", "L001", today, 10
        );

        clock.setToday(today.plusDays(1));

        service.deleteExpiredLot("P001", "L001");

        assertTrue(product.getLots().isEmpty());
    }


    @Test
    void shouldRejectDeletingLotThatIsNotExpired() {
        Product product = createProductWithLot(
                "P001", "L001", today.plusDays(10), 10
        );

        assertThrows(
                IllegalStateException.class,
                () -> service.deleteExpiredLot("P001", "L001")
        );

        assertEquals(1, product.getLots().size());
        assertEquals(10, product.getLots().get(0).getBalance());
    }

    @Test
    void shouldRejectDeletingNonexistentLot() {
        service.createProduct("P001", "Arroz", 10, 180);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.deleteExpiredLot("P001", "L999")
        );
    }

    @Test
    void shouldRejectDeletingLotWhenProductDoesNotExist() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.deleteExpiredLot("P999", "L001")
        );
    }



    // Repositorio en memoria: no crea ni modifica archivos reales.
    private static class InMemoryProductRepository
            implements ProductRepository {

        private final Map<String, Product> products = new HashMap<>();

        @Override
        public List<Product> findAll() {
            return new ArrayList<>(products.values());
        }

        @Override
        public Product findById(String code) {
            return products.get(code);
        }

        @Override
        public void save(Product product) {
            products.put(product.getCode(), product);
        }

        @Override
        public void delete(String code) {
            products.remove(code);
        }

        @Override
        public void saveAll(List<Product> products) {
            for (Product product : products) {
                save(product);
            }
        }
    }

    // Reloj controlado para que las pruebas sean reproducibles.
    private static class FixedClock implements ClockPort {

        private LocalDate today;

        private FixedClock(LocalDate today) {
            this.today = today;
        }

        @Override
        public LocalDate today() {
            return today;
        }

        void setToday(LocalDate today) {
            this.today = today;
        }
    }


    @Test
    void shouldGenerateLowStockAlertWhenAvailableStockIsBelowMinimum() {
        createProductWithLot("P001", "L001", today.plusDays(30), 4);

        List<Alert> alerts = service.getAlerts();

        assertTrue(alerts.stream().anyMatch(alert ->
                "STOCK_BAJO".equals(alert.getType())
        ));
    }

    @Test
    void shouldNotGenerateLowStockAlertWhenStockEqualsMinimum() {
        createProductWithLot("P001", "L001", today.plusDays(30), 5);

        List<Alert> alerts = service.getAlerts();

        assertFalse(alerts.stream().anyMatch(alert ->
                "STOCK_BAJO".equals(alert.getType())
        ));
    }

    @Test
    void shouldNotGenerateLowStockAlertWhenMinimumStockIsZero() {
        service.createProduct("P001", "Arroz", 0, 180);

        List<Alert> alerts = service.getAlerts();

        assertFalse(alerts.stream().anyMatch(alert ->
                "STOCK_BAJO".equals(alert.getType())
        ));
    }


    @Test
    void shouldGenerateNearExpiryAlertWhenLotEntersConfiguredThreshold() {
        createProductWithLot(
                "P001", "L001", today.plusDays(180), 10
        );

        List<Alert> alerts = service.getAlerts();

        assertTrue(alerts.stream().anyMatch(alert ->
                "PROXIMO_A_CADUCAR".equals(alert.getType())
        ));
    }

    @Test
    void shouldGenerateNearExpiryAlertExactlyAtThresholdBoundary() {
        createProductWithLot(
                "P001", "L001", today.plusDays(180), 10
        );

        clock.setToday(today.plusDays(1));

        List<Alert> alerts = service.getAlerts();

        assertTrue(alerts.stream().anyMatch(alert ->
                "PROXIMO_A_CADUCAR".equals(alert.getType())
        ));
    }

    @Test
    void shouldGenerateExpiredAlertWhenTodayIsAfterDueDate() {
        createProductWithLot(
                "P001", "L001", today, 10
        );

        clock.setToday(today.plusDays(1));

        List<Alert> alerts = service.getAlerts();

        assertTrue(alerts.stream().anyMatch(alert ->
                "CADUCADO".equals(alert.getType())
        ));
    }

}
