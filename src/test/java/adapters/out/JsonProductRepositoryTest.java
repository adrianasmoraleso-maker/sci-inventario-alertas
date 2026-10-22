package adapters.out;

import domain.Lot;
import domain.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JsonProductRepositoryTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void createsAnEmptyInventoryFileWhenItDoesNotExist() throws Exception {
        Path inventoryFile = temporaryDirectory.resolve("inventory.json");

        JsonProductRepository repository = new JsonProductRepository(inventoryFile);

        assertTrue(Files.exists(inventoryFile));
        assertTrue(repository.findAll().isEmpty());
        assertEquals("[]", Files.readString(inventoryFile).trim());
    }

    @Test
    void persistsProductsAndLotDatesAcrossRepositoryInstances() {
        Path inventoryFile = temporaryDirectory.resolve("inventory.json");
        JsonProductRepository repository = new JsonProductRepository(inventoryFile);
        LocalDate dueDate = LocalDate.of(2027, 3, 15);
        Product product = new Product("P001", "Arroz", 5, 30);
        product.addLot(new Lot("L001", dueDate, 12), LocalDate.of(2026, 10, 22));

        repository.save(product);
        Product restored = new JsonProductRepository(inventoryFile).findById("P001");

        assertNotNull(restored);
        assertEquals("Arroz", restored.getName());
        assertEquals(5, restored.getMinStock());
        assertEquals(30, restored.getAlertThresholdDays());
        assertEquals(1, restored.getLots().size());
        assertEquals(dueDate, restored.getLots().get(0).getDueDate());
        assertEquals(12, restored.getLots().get(0).getBalance());
    }

    @Test
    void saveReplacesAnExistingProductWithTheSameCode() {
        Path inventoryFile = temporaryDirectory.resolve("inventory.json");
        JsonProductRepository repository = new JsonProductRepository(inventoryFile);
        repository.save(new Product("P001", "Nombre anterior", 1, 10));

        repository.save(new Product("P001", "Nombre actualizado", 2, 20));
        List<Product> restoredProducts = new JsonProductRepository(inventoryFile).findAll();

        assertEquals(1, restoredProducts.size());
        assertEquals("Nombre actualizado", restoredProducts.get(0).getName());
        assertEquals(2, restoredProducts.get(0).getMinStock());
        assertEquals(20, restoredProducts.get(0).getAlertThresholdDays());
    }

    @Test
    void saveAllReplacesThePersistedInventoryAndDeleteSurvivesReload() {
        Path inventoryFile = temporaryDirectory.resolve("inventory.json");
        JsonProductRepository repository = new JsonProductRepository(inventoryFile);
        repository.save(new Product("OLD", "Antiguo", 0, 10));
        Product remaining = new Product("KEEP", "Conservar", 1, 20);
        repository.saveAll(List.of(remaining, new Product("REMOVE", "Eliminar", 0, 15)));

        repository.delete("REMOVE");
        JsonProductRepository reloaded = new JsonProductRepository(inventoryFile);

        assertNull(reloaded.findById("OLD"));
        assertNull(reloaded.findById("REMOVE"));
        assertEquals(List.of("KEEP"), reloaded.findAll().stream().map(Product::getCode).toList());
    }
}
