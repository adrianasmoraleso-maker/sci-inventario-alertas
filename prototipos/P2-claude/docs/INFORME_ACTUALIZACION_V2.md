# Informe de la actualización a la versión 2 (SDD v0.4, sección 8)

## Archivos por zona

**Dominio** (`src/main/java/domain`)
- Modificados: `Product` (validaciones RN14–RN17, `update`, estados, orden FEFO, `processExit` devuelve la asignación), `Lot` (fecha obligatoria, `isExpiringSoon`, `daysRemaining`, `getStatus`).
- Nuevos: `LotStatus`, `ProductStatus`, `ExitAllocation`, `PersistenceException`.

**Aplicación** (`src/main/java/application`)
- Modificados: `InventoryService` (valida antes de guardar, usa `Product.update`, devuelve la asignación FEFO, alertas con las reglas del dominio y fechas dd/mm/aaaa), `InventoryUseCase` (`registerExit` ahora devuelve la lista de asignaciones).

**Adaptadores**
- Salida: `JsonProductRepository` reescrito (copia `.bak`, guardado con archivo temporal, errores claros, copias defensivas).
- Entrada: `SwingApp` reescrito; nuevos `Theme`, `Icons`, `Card`, `StatCard`, `AppButton`, `StyledTextField`, `StyledCombo`, `FormField`, `DataTable`, `Sidebar`, `Dialogs`, `Toast`, `Messages`, `Validation`, `Section`, `AppShell`, `ScreenPanel`, `ProductItem`, `FormPanel`, `ProductForm`, `LotForm`, `EntryForm` y las pantallas `InicioScreen`, `ProductosScreen`, `LotesScreen`, `SalidasScreen`, `ExistenciasScreen`, `AlertasScreen`.
- `Main`: muestra el aviso de archivo dañado sin cerrar la aplicación.

**Pruebas** (`src/test/java`): `ProductTest`, `LotTest`, `InventoryServiceTest`, `ExitAtomicityTest`, `JsonProductRepositoryTest`, `HexagonalArchitectureTest`, `SwingAppTest`, `testsupport/*`.

**Build:** `pom.xml` (surefire 3.2.5, exec plugin, JUnit 5), `mvnw`, `mvnw.cmd`, `.mvn/wrapper/`.

## Resultado de las pruebas
- Con entorno gráfico: 101 pruebas, 101 aprobadas, 0 fallidas, 0 omitidas.
- En modo headless: 101 pruebas, 76 aprobadas, 0 fallidas, 25 omitidas (las de interfaz).
- Ejecutadas con la consola de JUnit 5 (el entorno de trabajo no pudo descargar de Maven Central).

## Fallos de la versión 1 contrarios a las secciones 1–7 (corregidos)
1. RF-UI05: la pantalla de salidas no mostraba la asignación FEFO por lote ni el aviso de stock bajo.
2. RF-UI14: los mensajes de alertas mostraban fechas en formato ISO; ahora dd/mm/aaaa.
3. RF-UI12: cada operación reconstruía todas las pantallas (perdía filtros y selección); ahora se refrescan los datos y se muestra una notificación.
4. RN01/RN04: una salida con cantidad ≤ 0 o un lote sin fecha solo se rechazaban en algunos caminos; ahora el dominio los rechaza siempre.
5. RNF06: si fallaba el guardado, el estado en memoria ya había cambiado; ahora el repositorio entrega copias y solo actualiza su estado si el guardado tuvo éxito.
6. Editar un producto lo movía al final del archivo; ahora conserva su posición.

## Supuestos
- «Registrar producto» (Inicio) lleva a Productos; el formulario se abre con «Nuevo producto».
- En Existencias los lotes van en orden FEFO: primero los no caducados por vencimiento, luego los caducados.
- «Días restantes» de un lote caducado se muestra como «—».
- «Entrada» siempre está habilitado; si el lote está caducado, el servicio rechaza con un mensaje claro (RN06). Solo «Eliminar» se deshabilita (lotes no caducados).
- Alertas: «Stock bajo» y «Próximo a caducar» en ámbar, «Caducado» en rojo.
- Sin FlatLaf: se usa Java2D y UIManager (sin dependencias nuevas).
- Los campos de texto numérico se validan al guardar (no se bloquean teclas) y el error aparece bajo el campo.

## Cambios que tocan el contrato
- `InventoryUseCase.registerExit` devuelve `List<ExitAllocation>` (antes `void`); es compatible con quien ignore el resultado.
- `ProductRepository` documenta `PersistenceException` para fallos de escritura (RNF09).

## Limitaciones
- El archivo de las pruebas originales no venía en el ZIP (solo `ProductTest.testFEFOExit`); las 10 pruebas fallidas se recrearon con los mismos nombres a partir del informe.
- Los botones de fila de las tablas se manejan con el ratón (no con teclado).
- Por debajo de ~1050 px de ancho las tablas con muchas columnas muestran scroll horizontal.
- `./mvnw test` no pudo ejecutarse en el entorno de trabajo (sin acceso a Maven Central); está pendiente de confirmar en tu máquina.
