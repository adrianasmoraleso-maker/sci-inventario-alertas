package domain;

/** Estado de un lote según las reglas RN05 y RN11. */
public enum LotStatus {
    VIGENTE("Vigente"),
    PROXIMO_A_CADUCAR("Próximo a caducar"),
    CADUCADO("Caducado");

    private final String label;

    LotStatus(String label) { this.label = label; }

    public String getLabel() { return label; }
}
