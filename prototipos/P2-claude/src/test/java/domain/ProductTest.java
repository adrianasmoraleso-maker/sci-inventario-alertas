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
        
        // Debe consumir primero L2 (vence antes) y luego L1
        assertEquals(0, p.getLots().get(1).getBalance()); // L2 vacío
        assertEquals(5, p.getLots().get(0).getBalance()); // L1 con 5 restantes
        assertEquals(5, p.getAvailableStock(today));
    }

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 14);

    // ---- RN14 a RN17: validaciones al construir y al modificar ----

    @Test
    void shouldRejectBlankProductName() {
        assertThrows(IllegalArgumentException.class, () -> new Product("P1", "   ", 10, 5));
        assertThrows(IllegalArgumentException.class, () -> new Product("P1", "", 10, 5));
        assertThrows(IllegalArgumentException.class, () -> new Product("P1", null, 10, 5));
    }

    @Test
    void shouldRejectNegativeMinimumStock() {
        assertThrows(IllegalArgumentException.class, () -> new Product("P1", "Test", -1, 5));
    }

    @Test
    void shouldRejectZeroAlertThreshold() {
        assertThrows(IllegalArgumentException.class, () -> new Product("P1", "Test", 10, 0));
    }

    @Test
    void shouldRejectNegativeAlertThreshold() {
        assertThrows(IllegalArgumentException.class, () -> new Product("P1", "Test", 10, -3));
    }

    @Test
    void shouldAcceptZeroMinimumStock() {
        assertEquals(0, new Product("P1", "Test", 0, 5).getMinStock());
    }

    @Test
    void shouldRejectNegativeMinimumStockWhenUpdating() {
        Product p = new Product("P1", "Test", 10, 5);
        assertThrows(IllegalArgumentException.class, () -> p.setMinStock(-1));
        assertEquals(10, p.getMinStock());
    }

    @Test
    void shouldRejectZeroAlertThresholdWhenUpdating() {
        Product p = new Product("P1", "Test", 10, 5);
        assertThrows(IllegalArgumentException.class, () -> p.setAlertThresholdDays(0));
        assertEquals(5, p.getAlertThresholdDays());
    }

    @Test
    void shouldRejectNegativeAlertThresholdWhenUpdating() {
        Product p = new Product("P1", "Test", 10, 5);
        assertThrows(IllegalArgumentException.class, () -> p.setAlertThresholdDays(-2));
        assertEquals(5, p.getAlertThresholdDays());
    }

    @Test
    void shouldRejectBlankNameWhenUpdating() {
        Product p = new Product("P1", "Test", 10, 5);
        assertThrows(IllegalArgumentException.class, () -> p.setName("  "));
        assertEquals("Test", p.getName());
    }

    @Test
    void updateShouldChangeNothingWhenAnyValueIsInvalid() {
        Product p = new Product("P1", "Test", 10, 5);
        assertThrows(IllegalArgumentException.class, () -> p.update("Nuevo", 20, 0));
        assertEquals("Test", p.getName());
        assertEquals(10, p.getMinStock());
        assertEquals(5, p.getAlertThresholdDays());
        p.update("Nuevo", 20, 7);
        assertEquals("Nuevo", p.getName());
        assertEquals(20, p.getMinStock());
        assertEquals(7, p.getAlertThresholdDays());
    }

    // ---- Reglas de negocio existentes ----

    @Test
    void exitShouldReturnAllocationPerLot() {
        Product p = new Product("P1", "Test", 0, 5);
        p.addLot(new Lot("L1", TODAY.plusDays(10), 10), TODAY);
        p.addLot(new Lot("L2", TODAY.plusDays(2), 4), TODAY);
        var allocations = p.processExit(7, TODAY);
        assertEquals(2, allocations.size());
        assertEquals("L2", allocations.get(0).lotCode());
        assertEquals(4, allocations.get(0).quantity());
        assertEquals("L1", allocations.get(1).lotCode());
        assertEquals(3, allocations.get(1).quantity());
    }

    @Test
    void exitShouldBeRejectedCompletelyWhenStockIsInsufficient() {
        Product p = new Product("P1", "Test", 0, 5);
        p.addLot(new Lot("L1", TODAY.plusDays(10), 5), TODAY);
        p.addLot(new Lot("L2", TODAY.minusDays(1).plusDays(1), 3), TODAY);
        assertThrows(IllegalStateException.class, () -> p.processExit(9, TODAY));
        assertEquals(5, p.getLots().get(0).getBalance());
        assertEquals(3, p.getLots().get(1).getBalance());
    }

    @Test
    void exitShouldIgnoreExpiredLots() {
        Product p = new Product("P1", "Test", 0, 5);
        p.getLots().add(new Lot("OLD", TODAY.minusDays(1), 50));
        p.addLot(new Lot("L1", TODAY.plusDays(10), 5), TODAY);
        assertThrows(IllegalStateException.class, () -> p.processExit(6, TODAY));
        p.processExit(5, TODAY);
        assertEquals(50, p.getLots().get(0).getBalance());
    }

    @Test
    void exitTiesShouldFollowRegistrationOrder() {
        Product p = new Product("P1", "Test", 0, 5);
        p.addLot(new Lot("A", TODAY.plusDays(5), 3), TODAY);
        p.addLot(new Lot("B", TODAY.plusDays(5), 3), TODAY);
        var allocations = p.processExit(4, TODAY);
        assertEquals("A", allocations.get(0).lotCode());
        assertEquals("B", allocations.get(1).lotCode());
    }

    @Test
    void exitShouldRejectNonPositiveQuantity() {
        Product p = new Product("P1", "Test", 0, 5);
        p.addLot(new Lot("L1", TODAY.plusDays(10), 5), TODAY);
        assertThrows(IllegalArgumentException.class, () -> p.processExit(0, TODAY));
        assertThrows(IllegalArgumentException.class, () -> p.processExit(-1, TODAY));
    }

    @Test
    void lotExpiringTodayStillCountsAsAvailable() {
        Product p = new Product("P1", "Test", 0, 5);
        p.addLot(new Lot("L1", TODAY, 5), TODAY);
        assertEquals(5, p.getAvailableStock(TODAY));
        assertEquals(0, p.getAvailableStock(TODAY.plusDays(1)));
        assertEquals(5, p.getExpiredStock(TODAY.plusDays(1)));
    }

    @Test
    void shouldRejectLotWithPastDueDateOrDuplicateCode() {
        Product p = new Product("P1", "Test", 0, 5);
        assertThrows(IllegalArgumentException.class, () -> p.addLot(new Lot("L1", TODAY.minusDays(1), 5), TODAY));
        p.addLot(new Lot("L1", TODAY, 5), TODAY);
        assertThrows(IllegalArgumentException.class, () -> p.addLot(new Lot("L1", TODAY.plusDays(3), 5), TODAY));
    }

    @Test
    void shouldOnlyRemoveExpiredLots() {
        Product p = new Product("P1", "Test", 0, 5);
        p.addLot(new Lot("L1", TODAY, 5), TODAY);
        assertThrows(IllegalStateException.class, () -> p.removeExpiredLot("L1", TODAY));
        p.removeExpiredLot("L1", TODAY.plusDays(1));
        assertTrue(p.getLots().isEmpty());
    }

    // ---- Estados derivados (RF-UI22) ----

    @Test
    void stockStatusShouldFollowRules() {
        Product p = new Product("P1", "Test", 10, 5);
        assertEquals(ProductStatus.SIN_STOCK, p.getStockStatus(TODAY));
        p.addLot(new Lot("L1", TODAY.plusDays(30), 4), TODAY);
        assertEquals(ProductStatus.STOCK_BAJO, p.getStockStatus(TODAY));
        p.getLots().get(0).setBalance(10);
        assertEquals(ProductStatus.DISPONIBLE, p.getStockStatus(TODAY));
        Product noMin = new Product("P2", "Test", 0, 5);
        noMin.addLot(new Lot("L1", TODAY.plusDays(30), 1), TODAY);
        assertFalse(noMin.isLowStock(TODAY));
        assertEquals(ProductStatus.DISPONIBLE, noMin.getStockStatus(TODAY));
    }

    @Test
    void lotStatusShouldFollowRn05AndRn11() {
        Product p = new Product("P1", "Test", 0, 10);
        p.addLot(new Lot("NEAR", TODAY.plusDays(10), 5), TODAY);   // límite exacto del umbral
        p.addLot(new Lot("FAR", TODAY.plusDays(11), 5), TODAY);
        p.addLot(new Lot("TODAY", TODAY, 5), TODAY);
        p.getLots().add(new Lot("OLD", TODAY.minusDays(1), 5));
        assertEquals(LotStatus.PROXIMO_A_CADUCAR, p.getLotStatus(p.getLots().get(0), TODAY));
        assertEquals(LotStatus.VIGENTE, p.getLotStatus(p.getLots().get(1), TODAY));
        assertEquals(LotStatus.PROXIMO_A_CADUCAR, p.getLotStatus(p.getLots().get(2), TODAY));
        assertEquals(LotStatus.CADUCADO, p.getLotStatus(p.getLots().get(3), TODAY));
        p.getLots().get(0).setBalance(0); // sin saldo no se considera próximo a caducar
        assertEquals(LotStatus.VIGENTE, p.getLotStatus(p.getLots().get(0), TODAY));
    }

    @Test
    void fefoOrderShouldPutExpiredLotsLast() {
        Product p = new Product("P1", "Test", 0, 5);
        p.getLots().add(new Lot("OLD", TODAY.minusDays(3), 1));
        p.addLot(new Lot("B", TODAY.plusDays(9), 1), TODAY);
        p.addLot(new Lot("A", TODAY.plusDays(2), 1), TODAY);
        var order = p.getLotsInFefoOrder(TODAY);
        assertEquals("A", order.get(0).getCode());
        assertEquals("B", order.get(1).getCode());
        assertEquals("OLD", order.get(2).getCode());
    }
}
