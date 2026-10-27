package application;

import domain.PersistenceException;
import domain.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import testsupport.FixedClock;
import testsupport.InMemoryProductRepository;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/** Una salida aceptada se guarda completa; una rechazada no altera saldos ni invoca la persistencia. */
class ExitAtomicityTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 14);
    private InMemoryProductRepository repo;
    private InventoryService service;

    @BeforeEach
    void setUp() {
        repo = new InMemoryProductRepository();
        service = new InventoryService(repo, new FixedClock(TODAY));
        service.createProduct("P1", "Test", 0, 30);
        service.registerLot("P1", "A", TODAY.plusDays(5), 4);
        service.registerLot("P1", "B", TODAY.plusDays(9), 4);
    }

    @Test
    void rejectedExitChangesNothingAndDoesNotSave() {
        int saves = repo.saveCount;
        assertThrows(IllegalStateException.class, () -> service.registerExit("P1", 9));
        Product p = repo.findById("P1");
        assertEquals(4, p.getLots().get(0).getBalance());
        assertEquals(4, p.getLots().get(1).getBalance());
        assertEquals(saves, repo.saveCount);
    }

    @Test
    void acceptedExitSplitsAcrossLotsAndSavesOnce() {
        int saves = repo.saveCount;
        service.registerExit("P1", 6);
        Product p = repo.findById("P1");
        assertEquals(0, p.getLots().get(0).getBalance());
        assertEquals(2, p.getLots().get(1).getBalance());
        assertEquals(saves + 1, repo.saveCount);
    }

    @Test
    void failedWriteIsReportedAsFailure() {
        repo.failOnSave = true;
        assertThrows(PersistenceException.class, () -> service.registerExit("P1", 2));
    }
}
