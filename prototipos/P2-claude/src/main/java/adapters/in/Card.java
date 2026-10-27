package adapters.in;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/** Tarjeta blanca de bordes redondeados con sombra suave. */
class Card extends JPanel {
    Card(LayoutManager layout) {
        super(layout);
        setOpaque(false);
        int pad = Theme.SHADOW_SIZE + Theme.SP_L;
        setBorder(BorderFactory.createEmptyBorder(pad, pad, pad + 2, pad));
    }

    Card() { this(new BorderLayout()); }

    /** Tarjeta sin relleno interno (la tabla ocupa todo el ancho). */
    Card withPadding(int padding) {
        int pad = Theme.SHADOW_SIZE + padding;
        setBorder(BorderFactory.createEmptyBorder(pad, pad, pad + 2, pad));
        return this;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int s = Theme.SHADOW_SIZE;
        int w = getWidth() - 2 * s;
        int h = getHeight() - 2 * s;
        for (int i = s; i > 0; i--) {
            int alpha = 2 + (s - i);
            g2.setColor(new Color(Theme.SHADOW.getRed(), Theme.SHADOW.getGreen(), Theme.SHADOW.getBlue(), alpha));
            g2.fill(new RoundRectangle2D.Double(s - i, s - i + 2, w + 2 * i, h + 2 * i,
                    Theme.RADIUS + i, Theme.RADIUS + i));
        }
        g2.setColor(Theme.CARD_BG);
        g2.fill(new RoundRectangle2D.Double(s, s, w, h, Theme.RADIUS, Theme.RADIUS));
        g2.setColor(Theme.BORDER);
        g2.draw(new RoundRectangle2D.Double(s + 0.5, s + 0.5, w - 1, h - 1, Theme.RADIUS, Theme.RADIUS));
        g2.dispose();
        super.paintComponent(g);
    }
}
