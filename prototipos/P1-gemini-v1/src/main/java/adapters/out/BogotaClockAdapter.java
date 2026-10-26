package adapters.out;

import domain.ClockPort;
import java.time.LocalDate;
import java.time.ZoneId;

public class BogotaClockAdapter implements ClockPort {
    @Override
    public LocalDate today() {
        return LocalDate.now(ZoneId.of("America/Bogota"));
    }
}
