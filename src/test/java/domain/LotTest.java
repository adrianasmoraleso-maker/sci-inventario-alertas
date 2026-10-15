
package domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class LotTest {

    @Test
    void shouldCreateLotWithValidData() {
        Lot lot = new Lot("L001", LocalDate.of(2026, 12, 31), 20);

        assertEquals("L001", lot.getCode());
        assertEquals(LocalDate.of(2026, 12, 31), lot.getDueDate());
        assertEquals(20, lot.getBalance());
    }

    @Test
    void shouldRejectZeroInitialBalance() {
        assertThrows(IllegalArgumentException.class,
                () -> new Lot("L001", LocalDate.of(2026, 12, 31), 0));
    }

    @Test
    void shouldRejectNegativeInitialBalance() {
        assertThrows(IllegalArgumentException.class,
                () -> new Lot("L001", LocalDate.of(2026, 12, 31), -5));
    }

    @Test
    void shouldRejectNegativeBalanceWhenUpdating() {
        Lot lot = new Lot("L001", LocalDate.of(2026, 12, 31), 20);

        assertThrows(IllegalArgumentException.class,
                () -> lot.setBalance(-1));
    }

    @Test
    void shouldAllowZeroBalanceWhenUpdating() {
        Lot lot = new Lot("L001", LocalDate.of(2026, 12, 31), 20);

        lot.setBalance(0);

        assertEquals(0, lot.getBalance());
    }

    @Test
    void shouldNotBeExpiredOnDueDate() {
        LocalDate dueDate = LocalDate.of(2026, 10, 14);
        Lot lot = new Lot("L001", dueDate, 10);

        assertFalse(lot.isExpired(dueDate));
    }

    @Test
    void shouldBeExpiredAfterDueDate() {
        LocalDate dueDate = LocalDate.of(2026, 10, 14);
        Lot lot = new Lot("L001", dueDate, 10);

        assertTrue(lot.isExpired(dueDate.plusDays(1)));
    }

    @Test
    void shouldNotBeExpiredBeforeDueDate() {
        LocalDate dueDate = LocalDate.of(2026, 10, 14);
        Lot lot = new Lot("L001", dueDate, 10);

        assertFalse(lot.isExpired(dueDate.minusDays(1)));
    }
}
