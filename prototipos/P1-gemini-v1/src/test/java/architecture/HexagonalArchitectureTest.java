package architecture;

import adapters.out.BogotaClockAdapter;
import adapters.out.JsonProductRepository;
import application.InventoryService;
import domain.ClockPort;
import domain.ProductRepository;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class HexagonalArchitectureTest {
    private static final Path SOURCE_ROOT = Path.of("src", "main", "java");
    private static final Pattern IMPORT = Pattern.compile("(?m)^\\s*import\\s+(?:static\\s+)?([\\w.*]+)\\s*;");

    @Test
    void domainDoesNotDependOnApplicationAdaptersSwingPersistenceOrJson() throws IOException {
        List<Path> domainSources = javaSources(SOURCE_ROOT.resolve("domain"));

        assertFalse(domainSources.isEmpty());
        for (Path source : domainSources) {
            String text = Files.readString(source);
            assertNoImportsFrom(source, text, List.of(
                    "application.", "adapters.", "javax.swing.", "java.io.",
                    "java.nio.file.", "com.google.gson."
            ));
        }
    }

    @Test
    void applicationDoesNotDependOnAdaptersOrInfrastructureLibraries() throws IOException {
        List<Path> applicationSources = javaSources(SOURCE_ROOT.resolve("application"));

        assertFalse(applicationSources.isEmpty());
        for (Path source : applicationSources) {
            String text = Files.readString(source);
            assertNoImportsFrom(source, text, List.of(
                    "adapters.", "javax.swing.", "java.io.", "java.nio.file.", "com.google.gson."
            ));
        }
    }

    @Test
    void outputAdaptersImplementTheDomainPorts() {
        assertTrue(List.of(JsonProductRepository.class.getInterfaces()).contains(ProductRepository.class));
        assertTrue(List.of(BogotaClockAdapter.class.getInterfaces()).contains(ClockPort.class));
        assertArrayEquals(new Class<?>[]{ProductRepository.class, ClockPort.class},
                InventoryService.class.getDeclaredConstructors()[0].getParameterTypes());
    }

    private static List<Path> javaSources(Path directory) throws IOException {
        try (Stream<Path> files = Files.walk(directory)) {
            return files.filter(path -> path.toString().endsWith(".java")).toList();
        }
    }

    private static void assertNoImportsFrom(Path source, String text, List<String> forbiddenPrefixes) {
        var imports = IMPORT.matcher(text);
        while (imports.find()) {
            String importedType = imports.group(1);
            for (String forbiddenPrefix : forbiddenPrefixes) {
                assertFalse(importedType.startsWith(forbiddenPrefix),
                        () -> source + " must not depend on " + importedType);
            }
        }
    }
}
