package adapters.in;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/** Botón con estilo del tema: primario (azul marino), secundario (borde) o pequeño de fila. */
class AppButton extends JButton {
    enum Style { PRIMARY, SECONDARY, SMALL }

    private final Style style;
    private boolean hover;

    AppButton(String text, Style style) {
        super(text);
        this.style = style;
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setFont(style == Style.SMALL ? Theme.FONT_SMALL_BOLD : Theme.FONT_BODY_BOLD);
        int h = style == Style.SMALL ? Theme.SMALL_BUTTON_HEIGHT : Theme.CONTROL_HEIGHT;
        int padX = style == Style.SMALL ? Theme.SP_M : Theme.SP_L + 2;
        setBorder(BorderFactory.createEmptyBorder(0, padX, 0, padX));
        FontMetrics fm = getFontMetrics(getFont());
        setPreferredSize(new Dimension(fm.stringWidth(text) + 2 * padX + 2, h));
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
            @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
        });
    }

    @Override
    public void setText(String text) {
        super.setText(text);
        if (style != null) {
            FontMetrics fm = getFontMetrics(getFont());
            Insets in = getInsets();
            setPreferredSize(new Dimension(fm.stringWidth(text) + in.left + in.right + 2, getPreferredSize().height));
        }
    }

    @Override
    public Dimension getMaximumSize() { return getPreferredSize(); }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int arc = style == Style.SMALL ? Theme.RADIUS_SMALL : Theme.RADIUS_SMALL + 2;
        RoundRectangle2D shape = new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 1, getHeight() - 1, arc, arc);
        Color fg;
        if (style == Style.PRIMARY) {
            g2.setColor(!isEnabled() ? Theme.DISABLED_BG : hover ? Theme.PRIMARY_HOVER : Theme.PRIMARY);
            g2.fill(shape);
            fg = isEnabled() ? Theme.PRIMARY_TEXT : Theme.DISABLED_TEXT;
        } else {
            g2.setColor(hover && isEnabled() ? Theme.TABLE_HEADER_BG : Theme.CARD_BG);
            g2.fill(shape);
            g2.setColor(isEnabled() ? Theme.BORDER.darker() : Theme.BORDER);
            g2.draw(shape);
            fg = isEnabled() ? Theme.TEXT : Theme.DISABLED_TEXT;
        }
        if (isFocusOwner()) {
            g2.setColor(Theme.FOCUS);
            g2.setStroke(new BasicStroke(2f));
            g2.draw(new RoundRectangle2D.Double(2, 2, getWidth() - 4, getHeight() - 4, arc - 2, arc - 2));
        }
        g2.setFont(getFont());
        g2.setColor(fg);
        FontMetrics fm = g2.getFontMetrics();
        int tx = (getWidth() - fm.stringWidth(getText())) / 2;
        int ty = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(getText(), tx, ty);
        g2.dispose();
    }
}
