package domain;
import java.util.List;

public interface ProductRepository {
    List<Product> findAll();
    Product findById(String code);
    void save(Product product);
    void delete(String code);
    void saveAll(List<Product> products);
}
