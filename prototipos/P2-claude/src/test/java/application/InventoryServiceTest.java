package application;

import domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import testsupport.FixedClock;
import testsupport.InMemoryProductRepository;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Casos de uso con repositorio en memoria y reloj fijo (sin archivos ni interfaz gráfica). */
class InventoryServiceTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 14);

    private InMemoryProductRepository repo;
    private FixedClock clock;
    private InventoryService service;

    @BeforeEach
    void setUp() {
        repo = new InMemoryProductRepository();
        clock = new FixedClock(TODAY);
        service = new InventoryService(repo, clock);
    }

    // ---- RN14 a RN17 ----

    @Test
    void shouldRejectBlankNameWhenCreatingProduct() {
        assertThrows(IllegalArgumentException.class, () -> service.createProduct("P1", "   ", 5, 30));
        assertNull(repo.findById("P1"));
        assertEquals(0, repo.saveCount);
    }

    @Test
    void shouldRejectNegativeMinimumStockWhenCreatingProduct() {
        assertThrows(IllegalArgumentException.class, () -> service.createProduct("P1", "Test", -1, 30));
        assertEquals(0, repo.saveCount);
    }

    @Test
    void shouldRejectInvalidThresholdWhenCreatingProduct() {
        assertThrows(IllegalArgumentException.class, () -> service.createProduct("P1", "Test", 5, 0));
        assertThrows(IllegalArgumentException.class, () -> service.createProduct("P1", "Test", 5, -7));
        assertEquals(0, repo.saveCount);
    }

    @Test
    void shouldRejectNegativeMinimumStockWhenEditingProduct() {
        service.createProduct("P1", "Test", 5, 30);
        int saves = repo.saveCount;
        assertThrows(IllegalArgumentException.class, () -> service.editProduct("P1", "Nuevo", -1, 30));
        assertEquals(saves, repo.saveCount);
        assertEquals("Test", repo.findById("P1").getName());
        assertEquals(5, repo.findById("P1").getMinStock());
    }

    @Test
    void shouldRejectZeroAlertThresholdWhenEditingProduct() {
        service.createProduct("P1", "Test", 5, 30);
        int saves = repo.saveCount;
        assertThrows(IllegalArgumentException.class, () -> service.editProduct("P1", "Nuevo", 5, 0));
        assertEquals(saves, repo.saveCount);
        assertEquals("Test", repo.findById("P1").getName());
        assertEquals(30, repo.findById("P1").getAlertThresholdDays());
    }

    @Test
    void shouldRejectNegativeAlertThresholdWhenEditingProduct() {
        service.createProduct("P1", "Test", 5, 30);
        assertThrows(IllegalArgumentException.class, () -> service.editProduct("P1", "Nuevo", 5, -10));
        assertEquals(30, repo.findById("P1").getAlertThresholdDays());
    }

    @Test
    void shouldRejectBlankNameWhenEditingProduct() {
        service.createProduct("P1", "Test", 5, 30);
        assertThrows(IllegalArgumentException.class, () -> service.editProduct("P1", " ", 5, 30));
        assertEquals("Test", repo.findById("P1").getName());
    }

    @Test
    void shouldEditProductWithValidValues() {
        service.createProduct("P1", "Test", 5, 30);
        service.editProduct("P1", "Nuevo", 0, 10);
        Product p = repo.findById("P1");
        assertEquals("Nuevo", p.getName());
        assertEquals(0, p.getMinStock());
        assertEquals(10, p.getAlertThresholdDays());
    }

    // ---- Productos ----

    @Test
    void shouldRejectDuplicateProductCode() {
        service.createProduct("P1", "Test", 5, 30);
        assertThrows(IllegalArgumentException.class, () -> service.createProduct("P1", "Otro", 1, 1));
        assertEquals("Test", repo.findById("P1").getName());
    }

    @Test
    void shouldFailToEditOrDeleteUnknownProduct() {
        assertThrows(IllegalArgumentException.class, () -> service.editProduct("X", "n", 1, 1));
        assertThrows(IllegalArgumentException.class, () -> service.deleteProduct("X"));
    }

    @Test
    void shouldNotDeleteProductWithBalance() {
        service.createProduct("P1", "Test", 5, 30);
        service.registerLot("P1", "L1", TODAY.plusDays(10), 3);
        assertThrows(IllegalStateException.class, () -> service.deleteProduct("P1"));
        assertNotNull(repo.findById("P1"));
    }

    @Test
    void shouldDeleteProductWhenAllLotsHaveZeroBalance() {
        service.createProduct("P1", "Test", 0, 30);
        service.registerLot("P1", "L1", TODAY.plusDays(10), 3);
        service.registerExit("P1", 3);
        service.deleteProduct("P1");
        assertNull(repo.findById("P1"));
    }

    // ---- Lotes y entradas ----

    @Test
    void shouldRegisterLotAndRejectInvalidOnes() {
        service.createProduct("P1", "Test", 0, 30);
        service.registerLot("P1", "L1", TODAY, 5);
        assertThrows(IllegalArgumentException.class, () -> service.registerLot("P1", "L1", TODAY, 5));
        assertThrows(IllegalArgumentException.class, () -> service.registerLot("P1", "L2", TODAY.minusDays(1), 5));
        assertThrows(IllegalArgumentException.class, () -> service.registerLot("P1", "L3", TODAY, 0));
        assertThrows(IllegalArgumentException.class, () -> service.registerLot("P1", "L4", TODAY, -2));
        assertThrows(IllegalArgumentException.class, () -> service.registerLot("P1", "L5", null, 2));
        assertEquals(1, repo.findById("P1").getLots().size());
    }

    @Test
    void entryShouldIncreaseBalance() {
        service.createProduct("P1", "Test", 0, 30);
        service.registerLot("P1", "L1", TODAY.plusDays(5), 5);
        service.registerEntry("P1", "L1", 7);
        assertEquals(12, repo.findById("P1").getLots().get(0).getBalance());
    }

    @Test
    void entryShouldRejectInvalidQuantityUnknownLotAndExpiredLot() {
        service.createProduct("P1", "Test", 0, 30);
        service.registerLot("P1", "L1", TODAY, 5);
        assertThrows(IllegalArgumentException.class, () -> service.registerEntry("P1", "L1", 0));
        assertThrows(IllegalArgumentException.class, () -> service.registerEntry("P1", "L1", -3));
        assertThrows(IllegalArgumentException.class, () -> service.registerEntry("P1", "NOPE", 3));
        clock.today = TODAY.plusDays(1);
        assertThrows(IllegalStateException.class, () -> service.registerEntry("P1", "L1", 3));
        assertEquals(5, repo.findById("P1").getLots().get(0).getBalance());
    }

    @Test
    void shouldDeleteOnlyExpiredLots() {
        service.createProduct("P1", "Test", 0, 30);
        service.registerLot("P1", "L1", TODAY, 5);
        assertThrows(IllegalStateException.class, () -> service.deleteExpiredLot("P1", "L1"));
        clock.today = TODAY.plusDays(1);
        service.deleteExpiredLot("P1", "L1");
        assertTrue(repo.findById("P1").getLots().isEmpty());
    }

    // ---- Salidas FEFO ----

    @Test
    void exitShouldFollowFefoAndReturnAllocations() {
        service.createProduct("P1", "Test", 0, 30);
        service.registerLot("P1", "LATE", TODAY.plusDays(20), 10);
        service.registerLot("P1", "SOON", TODAY.plusDays(3), 4);
        List<ExitAllocation> result = service.registerExit("P1", 6);
        assertEquals(2, result.size());
        assertEquals("SOON", result.get(0).lotCode());
        assertEquals(4, result.get(0).quantity());
        assertEquals("LATE", result.get(1).lotCode());
        assertEquals(2, result.get(1).quantity());
        assertEquals(8, repo.findById("P1").getAvailableStock(TODAY));
    }

    @Test
    void exitShouldRejectNonPositiveQuantityAndUnknownProduct() {
        service.createProduct("P1", "Test", 0, 30);
        service.registerLot("P1", "L1", TODAY.plusDays(3), 4);
        assertThrows(IllegalArgumentException.class, () -> service.registerExit("P1", 0));
        assertThrows(IllegalArgumentException.class, () -> service.registerExit("P1", -2));
        assertThrows(IllegalArgumentException.class, () -> service.registerExit("X", 1));
    }

    // ---- Alertas ----

    @Test
    void alertsShouldCoverLowStockExpiringAndExpired() {
        service.createProduct("P1", "Test", 20, 10);
        service.registerLot("P1", "NEAR", TODAY.plusDays(10), 5);
        service.registerLot("P1", "FAR", TODAY.plusDays(60), 5);
        service.registerLot("P1", "OLD", TODAY, 5);
        clock.today = TODAY.plusDays(1);
        List<Alert> alerts = service.getAlerts();
        assertEquals(3, alerts.size());
        assertTrue(alerts.stream().anyMatch(a -> a.getType().equals("STOCK_BAJO") && a.getLotCode() == null));
        assertTrue(alerts.stream().anyMatch(a -> a.getType().equals("PROXIMO_A_CADUCAR") && "NEAR".equals(a.getLotCode())));
        Alert expired = alerts.stream().filter(a -> a.getType().equals("CADUCADO")).findFirst().orElseThrow();
        assertEquals("OLD", expired.getLotCode());
        assertTrue(expired.getMessage().contains("14/10/2026"), "la fecha debe verse como dd/mm/aaaa");
    }

    @Test
    void zeroMinimumStockNeverRaisesLowStockAlert() {
        service.createProduct("P1", "Test", 0, 30);
        assertTrue(service.getAlerts().isEmpty());
    }
}
