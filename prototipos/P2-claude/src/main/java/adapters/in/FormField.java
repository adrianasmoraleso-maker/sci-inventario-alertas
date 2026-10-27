package adapters.in;

import javax.swing.*;
import java.awt.*;

/** Etiqueta + campo de texto + mensaje de error en rojo debajo del campo. */
class FormField extends JPanel {
    private final StyledTextField input;
    private final JLabel error = new JLabel(" ");

    FormField(String name, String label, int columns) {
        super();
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        input = new StyledTextField(columns);
        input.setName(name);
        error.setName(name + "-error");
        error.setFont(Theme.FONT_SMALL);
        error.setForeground(Theme.ERROR);

        JLabel title = new JLabel(label);
        title.setFont(Theme.FONT_SMALL_BOLD);
        title.setForeground(Theme.TEXT);
        title.setLabelFor(input);

        for (JComponent c : new JComponent[]{title, input, error}) c.setAlignmentX(LEFT_ALIGNMENT);
        add(title);
        add(Box.createVerticalStrut(Theme.SP_XS + 2));
        add(input);
        add(Box.createVerticalStrut(Theme.SP_XS));
        add(error);
        input.setMaximumSize(new Dimension(Integer.MAX_VALUE, Theme.CONTROL_HEIGHT));

        // al volver a escribir, desaparece el error anterior
        input.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { clearError(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { clearError(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { clearError(); }
        });
    }

    StyledTextField input() { return input; }

    String text() { return input.getText(); }

    void setError(String message) {
        error.setText(message == null || message.isEmpty() ? " " : message);
        input.setInvalid(message != null && !message.isEmpty());
    }

    void clearError() { setError(null); }

    String errorText() { return error.getText().trim(); }
}
