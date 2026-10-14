package domain;

import java.time.LocalDate;

public class Lot {
    private String code;
    private LocalDate dueDate;
    private int balance;

    public Lot(String code, LocalDate dueDate, int balance) {
        if (balance <= 0) throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        this.code = code;
        this.dueDate = dueDate;
        this.balance = balance;
    }

    public String getCode() { return code; }
    public LocalDate getDueDate() { return dueDate; }
    public int getBalance() { return balance; }
    public void setBalance(int balance) { 
        if (balance < 0) throw new IllegalArgumentException("El saldo no puede ser negativo");
        this.balance = balance; 
    }

    public boolean isExpired(LocalDate today) {
        return today.isAfter(dueDate);
    }
}
