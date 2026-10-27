package domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class LotTest {
    private static final LocalDate DUE = LocalDate.of(2026, 10, 14);

    @Test
    void shouldRejectNonPositiveInitialQuantity() {
        assertThrows(IllegalArgumentException.class, () -> new Lot("L1", DUE, 0));
        assertThrows(IllegalArgumentException.class, () -> new Lot("L1", DUE, -4));
    }

    @Test
    void shouldRequireDueDate() {
        assertThrows(IllegalArgumentException.class, () -> new Lot("L1", null, 5));
    }

    @Test
    void balanceCanReachZeroButNeverNegative() {
        Lot l = new Lot("L1", DUE, 5);
        l.setBalance(0);
        assertEquals(0, l.getBalance());
        assertThrows(IllegalArgumentException.class, () -> l.setBalance(-1));
        assertEquals(0, l.getBalance());
    }

    @Test
    void expiryBoundaries() {
        Lot l = new Lot("L1", DUE, 5);
        assertFalse(l.isExpired(DUE.minusDays(1)));
        assertFalse(l.isExpired(DUE));
        assertTrue(l.isExpired(DUE.plusDays(1)));
    }

    @Test
    void daysRemaining() {
        Lot l = new Lot("L1", DUE, 5);
        assertEquals(3, l.daysRemaining(DUE.minusDays(3)));
        assertEquals(0, l.daysRemaining(DUE));
        assertEquals(-2, l.daysRemaining(DUE.plusDays(2)));
    }

    @Test
    void expiringSoonRespectsThresholdBoundary() {
        Lot l = new Lot("L1", DUE, 5);
        assertTrue(l.isExpiringSoon(DUE.minusDays(10), 10));
        assertFalse(l.isExpiringSoon(DUE.minusDays(11), 10));
        assertFalse(l.isExpiringSoon(DUE.plusDays(1), 10));
    }
}
