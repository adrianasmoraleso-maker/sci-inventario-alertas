package adapters.in;

import domain.Alert;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/** Inicio: indicadores generales y accesos rápidos (RF-UI02, RF-UI21). */
class InicioScreen extends ScreenPanel {
    private final StatCard total = new StatCard("stat-total", Icons.BOX, Theme.Tone.BLUE, "Total de productos");
    private final StatCard low = new StatCard("stat-low", Icons.TREND_DOWN, Theme.Tone.AMBER, "Stock bajo");
    private final StatCard soon = new StatCard("stat-soon", Icons.CLOCK, Theme.Tone.AMBER, "Próximos a caducar");
    private final StatCard expired = new StatCard("stat-expired", Icons.BLOCK, Theme.Tone.RED, "Caducados");

    InicioScreen(AppShell shell) {
        super(shell, "screen-inicio");

        JPanel stats = new JPanel(new GridLayout(1, 4, Theme.SP_XS, 0));
        stats.setOpaque(false);
        stats.add(total);
        stats.add(low);
        stats.add(soon);
        stats.add(expired);

        Card quick = new Card(new BorderLayout(0, Theme.SP_M));
        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        JLabel title = cardTitle("Accesos rápidos");
        title.setAlignmentX(LEFT_ALIGNMENT);
        JLabel hint = toolbarLabel("Atajos a las tareas más frecuentes del inventario.");
        hint.setAlignmentX(LEFT_ALIGNMENT);
        heading.add(title);
        heading.add(Box.createVerticalStrut(Theme.SP_XS));
        heading.add(hint);

        AppButton addProduct = new AppButton("Registrar producto", AppButton.Style.PRIMARY);
        addProduct.setName("quick-product");
        addProduct.addActionListener(e -> shell.navigate(Section.PRODUCTOS));
        AppButton exit = new AppButton("Registrar salida", AppButton.Style.SECONDARY);
        exit.setName("quick-exit");
        exit.addActionListener(e -> shell.navigate(Section.SALIDAS));
        AppButton alerts = new AppButton("Ver alertas", AppButton.Style.SECONDARY);
        alerts.setName("quick-alerts");
        alerts.addActionListener(e -> shell.navigate(Section.ALERTAS));

        quick.add(heading, BorderLayout.NORTH);
        quick.add(row(addProduct, exit, alerts), BorderLayout.CENTER);

        JPanel column = new JPanel(new BorderLayout(0, Theme.SP_S));
        column.setOpaque(false);
        column.add(stats, BorderLayout.NORTH);
        column.add(quick, BorderLayout.CENTER);
        add(column, BorderLayout.NORTH);
    }

    @Override
    void refresh() {
        List<Alert> alerts = shell.useCase().getAlerts();
        total.setValue(String.valueOf(shell.useCase().listProducts().size()));
        low.setValue(String.valueOf(count(alerts, "STOCK_BAJO")));
        soon.setValue(String.valueOf(count(alerts, "PROXIMO_A_CADUCAR")));
        expired.setValue(String.valueOf(count(alerts, "CADUCADO")));
    }

    private static long count(List<Alert> alerts, String type) {
        return alerts.stream().filter(a -> type.equals(a.getType())).count();
    }
}
