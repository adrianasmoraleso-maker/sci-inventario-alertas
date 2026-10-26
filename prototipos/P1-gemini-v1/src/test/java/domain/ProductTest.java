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
}
