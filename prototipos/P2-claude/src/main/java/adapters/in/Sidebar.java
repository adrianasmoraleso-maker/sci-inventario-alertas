package adapters.in;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

/** Barra lateral oscura con el nombre de la aplicación y los seis ítems del menú. */
class Sidebar extends JPanel {
    private final Map<Section, NavButton> buttons = new EnumMap<>(Section.class);

    Sidebar(Consumer<Section> onSelect) {
        super();
        setName("sidebar");
        setBackground(Theme.SIDEBAR_BG);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(Theme.SIDEBAR_WIDTH, 10));
        setBorder(BorderFactory.createEmptyBorder(Theme.SP_XL, Theme.SP_M, Theme.SP_XL, Theme.SP_M));

        add(new Brand());
        add(Box.createVerticalStrut(Theme.SP_XL + Theme.SP_S));
        for (Section s : Section.values()) {
            NavButton b = new NavButton(s);
            b.addActionListener(e -> onSelect.accept(s));
            b.setAlignmentX(LEFT_ALIGNMENT);
            buttons.put(s, b);
            add(b);
            add(Box.createVerticalStrut(Theme.SP_XS));
        }
        add(Box.createVerticalGlue());
    }

    void select(Section active) {
        buttons.forEach((s, b) -> b.setActive(s == active));
    }

    /** Marca y nombre de la aplicación. */
    private static final class Brand extends JComponent {
        Brand() {
            setPreferredSize(new Dimension(10, 48));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
            setAlignmentX(LEFT_ALIGNMENT);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int x = Theme.SP_S;
            int box = 38;
            int y = (getHeight() - box) / 2;
            g2.setColor(Theme.Tone.BLUE.fg);
            g2.fill(new RoundRectangle2D.Double(x, y, box, box, 12, 12));
            Icons.BOX.paint(g2, x + 7, y + 7, 24, Color.WHITE);
            g2.setColor(Color.WHITE);
            g2.setFont(Theme.FONT_BRAND);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString("SCI", x + box + Theme.SP_M, y + fm.getAscent() - 1);
            g2.setColor(Theme.SIDEBAR_TEXT);
            g2.setFont(Theme.FONT_SMALL);
            g2.drawString("Control de Inventario", x + box + Theme.SP_M, y + box - 4);
            g2.dispose();
        }
    }

    /** Ítem del menú con ícono; el activo se resalta. */
    static final class NavButton extends JButton {
        private final Section section;
        private boolean active;
        private boolean hover;

        NavButton(Section section) {
            super(section.getLabel());
            this.section = section;
            setName("nav-" + section.getLabel());
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setHorizontalAlignment(SwingConstants.LEFT);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFont(Theme.FONT_NAV);
            setPreferredSize(new Dimension(10, 44));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            });
        }

        void setActive(boolean active) {
            this.active = active;
            repaint();
        }

        boolean isActive() { return active; }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            if (active || hover) {
                g2.setColor(active ? Theme.SIDEBAR_ITEM_ACTIVE : Theme.SIDEBAR_ITEM_HOVER);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), Theme.RADIUS_SMALL + 2,
                        Theme.RADIUS_SMALL + 2));
            }
            Color fg = active ? Theme.SIDEBAR_TEXT_ACTIVE : Theme.SIDEBAR_TEXT;
            section.icon().paint(g2, Theme.SP_L, (getHeight() - 22) / 2, 22, fg);
            g2.setColor(fg);
            g2.setFont(active ? Theme.font(Font.BOLD, 14f) : Theme.FONT_NAV);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(getText(), Theme.SP_L + 22 + Theme.SP_M,
                    (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            if (isFocusOwner()) {
                g2.setColor(Theme.FOCUS);
                g2.setStroke(new BasicStroke(2f));
                g2.draw(new RoundRectangle2D.Double(1, 1, getWidth() - 2, getHeight() - 2, Theme.RADIUS_SMALL + 2,
                        Theme.RADIUS_SMALL + 2));
            }
            g2.dispose();
        }
    }
}
