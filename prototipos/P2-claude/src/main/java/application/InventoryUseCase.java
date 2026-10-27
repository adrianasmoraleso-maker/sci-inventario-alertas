package application;

import domain.Alert;
import domain.ExitAllocation;
import domain.Product;
import java.time.LocalDate;
import java.util.List;

public interface InventoryUseCase {
    void createProduct(String code, String name, int minStock, int alertThreshold);
    List<Product> listProducts();
    void editProduct(String code, String name, int minStock, int alertThreshold);
    void deleteProduct(String code);
    void registerLot(String productCode, String lotCode, LocalDate dueDate, int quantity);
    void registerEntry(String productCode, String lotCode, int quantity);
    List<ExitAllocation> registerExit(String productCode, int quantity);
    List<Alert> getAlerts();
    void deleteExpiredLot(String productCode, String lotCode);
}
