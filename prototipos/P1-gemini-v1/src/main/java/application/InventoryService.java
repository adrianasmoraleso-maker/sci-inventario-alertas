package application;

import domain.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class InventoryService implements InventoryUseCase {
    private final ProductRepository repository;
    private final ClockPort clock;

    public InventoryService(ProductRepository repository, ClockPort clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Override
    public void createProduct(String code, String name, int minStock, int alertThreshold) {
        if (repository.findById(code) != null) {
            throw new IllegalArgumentException("Ya existe un producto con el código dado");
        }
        Product p = new Product(code, name, minStock, alertThreshold);
        repository.save(p);
    }

    @Override
    public List<Product> listProducts() {
        return repository.findAll();
    }

    @Override
    public void editProduct(String code, String name, int minStock, int alertThreshold) {
        Product p = repository.findById(code);
        if (p == null) throw new IllegalArgumentException("Producto no encontrado");
        p.setName(name);
        p.setMinStock(minStock);
        p.setAlertThresholdDays(alertThreshold);
        repository.save(p);
    }

    @Override
    public void deleteProduct(String code) {
        Product p = repository.findById(code);
        if (p == null) throw new IllegalArgumentException("Producto no encontrado");
        if (p.getLots().stream().anyMatch(l -> l.getBalance() > 0)) {
            throw new IllegalStateException("No se puede eliminar un producto con lotes que tengan saldo mayor a 0");
        }
        repository.delete(code);
    }

    @Override
    public void registerLot(String productCode, String lotCode, LocalDate dueDate, int quantity) {
        Product p = repository.findById(productCode);
        if (p == null) throw new IllegalArgumentException("Producto no encontrado");
        p.addLot(new Lot(lotCode, dueDate, quantity), clock.today());
        repository.save(p);
    }

    @Override
    public void registerEntry(String productCode, String lotCode, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        Product p = repository.findById(productCode);
        if (p == null) throw new IllegalArgumentException("Producto no encontrado");
        Lot lot = p.getLots().stream().filter(l -> l.getCode().equals(lotCode)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Lote no encontrado"));
        if (lot.isExpired(clock.today())) {
            throw new IllegalStateException("No se puede añadir entradas a un lote caducado");
        }
        lot.setBalance(lot.getBalance() + quantity);
        repository.save(p);
    }

    @Override
    public void registerExit(String productCode, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        Product p = repository.findById(productCode);
        if (p == null) throw new IllegalArgumentException("Producto no encontrado");
        p.processExit(quantity, clock.today());
        repository.save(p);
    }

    @Override
    public List<Alert> getAlerts() {
        List<Alert> alerts = new ArrayList<>();
        LocalDate today = clock.today();
        for (Product p : repository.findAll()) {
            int available = p.getAvailableStock(today);
            if (p.getMinStock() > 0 && available < p.getMinStock()) {
                alerts.add(new Alert("STOCK_BAJO", p.getCode(), null, "Stock disponible menor al mínimo (" + available + " < " + p.getMinStock() + ")"));
            }
            for (Lot l : p.getLots()) {
                if (l.isExpired(today)) {
                    alerts.add(new Alert("CADUCADO", p.getCode(), l.getCode(), "Lote caducado el " + l.getDueDate()));
                } else if (l.getBalance() > 0) {
                    long daysToExpiry = ChronoUnit.DAYS.between(today, l.getDueDate());
                    if (daysToExpiry <= p.getAlertThresholdDays()) {
                        alerts.add(new Alert("PROXIMO_A_CADUCAR", p.getCode(), l.getCode(), "Vence en " + daysToExpiry + " días"));
                    }
                }
            }
        }
        return alerts;
    }

    @Override
    public void deleteExpiredLot(String productCode, String lotCode) {
        Product p = repository.findById(productCode);
        if (p == null) throw new IllegalArgumentException("Producto no encontrado");
        p.removeExpiredLot(lotCode, clock.today());
        repository.save(p);
    }
}
