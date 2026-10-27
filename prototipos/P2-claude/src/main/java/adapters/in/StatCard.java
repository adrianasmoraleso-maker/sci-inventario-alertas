package adapters.in;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/** Tarjeta de indicador: ícono, título y cifra. */
class StatCard extends Card {
    private final JLabel value = new JLabel("0");

    StatCard(String name, Icons icon, Theme.Tone tone, String title) {
        super(new BorderLayout(Theme.SP_L, 0));
        setName(name);
        JComponent iconBox = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(tone.bg);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), Theme.RADIUS, Theme.RADIUS));
                icon.paint(g2, (getWidth() - 26) / 2, (getHeight() - 26) / 2, 26, tone.fg);
                g2.dispose();
            }
        };
        iconBox.setPreferredSize(new Dimension(52, 52));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(Theme.FONT_BODY);
        titleLabel.setForeground(Theme.TEXT_MUTED);
        value.setName(name + "-value");
        value.setFont(Theme.FONT_FIGURE);
        value.setForeground(Theme.TEXT);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);
        value.setAlignmentX(LEFT_ALIGNMENT);
        text.add(titleLabel);
        text.add(value);

        JPanel iconWrap = new JPanel(new GridBagLayout());
        iconWrap.setOpaque(false);
        iconWrap.add(iconBox);
        add(iconWrap, BorderLayout.WEST);
        add(text, BorderLayout.CENTER);
    }

    void setValue(String text) { value.setText(text); }

    String getValue() { return value.getText(); }
}
