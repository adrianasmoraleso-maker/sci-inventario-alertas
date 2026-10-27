package adapters.in;

import domain.Product;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/** Base de las pantallas: fondo claro, relleno estándar y {@link #refresh()} para recargar datos. */
abstract class ScreenPanel extends JPanel {
    protected final AppShell shell;

    ScreenPanel(AppShell shell, String name) {
        super(new BorderLayout(0, Theme.SP_M));
        this.shell = shell;
        setName(name);
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(0, Theme.SP_L - Theme.SHADOW_SIZE + 4, Theme.SP_XL - Theme.SHADOW_SIZE,
                Theme.SP_L - Theme.SHADOW_SIZE + 4));
    }

    /** Vuelve a leer los datos y actualiza tablas e indicadores. */
    abstract void refresh();

    /** Fila de herramientas: componentes a la izquierda y a la derecha, centrados verticalmente. */
    static JPanel toolbar(JComponent[] left, JComponent... right) {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.add(row(left), BorderLayout.WEST);
        bar.add(row(right), BorderLayout.EAST);
        bar.setBorder(BorderFactory.createEmptyBorder(0, 0, Theme.SP_M, 0));
        return bar;
    }

    static JPanel row(JComponent... items) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        for (int i = 0; i < items.length; i++) {
            items[i].setAlignmentY(Component.CENTER_ALIGNMENT);
            p.add(items[i]);
            if (i < items.length - 1) p.add(Box.createHorizontalStrut(Theme.SP_S + 2));
        }
        return p;
    }

    static JLabel toolbarLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_BODY);
        l.setForeground(Theme.TEXT_MUTED);
        return l;
    }

    static JLabel cardTitle(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_CARD_TITLE);
        l.setForeground(Theme.TEXT);
        return l;
    }

    /** Recarga un selector de productos conservando la selección actual (por código). */
    static void fillProducts(StyledCombo<ProductItem> combo, List<Product> products, ProductItem first) {
        String previous = selectedCode(combo);
        combo.removeAllItems();
        if (first != null) combo.addItem(first);
        for (Product p : products) combo.addItem(ProductItem.of(p));
        int index = combo.getItemCount() > 0 ? 0 : -1;
        for (int i = 0; i < combo.getItemCount(); i++) {
            String code = combo.getItemAt(i).code();
            if (previous != null && previous.equals(code)) index = i;
        }
        if (index >= 0) combo.setSelectedIndex(index);
        combo.fitWidth(200, 420);
    }

    static String selectedCode(StyledCombo<ProductItem> combo) {
        ProductItem item = (ProductItem) combo.getSelectedItem();
        return item == null ? null : item.code();
    }

    static Product find(List<Product> products, String code) {
        if (code == null) return null;
        for (Product p : products) if (p.getCode().equals(code)) return p;
        return null;
    }
}
