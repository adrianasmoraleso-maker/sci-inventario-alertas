package adapters.in;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

/** Lista desplegable con el estilo del tema (para selectores y filtros). */
class StyledCombo<T> extends JComboBox<T> {

    StyledCombo(T[] items) {
        super(items);
        init();
    }

    StyledCombo() {
        super();
        init();
    }

    private void init() {
        setOpaque(false);
        setFont(Theme.FONT_BODY);
        setForeground(Theme.TEXT);
        setBorder(BorderFactory.createEmptyBorder(0, Theme.SP_XS, 0, 0));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(Math.max(getPreferredSize().width + 8, 130), Theme.CONTROL_HEIGHT));
        setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean selected, boolean focus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, selected, focus);
                l.setFont(Theme.FONT_BODY);
                l.setBorder(BorderFactory.createEmptyBorder(Theme.SP_S, Theme.SP_M, Theme.SP_S, Theme.SP_M));
                if (index == -1) {
                    l.setOpaque(false);
                    l.setForeground(Theme.TEXT);
                } else {
                    l.setOpaque(true);
                    l.setBackground(selected ? Theme.TABLE_HEADER_BG : Theme.CARD_BG);
                    l.setForeground(Theme.TEXT);
                }
                return l;
            }
        });
    }

    @Override
    public void updateUI() {
        setUI(new FlatComboUI());
    }

    /** Ajusta el ancho del selector al contenido (útil con nombres largos). */
    void fitWidth(int min, int max) {
        int w = min;
        FontMetrics fm = getFontMetrics(getFont());
        for (int i = 0; i < getItemCount(); i++) {
            w = Math.max(w, fm.stringWidth(String.valueOf(getItemAt(i))) + 90);
        }
        setPreferredSize(new Dimension(Math.min(w, max), Theme.CONTROL_HEIGHT));
    }

    @Override
    public Dimension getMaximumSize() { return getPreferredSize(); }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int arc = Theme.RADIUS_SMALL + 2;
        RoundRectangle2D shape = new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 1, getHeight() - 1, arc, arc);
        g2.setColor(isEnabled() ? Theme.CARD_BG : Theme.TABLE_HEADER_BG);
        g2.fill(shape);
        g2.setColor(hasFocus() ? Theme.FOCUS : Theme.BORDER.darker());
        g2.draw(shape);
        g2.dispose();
        super.paintComponent(g);
    }

    private static final class FlatComboUI extends BasicComboBoxUI {
        @Override
        protected JButton createArrowButton() {
            JButton b = new JButton() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(Theme.TEXT_MUTED);
                    g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    int cx = getWidth() / 2, cy = getHeight() / 2;
                    Path2D p = new Path2D.Double();
                    p.moveTo(cx - 4, cy - 2); p.lineTo(cx, cy + 2); p.lineTo(cx + 4, cy - 2);
                    g2.draw(p);
                    g2.dispose();
                }
            };
            b.setContentAreaFilled(false);
            b.setBorderPainted(false);
            b.setFocusPainted(false);
            b.setOpaque(false);
            b.setPreferredSize(new Dimension(28, Theme.CONTROL_HEIGHT));
            return b;
        }

        @Override
        public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
            // el fondo lo pinta el propio componente
        }
    }
}
