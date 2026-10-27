package adapters.in;

import application.InventoryUseCase;
import domain.ClockPort;

/** Formulario para registrar un lote de un producto (RF05). */
class LotForm extends FormPanel {
    private final InventoryUseCase useCase;
    private final ClockPort clock;
    private final String productCode;
    private final FormField lotCode = new FormField("field-lot-code", "Código de lote", 20);
    private final FormField dueDate = new FormField("field-due-date", "Fecha de vencimiento (dd/mm/aaaa)", 20);
    private final FormField quantity = new FormField("field-quantity", "Cantidad inicial", 20);

    LotForm(InventoryUseCase useCase, ClockPort clock, String productCode, String productLabel,
            java.util.function.Consumer<String> onSaved, Runnable onClose) {
        super("Registrar lote", onSaved, onClose);
        this.useCase = useCase;
        this.clock = clock;
        this.productCode = productCode;
        addNote("Producto: " + productLabel);
        addField(lotCode);
        addField(dueDate);
        addField(quantity);
        dueDate.input().setPlaceholder("dd/mm/aaaa");
    }

    @Override
    protected boolean validateFields() {
        boolean ok = true;
        ok &= check(lotCode, Validation.requiredError(lotCode.text()));
        ok &= check(dueDate, Validation.dateError(dueDate.text(), clock.today()));
        ok &= check(quantity, Validation.integerError(quantity.text(), 1));
        return ok;
    }

    @Override
    protected void perform() {
        useCase.registerLot(productCode, lotCode.text().trim(), Validation.parseDate(dueDate.text()),
                Validation.parseInt(quantity.text()));
    }

    @Override
    protected String successMessage() { return "Lote registrado"; }
}
