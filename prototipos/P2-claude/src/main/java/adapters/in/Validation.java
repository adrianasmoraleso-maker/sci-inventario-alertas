package adapters.in;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.regex.Pattern;

/**
 * Validación de entradas del formulario (RF-UI08, RF-UI15). Solo comprueba el formato de lo
 * que escribe la persona; las reglas de negocio las aplica el dominio.
 */
final class Validation {
    private Validation() { }

    static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);
    private static final Pattern INTEGER = Pattern.compile("[+-]?[0-9]+");

    static final String REQUIRED = "Este campo es obligatorio.";

    /** Devuelve el mensaje de error para un entero con valor mínimo {@code min}, o null si es válido. */
    static String integerError(String raw, int min) {
        String text = raw == null ? "" : raw.trim();
        if (text.isEmpty()) return REQUIRED;
        if (!INTEGER.matcher(text).matches() || text.startsWith("+")) {
            return "Ingresa solo números enteros (sin decimales, letras ni símbolos).";
        }
        if (text.startsWith("-")) return "No se permiten valores negativos.";
        long value;
        try {
            value = Long.parseLong(text);
        } catch (NumberFormatException e) {
            return text.startsWith("-") ? "No se permiten valores negativos." : "El número es demasiado grande.";
        }
        if (value < 0) return "No se permiten valores negativos.";
        if (value > Integer.MAX_VALUE) return "El número es demasiado grande.";
        if (value < min) return min == 1 ? "Debe ser mayor que 0." : "Debe ser " + min + " o mayor.";
        return null;
    }

    /** Solo se llama después de comprobar que {@link #integerError} devuelve null. */
    static int parseInt(String raw) {
        return Integer.parseInt(raw.trim());
    }

    static String requiredError(String raw) {
        return raw == null || raw.trim().isEmpty() ? REQUIRED : null;
    }

    /** Devuelve el error de una fecha dd/mm/aaaa que no puede ser anterior a {@code today}, o null. */
    static String dateError(String raw, LocalDate today) {
        String text = raw == null ? "" : raw.trim();
        if (text.isEmpty()) return REQUIRED;
        try {
            LocalDate date = LocalDate.parse(text, DATE_FORMAT);
            if (date.isBefore(today)) return "La fecha no puede ser anterior a hoy.";
            return null;
        } catch (DateTimeParseException e) {
            return "Fecha no válida. Usa el formato dd/mm/aaaa.";
        }
    }

    static LocalDate parseDate(String raw) {
        return LocalDate.parse(raw.trim(), DATE_FORMAT);
    }
}
