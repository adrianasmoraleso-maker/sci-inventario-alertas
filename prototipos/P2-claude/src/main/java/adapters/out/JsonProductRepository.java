package adapters.out;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import domain.Lot;
import domain.PersistenceException;
import domain.Product;
import domain.ProductRepository;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Persistencia en un único archivo JSON (un registro por producto).
 *
 * - El formato es el de la versión 1 (campos code, name, minStock, alertThresholdDays, lots).
 * - Lectura: si el archivo está malformado se conserva una copia (.bak) y se inicia vacío;
 *   el motivo queda disponible en {@link #getStartupWarning()} para mostrarlo al usuario.
 * - Escritura: se escribe un archivo temporal y luego se reemplaza el archivo de datos.
 *   Si falla, se lanza {@link PersistenceException} y el estado en memoria no cambia.
 */
public class JsonProductRepository implements ProductRepository {
    public static final String DEFAULT_FILE_NAME = "inventory.json";
    private static final Type LIST_TYPE = new TypeToken<ArrayList<Product>>() { }.getType();

    private final Path file;
    private final Gson gson;
    private List<Product> cachedProducts = new ArrayList<>();
    private String startupWarning;
    private boolean writeBlocked;

    public JsonProductRepository() {
        this(Paths.get(DEFAULT_FILE_NAME));
    }

    public JsonProductRepository(Path file) {
        this.file = file.toAbsolutePath();
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .setPrettyPrinting()
                .create();
        load();
    }

    /** Mensaje para el usuario si hubo un problema al iniciar (archivo dañado, sin permisos...); si no, null. */
    public String getStartupWarning() {
        return startupWarning;
    }

    // ------------------------------------------------------------------ carga

    private void load() {
        if (!Files.exists(file)) {
            try {
                writeAtomically(toJson(new ArrayList<>()));
            } catch (PersistenceException e) {
                startupWarning = "No se pudo crear el archivo de datos (" + file.getFileName() + "). "
                        + "La aplicación seguirá abierta, pero los cambios no se podrán guardar hasta resolverlo.";
            }
            return;
        }

        String content;
        try {
            content = decode(Files.readAllBytes(file));
        } catch (IOException | RuntimeException e) {
            writeBlocked = true; // no se sobrescribe un archivo que no se pudo leer
            startupWarning = "No se pudo leer el archivo de datos (" + file.getFileName() + "). "
                    + "La aplicación inició vacía y no guardará cambios para no perder su contenido. "
                    + "Revise los permisos del archivo.";
            return;
        }

        try {
            cachedProducts = parse(content);
        } catch (RuntimeException e) {
            handleMalformed();
        }
    }

    /** Lee como UTF-8; si no es válido (archivos de la versión 1 con codificación de la plataforma), usa la del sistema. */
    private static String decode(byte[] bytes) {
        try {
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes)).toString();
        } catch (CharacterCodingException e) {
            return new String(bytes, Charset.defaultCharset());
        }
    }

    private List<Product> parse(String content) {
        if (content.isBlank()) return new ArrayList<>();
        List<Product> products = gson.fromJson(content, LIST_TYPE);
        if (products == null) return new ArrayList<>();
        for (Product p : products) {
            if (p == null || p.getCode() == null) throw new IllegalStateException("Producto sin código");
            for (Lot l : p.getLots()) {
                if (l == null || l.getCode() == null || l.getDueDate() == null) {
                    throw new IllegalStateException("Lote incompleto");
                }
            }
        }
        return products;
    }

    private void handleMalformed() {
        cachedProducts = new ArrayList<>();
        Path backup = file.resolveSibling(file.getFileName() + ".bak");
        if (Files.exists(backup)) {
            backup = file.resolveSibling(file.getFileName() + ".bak." + System.currentTimeMillis());
        }
        try {
            Files.copy(file, backup, StandardCopyOption.COPY_ATTRIBUTES);
            startupWarning = "El archivo de datos (" + file.getFileName() + ") está dañado o tiene un formato "
                    + "no válido. Se guardó una copia en «" + backup.getFileName() + "» y la aplicación inició "
                    + "con el inventario vacío.";
        } catch (IOException e) {
            writeBlocked = true;
            startupWarning = "El archivo de datos (" + file.getFileName() + ") está dañado y no se pudo "
                    + "guardar una copia. La aplicación inició vacía y no guardará cambios para no perder "
                    + "su contenido.";
        }
    }

    // ---------------------------------------------------------------- escritura

    private String toJson(List<Product> products) {
        return gson.toJson(products, LIST_TYPE);
    }

    private void persist(List<Product> newState) {
        if (writeBlocked) {
            throw new PersistenceException("No se pudo guardar: el archivo de datos original no se pudo leer o "
                    + "respaldar y no se sobrescribirá. La operación no se realizó.");
        }
        writeAtomically(toJson(newState));
        cachedProducts = newState; // solo si el guardado tuvo éxito
    }

    /** Escribe en un archivo temporal y luego lo mueve sobre el archivo de datos. */
    private void writeAtomically(String json) {
        Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
        try {
            try (FileChannel ch = FileChannel.open(tmp, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
                ByteBuffer buf = ByteBuffer.wrap(json.getBytes(StandardCharsets.UTF_8));
                while (buf.hasRemaining()) ch.write(buf);
                ch.force(true);
            }
            try {
                Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException | RuntimeException e) {
            try { Files.deleteIfExists(tmp); } catch (IOException ignored) { /* nada más que hacer */ }
            throw new PersistenceException("No se pudieron guardar los cambios en el archivo de datos ("
                    + file.getFileName() + "). Revise los permisos y el espacio en disco. "
                    + "La operación no se realizó.", e);
        }
    }

    // --------------------------------------------------------------- repositorio

    private Product copy(Product p) {
        return gson.fromJson(gson.toJson(p), Product.class);
    }

    private List<Product> copyAll(List<Product> products) {
        List<Product> out = new ArrayList<>();
        for (Product p : products) out.add(copy(p));
        return out;
    }

    @Override
    public List<Product> findAll() {
        return copyAll(cachedProducts);
    }

    @Override
    public Product findById(String code) {
        return cachedProducts.stream().filter(p -> p.getCode().equals(code)).findFirst().map(this::copy).orElse(null);
    }

    @Override
    public void save(Product product) {
        List<Product> next = copyAll(cachedProducts);
        Product incoming = copy(product);
        boolean replaced = false;
        for (int i = 0; i < next.size(); i++) {
            if (next.get(i).getCode().equals(incoming.getCode())) {
                next.set(i, incoming);
                replaced = true;
                break;
            }
        }
        if (!replaced) next.add(incoming);
        persist(next);
    }

    @Override
    public void delete(String code) {
        List<Product> next = copyAll(cachedProducts);
        next.removeIf(p -> p.getCode().equals(code));
        persist(next);
    }

    @Override
    public void saveAll(List<Product> products) {
        persist(copyAll(products));
    }

    static class LocalDateAdapter extends TypeAdapter<LocalDate> {
        @Override
        public void write(JsonWriter out, LocalDate value) throws IOException {
            if (value == null) { out.nullValue(); }
            else { out.value(value.toString()); }
        }

        @Override
        public LocalDate read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            return LocalDate.parse(in.nextString());
        }
    }
}
