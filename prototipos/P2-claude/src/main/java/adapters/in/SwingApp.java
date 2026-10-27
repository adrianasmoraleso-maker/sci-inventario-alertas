package adapters.in;

import application.InventoryUseCase;
import domain.ClockPort;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Adaptador de entrada: ventana de escritorio con barra lateral y seis pantallas.
 * Solo recoge datos, llama a los casos de uso y muestra resultados; las reglas viven en el dominio.
 */
public class SwingApp extends JFrame implements AppShell {
    private final InventoryUseCase useCase;
    private final ClockPort clock;
    private final Sidebar sidebar;
    private final JLabel title = new JLabel();
    private final CardLayout cards = new CardLayout();
    private final JPanel screenHost = new JPanel(cards);
    private final Map<Section, ScreenPanel> screens = new EnumMap<>(Section.class);
    private Section current;

    public SwingApp(InventoryUseCase useCase, ClockPort clock) {
        Theme.install();
        this.useCase = useCase;
        this.clock = clock;
        setTitle("SCI — Sistema de Control de Inventario y Alertas");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(Theme.MIN_WINDOW);
        setSize(Theme.DEFAULT_WINDOW);
        setLocationRelativeTo(null);

        sidebar = new Sidebar(this::navigate);

        screens.put(Section.INICIO, new InicioScreen(this));
        screens.put(Section.PRODUCTOS, new ProductosScreen(this));
        screens.put(Section.LOTES, new LotesScreen(this));
        screens.put(Section.SALIDAS, new SalidasScreen(this));
        screens.put(Section.EXISTENCIAS, new ExistenciasScreen(this));
        screens.put(Section.ALERTAS, new AlertasScreen(this));
        screens.forEach((section, screen) -> screenHost.add(screen, section.name()));
        screenHost.setOpaque(false);

        title.setName("section-title");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT);
        title.setBorder(BorderFactory.createEmptyBorder(Theme.SP_XL, Theme.SP_XL, Theme.SP_M, Theme.SP_XL));

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(Theme.BACKGROUND);
        main.add(title, BorderLayout.NORTH);
        main.add(screenHost, BorderLayout.CENTER);

        JPanel root = new JPanel(new BorderLayout());
        root.add(sidebar, BorderLayout.WEST);
        root.add(main, BorderLayout.CENTER);
        setContentPane(root);

        navigate(Section.INICIO);
    }

    // ------------------------------------------------------------ AppShell

    @Override public InventoryUseCase useCase() { return useCase; }
    @Override public ClockPort clock() { return clock; }
    @Override public Component window() { return this; }

    @Override
    public void navigate(Section section) {
        current = section;
        sidebar.select(section);
        title.setText(section.getLabel());
        screens.get(section).refresh();
        cards.show(screenHost, section.name());
    }

    @Override
    public void showLots(String productCode) {
        ((LotesScreen) screens.get(Section.LOTES)).selectProduct(productCode);
        navigate(Section.LOTES);
    }

    @Override
    public void notifySuccess(String message) {
        refreshAll();
        Toast.show(getRootPane(), message);
    }

    @Override
    public void refreshAll() {
        screens.values().forEach(ScreenPanel::refresh);
    }

    // ------------------------------------------------------------ utilidades

    /** Muestra un aviso general (por ejemplo, un archivo de datos dañado al iniciar). */
    public void showWarning(String message) {
        Dialogs.warning(this, "Aviso sobre los datos", message);
    }

    /** Secciones del menú, en orden (usado por pruebas). */
    public List<String> menuLabels() {
        List<String> labels = new ArrayList<>();
        for (Section s : Section.values()) labels.add(s.getLabel());
        return labels;
    }

    public Section currentSection() { return current; }
}
