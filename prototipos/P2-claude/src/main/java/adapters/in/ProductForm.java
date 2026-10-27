package adapters.in;

import application.InventoryUseCase;
import domain.Product;

/** Formulario para crear un producto o editar uno existente (RF01, RF03, RF-UI17). */
class ProductForm extends FormPanel {
    static final int DEFAULT_THRESHOLD_DAYS = 180;

    private final InventoryUseCase useCase;
    private final Product existing;
    private final FormField code = new FormField("field-code", "Código", 20);
    private final FormField name = new FormField("field-name", "Nombre", 20);
    private final FormField minStock = new FormField("field-min-stock", "Stock mínimo", 20);
    private final FormField threshold = new FormField("field-threshold", "Umbral de alerta de caducidad (días)", 20);

    /** {@code existing == null} crea un producto nuevo. */
    ProductForm(InventoryUseCase useCase, Product existing, java.util.function.Consumer<String> onSaved, Runnable onClose) {
        super(existing == null ? "Crear producto" : "Guardar", onSaved, onClose);
        this.useCase = useCase;
        this.existing = existing;
        addField(code);
        addField(name);
        addField(minStock);
        addField(threshold);
        if (existing == null) {
            minStock.input().setText("0");
            threshold.input().setText(String.valueOf(DEFAULT_THRESHOLD_DAYS));
        } else {
            code.input().setText(existing.getCode());
            code.input().setEnabled(false);
            name.input().setText(existing.getName());
            minStock.input().setText(String.valueOf(existing.getMinStock()));
            threshold.input().setText(String.valueOf(existing.getAlertThresholdDays()));
        }
    }

    @Override
    protected boolean validateFields() {
        boolean ok = true;
        if (existing == null) ok &= check(code, Validation.requiredError(code.text()));
        ok &= check(name, Validation.requiredError(name.text()));
        ok &= check(minStock, Validation.integerError(minStock.text(), 0));
        ok &= check(threshold, Validation.integerError(threshold.text(), 1));
        return ok;
    }

    @Override
    protected void perform() {
        int min = Validation.parseInt(minStock.text());
        int days = Validation.parseInt(threshold.text());
        if (existing == null) {
            useCase.createProduct(code.text().trim(), name.text().trim(), min, days);
        } else {
            useCase.editProduct(existing.getCode(), name.text().trim(), min, days);
        }
    }

    @Override
    protected String successMessage() {
        return existing == null ? "Producto creado" : "Producto actualizado";
    }
}
