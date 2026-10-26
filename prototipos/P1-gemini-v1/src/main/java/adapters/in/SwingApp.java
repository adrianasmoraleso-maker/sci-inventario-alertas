package adapters.in;

import application.InventoryUseCase;
import domain.Alert;
import domain.Product;
import domain.Lot;
import domain.ClockPort;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SwingApp extends JFrame {
    private final InventoryUseCase useCase;
    private final ClockPort clock;
    private JTabbedPane tabbedPane;
    
    private JPanel panelInicio;
    private JPanel panelProductos;
    private JPanel panelLotes;
    private JPanel panelSalidas;
    private JPanel panelExistencias;
    private JPanel panelAlertas;

    public SwingApp(InventoryUseCase useCase, ClockPort clock) {
        this.useCase = useCase;
        this.clock = clock;
        setTitle("Sistema de Control de Inventario y Alertas (SCI)");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        tabbedPane = new JTabbedPane();
        
        panelInicio = createInicioPanel();
        panelProductos = createProductosPanel();
        panelLotes = createLotesPanel();
        panelSalidas = createSalidasPanel();
        panelExistencias = createExistenciasPanel();
        panelAlertas = createAlertasPanel();

        tabbedPane.addTab("Inicio", panelInicio);
        tabbedPane.addTab("Productos", panelProductos);
        tabbedPane.addTab("Lotes", panelLotes);
        tabbedPane.addTab("Salidas", panelSalidas);
        tabbedPane.addTab("Existencias", panelExistencias);
        tabbedPane.addTab("Alertas", panelAlertas);
        
        tabbedPane.addChangeListener(e -> refreshUI());

        add(tabbedPane);
        refreshUI();
    }

    private void refreshUI() {
        tabbedPane.setComponentAt(0, createInicioPanel());
        tabbedPane.setComponentAt(1, createProductosPanel());
        tabbedPane.setComponentAt(2, createLotesPanel());
        tabbedPane.setComponentAt(3, createSalidasPanel());
        tabbedPane.setComponentAt(4, createExistenciasPanel());
        tabbedPane.setComponentAt(5, createAlertasPanel());
    }

    private void showError(Exception ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
    private void showSuccess(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Éxito", JOptionPane.INFORMATION_MESSAGE);
        refreshUI();
    }

    private JPanel createInicioPanel() {
        JPanel p = new JPanel(new GridLayout(3, 1));
        List<Product> prods = useCase.listProducts();
        List<Alert> alerts = useCase.getAlerts();
        
        p.add(new JLabel("Resumen de Inventario", SwingConstants.CENTER));
        p.add(new JLabel("Total Productos: " + prods.size() + " | Total Alertas: " + alerts.size(), SwingConstants.CENTER));
        
        JPanel btnPanel = new JPanel();
        JButton btnAdd = new JButton("Registrar Producto");
        btnAdd.addActionListener(e -> tabbedPane.setSelectedIndex(1));
        JButton btnSalida = new JButton("Registrar Salida");
        btnSalida.addActionListener(e -> tabbedPane.setSelectedIndex(3));
        btnPanel.add(btnAdd);
        btnPanel.add(btnSalida);
        p.add(btnPanel);
        
        return p;
    }

    private JPanel createProductosPanel() {
        JPanel p = new JPanel(new BorderLayout());
        String[] cols = {"Código", "Nombre", "Min Stock", "Umbral", "Disponible"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        
        for (Product prod : useCase.listProducts()) {
            model.addRow(new Object[]{prod.getCode(), prod.getName(), prod.getMinStock(), prod.getAlertThresholdDays(), prod.getAvailableStock(clock.today())});
        }
        
        JTable table = new JTable(model);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        
        JPanel form = new JPanel();
        JTextField txtCode = new JTextField(5);
        JTextField txtName = new JTextField(10);
        JTextField txtMin = new JTextField(3);
        JTextField txtUmbral = new JTextField(3);
        
        form.add(new JLabel("Cod:")); form.add(txtCode);
        form.add(new JLabel("Nombre:")); form.add(txtName);
        form.add(new JLabel("Min:")); form.add(txtMin);
        form.add(new JLabel("Umbral:")); form.add(txtUmbral);
        
        JButton btnAdd = new JButton("Crear");
        btnAdd.addActionListener(e -> {
            try {
                useCase.createProduct(txtCode.getText(), txtName.getText(), Integer.parseInt(txtMin.getText()), Integer.parseInt(txtUmbral.getText()));
                showSuccess("Producto creado");
            } catch (Exception ex) { showError(ex); }
        });
        
        JButton btnDel = new JButton("Eliminar");
        btnDel.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                if(JOptionPane.showConfirmDialog(this, "¿Eliminar producto?") == JOptionPane.YES_OPTION) {
                    try {
                        useCase.deleteProduct(model.getValueAt(row, 0).toString());
                        showSuccess("Producto eliminado");
                    } catch (Exception ex) { showError(ex); }
                }
            } else { JOptionPane.showMessageDialog(this, "Seleccione un producto"); }
        });
        
        form.add(btnAdd);
        form.add(btnDel);
        p.add(form, BorderLayout.SOUTH);
        
        return p;
    }

    private JPanel createLotesPanel() {
        JPanel p = new JPanel(new BorderLayout());
        String[] cols = {"Producto", "Lote", "Vencimiento", "Saldo", "Estado"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        
        LocalDate today = clock.today();
        for (Product prod : useCase.listProducts()) {
            for (Lot l : prod.getLots()) {
                String estado = l.isExpired(today) ? "Caducado" : "Vigente";
                model.addRow(new Object[]{prod.getCode(), l.getCode(), l.getDueDate().toString(), l.getBalance(), estado});
            }
        }
        
        JTable table = new JTable(model);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        
        JPanel form = new JPanel();
        JTextField txtProd = new JTextField(5);
        JTextField txtLot = new JTextField(5);
        JTextField txtDate = new JTextField(10); // dd/MM/yyyy
        JTextField txtQty = new JTextField(3);
        
        form.add(new JLabel("Prod Cod:")); form.add(txtProd);
        form.add(new JLabel("Lote Cod:")); form.add(txtLot);
        form.add(new JLabel("Vence(dd/MM/yyyy):")); form.add(txtDate);
        form.add(new JLabel("Cant:")); form.add(txtQty);
        
        JButton btnAdd = new JButton("Reg Lote");
        btnAdd.addActionListener(e -> {
            try {
                LocalDate date = LocalDate.parse(txtDate.getText(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                useCase.registerLot(txtProd.getText(), txtLot.getText(), date, Integer.parseInt(txtQty.getText()));
                showSuccess("Lote registrado");
            } catch (Exception ex) { showError(ex); }
        });
        
        JButton btnDel = new JButton("Eliminar Caducado");
        btnDel.addActionListener(e -> {
            int row = table.getSelectedRow();
            if(row >= 0) {
                try {
                    useCase.deleteExpiredLot(model.getValueAt(row, 0).toString(), model.getValueAt(row, 1).toString());
                    showSuccess("Lote caducado eliminado");
                } catch(Exception ex) { showError(ex); }
            }
        });
        
        form.add(btnAdd);
        form.add(btnDel);
        p.add(form, BorderLayout.SOUTH);
        return p;
    }

    private JPanel createSalidasPanel() {
        JPanel p = new JPanel();
        JTextField txtProd = new JTextField(8);
        JTextField txtQty = new JTextField(5);
        
        p.add(new JLabel("Producto:")); p.add(txtProd);
        p.add(new JLabel("Cantidad:")); p.add(txtQty);
        
        JButton btn = new JButton("Confirmar Salida FEFO");
        btn.addActionListener(e -> {
            if(JOptionPane.showConfirmDialog(this, "¿Registrar salida?") == JOptionPane.YES_OPTION) {
                try {
                    useCase.registerExit(txtProd.getText(), Integer.parseInt(txtQty.getText()));
                    showSuccess("Salida registrada correctamente");
                } catch(Exception ex) { showError(ex); }
            }
        });
        p.add(btn);
        return p;
    }

    private JPanel createExistenciasPanel() {
        JPanel p = new JPanel(new BorderLayout());
        String[] cols = {"Producto", "Disponible", "Caducado"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        
        for(Product prod : useCase.listProducts()) {
            model.addRow(new Object[]{prod.getCode(), prod.getAvailableStock(clock.today()), prod.getExpiredStock(clock.today())});
        }
        p.add(new JScrollPane(new JTable(model)), BorderLayout.CENTER);
        return p;
    }

    private JPanel createAlertasPanel() {
        JPanel p = new JPanel(new BorderLayout());
        String[] cols = {"Tipo", "Producto", "Lote", "Mensaje"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        
        for(Alert a : useCase.getAlerts()) {
            model.addRow(new Object[]{a.getType(), a.getProductCode(), a.getLotCode() == null ? "-" : a.getLotCode(), a.getMessage()});
        }
        p.add(new JScrollPane(new JTable(model)), BorderLayout.CENTER);
        return p;
    }
}
