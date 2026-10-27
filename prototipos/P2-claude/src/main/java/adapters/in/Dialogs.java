package adapters.in;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.function.Function;

/** Diálogos con el estilo del tema: confirmación, mensajes y formularios. */
final class Dialogs {
    private Dialogs() { }

    static boolean confirm(Component parent, String title, String message, String confirmText) {
        boolean[] result = {false};
        JDialog dialog = baseDialog(parent, title);
        AppButton ok = new AppButton(confirmText, AppButton.Style.PRIMARY);
        AppButton cancel = new AppButton("Cancelar", AppButton.Style.SECONDARY);
        ok.setName("dialog-confirm");
        cancel.setName("dialog-cancel");
        ok.addActionListener(e -> { result[0] = true; dialog.dispose(); });
        cancel.addActionListener(e -> dialog.dispose());
        show(dialog, title, bodyText(message), buttons(cancel, ok), ok);
        return result[0];
    }

    static void error(Component parent, String message) {
        message(parent, "No se pudo completar la acción", message);
    }

    static void warning(Component parent, String title, String message) {
        message(parent, title, message);
    }

    private static void message(Component parent, String title, String message) {
        JDialog dialog = baseDialog(parent, title);
        AppButton ok = new AppButton("Entendido", AppButton.Style.PRIMARY);
        ok.setName("dialog-ok");
        ok.addActionListener(e -> dialog.dispose());
        show(dialog, title, bodyText(message), buttons(ok), ok);
    }

    /** Abre un formulario modal; la fábrica recibe la acción para cerrar el diálogo. */
    static void showForm(Component parent, String title, Function<Runnable, JComponent> factory) {
        JDialog dialog = baseDialog(parent, title);
        JComponent form = factory.apply(dialog::dispose);
        show(dialog, title, form, null, null);
    }

    // ----------------------------------------------------------------- interno

    private static JDialog baseDialog(Component parent, String title) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);
        return dialog;
    }

    private static JComponent bodyText(String message) {
        String safe = message.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        JLabel l = new JLabel("<html><body style='width:340px'>" + safe + "</body></html>");
        l.setFont(Theme.FONT_BODY);
        l.setForeground(Theme.TEXT);
        return l;
    }

    private static JComponent buttons(JComponent... components) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, Theme.SP_S, 0));
        p.setOpaque(false);
        for (JComponent c : components) p.add(c);
        return p;
    }

    private static void show(JDialog dialog, String title, JComponent body, JComponent footer, JButton defaultButton) {
        JPanel root = new JPanel(new BorderLayout(0, Theme.SP_L));
        root.setBackground(Theme.CARD_BG);
        root.setBorder(BorderFactory.createEmptyBorder(Theme.SP_XL, Theme.SP_XL, Theme.SP_XL, Theme.SP_XL));
        JLabel heading = new JLabel(title);
        heading.setFont(Theme.FONT_CARD_TITLE);
        heading.setForeground(Theme.TEXT);
        root.add(heading, BorderLayout.NORTH);
        root.add(body, BorderLayout.CENTER);
        if (footer != null) root.add(footer, BorderLayout.SOUTH);
        dialog.setContentPane(root);
        if (defaultButton != null) dialog.getRootPane().setDefaultButton(defaultButton);
        dialog.getRootPane().registerKeyboardAction(e -> dialog.dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        dialog.pack();
        dialog.setLocationRelativeTo(dialog.getOwner());
        dialog.setVisible(true);
    }
}
