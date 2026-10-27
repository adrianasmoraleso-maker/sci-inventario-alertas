package adapters.out;

import domain.Lot;
import domain.PersistenceException;
import domain.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/** Persistencia en directorios temporales: ciclo normal, formato de la versión 1, JSON malformado y fallos de escritura. */
class JsonProductRepositoryTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 14);

    @TempDir
    Path dir;

    private Path dataFile() { return dir.resolve("inventory.json"); }

    private Product sample(String code) {
        Product p = new Product(code, "Producto " + code, 5, 90);
        p.addLot(new Lot("L1", TODAY.plusDays(30), 12), TODAY);
        return p;
    }

    private long tempFiles() throws IOException {
        try (Stream<Path> s = Files.list(dir)) {
            return s.filter(f -> f.getFileName().toString().endsWith(".tmp")).count();
        }
    }

    // ---- ciclo normal ----

    @Test
    void createsEmptyFileOnFirstRun() throws IOException {
        JsonProductRepository repo = new JsonProductRepository(dataFile());
        assertTrue(Files.exists(dataFile()));
        assertTrue(repo.findAll().isEmpty());
        assertNull(repo.getStartupWarning());
    }

    @Test
    void savedProductsSurviveReload() {
        JsonProductRepository repo = new JsonProductRepository(dataFile());
        repo.save(sample("P1"));
        repo.save(sample("P2"));

        JsonProductRepository reloaded = new JsonProductRepository(dataFile());
        assertEquals(2, reloaded.findAll().size());
        Product p = reloaded.findById("P1");
        assertEquals("Producto P1", p.getName());
        assertEquals(5, p.getMinStock());
        assertEquals(90, p.getAlertThresholdDays());
        assertEquals(1, p.getLots().size());
        assertEquals(TODAY.plusDays(30), p.getLots().get(0).getDueDate());
        assertEquals(12, p.getLots().get(0).getBalance());
    }

    @Test
    void saveReplacesByCodeKeepingPosition() {
        JsonProductRepository repo = new JsonProductRepository(dataFile());
        repo.save(sample("P1"));
        repo.save(sample("P2"));
        Product edited = repo.findById("P1");
        edited.update("Renombrado", 1, 10);
        repo.save(edited);
        List<Product> all = new JsonProductRepository(dataFile()).findAll();
        assertEquals(2, all.size());
        assertEquals("P1", all.get(0).getCode());
        assertEquals("Renombrado", all.get(0).getName());
    }

    @Test
    void deleteAndSaveAllArePersisted() {
        JsonProductRepository repo = new JsonProductRepository(dataFile());
        repo.save(sample("P1"));
        repo.save(sample("P2"));
        repo.delete("P1");
        assertNull(new JsonProductRepository(dataFile()).findById("P1"));
        repo.saveAll(List.of(sample("P9")));
        List<Product> all = new JsonProductRepository(dataFile()).findAll();
        assertEquals(1, all.size());
        assertEquals("P9", all.get(0).getCode());
    }

    @Test
    void returnedProductsAreCopiesUntilSaved() {
        JsonProductRepository repo = new JsonProductRepository(dataFile());
        repo.save(sample("P1"));
        Product copy = repo.findById("P1");
        copy.getLots().get(0).setBalance(0);
        assertEquals(12, repo.findById("P1").getLots().get(0).getBalance());
    }

    @Test
    void successfulSaveLeavesNoTemporaryFile() throws IOException {
        JsonProductRepository repo = new JsonProductRepository(dataFile());
        repo.save(sample("P1"));
        assertEquals(0, tempFiles());
    }

    // ---- compatibilidad con la versión 1 ----

    @Test
    void loadsFileWrittenByVersionOneWithoutLoss() throws IOException {
        String v1 = """
                [
                  {
                    "code": "MED-001",
                    "name": "Ácido fólico 5 mg",
                    "minStock": 20,
                    "alertThresholdDays": 180,
                    "lots": [
                      { "code": "L-2026-A", "dueDate": "2027-03-31", "balance": 40 },
                      { "code": "L-2026-B", "dueDate": "2026-09-30", "balance": 7 }
                    ]
                  },
                  {
                    "code": "MED-002",
                    "name": "Jeringa 5 ml",
                    "minStock": 0,
                    "alertThresholdDays": 30,
                    "lots": []
                  }
                ]
                """;
        Files.writeString(dataFile(), v1, StandardCharsets.UTF_8);

        JsonProductRepository repo = new JsonProductRepository(dataFile());
        assertNull(repo.getStartupWarning());
        assertEquals(2, repo.findAll().size());
        Product p = repo.findById("MED-001");
        assertEquals("Ácido fólico 5 mg", p.getName());
        assertEquals(20, p.getMinStock());
        assertEquals(180, p.getAlertThresholdDays());
        assertEquals(2, p.getLots().size());
        assertEquals(LocalDate.of(2027, 3, 31), p.getLots().get(0).getDueDate());
        assertEquals(40, p.getLots().get(0).getBalance());
        assertEquals(7, p.getLots().get(1).getBalance());
        assertEquals(0, repo.findById("MED-002").getLots().size());
    }

    @Test
    void versionOneFileWithoutLotsFieldStillLoads() throws IOException {
        Files.writeString(dataFile(),
                "[{\"code\":\"A\",\"name\":\"Uno\",\"minStock\":1,\"alertThresholdDays\":5}]", StandardCharsets.UTF_8);
        JsonProductRepository repo = new JsonProductRepository(dataFile());
        assertNull(repo.getStartupWarning());
        assertEquals(0, repo.findById("A").getLots().size());
    }

    @Test
    void fileInPlatformEncodingStillLoads() throws IOException {
        String json = "[{\"code\":\"A\",\"name\":\"Cañón\",\"minStock\":1,\"alertThresholdDays\":5,\"lots\":[]}]";
        Files.write(dataFile(), json.getBytes(StandardCharsets.ISO_8859_1));
        JsonProductRepository repo = new JsonProductRepository(dataFile());
        assertNull(repo.getStartupWarning());
        assertEquals(1, repo.findAll().size());
    }

    // ---- RNF08: JSON ilegible o malformado ----

    @Test
    void malformedJsonKeepsBackupAndStartsEmpty() throws IOException {
        String broken = "[{\"code\": \"P1\", \"name\": ";
        Files.writeString(dataFile(), broken);

        JsonProductRepository repo = new JsonProductRepository(dataFile());

        assertNotNull(repo.getStartupWarning(), "debe haber un mensaje claro para el usuario");
        assertTrue(repo.getStartupWarning().contains(".bak"));
        assertTrue(repo.findAll().isEmpty());
        Path backup = dir.resolve("inventory.json.bak");
        assertTrue(Files.exists(backup));
        assertEquals(broken, Files.readString(backup));

        // la aplicación sigue funcionando y el respaldo no se pierde
        repo.save(sample("P1"));
        assertEquals(1, new JsonProductRepository(dataFile()).findAll().size());
        assertEquals(broken, Files.readString(backup));
    }

    @Test
    void jsonWithWrongShapeIsTreatedAsMalformed() throws IOException {
        Files.writeString(dataFile(), "{\"code\": \"P1\"}");
        JsonProductRepository repo = new JsonProductRepository(dataFile());
        assertNotNull(repo.getStartupWarning());
        assertTrue(Files.exists(dir.resolve("inventory.json.bak")));
    }

    @Test
    void productWithoutCodeIsTreatedAsMalformed() throws IOException {
        Files.writeString(dataFile(), "[{\"name\":\"Sin código\",\"minStock\":1,\"alertThresholdDays\":5}]");
        JsonProductRepository repo = new JsonProductRepository(dataFile());
        assertNotNull(repo.getStartupWarning());
        assertTrue(repo.findAll().isEmpty());
    }

    @Test
    void lotWithInvalidDateIsTreatedAsMalformed() throws IOException {
        Files.writeString(dataFile(), "[{\"code\":\"A\",\"name\":\"Uno\",\"minStock\":1,\"alertThresholdDays\":5,"
                + "\"lots\":[{\"code\":\"L\",\"dueDate\":\"no-es-fecha\",\"balance\":3}]}]");
        JsonProductRepository repo = new JsonProductRepository(dataFile());
        assertNotNull(repo.getStartupWarning());
        assertTrue(Files.exists(dir.resolve("inventory.json.bak")));
    }

    @Test
    void existingBackupIsNeverOverwritten() throws IOException {
        Files.writeString(dir.resolve("inventory.json.bak"), "respaldo anterior");
        Files.writeString(dataFile(), "esto no es json");
        new JsonProductRepository(dataFile());
        assertEquals("respaldo anterior", Files.readString(dir.resolve("inventory.json.bak")));
        try (Stream<Path> s = Files.list(dir)) {
            assertTrue(s.anyMatch(f -> f.getFileName().toString().startsWith("inventory.json.bak.")));
        }
    }

    @Test
    void blankFileIsJustAnEmptyInventory() throws IOException {
        Files.writeString(dataFile(), "   \n");
        JsonProductRepository repo = new JsonProductRepository(dataFile());
        assertNull(repo.getStartupWarning());
        assertTrue(repo.findAll().isEmpty());
    }

    // ---- RNF09: guardado seguro y fallos de escritura ----

    @Test
    void failedWriteThrowsClearErrorAndKeepsPreviousState() throws IOException {
        JsonProductRepository repo = new JsonProductRepository(dataFile());
        repo.save(sample("P1"));

        // se simula un fallo de disco: el destino pasa a ser un directorio no vacío
        Files.delete(dataFile());
        Files.createDirectory(dataFile());
        Files.createFile(dataFile().resolve("bloqueo"));

        PersistenceException ex = assertThrows(PersistenceException.class, () -> repo.save(sample("P2")));
        assertNotNull(ex.getMessage());
        assertTrue(ex.getMessage().contains("No se pudieron guardar"));

        assertEquals(1, repo.findAll().size(), "el estado en memoria no cambia si el guardado falla");
        assertNull(repo.findById("P2"));
        assertEquals(0, tempFiles(), "no debe quedar el archivo temporal");
        assertThrows(PersistenceException.class, () -> repo.delete("P1"));
        assertNotNull(repo.findById("P1"));
    }

    @Test
    void interruptedWriteLeavesDataFileIntact() throws IOException {
        JsonProductRepository repo = new JsonProductRepository(dataFile());
        repo.save(sample("P1"));
        String before = Files.readString(dataFile());
        // un archivo temporal huérfano de una escritura interrumpida no afecta al archivo de datos
        Files.writeString(dir.resolve("inventory.json.tmp"), "[{\"code\":");
        assertEquals(before, Files.readString(dataFile()));
        assertEquals(1, new JsonProductRepository(dataFile()).findAll().size());
        repo.save(sample("P2"));
        assertEquals(2, new JsonProductRepository(dataFile()).findAll().size());
    }

    @Test
    void cannotCreateDataFileShowsWarningButAppKeepsRunning() throws IOException {
        Path missingDir = dir.resolve("no-existe").resolve("inventory.json");
        JsonProductRepository repo = new JsonProductRepository(missingDir);
        assertNotNull(repo.getStartupWarning());
        assertTrue(repo.findAll().isEmpty());
        assertThrows(PersistenceException.class, () -> repo.save(sample("P1")));
    }
}
