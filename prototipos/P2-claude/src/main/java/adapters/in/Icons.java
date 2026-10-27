package adapters.in;

import java.awt.*;
import java.awt.geom.*;

/** Íconos vectoriales dibujados con Java2D (sin imágenes externas). */
public enum Icons {
    HOME, BOX, LAYERS, ARROW_OUT, BARS, ALERT, TREND_DOWN, CLOCK, CHECK, BLOCK, TARGET;

    /** Dibuja el ícono dentro de un cuadrado de lado {@code size} con esquina (x, y). */
    public void paint(Graphics2D g, int x, int y, int size, Color color) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.translate(x, y);
        double s = size / 24.0;
        g2.scale(s, s);
        g2.setColor(color);
        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        switch (this) {
            case HOME -> {
                Path2D p = new Path2D.Double();
                p.moveTo(3, 11); p.lineTo(12, 3); p.lineTo(21, 11);
                g2.draw(p);
                Path2D b = new Path2D.Double();
                b.moveTo(5.5, 9.5); b.lineTo(5.5, 20); b.lineTo(18.5, 20); b.lineTo(18.5, 9.5);
                g2.draw(b);
                g2.draw(new Line2D.Double(10, 20, 10, 14));
                g2.draw(new Line2D.Double(14, 20, 14, 14));
                g2.draw(new Line2D.Double(10, 14, 14, 14));
            }
            case BOX -> {
                g2.draw(new RoundRectangle2D.Double(3, 4, 18, 5, 2, 2));
                Path2D b = new Path2D.Double();
                b.moveTo(4.5, 9); b.lineTo(4.5, 19); b.lineTo(19.5, 19); b.lineTo(19.5, 9);
                g2.draw(b);
                g2.draw(new Line2D.Double(10, 13, 14, 13));
            }
            case LAYERS -> {
                Path2D top = new Path2D.Double();
                top.moveTo(12, 3.5); top.lineTo(21, 8); top.lineTo(12, 12.5); top.lineTo(3, 8); top.closePath();
                g2.draw(top);
                Path2D mid = new Path2D.Double();
                mid.moveTo(3, 12); mid.lineTo(12, 16.5); mid.lineTo(21, 12);
                g2.draw(mid);
                Path2D low = new Path2D.Double();
                low.moveTo(3, 16); low.lineTo(12, 20.5); low.lineTo(21, 16);
                g2.draw(low);
            }
            case ARROW_OUT -> {
                g2.draw(new Line2D.Double(12, 15, 12, 4));
                Path2D head = new Path2D.Double();
                head.moveTo(7.5, 8.5); head.lineTo(12, 4); head.lineTo(16.5, 8.5);
                g2.draw(head);
                Path2D tray = new Path2D.Double();
                tray.moveTo(4, 14); tray.lineTo(4, 20); tray.lineTo(20, 20); tray.lineTo(20, 14);
                g2.draw(tray);
            }
            case BARS -> {
                g2.draw(new Line2D.Double(5, 20, 5, 12));
                g2.draw(new Line2D.Double(12, 20, 12, 5));
                g2.draw(new Line2D.Double(19, 20, 19, 9));
            }
            case ALERT -> {
                Path2D t = new Path2D.Double();
                t.moveTo(12, 3.5); t.lineTo(21.5, 20); t.lineTo(2.5, 20); t.closePath();
                g2.draw(t);
                g2.draw(new Line2D.Double(12, 9.5, 12, 14));
                g2.fill(new Ellipse2D.Double(11, 16, 2, 2));
            }
            case TREND_DOWN -> {
                Path2D line = new Path2D.Double();
                line.moveTo(3, 7); line.lineTo(9.5, 13.5); line.lineTo(13.5, 9.5); line.lineTo(21, 17);
                g2.draw(line);
                Path2D head = new Path2D.Double();
                head.moveTo(15, 17.5); head.lineTo(21, 17.5); head.lineTo(21, 11.5);
                g2.draw(head);
            }
            case CLOCK -> {
                g2.draw(new Ellipse2D.Double(3, 3, 18, 18));
                Path2D hands = new Path2D.Double();
                hands.moveTo(12, 7); hands.lineTo(12, 12); hands.lineTo(15.5, 14);
                g2.draw(hands);
            }
            case CHECK -> {
                g2.draw(new Ellipse2D.Double(3, 3, 18, 18));
                Path2D tick = new Path2D.Double();
                tick.moveTo(8, 12.5); tick.lineTo(11, 15.5); tick.lineTo(16.5, 9);
                g2.draw(tick);
            }
            case BLOCK -> {
                g2.draw(new Ellipse2D.Double(3, 3, 18, 18));
                g2.draw(new Line2D.Double(6, 6, 18, 18));
            }
            case TARGET -> {
                g2.draw(new Ellipse2D.Double(3, 3, 18, 18));
                g2.draw(new Ellipse2D.Double(7.5, 7.5, 9, 9));
                g2.fill(new Ellipse2D.Double(10.5, 10.5, 3, 3));
            }
        }
        g2.dispose();
    }
}
