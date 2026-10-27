import adapters.in.SwingApp;
import adapters.out.BogotaClockAdapter;
import adapters.out.JsonProductRepository;
import application.InventoryService;
import domain.ClockPort;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        JsonProductRepository repo = new JsonProductRepository();
        ClockPort clock = new BogotaClockAdapter();
        InventoryService service = new InventoryService(repo, clock);

        SwingUtilities.invokeLater(() -> {
            SwingApp app = new SwingApp(service, clock);
            app.setVisible(true);
            // Si el archivo de datos estaba dañado o no se pudo leer/crear, se informa sin cerrar la aplicación.
            if (repo.getStartupWarning() != null) {
                app.showWarning(repo.getStartupWarning());
            }
        });
    }
}
