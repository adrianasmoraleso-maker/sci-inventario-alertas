package adapters.in;

import domain.Product;

/** Elemento de un selector de productos ("CÓDIGO — Nombre"). {@code code == null} es la opción "Todos". */
record ProductItem(String code, String label) {
    static ProductItem of(Product p) { return new ProductItem(p.getCode(), p.getCode() + " — " + p.getName()); }

    static ProductItem all(String label) { return new ProductItem(null, label); }

    @Override public String toString() { return label; }
}
