package testsupport;

import domain.ClockPort;

import java.time.LocalDate;

/** Reloj fijo (y modificable) para pruebas deterministas. */
public class FixedClock implements ClockPort {
    public LocalDate today;

    public FixedClock(LocalDate today) { this.today = today; }

    @Override public LocalDate today() { return today; }
}
