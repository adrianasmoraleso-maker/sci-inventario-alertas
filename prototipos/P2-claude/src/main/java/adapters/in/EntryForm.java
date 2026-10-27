package adapters.in;

import application.InventoryUseCase;

/** Formulario para registrar una entrada adicional sobre un lote (RF06). */
class EntryForm extends FormPanel {
    private final InventoryUseCase useCase;
    private final String productCode;
    private final String lotCode;
    private final FormField quantity = new FormField("field-quantity", "Cantidad a ingresar", 20);

    EntryForm(InventoryUseCase useCase, String productCode, String lotCode, java.util.function.Consumer<String> onSaved, Runnable onClose) {
        super("Registrar entrada", onSaved, onClose);
        this.useCase = useCase;
        this.productCode = productCode;
        this.lotCode = lotCode;
        addNote("Producto: " + productCode + "  ·  Lote: " + lotCode);
        addField(quantity);
    }

    @Override
    protected boolean validateFields() {
        return check(quantity, Validation.integerError(quantity.text(), 1));
    }

    @Override
    protected void perform() {
        useCase.registerEntry(productCode, lotCode, Validation.parseInt(quantity.text()));
    }

    @Override
    protected String successMessage() { return "Entrada registrada"; }
}
