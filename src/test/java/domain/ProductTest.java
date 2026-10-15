
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

    @Test
    void shouldNotModifyAnyLotWhenExitIsRejected() {
        Product p = new Product("P1", "Test", 0, 5);
        LocalDate today = LocalDate.of(2026, 10, 14);

        Lot lot1 = new Lot("L1", LocalDate.of(2026, 10, 15), 3);
        Lot lot2 = new Lot("L2", LocalDate.of(2026, 10, 20), 4);

        p.addLot(lot1, today);
        p.addLot(lot2, today);

        assertThrows(IllegalStateException.class,
                () -> p.processExit(8, today));

        assertEquals(3, lot1.getBalance());
        assertEquals(4, lot2.getBalance());
        assertEquals(7, p.getAvailableStock(today));
    }

    @Test
    void shouldConsumeMultipleLotsInFEFOOrder() {
        Product p = new Product("P1", "Test", 0, 5);
        LocalDate today = LocalDate.of(2026, 10, 14);

        Lot firstToExpire = new Lot("L1", LocalDate.of(2026, 10, 15), 3);
        Lot secondToExpire = new Lot("L2", LocalDate.of(2026, 10, 20), 5);

        p.addLot(firstToExpire, today);
        p.addLot(secondToExpire, today);

        p.processExit(6, today);

        assertEquals(0, firstToExpire.getBalance());
        assertEquals(2, secondToExpire.getBalance());
        assertEquals(2, p.getAvailableStock(today));
    }

    @Test
    void shouldUseRegistrationOrderWhenDueDatesAreEqual() {
        Product p = new Product("P1", "Test", 0, 5);
        LocalDate today = LocalDate.of(2026, 10, 14);
        LocalDate dueDate = LocalDate.of(2026, 10, 20);

        Lot firstRegistered = new Lot("L1", dueDate, 5);
        Lot secondRegistered = new Lot("L2", dueDate, 5);

        p.addLot(firstRegistered, today);
        p.addLot(secondRegistered, today);

        p.processExit(3, today);

        assertEquals(2, firstRegistered.getBalance());
        assertEquals(5, secondRegistered.getBalance());
    }


    @Test
    void shouldCountExpiredStockSeparately() {
        LocalDate today = LocalDate.of(2026, 10, 14);
        Product product = new Product("P001", "Arroz", 5, 180);

        Lot expiredLot = new Lot("L001", today, 4);
        product.addLot(expiredLot, today);

        Lot availableLot = new Lot("L002", today.plusDays(5), 7);
        product.addLot(availableLot, today);

        LocalDate nextDay = today.plusDays(1);

        assertEquals(7, product.getAvailableStock(nextDay));
        assertEquals(4, product.getExpiredStock(nextDay));
    }


    @Test
    void shouldIncludeLotOnItsExpirationDateInAvailableStock() {
        LocalDate today = LocalDate.of(2026, 10, 14);
        Product product = new Product("P001", "Arroz", 5, 180);

        product.addLot(new Lot("L001", today, 6), today);

        assertEquals(6, product.getAvailableStock(today));
        assertEquals(0, product.getExpiredStock(today));
    }


    @Test
    void shouldRejectBlankProductName() {
        LocalDate today = LocalDate.of(2026, 10, 14);

        assertThrows(IllegalArgumentException.class, () ->
                new Product("P001", "   ", 5, 180)
        );
    }

    @Test
    void shouldRejectNegativeMinimumStock() {
        assertThrows(IllegalArgumentException.class, () ->
                new Product("P001", "Arroz", -1, 180)
        );
    }

    @Test
    void shouldRejectZeroAlertThreshold() {
        assertThrows(IllegalArgumentException.class, () ->
                new Product("P001", "Arroz", 5, 0)
        );
    }

    @Test
    void shouldRejectNegativeAlertThreshold() {
        assertThrows(IllegalArgumentException.class, () ->
                new Product("P001", "Arroz", 5, -1)
        );
    }

    @Test
    void shouldRejectNegativeMinimumStockWhenUpdating() {
        Product product = new Product("P001", "Arroz", 5, 180);

        assertThrows(IllegalArgumentException.class, () ->
                product.setMinStock(-1)
        );
    }

    @Test
    void shouldRejectZeroAlertThresholdWhenUpdating() {
        Product product = new Product("P001", "Arroz", 5, 180);

        assertThrows(IllegalArgumentException.class, () ->
                product.setAlertThresholdDays(0)
        );
    }

    @Test
    void shouldRejectNegativeAlertThresholdWhenUpdating() {
        Product product = new Product("P001", "Arroz", 5, 180);

        assertThrows(IllegalArgumentException.class, () ->
                product.setAlertThresholdDays(-1)
        );
    }

    @Test
    void shouldAllowZeroMinimumStockWhenUpdating() {
        Product product = new Product("P001", "Arroz", 5, 180);

        product.setMinStock(0);

        assertEquals(0, product.getMinStock());
    }

    @Test
    void shouldAllowPositiveAlertThresholdWhenUpdating() {
        Product product = new Product("P001", "Arroz", 5, 180);

        product.setAlertThresholdDays(1);

        assertEquals(1, product.getAlertThresholdDays());
    }


}
