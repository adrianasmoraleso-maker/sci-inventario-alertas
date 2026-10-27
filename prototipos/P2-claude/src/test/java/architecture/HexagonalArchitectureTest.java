package architecture;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/** Regla de dependencias (RNF01 a RNF04): comprueba los imports de cada zona. */
class HexagonalArchitectureTest {
    private static final Path MAIN = Paths.get("src", "main", "java");

    private static List<Path> sources(String zone) throws IOException {
        try (Stream<Path> s = Files.walk(MAIN.resolve(zone))) {
            return s.filter(p -> p.toString().endsWith(".java")).collect(Collectors.toList());
        }
    }

    private static List<String> imports(Path file) throws IOException {
        return Files.readAllLines(file).stream().map(String::trim)
                .filter(l -> l.startsWith("import ")).collect(Collectors.toList());
    }

    private static void assertNoImports(String zone, String... forbidden) throws IOException {
        for (Path file : sources(zone)) {
            for (String line : imports(file)) {
                for (String f : forbidden) {
                    assertFalse(line.startsWith("import " + f) || line.startsWith("import static " + f),
                            file + " importa " + f + " (" + line + ")");
                }
            }
        }
    }

    @Test
    void domainDoesNotDependOnFrameworksIoOrOuterZones() throws IOException {
        assertNoImports("domain", "javax.swing", "java.awt", "java.io", "java.nio.file", "com.google.gson",
                "application", "adapters");
    }

    @Test
    void applicationDependsOnlyOnDomainAndPorts() throws IOException {
        assertNoImports("application", "javax.swing", "java.awt", "java.io", "java.nio.file", "com.google.gson",
                "adapters");
    }

    @Test
    void guiDoesNotTouchFilesJsonOrOutputAdapters() throws IOException {
        assertNoImports("adapters/in", "java.io", "java.nio.file", "com.google.gson", "adapters.out");
    }

    @Test
    void portsAreInterfaces() {
        assertTrue(domain.ProductRepository.class.isInterface());
        assertTrue(domain.ClockPort.class.isInterface());
        assertTrue(application.InventoryUseCase.class.isInterface());
    }

    @Test
    void outputAdaptersImplementPorts() {
        assertTrue(domain.ProductRepository.class.isAssignableFrom(adapters.out.JsonProductRepository.class));
        assertTrue(domain.ClockPort.class.isAssignableFrom(adapters.out.BogotaClockAdapter.class));
    }

    @Test
    void serviceIsBuiltFromInterfaces() {
        for (Constructor<?> c : application.InventoryService.class.getConstructors()) {
            for (Class<?> type : c.getParameterTypes()) {
                assertTrue(type.isInterface(), "el constructor del servicio debe recibir puertos, no " + type);
            }
        }
    }

    @Test
    void guiOnlyKnowsTheInputPort() throws IOException {
        for (Path file : sources("adapters/in")) {
            for (String line : imports(file)) {
                assertFalse(line.contains("application.InventoryService"), file + " depende del servicio concreto");
            }
        }
    }
}
