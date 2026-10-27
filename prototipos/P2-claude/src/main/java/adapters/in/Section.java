package adapters.in;

/** Las seis secciones del menú (RF-UI19). */
public enum Section {
    INICIO("Inicio", Icons.HOME),
    PRODUCTOS("Productos", Icons.BOX),
    LOTES("Lotes", Icons.LAYERS),
    SALIDAS("Salidas", Icons.ARROW_OUT),
    EXISTENCIAS("Existencias", Icons.BARS),
    ALERTAS("Alertas", Icons.ALERT);

    private final String label;
    private final Icons icon;

    Section(String label, Icons icon) { this.label = label; this.icon = icon; }

    public String getLabel() { return label; }
    Icons icon() { return icon; }
}
