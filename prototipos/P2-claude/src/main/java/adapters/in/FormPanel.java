package adapters.in;

import javax.swing.*;
import java.awt.*;

/**
 * Base de los formularios: campos con error junto al campo, mensaje general para errores de reglas
 * y botones Guardar / Cancelar. Un valor inválido nunca llega al caso de uso.
 */
abstract class FormPanel extends JPanel {
    private final JLabel formError = new JLabel(" ");
    private final JPanel fields = new JPanel();
    private final AppButton saveButton;
    private final AppButton cancelButton = new AppButton("Cancelar", AppButton.Style.SECONDARY);
    private final java.util.function.Consumer<String> onSaved;
    private final Runnable onClose;

    FormPanel(String saveText, java.util.function.Consumer<String> onSaved, Runnable onClose) {
        super(new BorderLayout(0, Theme.SP_M));
        this.onSaved = onSaved;
        this.onClose = onClose;
        setOpaque(false);

        fields.setOpaque(false);
        fields.setLayout(new BoxLayout(fields, BoxLayout.Y_AXIS));

        formError.setName("form-error");
        formError.setFont(Theme.FONT_SMALL_BOLD);
        formError.setForeground(Theme.ERROR);

        saveButton = new AppButton(saveText, AppButton.Style.PRIMARY);
        saveButton.setName("form-save");
        cancelButton.setName("form-cancel");
        saveButton.addActionListener(e -> save());
        cancelButton.addActionListener(e -> onClose.run());

        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        south.add(formError, BorderLayout.NORTH);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, Theme.SP_S, Theme.SP_S));
        buttons.setOpaque(false);
        buttons.add(cancelButton);
        buttons.add(saveButton);
        south.add(buttons, BorderLayout.CENTER);

        add(fields, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(Math.max(380, d.width), d.height);
    }

    /** Agrega un campo; Enter en el campo guarda el formulario. */
    protected <T extends FormField> T addField(T field) {
        field.setAlignmentX(LEFT_ALIGNMENT);
        field.input().addActionListener(e -> save());
        fields.add(field);
        fields.add(Box.createVerticalStrut(Theme.SP_XS));
        return field;
    }

    protected void addNote(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_SMALL);
        l.setForeground(Theme.TEXT_MUTED);
        l.setAlignmentX(LEFT_ALIGNMENT);
        fields.add(l);
        fields.add(Box.createVerticalStrut(Theme.SP_M));
    }

    /** Revisa todos los campos, muestra los errores junto a cada uno y devuelve true si todo es válido. */
    protected abstract boolean validateFields();

    /** Llama al caso de uso con valores ya validados. Puede lanzar errores de reglas de negocio. */
    protected abstract void perform();

    protected abstract String successMessage();

    /** Acción de Guardar (también accesible desde pruebas). */
    void save() {
        formError.setText(" ");
        if (!validateFields()) return;
        try {
            perform();
        } catch (Exception ex) {
            formError.setText("<html><body style='width:340px'>" + escape(Messages.of(ex)) + "</body></html>");
            return;
        }
        onSaved.accept(successMessage());
        onClose.run();
    }

    String formErrorText() { return formError.getText().trim(); }

    private static String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    /** Pone el error en el campo si lo hay y devuelve si el campo es válido. */
    protected static boolean check(FormField field, String error) {
        field.setError(error);
        return error == null;
    }
}
