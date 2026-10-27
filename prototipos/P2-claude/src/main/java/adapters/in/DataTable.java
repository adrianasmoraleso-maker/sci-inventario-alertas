package adapters.in;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

/**
 * Tabla de solo lectura con el estilo del tema: encabezado diferenciado, filas con separadores finos,
 * etiquetas tipo píldora y botones pequeños con borde por fila.
 *
 * Los botones de fila se dibujan y se detectan con el ratón sin convertir las celdas en editables:
 * ninguna celda se puede editar (RF-UI16).
 */
class DataTable extends JPanel {

    enum Kind { TEXT, PILL, ACTIONS }

    /** Valor de una celda tipo píldora. */
    record Badge(String text, Theme.Tone tone) { }

    record Column(String title, Kind kind, int width) { }

    private static final int ACTION_PAD = 8;
    private static final int ACTION_GAP = 4;

    private final JTable table;
    private final DefaultTableModel model;
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final JLabel emptyTitle = new JLabel("", SwingConstants.CENTER);
    private final JLabel emptyHint = new JLabel("", SwingConstants.CENTER);
    private final String[] actionLabels;
    private final int actionsColumn;

    private BiPredicate<Integer, Integer> actionEnabled = (row, action) -> true;
    private BiConsumer<Integer, Integer> actionHandler = (row, action) -> { };
    private int hoverRow = -1;
    private int hoverAction = -1;

    DataTable(String name, Column[] columns, String... actionLabels) {
        super(new BorderLayout());
        setOpaque(false);
        this.actionLabels = actionLabels;

        String[] titles = new String[columns.length];
        int actionsCol = -1;
        for (int i = 0; i < columns.length; i++) {
            titles[i] = columns[i].title();
            if (columns[i].kind() == Kind.ACTIONS) actionsCol = i;
        }
        this.actionsColumn = actionsCol;

        model = new DefaultTableModel(titles, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model) {
            @Override public boolean editCellAt(int row, int column, EventObject e) { return false; }
            @Override public boolean isCellEditable(int row, int column) { return false; }
            @Override public boolean getScrollableTracksViewportWidth() {
                // ocupa todo el ancho disponible; solo si no caben las columnas mínimas aparece scroll horizontal
                return !(getParent() instanceof JViewport vp) || vp.getWidth() >= minimumTotalWidth();
            }
        };
        table.setName(name);
        table.setRowHeight(Theme.ROW_HEIGHT);
        table.setShowGrid(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(Theme.ROW_SEPARATOR);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setRowSelectionAllowed(false);
        table.setColumnSelectionAllowed(false);
        table.setFocusable(false);
        table.setFillsViewportHeight(true);
        table.setBackground(Theme.CARD_BG);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setResizingAllowed(false);
        table.getTableHeader().setDefaultRenderer(new HeaderRenderer());
        table.getTableHeader().setPreferredSize(new Dimension(10, 42));

        // la columna más ancha (p. ej. Nombre o Mensaje) absorbe el espacio sobrante
        int flex = 0;
        for (int i = 1; i < columns.length; i++) if (columns[i].width() > columns[flex].width()) flex = i;
        FontMetrics fm = table.getFontMetrics(Theme.FONT_SMALL_BOLD);
        for (int i = 0; i < columns.length; i++) {
            TableColumn tc = table.getColumnModel().getColumn(i);
            switch (columns[i].kind()) {
                case TEXT -> tc.setCellRenderer(new TextRenderer());
                case PILL -> tc.setCellRenderer(new PillRenderer());
                case ACTIONS -> {
                    tc.setCellRenderer(new ActionsRenderer());
                    int w = actionsWidth(fm) + Theme.SP_S;
                    tc.setMinWidth(w);
                    tc.setMaxWidth(w);
                    tc.setPreferredWidth(w);
                }
            }
            if (columns[i].kind() != Kind.ACTIONS) {
                int min = fm.stringWidth(columns[i].title()) + Theme.SP_M + Theme.SP_S + 2;
                if (columns[i].kind() == Kind.PILL) min = Math.max(min, 106);
                tc.setMinWidth(min);
                tc.setPreferredWidth(i == flex ? Math.max(min, columns[i].width()) : min + 10);
            }
        }

        MouseAdapter mouse = new MouseAdapter() {
            @Override public void mouseMoved(MouseEvent e) { updateHover(e.getPoint()); }
            @Override public void mouseExited(MouseEvent e) { setHover(-1, -1); }
            @Override public void mouseClicked(MouseEvent e) {
                int[] hit = hitTest(e.getPoint());
                if (hit[0] >= 0 && hit[1] >= 0 && actionEnabled.test(hit[0], hit[1])) {
                    actionHandler.accept(hit[0], hit[1]);
                }
            }
        };
        table.addMouseListener(mouse);
        table.addMouseMotionListener(mouse);

        JScrollPane scroll = new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Theme.CARD_BG);
        scroll.getVerticalScrollBar().setUI(new SlimScrollBarUI());
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(10, 10));
        scroll.getVerticalScrollBar().setUnitIncrement(Theme.ROW_HEIGHT / 2);
        JPanel corner = new JPanel();
        corner.setBackground(Theme.TABLE_HEADER_BG);
        scroll.setCorner(ScrollPaneConstants.UPPER_RIGHT_CORNER, corner);

        JPanel empty = new JPanel(new GridBagLayout());
        empty.setBackground(Theme.CARD_BG);
        emptyTitle.setFont(Theme.FONT_CARD_TITLE);
        emptyTitle.setForeground(Theme.TEXT);
        emptyTitle.setName(name + "-empty");
        emptyHint.setFont(Theme.FONT_BODY);
        emptyHint.setForeground(Theme.TEXT_MUTED);
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        gc.insets = new Insets(0, 0, Theme.SP_XS, 0);
        empty.add(emptyTitle, gc);
        gc.gridy = 1;
        empty.add(emptyHint, gc);

        content.setOpaque(false);
        content.add(scroll, "table");
        content.add(empty, "empty");
        add(content, BorderLayout.CENTER);
        cards.show(content, "empty");
    }

    private int minimumTotalWidth() {
        int sum = 0;
        TableColumnModel cm = table.getColumnModel();
        for (int i = 0; i < cm.getColumnCount(); i++) sum += cm.getColumn(i).getMinWidth();
        return sum;
    }

    // ------------------------------------------------------------- API pública

    JTable table() { return table; }

    int rowCount() { return model.getRowCount(); }

    Object valueAt(int row, int column) { return model.getValueAt(row, column); }

    void setEmptyMessage(String title, String hint) {
        emptyTitle.setText(title);
        emptyHint.setText(hint);
    }

    void setRows(List<Object[]> rows) {
        model.setRowCount(0);
        for (Object[] r : rows) model.addRow(r);
        hoverRow = -1;
        hoverAction = -1;
        cards.show(content, rows.isEmpty() ? "empty" : "table");
    }

    void setActionEnabled(BiPredicate<Integer, Integer> predicate) { this.actionEnabled = predicate; }

    void setActionHandler(BiConsumer<Integer, Integer> handler) { this.actionHandler = handler; }

    // -------------------------------------------------------- botones de fila

    private int actionsWidth(FontMetrics fm) {
        int w = Theme.SP_XS;
        for (String label : actionLabels) w += fm.stringWidth(label) + 2 * ACTION_PAD + ACTION_GAP;
        return w;
    }

    /** Rectángulos de los botones relativos a la esquina de la celda. */
    private List<Rectangle> actionRects(int cellHeight) {
        FontMetrics fm = table.getFontMetrics(Theme.FONT_SMALL_BOLD);
        List<Rectangle> rects = new ArrayList<>();
        int x = Theme.SP_XS;
        int h = Theme.SMALL_BUTTON_HEIGHT;
        int y = (cellHeight - h) / 2;
        for (String label : actionLabels) {
            int w = fm.stringWidth(label) + 2 * ACTION_PAD;
            rects.add(new Rectangle(x, y, w, h));
            x += w + ACTION_GAP;
        }
        return rects;
    }

    /** Devuelve {fila, índice de acción} bajo el punto, o {-1, -1}. */
    private int[] hitTest(Point p) {
        int row = table.rowAtPoint(p);
        int col = table.columnAtPoint(p);
        if (row < 0 || col < 0 || col != actionsColumn) return new int[]{-1, -1};
        Rectangle cell = table.getCellRect(row, col, false);
        List<Rectangle> rects = actionRects(cell.height);
        for (int i = 0; i < rects.size(); i++) {
            Rectangle r = rects.get(i);
            if (r.contains(p.x - cell.x, p.y - cell.y)) return new int[]{row, i};
        }
        return new int[]{row, -1};
    }

    private void updateHover(Point p) {
        int[] hit = hitTest(p);
        boolean overEnabled = hit[0] >= 0 && hit[1] >= 0 && actionEnabled.test(hit[0], hit[1]);
        table.setCursor(overEnabled ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
        setHover(hit[0], hit[1]);
    }

    private void setHover(int row, int action) {
        if (row != hoverRow || action != hoverAction) {
            hoverRow = row;
            hoverAction = action;
            table.repaint();
        }
    }

    // ------------------------------------------------------------ renderizadores

    private static class HeaderRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean sel, boolean focus,
                                                       int row, int column) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, value, false, false, row, column);
            l.setFont(Theme.FONT_SMALL_BOLD);
            l.setForeground(Theme.TEXT_MUTED);
            l.setBackground(Theme.TABLE_HEADER_BG);
            l.setOpaque(true);
            l.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(1, 0, 1, 0, Theme.BORDER),
                    BorderFactory.createEmptyBorder(0, Theme.SP_M, 0, Theme.SP_S)));
            return l;
        }
    }

    private static class TextRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean sel, boolean focus,
                                                       int row, int column) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, value, false, false, row, column);
            l.setFont(Theme.FONT_BODY);
            l.setForeground(Theme.TEXT);
            l.setBackground(Theme.CARD_BG);
            l.setBorder(BorderFactory.createEmptyBorder(0, Theme.SP_M, 0, Theme.SP_S));
            return l;
        }
    }

    private static class PillRenderer extends JComponent implements TableCellRenderer {
        private Badge badge;

        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean sel, boolean focus,
                                                       int row, int column) {
            badge = value instanceof Badge b ? b : null;
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(Theme.CARD_BG);
            g2.fillRect(0, 0, getWidth(), getHeight());
            if (badge != null) {
                g2.setFont(Theme.FONT_SMALL_BOLD);
                FontMetrics fm = g2.getFontMetrics();
                int w = fm.stringWidth(badge.text()) + 2 * Theme.SP_M;
                int h = 26;
                int x = Theme.SP_M;
                int y = (getHeight() - h) / 2;
                g2.setColor(badge.tone().bg);
                g2.fill(new RoundRectangle2D.Double(x, y, w, h, h, h));
                g2.setColor(badge.tone().fg);
                g2.drawString(badge.text(), x + Theme.SP_M, y + (h - fm.getHeight()) / 2 + fm.getAscent());
            }
            g2.dispose();
        }
    }

    private class ActionsRenderer extends JComponent implements TableCellRenderer {
        private int row;

        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean sel, boolean focus,
                                                       int row, int column) {
            this.row = row;
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(Theme.CARD_BG);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setFont(Theme.FONT_SMALL_BOLD);
            FontMetrics fm = g2.getFontMetrics();
            List<Rectangle> rects = actionRects(getHeight());
            for (int i = 0; i < rects.size(); i++) {
                Rectangle r = rects.get(i);
                boolean enabled = actionEnabled.test(row, i);
                boolean hover = enabled && row == hoverRow && i == hoverAction;
                RoundRectangle2D shape = new RoundRectangle2D.Double(r.x + 0.5, r.y + 0.5, r.width - 1, r.height - 1,
                        Theme.RADIUS_SMALL, Theme.RADIUS_SMALL);
                g2.setColor(hover ? Theme.TABLE_HEADER_BG : Theme.CARD_BG);
                g2.fill(shape);
                g2.setColor(enabled ? Theme.BORDER.darker() : Theme.BORDER);
                g2.draw(shape);
                g2.setColor(enabled ? Theme.TEXT : Theme.DISABLED_TEXT);
                g2.drawString(actionLabels[i], r.x + ACTION_PAD,
                        r.y + (r.height - fm.getHeight()) / 2 + fm.getAscent());
            }
            g2.dispose();
        }
    }

    /** Barra de desplazamiento delgada y redondeada. */
    static class SlimScrollBarUI extends BasicScrollBarUI {
        @Override protected void configureScrollBarColors() {
            thumbColor = Theme.BORDER.darker();
            trackColor = Theme.CARD_BG;
        }
        @Override protected JButton createDecreaseButton(int orientation) { return zeroButton(); }
        @Override protected JButton createIncreaseButton(int orientation) { return zeroButton(); }

        private JButton zeroButton() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            b.setMinimumSize(new Dimension(0, 0));
            b.setMaximumSize(new Dimension(0, 0));
            return b;
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
            g.setColor(trackColor);
            g.fillRect(r.x, r.y, r.width, r.height);
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            if (r.isEmpty() || !scrollbar.isEnabled()) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(thumbColor);
            g2.fill(new RoundRectangle2D.Double(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 6, 6));
            g2.dispose();
        }
    }
}
