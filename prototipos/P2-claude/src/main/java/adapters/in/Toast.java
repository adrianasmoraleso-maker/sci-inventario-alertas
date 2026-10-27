package adapters.in;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/** Notificación breve que aparece abajo a la derecha tras una operación exitosa (RF-UI12). */
final class Toast {
    private Toast() { }

    static void show(JRootPane root, String message) {
        JLayeredPane layer = root.getLayeredPane();
        JLabel label = new JLabel(message) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.PRIMARY);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), Theme.RADIUS, Theme.RADIUS));
                g2.setColor(Theme.Tone.GREEN.fg.brighter());
                g2.fillOval(Theme.SP_L, getHeight() / 2 - 4, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        label.setName("toast");
        label.setOpaque(false);
        label.setFont(Theme.FONT_BODY_BOLD);
        label.setForeground(Color.WHITE);
        label.setBorder(BorderFactory.createEmptyBorder(Theme.SP_M + 2, Theme.SP_L + 20, Theme.SP_M + 2, Theme.SP_XL));
        Dimension d = label.getPreferredSize();
        label.setSize(d);
        label.setLocation(layer.getWidth() - d.width - Theme.SP_XL, layer.getHeight() - d.height - Theme.SP_XL);
        layer.add(label, JLayeredPane.POPUP_LAYER);
        layer.repaint();

        Timer timer = new Timer(2800, e -> {
            layer.remove(label);
            layer.repaint();
        });
        timer.setRepeats(false);
        timer.start();
    }
}
