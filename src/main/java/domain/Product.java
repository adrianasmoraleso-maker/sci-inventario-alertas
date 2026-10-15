package domain;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Product {
    private String code;
    private String name;
    private int minStock;
    private int alertThresholdDays;
    private List<Lot> lots;

    public Product(String code, String name, int minStock, int alertThresholdDays) {
        this.code = code;
        this.name = name;
        this.minStock = minStock;
        this.alertThresholdDays = alertThresholdDays;
        this.lots = new ArrayList<>();
    }

    public String getCode() { return code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getMinStock() { return minStock; }
    public void setMinStock(int minStock) { this.minStock = minStock; }
    public int getAlertThresholdDays() { return alertThresholdDays; }
    public void setAlertThresholdDays(int alertThresholdDays) { this.alertThresholdDays = alertThresholdDays; }
    public List<Lot> getLots() { return lots; }

    public int getAvailableStock(LocalDate today) {
        return lots.stream().filter(l -> !l.isExpired(today)).mapToInt(Lot::getBalance).sum();
    }

    public int getExpiredStock(LocalDate today) {
        return lots.stream().filter(l -> l.isExpired(today)).mapToInt(Lot::getBalance).sum();
    }

    public void addLot(Lot lot, LocalDate today) {
        if (lot.getDueDate().isBefore(today)) {
            throw new IllegalArgumentException("El lote no puede tener fecha de vencimiento pasada");
        }
        if (lots.stream().anyMatch(l -> l.getCode().equals(lot.getCode()))) {
            throw new IllegalArgumentException("Ya existe un lote con ese código en el producto");
        }
        lots.add(lot);
    }

    public void removeExpiredLot(String lotCode, LocalDate today) {
        Lot lot = lots.stream().filter(l -> l.getCode().equals(lotCode)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Lote no encontrado"));
        if (!lot.isExpired(today)) {
            throw new IllegalStateException("El lote no está caducado");
        }
        lots.remove(lot);
    }

    public void processExit(int quantity, LocalDate today) {
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad debe ser mayor a cero"
            );
        }

        if (getAvailableStock(today) < quantity) {
            throw new IllegalStateException(
                    "Stock insuficiente para realizar la salida"
            );
        }

        List<Lot> validLots = new ArrayList<>();
        for (Lot l : lots) {
            if (!l.isExpired(today) && l.getBalance() > 0) {
                validLots.add(l);
            }
        }
        validLots.sort(Comparator.comparing(Lot::getDueDate));

        int remaining = quantity;
        for (Lot l : validLots) {
            if (remaining <= 0) break;
            int take = Math.min(l.getBalance(), remaining);
            l.setBalance(l.getBalance() - take);
            remaining -= take;
        }
    }
}
