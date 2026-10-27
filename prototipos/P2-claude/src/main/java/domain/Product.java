package domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Product {
    // Los nombres de los campos forman parte del formato del archivo JSON: no renombrar.
    private String code;
    private String name;
    private int minStock;
    private int alertThresholdDays;
    private List<Lot> lots;

    public Product(String code, String name, int minStock, int alertThresholdDays) {
        validateName(name);
        validateMinStock(minStock);
        validateAlertThreshold(alertThresholdDays);
        this.code = code;
        this.name = name;
        this.minStock = minStock;
        this.alertThresholdDays = alertThresholdDays;
        this.lots = new ArrayList<>();
    }

    // ---- Validaciones (RN14, RN15, RN16) ----

    private static void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del producto no puede estar vacío");
        }
    }

    private static void validateMinStock(int minStock) {
        if (minStock < 0) {
            throw new IllegalArgumentException("El stock mínimo debe ser un entero mayor o igual a 0");
        }
    }

    private static void validateAlertThreshold(int days) {
        if (days <= 0) {
            throw new IllegalArgumentException("El umbral de alerta debe ser un entero mayor a 0");
        }
    }

    public String getCode() { return code; }
    public String getName() { return name; }
    public int getMinStock() { return minStock; }
    public int getAlertThresholdDays() { return alertThresholdDays; }

    public List<Lot> getLots() {
        if (lots == null) lots = new ArrayList<>(); // datos antiguos sin lista de lotes
        return lots;
    }

    public void setName(String name) {
        validateName(name);
        this.name = name;
    }

    public void setMinStock(int minStock) {
        validateMinStock(minStock);
        this.minStock = minStock;
    }

    public void setAlertThresholdDays(int alertThresholdDays) {
        validateAlertThreshold(alertThresholdDays);
        this.alertThresholdDays = alertThresholdDays;
    }

    /** Edita los tres datos a la vez: si alguno es inválido, no se modifica ninguno (RN17). */
    public void update(String name, int minStock, int alertThresholdDays) {
        validateName(name);
        validateMinStock(minStock);
        validateAlertThreshold(alertThresholdDays);
        this.name = name;
        this.minStock = minStock;
        this.alertThresholdDays = alertThresholdDays;
    }

    // ---- Existencias (RN07) ----

    public int getAvailableStock(LocalDate today) {
        return getLots().stream().filter(l -> !l.isExpired(today)).mapToInt(Lot::getBalance).sum();
    }

    public int getExpiredStock(LocalDate today) {
        return getLots().stream().filter(l -> l.isExpired(today)).mapToInt(Lot::getBalance).sum();
    }

    /** RN10: stock bajo si disponible < mínimo; con mínimo 0 nunca hay alerta. */
    public boolean isLowStock(LocalDate today) {
        return minStock > 0 && getAvailableStock(today) < minStock;
    }

    /** Presentación derivada: Sin stock prevalece sobre Stock bajo. */
    public ProductStatus getStockStatus(LocalDate today) {
        if (getAvailableStock(today) == 0) return ProductStatus.SIN_STOCK;
        if (isLowStock(today)) return ProductStatus.STOCK_BAJO;
        return ProductStatus.DISPONIBLE;
    }

    public LotStatus getLotStatus(Lot lot, LocalDate today) {
        return lot.getStatus(today, alertThresholdDays);
    }

    // ---- Lotes ----

    public void addLot(Lot lot, LocalDate today) {
        if (lot.getDueDate().isBefore(today)) {
            throw new IllegalArgumentException("El lote no puede tener fecha de vencimiento pasada");
        }
        if (getLots().stream().anyMatch(l -> l.getCode().equals(lot.getCode()))) {
            throw new IllegalArgumentException("Ya existe un lote con ese código en el producto");
        }
        getLots().add(lot);
    }

    public void removeExpiredLot(String lotCode, LocalDate today) {
        Lot lot = getLots().stream().filter(l -> l.getCode().equals(lotCode)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Lote no encontrado"));
        if (!lot.isExpired(today)) {
            throw new IllegalStateException("El lote no está caducado");
        }
        getLots().remove(lot);
    }

    /**
     * Orden FEFO (único lugar donde se define): primero los lotes no caducados por vencimiento más
     * próximo (empates por orden de registro), después los caducados por vencimiento.
     */
    public List<Lot> getLotsInFefoOrder(LocalDate today) {
        List<Lot> ordered = new ArrayList<>(getLots());
        // List.sort es estable: los empates conservan el orden de registro (RN08).
        ordered.sort(Comparator
                .comparing((Lot l) -> l.isExpired(today))
                .thenComparing(Lot::getDueDate));
        return ordered;
    }

    /**
     * RN08 y RN09: consume por FEFO y devuelve cómo se repartió la salida.
     * Si no alcanza el stock disponible se rechaza completa y no cambia ningún saldo.
     */
    public List<ExitAllocation> processExit(int quantity, LocalDate today) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }
        if (getAvailableStock(today) < quantity) {
            throw new IllegalStateException("Stock insuficiente para realizar la salida");
        }

        List<ExitAllocation> allocations = new ArrayList<>();
        int remaining = quantity;
        for (Lot l : getLotsInFefoOrder(today)) {
            if (remaining <= 0) break;
            if (l.isExpired(today) || l.getBalance() <= 0) continue;
            int take = Math.min(l.getBalance(), remaining);
            l.setBalance(l.getBalance() - take);
            remaining -= take;
            allocations.add(new ExitAllocation(l.getCode(), l.getDueDate(), take));
        }
        return allocations;
    }
}
