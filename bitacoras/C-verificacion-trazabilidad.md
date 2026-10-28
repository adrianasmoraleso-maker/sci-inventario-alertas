# Bitácora C: Verificación y trazabilidad (Dimensión 2)



## 1. Inventario de pruebas

### 1.1 Archivos de prueba `[auto]`

| Archivo | P1  | P2 | Mismo nombre de prueba en ambos |
|---|---|---|---|
| `application/InventoryServiceTest` | 45 ejecutadas | 20 | 4 |
| `domain/ProductTest` | 19 | 22 | 8 |
| `domain/LotTest` | 8 | 6 | 0 |
| `application/ExitAtomicityTest` | 10 | 3 | — |
| `adapters/out/JsonProductRepositoryTest` | — (no ejecutada; solo hay un archivo de prueba de referencia) | 18 | — |
| `architecture/HexagonalArchitectureTest` | 3 | 7 | — |
| `adapters/in/SwingAppTest` | 3 | 25 | — |
| `testsupport/FixedClock`, `testsupport/InMemoryProductRepository` | — | apoyo (sin `@Test`) | — |
| **Total ejecutado** | **88** | **101** | **12** |



## 2. Resultados de ejecución

La evidencia disponible corresponde a las ejecuciones Maven archivadas, no a una ejecución registrada desde IntelliJ. Los reportes Surefire no identifican a las personas ejecutoras; los nombres consignados en la tabla fueron añadidos por el equipo para indicar quién estuvo a cargo de cada suite. Símbolos: `✔` aprobado · `✖` falló · `NE` no ejecutado/no verificable con la evidencia disponible. `SwingAppTest` se ejecutó en ambos prototipos (3 pruebas en P1 y 25 en P2), sin omitidas en esos reportes.

Según el equipo, las verificaciones manuales de interfaz de P1 indicadas en la matriz sí se realizaron. Se conservaron capturas de algunas pantallas, pero no un registro detallado de los pasos y resultados de cada verificación. Por ello, sus estados representan la valoración retrospectiva del equipo; las capturas documentan aspectos visibles de la interfaz, pero no bastan para corroborar cada resultado.

| Archivo de prueba | Ejecutó (fecha, quién) | P1: aprobadas / falladas / omitidas | P2: aprobadas / falladas / omitidas | Evidencia | Notas |
|---|---|---|---|---|---|
| `domain/ProductTest` | 27/10/2026; Adriana Morales | 10 / 9 / 0 | 22 / 0 / 0 | `P1/surefire-reports/domain.ProductTest.txt`; `P2/TEST-domain.ProductTest.xml` | P1: 9 fallos, incluidos nombre vacío, mínimo/umbral inválidos y salida ≤ 0. P2: todas aprobadas. |
| `domain/LotTest` | 27/10/2026; Adriana Morales | 8 / 0 / 0 | 6 / 0 / 0 | `P1/surefire-reports/domain.LotTest.txt`; `P2/TEST-domain.LotTest.xml` | Sin fallos en ninguno de los dos reportes. |
| `application/InventoryServiceTest` | 27/10/2026; Adriana Morales | 42 / 3 / 0 | 20 / 0 / 0 | `P1/surefire-reports/application.InventoryServiceTest.txt`; `P2/TEST-application.InventoryServiceTest.xml` | Los 3 fallos de P1 son validaciones al editar: mínimo negativo (RN15) y umbral cero/negativo (RN16), en contra del mandato de RN17. |
| `application/ExitAtomicityTest` | 27/10/2026; Adriana Morales | 10 / 0 / 0 | 3 / 0 / 0 | `P1/surefire-reports/application.ExitAtomicityTest.txt`; `P2/TEST-application.ExitAtomicityTest.xml` | Sin fallos en ninguno de los dos reportes. |
| `adapters/out/JsonProductRepositoryTest` | 27/10/2026; Said Martelo | NE / NE / NE | 18 / 0 / 0 | P1: `P1/JsonProductRepositoryTest.java.txt` (prueba de referencia, sin reporte de ejecución); P2: `P2/TEST-adapters.out.JsonProductRepositoryTest.xml` | No atribuir a P1 pruebas de persistencia ejecutadas: no existe reporte Surefire de esa suite. |
| `architecture/HexagonalArchitectureTest` | 27/10/2026; Said Martelo  | 3 / 0 / 0 | 7 / 0 / 0 | `P1/surefire-reports/architecture.HexagonalArchitectureTest.txt`; `P2/TEST-architecture.HexagonalArchitectureTest.xml` | Sin fallos en ninguno de los dos reportes. |
| `adapters/in/SwingAppTest` | 27/10/2026; Said Martelo  | 3 / 0 / 0 | 25 / 0 / 0 | `P1/surefire-reports/adapters.in.SwingAppTest.txt`; `P2/TEST-adapters.in.SwingAppTest.xml` | Las pruebas aparecen ejecutadas, no omitidas; los reportes no registran fallos. |
| **Total** | 27/10/2026; Adriana Morales, Said Martelo | **76 / 12 / 0 (88 ejecutadas)** | **101 / 0 / 0 (101 ejecutadas)** | `P1/test-run.txt`; `P2/test-run.txt` y reportes Surefire | Errores: 0 en ambos. El informe previo de P2 (76 aprobadas y 25 omitidas en headless) describe otra ejecución y no debe mezclarse con este resultado. |

El informe narrativo de actualización de P2 describe además una ejecución headless con 76 aprobadas y 25 omitidas. Esa cifra es distinta de la ejecución Maven archivada aquí (101 ejecutadas, 101 aprobadas, 0 omitidas); conservar ambos resultados separados evita presentarlos como una sola corrida.
Las rutas `P1/...` y `P2/...` de la columna Evidencia son relativas a `evidencia/pruebas/`.

## 3. Matriz de trazabilidad especificación-prueba-resultado

**Cómo leerla.** «Spec» indica en qué versión aparece el requisito. Las columnas de pruebas candidatas se generaron buscando nombres de métodos y **son una pista, no una verificación**: pueden incluir pruebas que no corresponden al requisito y omitir otras. En P1, la anotación «Prueba de interfaz realizada manualmente» informa una verificación retrospectiva comunicada por el equipo; no es una prueba candidata automatizada ni cuenta con registro detallado o captura archivada. `—` significa que no se encontró una prueba candidata automatizada. Las columnas P1 y P2 las llena el equipo: `✔` cumple · `◐` cumple parcialmente · `✖` no cumple · `NE` no evaluable. **El resultado lo deciden la ejecución de las pruebas y los criterios de verificación de la especificación.**

Criterio para `◐`: la evidencia es mixta (por ejemplo, un camino o capa pasa y otro falla) o cubre solo parte de los subcriterios; `✖` indica que un fallo directo o una incompatibilidad comprobada impide cumplir el requisito completo. `NE` indica que la evidencia no permite decidir.


| ID | Spec | Descripción (copiar de la especificación) | Pruebas candidatas P1 (búsqueda inicial) | Pruebas candidatas P2 | P1 | P2 | Notas y DEF |
|---|---|---|---|---|---|---|---|
| RF01 | v0.1 | Crear un producto con código único, nombre, stock mínimo y umbral de alerta de caducidad en días (por defecto 180). | 2: shouldCreateProductSuccessfully, shouldRejectDuplicateProductCode | 4: shouldRejectBlankNameWhenCreatingProduct, shouldRejectNegativeMinimumStockWhenCreatingProduct… | ✔ | ✔ | |
| RF02 | v0.1 | Listar productos y consultar uno. | 1: shouldListAllProducts | — | ✔ | NE | |
| RF03 | v0.1 | Editar nombre, stock mínimo y umbral de alerta de un producto. | 2: shouldEditProductSuccessfully, shouldRejectEditingNonexistentProduct | 3: shouldEditProductWithValidValues, shouldFailToEditOrDeleteUnknownProduct… | ✔ | ✔ | |
| RF04 | v0.1 | Eliminar un producto, solo si ninguno de sus lotes tiene saldo mayor que 0. | 2: shouldDeleteProductWithoutPositiveStock, shouldRejectDeletingProductWithPositiveStock | 3: shouldFailToEditOrDeleteUnknownProduct, shouldNotDeleteProductWithBalance… | ✔ | ✔ | |
| RF05 | v0.1 | Registrar un lote de un producto con código, fecha de vencimiento y cantidad inicial. | 9: shouldRegisterLotSuccessfully, shouldRejectLotWithZeroInitialQuantity… | 2: shouldRegisterLotAndRejectInvalidOnes, shouldRejectLotWithPastDueDateOrDuplicateCode | ✔ | ✔ | |
| RF06 | v0.1 | Registrar una entrada adicional sobre un lote no caducado. | 7: shouldRegisterEntryAndIncreaseLotBalance, shouldRejectEntryWithZeroQuantity… | 2: entryShouldIncreaseBalance, entryShouldRejectInvalidQuantityUnknownLotAndExpiredLot | ✔ | ✔ | |
| RF07 | v0.1 | Registrar la salida de una cantidad de un producto, asignada automáticamente a lotes según FEFO y repartida entre varios si hac… | 14: shouldRegisterExitUsingFEFO, shouldRejectExitWhenAvailableStockIsInsufficient… | 10: rejectedExitChangesNothingAndDoesNotSave, acceptedExitSplitsAcrossLotsAndSavesOnce… | ✔ | ✔ | Los fallos de cantidad no positiva se registran en RN01 (DEF-001); las pruebas de salida FEFO válida pasan. |
| RF08 | v0.1 | Consultar existencias de un producto: stock disponible, stock caducado y lotes con estado y días restantes. | 3: shouldExcludeExpiredLotsFromAvailableStock, shouldCountExpiredStockSeparately… | 2: daysRemaining, lotExpiringTodayStillCountsAsAvailable | ✔ | ✔ | |
| RF09 | v0.1 | Consultar alertas: stock bajo, próximo a caducar y caducado. | 4: shouldGenerateLowStockAlertWhenAvailableStockIsBelowMinimum, shouldNotGenerateLowStockAlertWhenStockEqualsMinimum… | 1: zeroMinimumStockNeverRaisesLowStockAlert | ✔ | ✔ | Los casos de alertas de stock bajo, proximidad y caducidad pasan en las suites de servicio. |
| RF10 | v0.1 | Eliminar un lote caducado, descartando su saldo. | 4: shouldDeleteExpiredLotSuccessfully, shouldRejectDeletingLotThatIsNotExpired… | 2: shouldDeleteOnlyExpiredLots, shouldOnlyRemoveExpiredLots | ✔ | ✔ | |
| RF11 | v0.1 | Guardar todos los cambios en el archivo de persistencia y cargarlos al iniciar la aplicación. | — | 5: createsEmptyFileOnFirstRun, savedProductsSurviveReload… | NE | ✔ | P1 no tiene reporte de pruebas de persistencia. |
| RN01 | v0.1 | Toda cantidad de entrada, salida o lote inicial es un entero mayor que 0. | 8: shouldRejectEntryWithZeroQuantity, shouldRejectEntryWithNegativeQuantity… | 4: entryShouldRejectInvalidQuantityUnknownLotAndExpiredLot, exitShouldRejectNonPositiveQuantityAndUnknownProduct… | ◐ | ✔ | P1: la entidad de dominio permite salida ≤ 0 al invocarse directamente, pero el servicio rechaza esos valores (DEF-001). |
| RN02 | v0.1 | El saldo de un lote nunca es negativo. | 3: shouldSkipLotWithZeroBalanceDuringFEFOExit, shouldRejectNegativeBalanceWhenUpdating… | 1: shouldDeleteProductWhenAllLotsHaveZeroBalance | ✔ | ✔ | |
| RN03 | v0.1 | El código de producto es único; el código de lote es único dentro de su producto. | 3: shouldRejectDuplicateProductCode, shouldRejectDuplicateLotCodeWithinProduct… | 2: shouldRejectDuplicateProductCode, shouldRejectLotWithPastDueDateOrDuplicateCode | ✔ | ✔ | |
| RN04 | v0.1 | El lote tiene fecha de vencimiento obligatoria, y al registrarlo no puede ser anterior a hoy. | 1: shouldRejectLotWithPastExpirationDate | 2: shouldRequireDueDate, shouldRejectLotWithPastDueDateOrDuplicateCode | ◐ | ✔ | P1 prueba fecha pasada, pero no obligatoriedad de fecha (DEF-012). |
| RN05 | v0.1 | Un lote está caducado si hoy > fecha de vencimiento (el día de vencimiento aún es utilizable). | 5: shouldGenerateExpiredAlertWhenTodayIsAfterDueDate, shouldNotBeExpiredOnDueDate… | 2: expiryBoundaries, lotStatusShouldFollowRn05AndRn11 | ✔ | ✔ | |
| RN06 | v0.1 | Un lote caducado no recibe entradas ni participa en salidas. | 3: shouldRejectEntryForExpiredLot, shouldNotConsumeExpiredLotsDuringExit… | 2: entryShouldRejectInvalidQuantityUnknownLotAndExpiredLot, exitShouldIgnoreExpiredLots | ✔ | ✔ | |
| RN07 | v0.1 | Stock disponible = suma de saldos de lotes no caducados. Stock caducado = suma de saldos de lotes caducados. | 6: shouldRejectExitWhenAvailableStockIsInsufficient, shouldGenerateLowStockAlertWhenAvailableStockIsBelowMinimum… | 1: lotExpiringTodayStillCountsAsAvailable | ✔ | ✔ | |
| RN08 | v0.1 | FEFO: la salida consume primero el lote no caducado con vencimiento más próximo; empates por orden de registro; se omiten lotes… | 7: shouldRegisterExitUsingFEFO, shouldSkipLotWithZeroBalanceDuringFEFOExit… | 4: exitShouldFollowFefoAndReturnAllocations, testFEFOExit… | ✔ | ✔ | |
| RN09 | v0.1 | Si el stock disponible es menor que la cantidad pedida, la salida se rechaza por completo y no cambia ningún saldo. | 4: shouldRejectExitWhenAvailableStockIsInsufficient, shouldNotModifyAnyLotWhenExitExceedsAvailableStock… | 2: rejectedExitChangesNothingAndDoesNotSave, exitShouldBeRejectedCompletelyWhenStockIsInsufficient | ✔ | ✔ | |
| RN10 | v0.1 | Hay stock bajo cuando stock disponible < stock mínimo. Con mínimo 0 nunca hay alerta. | 3: shouldGenerateLowStockAlertWhenAvailableStockIsBelowMinimum, shouldNotGenerateLowStockAlertWhenStockEqualsMinimum… | 3: alertsShouldCoverLowStockExpiringAndExpired, zeroMinimumStockNeverRaisesLowStockAlert… | ✔ | ✔ | |
| RN11 | v0.1 | Un lote está próximo a caducar si no está caducado, tiene saldo > 0 y hoy >= vencimiento − umbral en días. | 2: shouldGenerateNearExpiryAlertWhenLotEntersConfiguredThreshold, shouldGenerateNearExpiryAlertExactlyAtThresholdBoundary | 3: alertsShouldCoverLowStockExpiringAndExpired, expiringSoonRespectsThresholdBoundary… | ✔ | ✔ | |
| RN12 | v0.1 | Las alertas se calculan al consultar; no se guardan. | — | — | NE | NE | |
| RN13 | v0.1 | Un producto con saldo > 0 no se elimina; solo se eliminan lotes caducados. | 2: shouldRejectDeletingProductWithPositiveStock, shouldRejectDeletingLotThatIsNotExpired | 3: shouldNotDeleteProductWithBalance, shouldDeleteOnlyExpiredLots… | ✔ | ✔ | |
| RN14 | v0.2 | El nombre de un producto no puede estar vacío ni contener solo espacios. | 1: shouldRejectBlankProductName | 4: shouldRejectBlankNameWhenCreatingProduct, shouldRejectBlankNameWhenEditingProduct… | ✖ | ✔ | P1: prueba de nombre vacío fallida (DEF-003). |
| RN15 | v0.2 | El stock mínimo es un entero ≥ 0, tanto al crear como al editar un producto. | 5: shouldNotGenerateLowStockAlertWhenMinimumStockIsZero, shouldRejectNegativeMinimumStockWhenEditingProduct… | 6: shouldRejectNegativeMinimumStockWhenCreatingProduct, shouldRejectNegativeMinimumStockWhenEditingProduct… | ✖ | ✔ | P1: pruebas de mínimo negativo al crear/editar fallidas (DEF-004). |
| RN16 | v0.2 | El umbral de alerta es un entero > 0, tanto al crear como al editar un producto. | 7: shouldRejectZeroAlertThresholdWhenEditingProduct, shouldRejectNegativeAlertThresholdWhenEditingProduct… | 7: shouldRejectInvalidThresholdWhenCreatingProduct, shouldRejectZeroAlertThresholdWhenEditingProduct… | ✖ | ✔ | P1: pruebas de umbral cero/negativo al crear/editar fallidas (DEF-005). |
| RN17 | v0.2 | Estas validaciones viven en el dominio (al construir y al modificar el producto) y los casos de uso las respetan. Ante un valor… | 10: shouldRejectNegativeMinimumStockWhenEditingProduct, shouldRejectZeroAlertThresholdWhenEditingProduct… | 12: shouldRejectBlankNameWhenCreatingProduct, shouldRejectNegativeMinimumStockWhenCreatingProduct… | ✖ | ✔ | P1: no cumple validaciones de creación/edición RN14–RN16. |
| RF-UI01 | v0.1 | Menú de navegación siempre visible (barra lateral o pestañas) con seis secciones: Inicio, Productos, Lotes, Salidas, Existencia… | — Prueba de interfaz realizada manualmente | menuHasExactlyTheSixSections, activeSectionIsHighlightedAndTitleChanges | ◐ | ✔ | P1 verifica las seis pestañas; no prueba explícitamente el resaltado de la sección activa. |
| RF-UI02 | v0.1 | Inicio — Número de productos, número de alertas por tipo y accesos rápidos a Registrar producto, Registrar salida y Alertas. |— Prueba de interfaz realizada manualmente | quickAccessButtonsNavigate, homeIndicatorsShowCounts | ◐ | ✔ | P1 prueba dos accesos rápidos, no los indicadores ni el acceso a Alertas. |
| RF-UI03 | v0.1 | Productos — Tabla con código, nombre, stock mínimo, umbral y stock disponible; formularios para crear, editar y eliminar; búsqu… | — Prueba de interfaz realizada manualmente | productsTableHasTheSpecifiedColumnsAndStatuses, productSearchFiltersByCodeOrName | ◐ | ✔ | P1 verifica columnas y acciones Crear/Eliminar; no búsqueda ni edición. |
| RF-UI04 | v0.1 | Lotes — Por producto: tabla de lotes (código, vencimiento, saldo, estado) y acciones para registrar lote, entrada adicional y e… | — Prueba de interfaz realizada manualmente | lotsScreenShowsStatusesWithDaysRemaining | NE | ◐ | |
| RF-UI05 | v0.1 | Salidas — Producto y cantidad. Al confirmar, muestra la asignación FEFO por lote y un aviso si queda stock bajo. | — Prueba de interfaz realizada manualmente| exitScreenRejectsInvalidQuantityBeforeAnyDialog (parcial: no se vio una prueba de la asignación FEFO en pantalla) | ✖ | ◐ | P1 no muestra asignación FEFO/aviso en pantalla (DEF-007); P2 no tiene prueba candidata directa de la asignación visual. |
| RF-UI06 | v0.1 | Existencias — Selector de producto; indicadores de disponible, caducado y mínimo; tabla de lotes en orden FEFO con estado y día… | — Prueba de interfaz realizada manualmente| stockExistenceTableFollowsFefoOrder | NE | ◐ | |
| RF-UI07 | v0.1 | Alertas — Lista con tipo, producto, lote y mensaje; filtro por tipo. | — (sin pruebas de interfaz en los 3 archivos) | alertsScreenFiltersByType | NE | ✔ | |
| RF-UI08 | v0.1 | Validación en los formularios: campos obligatorios, cantidad entera > 0, stock mínimo ≥ 0, umbral > 0, fecha no pasada. El erro… | — Prueba de interfaz realizada manualmente| emptyRequiredFieldsAreReportedNextToTheField, lotFormRejectsPastDatesInvalidDatesAndBadQuantities | NE | ◐ | |
| RF-UI09 | v0.1 | Los errores de reglas (código duplicado, stock insuficiente, lote caducado, lote no caducado al eliminar, producto con saldo) s… |— Prueba de interfaz realizada manualmente | ruleErrorsAreShownAsMessagesInsideTheForm | NE | ✔ | |
| RF-UI10 | v0.1 | Confirmación antes de eliminar y antes de registrar una salida. | — Prueba de interfaz realizada manualmente | — | NE | NE | |
| RF-UI11 | v0.1 | Estados por color y por texto: Vigente (verde), Próximo a caducar (ámbar), Caducado (rojo). Las alertas usan los mismos colores. | — (sin pruebas de interfaz en los 3 archivos) | productsTableHasTheSpecifiedColumnsAndStatuses (parcial) | NE | ◐ | |
| RF-UI12 | v0.1 | Tras cada operación exitosa se muestra una notificación y las tablas se actualizan sin reiniciar. | — Prueba de interfaz realizada manualmente | — | ✖ | NE | P1 reconstruye pantallas y pierde filtros/selección (DEF-011); no hay prueba candidata directa. |
| RF-UI13 | v0.1 | Toda tabla o lista vacía muestra un mensaje que orienta al usuario. |— Prueba de interfaz realizada manualmente| emptyTablesShowGuidanceMessage | NE | ✔ | |
| RF-UI14 | v0.1 | Textos en español y fechas en formato dd/mm/aaaa. | — (sin pruebas de interfaz en los 3 archivos) | — | ✖ | NE | P1 formatea fechas como ISO (DEF-008); no hay prueba candidata directa. |
| RF-UI15 | v0.2 | Campos numéricos. Todos los campos numéricos (cantidad del lote, entrada, salida, stock mínimo, umbral) solo aceptan enteros. S… | — Prueba de interfaz realizada manualmente | negativeNumbersNeverReachTheUseCase, decimalsTextAndEmptyAreRejected, thresholdMustBeGreaterThanZeroAndMinStockMayBeZero | ✖ | ✔ | P1 no valida texto no numérico en el formulario (DEF-006). |
| RF-UI16 | v0.2 | Tablas de solo lectura. Ninguna celda de ninguna tabla o lista se puede editar directamente (ni con doble clic ni al escribir s… | — Prueba de interfaz realizada manualmente | noTableCellIsEditableInAnyScreen, tablesAreSeededSoTheEditabilityCheckIsMeaningful | ✖ | ✔ | P1 usa tablas editables por defecto (DEF-002). |
| RF-UI17 | v0.2 | Edición por formulario. La acción Editar abre un formulario con los valores actuales y las mismas validaciones de la creación,… | — Prueba de interfaz realizada manualmente| editFormShowsCurrentValuesValidatesAndCancelDiscards | NE | ✔ | |
| RF-UI18 | v0.2 | Estructura general. Barra lateral oscura a la izquierda con el nombre de la aplicación arriba y los ítems del menú con ícono; e… | — Prueba de interfaz realizada manualmente | — | NE | NE | |
| RF-UI19 | v0.2 | Menús. Solo los seis existentes: Inicio, Productos, Lotes, Salidas, Existencias y Alertas. No se agrega nada de la plantilla qu… |— Prueba de interfaz realizada manualmente | templateOnlyMenusAreNotPresent | NE | ✔ | |
| RF-UI20 | v0.2 | Pantallas de lista (Productos, Lotes, Alertas y tablas de Existencias). Tarjeta principal con barra de herramientas (búsqueda o… | — Prueba de interfaz realizada manualmente| — | NE | NE | |
| RF-UI21 | v0.2 | Tarjetas de indicadores. Fila de tarjetas con ícono, título y cifra, como en la plantilla, solo en Inicio (total de productos,… | — Prueba de interfaz realizada manualmente | homeIndicatorsShowCounts (parcial) | NE | ◐ | |
| RF-UI22 | v0.2 | Estados por color y texto. Lote: Vigente (verde), Próximo a caducar (ámbar), Caducado (rojo). Producto, solo como presentación… | — Prueba de interfaz realizada manualmente | productsTableHasTheSpecifiedColumnsAndStatuses, stockStatusShouldFollowRules | NE | ✔ | |
| RF-UI23 | v0.2 | Formularios y diálogos con el mismo estilo: bordes redondeados, botón primario azul marino, botón secundario con borde, errores… | — Prueba de interfaz realizada manualmente | — | NE | NE | |
| RF-UI24 | v0.2 | Tema centralizado. Colores, tipografía y espaciados se definen en un solo lugar (clase de tema), sin valores sueltos en cada pa… | — Prueba de interfaz realizada manualmente | theThemeIsTheSingleSourceOfColors | NE | ✔ | |
| RF-UI25 | v0.2 | Ventana. Redimensionable, con tamaño mínimo de 1000 × 650 px. Las tablas ocupan el ancho disponible y tienen scroll vertical pr… |— Prueba de interfaz realizada manualmente| windowHasMinimumSize | NE | ✔ | |
| RF-UI26 | v0.2 | Sin cambios funcionales. El rediseño no cambia reglas, casos de uso ni puertos. La interfaz sigue siendo un adaptador de entrad… |— Prueba de interfaz realizada manualmente | — | NE | NE | |
| RNF01 | v0.1 | Arquitectura hexagonal (puertos y adaptadores). Tres zonas: dominio (entidades, reglas, FEFO), aplicación (casos de uso y puert… | — | domainDoesNotDependOnFrameworksIoOrOuterZones, applicationDependsOnlyOnDomainAndPorts | ✔ | ✔ | P1: `P1/surefire-reports/architecture.HexagonalArchitectureTest.txt` pasa sus tres pruebas de arquitectura. |
| RNF02 | v0.1 | Regla de dependencias. Las dependencias apuntan hacia adentro. El dominio no importa Swing, java.io, java.nio.file ni librerías… | — | domainDoesNotDependOnFrameworksIoOrOuterZones, applicationDependsOnlyOnDomainAndPorts | ✔ | ✔ | P1: la prueba de arquitectura verifica que dominio/aplicación no importen infraestructura externa. |
| RNF03 | v0.1 | Puertos explícitos. Entrada: un caso de uso por operación (RF01 a RF10). Salida: repositorio de productos y proveedor de fecha… | — | portsAreInterfaces, outputAdaptersImplementPorts | ✔ | ✔ | P1: la prueba de arquitectura comprueba que los adaptadores implementan los puertos de salida. |
| RNF04 | v0.1 | La interfaz gráfica no contiene reglas de negocio. Solo recoge datos, llama a casos de uso y muestra resultados. | — | guiDoesNotTouchFilesJsonOrOutputAdapters, guiOnlyKnowsTheInputPort | NE | ✔ | |
| RNF05 | v0.1 | Pruebas unitarias (JUnit 5) del dominio y de los casos de uso, con repositorios en memoria y reloj fijo, sin archivos ni interf… | — | — | ✔ | ✔ | P1/P2 incluyen pruebas JUnit del dominio y aplicación con dobles de prueba. |
| RNF06 | v0.1 | Archivo de datos. Si no existe, se crea vacío. Si no se puede leer o escribir, se muestra un mensaje claro y la aplicación no s… | — | failedWriteThrowsClearErrorAndKeepsPreviousState, cannotCreateDataFileShowsWarningButAppKeepsRunning, ExitAtomicityTest (3) | ◐ | ✔ | P1 ejecuta pruebas de atomicidad de salida, pero no prueba persistencia de archivo. |
| RNF07 | v0.1 | Responsabilidades separadas. Ninguna clase mezcla interfaz, reglas y acceso a archivos; FEFO y caducidad existen en un solo lugar. | — | serviceIsBuiltFromInterfaces (parcial) | ◐ | ◐ | P1: pruebas de arquitectura y separación pasan; no hay una prueba directa que cubra todas las responsabilidades indicadas. |
| RNF08 | v0.2 | Archivo JSON ilegible o malformado. Se muestra un mensaje claro y la aplicación no se cierra. Antes de iniciar con datos vacíos… | — | malformedJsonKeepsBackupAndStartsEmpty, existingBackupIsNeverOverwritten | ✖ | ✔ | P1 no crea respaldo ante JSON malformado (DEF-010). |
| RNF09 | v0.2 | Guardado seguro. Se escribe primero en un archivo temporal y luego se reemplaza el archivo de datos, de modo que una escritura… | — | interruptedWriteLeavesDataFileIntact, successfulSaveLeavesNoTemporaryFile | ✖ | ✔ | P1 no usa guardado temporal seguro (DEF-009). |
| RNF10 | v0.2 | Pruebas nuevas. Pruebas unitarias del dominio y del servicio para RN14 a RN17 (creación y edición); pruebas de persistencia par… | — | (meta-requisito sobre pruebas: se verifica con la Bitácora C) | ✖ | ✔ | P1 falla validaciones nuevas; P2 ejecuta la suite con 0 fallos. |
| RNF11 | v0.2 | Ejecución de pruebas sin instalar Maven. El proyecto incluye Maven Wrapper (mvnw) para ejecutar las pruebas desde la terminal,… | — | (se verifica por existencia de `mvnw`, `mvnw.cmd` y `.mvn/`: presentes en P2 `[auto]`) | ✖ | ✔ | El wrapper está presente y se usó en P2; no consta en P1. |

Requisitos sin prueba candidata encontrada en la búsqueda por nombre `[auto]`: **P1:** RF11, RN12, la mayoría de los requisitos de interfaz y RNF08–RNF11. Esto no significa que P1 carezca de pruebas de interfaz o arquitectura: ejecutó `SwingAppTest` (3) y `HexagonalArchitectureTest` (3), pero esos casos no cubren todos esos requisitos. **P2:** RF02, RN12, RF-UI10, RF-UI12, RF-UI14, RF-UI18, RF-UI20, RF-UI23, RF-UI26 y RNF05. Las candidatas no equivalen a cobertura demostrada; por ello, no se asigna cumplimiento a un requisito sin una prueba identificable o revisión documentada.

Nota de versiones: `Especificacion_SDD_SCI_v0.1.md` contiene SDD 0.3; `Especificacion_SDD_SCI_v0.2.md` contiene SDD 0.4. Las etiquetas `v0.1`/`v0.2` de esta matriz nombran los archivos para mantener la trazabilidad con D.

## 4. Indicadores de cumplimiento (se calculan al terminar la matriz)

| Indicador | Cómo se calcula | P1 (sobre v0.1) | P1 (sobre v0.2) | P2 (sobre v0.2) |
|---|---|---|---|---|
| % de requisitos funcionales cumplidos | RF con `✔` ÷ 11 | 10/11 (90,9 %) | 10/11 (90,9 %) | 10/11 (90,9 %) |
| % de reglas de negocio cumplidas | RN con `✔` ÷ 13 (v0.1) o ÷ 17 (v0.2) | 10/13 (76,9 %) | 10/17 (58,8 %) | 16/17 (94,1 %) |
| % de requisitos de interfaz cumplidos | RF-UI con `✔` ÷ 14 (v0.1) o ÷ 26 (v0.2) | 0/14 (0 %) | 0/26 (0 %) | 13/26 (50 %) |
| % de requisitos no funcionales cumplidos | RNF con `✔` ÷ 7 (v0.1) o ÷ 11 (v0.2) | 4/7 (57,1 %) | 4/11 (36,4 %) | 10/11 (90,9 %) |
| % de casos de prueba aprobados | Aprobados ÷ ejecutados (reportes archivados) | 76/88 (86,4 %) | 76/88 (86,4 %) | 101/101 (100 %) |
| N.º de defectos abiertos por severidad | Desde la Bitácora D | 8 (7 altos, 1 medio) | 14 (12 altos, 2 medios) | 0 |

Para los indicadores de requisitos, el denominador es el total de requisitos de esa categoría aunque algunos queden `NE`; solo `✔` cuenta como cumplido y `◐` no se redondea a cumplimiento. Las columnas de P1 sobre v0.1 y v0.2 usan el mismo prototipo y la misma corrida de pruebas; cambia el conjunto de requisitos evaluado. El porcentaje de pruebas corresponde a ejecuciones observadas, no a cobertura completa de requisitos.
