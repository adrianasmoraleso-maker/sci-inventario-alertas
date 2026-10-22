package adapters.out;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import domain.Product;
import domain.ProductRepository;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class JsonProductRepository implements ProductRepository {
    private final Path filePath;
    private final Gson gson;
    private List<Product> cachedProducts;

    public JsonProductRepository() {
        this(Path.of("inventory.json"));
    }

    public JsonProductRepository(Path filePath) {
        this.filePath = filePath;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .setPrettyPrinting()
                .create();
        loadFromFile();
    }

    private void loadFromFile() {
        File file = filePath.toFile();
        if (!file.exists()) {
            cachedProducts = new ArrayList<>();
            saveToFile();
            return;
        }
        try (Reader reader = new FileReader(file)) {
            Type listType = new TypeToken<ArrayList<Product>>(){}.getType();
            cachedProducts = gson.fromJson(reader, listType);
            if (cachedProducts == null) cachedProducts = new ArrayList<>();
        } catch (Exception e) {
            System.err.println("Error leyendo archivo JSON, iniciando vacío. " + e.getMessage());
            cachedProducts = new ArrayList<>();
        }
    }

    private void saveToFile() {
        try (Writer writer = new FileWriter(filePath.toFile())) {
            gson.toJson(cachedProducts, writer);
        } catch (IOException e) {
            System.err.println("No se pudo guardar en el archivo JSON. " + e.getMessage());
        }
    }

    @Override
    public List<Product> findAll() {
        return new ArrayList<>(cachedProducts);
    }

    @Override
    public Product findById(String code) {
        return cachedProducts.stream().filter(p -> p.getCode().equals(code)).findFirst().orElse(null);
    }

    @Override
    public void save(Product product) {
        Product existing = findById(product.getCode());
        if (existing != null) {
            cachedProducts.remove(existing);
        }
        cachedProducts.add(product);
        saveToFile();
    }

    @Override
    public void delete(String code) {
        cachedProducts.removeIf(p -> p.getCode().equals(code));
        saveToFile();
    }

    @Override
    public void saveAll(List<Product> products) {
        this.cachedProducts = new ArrayList<>(products);
        saveToFile();
    }

    static class LocalDateAdapter extends TypeAdapter<LocalDate> {
        @Override
        public void write(JsonWriter out, LocalDate value) throws IOException {
            if (value == null) { out.nullValue(); }
            else { out.value(value.toString()); }
        }
        @Override
        public LocalDate read(JsonReader in) throws IOException {
            if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            return LocalDate.parse(in.nextString());
        }
    }
}
