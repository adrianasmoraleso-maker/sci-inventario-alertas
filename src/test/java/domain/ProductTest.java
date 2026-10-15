
package domain;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    @Test
    void testFEFOExit() {
        Product p = new Product("P1", "Test", 10, 5);
        LocalDate today = LocalDate.of(2026, 10, 14);

        p.addLot(new Lot("L1", LocalDate.of(2026, 10, 20), 10), today);
        p.addLot(new Lot("L2", LocalDate.of(2026, 10, 15), 10), today);

        p.processExit(15, today);

        assertEquals(0, p.getLots().get(1).getBalance());
        assertEquals(5, p.getLots().get(0).getBalance());
        assertEquals(5, p.getAvailableStock(today));
    }

    @Test
    void shouldRejectExitWhenStockIsInsufficient() {
        Product p = new Product("P1", "Test", 0, 5);
        LocalDate today = LocalDate.of(2026, 10, 14);

        p.addLot(new Lot("L1", LocalDate.of(2026, 10, 20), 5), today);

        assertThrows(IllegalStateException.class,
                () -> p.processExit(6, today));

        assertEquals(5, p.getAvailableStock(today));
    }

    @Test
    void shouldRejectExitWithZeroQuantity() {
        Product p = new Product("P1", "Test", 0, 5);
        LocalDate today = LocalDate.of(2026, 10, 14);

        p.addLot(new Lot("L1", LocalDate.of(2026, 10, 20), 5), today);

        assertThrows(IllegalArgumentException.class,
                () -> p.processExit(0, today));
    }

    @Test
    void shouldRejectExitWithNegativeQuantity() {
        Product p = new Product("P1", "Test", 0, 5);
        LocalDate today = LocalDate.of(2026, 10, 14);

        p.addLot(new Lot("L1", LocalDate.of(2026, 10, 20), 5), today);

        assertThrows(IllegalArgumentException.class,
                () -> p.processExit(-3, today));

        assertEquals(5, p.getAvailableStock(today));
    }

    @Test
    void shouldExcludeExpiredLotsFromAvailableStock() {
        Product p = new Product("P1", "Test", 0, 5);
        LocalDate today = LocalDate.of(2026, 10, 14);

        p.addLot(new Lot("L1", LocalDate.of(2026, 10, 13), 7), today.minusDays(1));
        p.addLot(new Lot("L2", LocalDate.of(2026, 10, 20), 5), today);

        assertEquals(5, p.getAvailableStock(today));
        assertEquals(7, p.getExpiredStock(today));
    }
}
