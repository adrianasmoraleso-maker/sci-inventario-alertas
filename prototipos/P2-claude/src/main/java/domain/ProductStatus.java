package domain;

/** Estado de presentación de un producto, derivado de reglas existentes (RN07 y RN10). */
public enum ProductStatus {
    DISPONIBLE("Disponible"),
    STOCK_BAJO("Stock bajo"),
    SIN_STOCK("Sin stock");

    private final String label;

    ProductStatus(String label) { this.label = label; }

    public String getLabel() { return label; }
}
