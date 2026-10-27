package domain;

/**
 * Error al leer o escribir el almacenamiento. Forma parte del contrato del puerto
 * {@link ProductRepository}: los adaptadores la lanzan cuando una operación no pudo
 * completarse y, en ese caso, no deben haber cambiado el estado persistido.
 */
public class PersistenceException extends RuntimeException {
    public PersistenceException(String message) {
        super(message);
    }

    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
