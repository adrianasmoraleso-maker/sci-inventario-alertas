package domain;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Lot {
    private String code;
    private LocalDate dueDate;
    private int balance;

    public Lot(String code, LocalDate dueDate, int balance) {
        if (balance <= 0) throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        if (dueDate == null) throw new IllegalArgumentException("La fecha de vencimiento es obligatoria");
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

    /** RN05: caducado si hoy > vencimiento (el día de vencimiento aún es utilizable). */
    public boolean isExpired(LocalDate today) {
        return today.isAfter(dueDate);
    }

    /** RN11: no caducado, saldo > 0 y hoy >= vencimiento - umbral (en días). */
    public boolean isExpiringSoon(LocalDate today, int thresholdDays) {
        return !isExpired(today)
                && balance > 0
                && !today.isBefore(dueDate.minusDays(thresholdDays));
    }

    /** Días que faltan para el vencimiento (negativo si ya caducó). */
    public long daysRemaining(LocalDate today) {
        return ChronoUnit.DAYS.between(today, dueDate);
    }

    public LotStatus getStatus(LocalDate today, int thresholdDays) {
        if (isExpired(today)) return LotStatus.CADUCADO;
        if (isExpiringSoon(today, thresholdDays)) return LotStatus.PROXIMO_A_CADUCAR;
        return LotStatus.VIGENTE;
    }
}
