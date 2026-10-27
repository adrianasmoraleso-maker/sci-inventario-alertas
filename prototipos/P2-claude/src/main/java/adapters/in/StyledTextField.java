package adapters.in;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/** Campo de texto redondeado con texto de ayuda y estado de error. */
class StyledTextField extends JTextField {
    private String placeholder = "";
    private boolean invalid;

    StyledTextField(int columns) {
        super(columns);
        setOpaque(false);
        setFont(Theme.FONT_BODY);
        setForeground(Theme.TEXT);
        setCaretColor(Theme.TEXT);
        setBorder(BorderFactory.createEmptyBorder(0, Theme.SP_M, 0, Theme.SP_M));
        setPreferredSize(new Dimension(getPreferredSize().width, Theme.CONTROL_HEIGHT));
        setMinimumSize(new Dimension(60, Theme.CONTROL_HEIGHT));
    }

    void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        repaint();
    }

    void setInvalid(boolean invalid) {
        this.invalid = invalid;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int arc = Theme.RADIUS_SMALL + 2;
        RoundRectangle2D shape = new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 1, getHeight() - 1, arc, arc);
        g2.setColor(isEnabled() ? Theme.CARD_BG : Theme.TABLE_HEADER_BG);
        g2.fill(shape);
        g2.dispose();
        super.paintComponent(g);
        if (getText().isEmpty() && !placeholder.isEmpty()) {
            Graphics2D p = (Graphics2D) g.create();
            p.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            p.setColor(Theme.TEXT_MUTED);
            p.setFont(getFont());
            FontMetrics fm = p.getFontMetrics();
            p.drawString(placeholder, getInsets().left, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            p.dispose();
        }
    }

    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int arc = Theme.RADIUS_SMALL + 2;
        boolean focus = isFocusOwner();
        g2.setColor(invalid ? Theme.ERROR : focus ? Theme.FOCUS : Theme.BORDER.darker());
        g2.setStroke(new BasicStroke(focus || invalid ? 1.6f : 1f));
        g2.draw(new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 1, getHeight() - 1, arc, arc));
        g2.dispose();
    }
}
