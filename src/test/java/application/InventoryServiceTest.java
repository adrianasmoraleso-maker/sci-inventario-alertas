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
}
