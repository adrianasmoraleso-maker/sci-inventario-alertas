package adapters.in;

import application.InventoryUseCase;
import domain.ClockPort;

import java.awt.*;

/** Lo que las pantallas necesitan del marco de la aplicación. */
interface AppShell {
    InventoryUseCase useCase();
    ClockPort clock();
    Component window();
    void navigate(Section section);
    void showLots(String productCode);
    void notifySuccess(String message);
    void refreshAll();
}
