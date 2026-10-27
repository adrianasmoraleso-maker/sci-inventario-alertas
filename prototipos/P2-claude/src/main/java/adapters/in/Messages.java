package adapters.in;

import domain.PersistenceException;

/** Convierte excepciones en mensajes claros en español; nunca expone trazas al usuario. */
final class Messages {
    private Messages() { }

    static final String UNEXPECTED = "Ocurrió un error inesperado. Inténtalo de nuevo.";

    static String of(Throwable ex) {
        if (ex instanceof IllegalArgumentException || ex instanceof IllegalStateException
                || ex instanceof PersistenceException) {
            String m = ex.getMessage();
            if (m != null && !m.isBlank()) return m;
        }
        ex.printStackTrace(); // solo en la consola, para diagnóstico
        return UNEXPECTED;
    }
}
