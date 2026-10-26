import adapters.in.SwingApp;
import adapters.out.BogotaClockAdapter;
import adapters.out.JsonProductRepository;
import application.InventoryService;
import domain.ClockPort;
import domain.ProductRepository;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        ProductRepository repo = new JsonProductRepository();
        ClockPort clock = new BogotaClockAdapter();
        InventoryService service = new InventoryService(repo, clock);
        
        SwingUtilities.invokeLater(() -> {
            SwingApp app = new SwingApp(service, clock);
            app.setVisible(true);
        });
    }
}
