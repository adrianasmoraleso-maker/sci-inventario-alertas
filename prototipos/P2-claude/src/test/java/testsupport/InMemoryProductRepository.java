package testsupport;

import domain.PersistenceException;
import domain.Product;
import domain.ProductRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Repositorio en memoria para pruebas; cuenta los guardados y puede simular un fallo de escritura. */
public class InMemoryProductRepository implements ProductRepository {
    private final Map<String, Product> data = new LinkedHashMap<>();
    public int saveCount;
    public boolean failOnSave;

    @Override public List<Product> findAll() { return new ArrayList<>(data.values()); }
    @Override public Product findById(String code) { return data.get(code); }

    @Override
    public void save(Product product) {
        if (failOnSave) throw new PersistenceException("Fallo de escritura simulado");
        saveCount++;
        data.put(product.getCode(), product);
    }

    @Override
    public void delete(String code) {
        if (failOnSave) throw new PersistenceException("Fallo de escritura simulado");
        data.remove(code);
    }

    @Override
    public void saveAll(List<Product> products) {
        data.clear();
        for (Product p : products) data.put(p.getCode(), p);
    }
}
