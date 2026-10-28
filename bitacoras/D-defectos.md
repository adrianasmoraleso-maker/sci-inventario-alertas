# Bitácora D: Registro de defectos



## Defectos del Prototipo 1 Gemini

Las referencias «v0.1» y «v0.2» identifican los archivos de especificación del repositorio (que contienen las versiones SDD 0.3 y 0.4, respectivamente). DEF-001 a DEF-005 conservan los identificadores del catálogo de la Bitácora B. En B, «v2 (informe de pruebas)» identifica cuándo se detectaron DEF-003–005; los reportes actuales de P1 también muestran esos fallos, mientras que P2 pasa los casos correspondientes. Los requisitos incorporados en la SDD 0.4 son posteriores a la generación de P1: se registran como brechas al contrastar P1 con esa versión, no como requisitos que Gemini hubiera recibido originalmente. Las rutas `P1/...` y `P2/...` de la columna Evidencia son relativas a `evidencia/pruebas/`.

| ID | Requisito (spec) | Descripción y dónde mirar | Evidencia primaria (ruta/fuente) | Resultado registrado | Severidad | Origen | P1 | P2 |
|---|---|---|---|---|---|---|---|---|
| DEF-001 | RN01 (v0.1) | `Product.processExit` de P1 no protege la regla ante una cantidad cero o negativa si se invoca directamente. El caso de uso `InventoryService.registerExit` sí rechaza ambas cantidades; por ello, no se afirma que la interfaz permita completar esas salidas. | `P1/surefire-reports/domain.ProductTest.txt` (`shouldRejectExitWithZeroQuantity`, `shouldRejectExitWithNegativeQuantity`); `P1/surefire-reports/application.InventoryServiceTest.txt`; `P2/TEST-domain.ProductTest.xml` | P1: fallan 2 pruebas directas de dominio, mientras las equivalentes del servicio pasan. P2: la prueba de dominio y las del servicio pasan. |Media |Salida incorrecta de la IA | X | — |
| DEF-002 | RF-UI16 (v0.2) | Las tablas de P1 se crean con modelos editables por defecto, sin sobrescribir `isCellEditable`; el equipo señaló este comportamiento en INT-P0-002 y luego se formalizó como regla de solo lectura. | Revisión de `P1-gemini-v1/src/main/java/adapters/in/SwingApp.java`; `P2/TEST-adapters.in.SwingAppTest.xml` (`noTableCellIsEditableInAnyScreen`) | P1: revisión de código; P2: prueba aprobada. |Alta |Problema observado por el equipo (INT-P0-002) | X | — |
| DEF-003 | RN14 (v0.2) | El constructor de `Product` de P1 no rechaza nombres vacíos o de solo espacios (`Product.java`); el paquete inicial también contiene un producto con nombre vacío. | `P1/surefire-reports/domain.ProductTest.txt` (`shouldRejectBlankProductName`); `P2/TEST-domain.ProductTest.xml` y `P2/TEST-application.InventoryServiceTest.xml` | P1: prueba fallida; P2: pruebas de creación/edición aprobadas. |Alta |Requisito agregado en SDD v0.4, posterior a P1 | X | — |
| DEF-004 | RN15 (v0.2) | P1 no rechaza el stock mínimo negativo al crear ni al editar un producto. | `P1/surefire-reports/domain.ProductTest.txt` (`shouldRejectNegativeMinimumStock`, `shouldRejectNegativeMinimumStockWhenUpdating`); `P1/surefire-reports/application.InventoryServiceTest.txt` (`shouldRejectNegativeMinimumStockWhenEditingProduct`); reportes P2 de `domain.ProductTest` e `InventoryServiceTest` | P1: 3 pruebas fallidas; P2: pruebas aprobadas. |Alta |Requisito agregado en SDD v0.4, posterior a P1 | X | — |
| DEF-005 | RN16 (v0.2) | P1 no rechaza el umbral de alerta cero o negativo al crear ni al editar. | `P1/surefire-reports/domain.ProductTest.txt` y `P1/surefire-reports/application.InventoryServiceTest.txt` (casos `shouldRejectZeroAlertThreshold*` y `shouldRejectNegativeAlertThreshold*`); reportes P2 de `domain.ProductTest` e `InventoryServiceTest` | P1: 6 fallos de RN16 (pruebas de creación y edición); los fallos de RN14 y RN15 se detallan en sus filas. P2: pruebas aprobadas. |Alta |Requisito agregado en SDD v0.4, posterior a P1 | X | — |
| DEF-006 | RF-UI15 (v0.2) | En P1, el texto no numérico llega a `Integer.parseInt` sin validación previa y puede producir una excepción en la interfaz. Es distinto del defecto de cantidades cero/negativas de DEF-001. | Revisión de `P1-gemini-v1/src/main/java/adapters/in/SwingApp.java`; `P2/TEST-adapters.in.SwingAppTest.xml` (validaciones de texto/decimal) | P1: revisión de código; P2: pruebas de formulario aprobadas. |Media |Requisito agregado en SDD v0.4, posterior a P1 | X | — |
| DEF-007 | RF-UI05 (v0.1) | La salida de P1 no muestra la asignación FEFO por lote ni el aviso de stock bajo; `Product.processExit` no devuelve la asignación. | Revisión de `P1-gemini-v1/src/main/java/domain/Product.java` y `adapters/in/SwingApp.java`; `prototipos/P2-claude/docs/INFORME_ACTUALIZACION_V2.md`; `P2/TEST-adapters.in.SwingAppTest.xml` | P1: revisión de código e informe de actualización; P2: suite de interfaz aprobada. |Alta |Salida incorrecta de la IA | X | — |
| DEF-008 | RF-UI14 (v0.1) | P1 muestra fechas de alerta en formato ISO en lugar de dd/mm/aaaa. | Revisión de `P1-gemini-v1/src/main/java/application/InventoryService.java`; `prototipos/P2-claude/docs/INFORME_ACTUALIZACION_V2.md`; `P2/TEST-application.InventoryServiceTest.xml` | P1: revisión de código e informe de actualización; P2: suite de servicio aprobada. |Alta |Salida incorrecta de la IA | X | — |
| DEF-009 | RNF09 (v0.2) | El repositorio de P1 escribe directamente sobre el archivo de datos, sin guardado temporal y reemplazo seguro. | Revisión de `P1-gemini-v1/src/main/java/adapters/out/JsonProductRepository.java`; `P2/TEST-adapters.out.JsonProductRepositoryTest.xml` | P1: revisión de código (no hay suite de repositorio ejecutada); P2: pruebas de guardado aprobadas. |Alta |Requisito agregado en SDD v0.4, posterior a P1 | X | — |
| DEF-010 | RNF08 (v0.2) | P1 no conserva una copia `.bak` cuando el JSON está malformado. | Revisión de `P1-gemini-v1/src/main/java/adapters/out/JsonProductRepository.java`; `P2/TEST-adapters.out.JsonProductRepositoryTest.xml` | P1: revisión de código (no hay suite de repositorio ejecutada); P2: pruebas de JSON malformado y respaldo aprobadas. |Alta |Requisito agregado en SDD v0.4, posterior a P1 | X | — |
| DEF-011 | RF-UI12 (v0.1) | En P1, las operaciones reconstruyen las pantallas y se pierden filtros y selección. | `prototipos/P2-claude/docs/INFORME_ACTUALIZACION_V2.md`; revisión de `P1-gemini-v1/src/main/java/adapters/in/SwingApp.java`; `P2/TEST-adapters.in.SwingAppTest.xml` | P1: informe de actualización y revisión de código; P2: suite de interfaz aprobada. |Alta |Salida incorrecta de la IA | X | — |
| DEF-012 | RN04 (v0.1) | El informe de actualización de P2 señala que la obligatoriedad de fecha del lote no se rechazaba en todos los caminos de P1. Este defecto histórico es distinto de los dos fallos RN01 registrados en DEF-001. | `prototipos/P2-claude/docs/INFORME_ACTUALIZACION_V2.md`; revisión de `P1-gemini-v1/src/main/java/domain/Lot.java`; `P2/TEST-domain.LotTest.xml` | P1: informe de actualización/revisión de código; P2: pruebas de fecha obligatoria aprobadas. |Alta |Salida incorrecta de la IA | X | — |
| DEF-013 | RNF06 (v0.1) | Si falla el guardado en P1, el estado en memoria ya había cambiado. | `prototipos/P2-claude/docs/INFORME_ACTUALIZACION_V2.md`; revisión de `P1-gemini-v1/src/main/java/application/InventoryService.java`; `P2/TEST-application.ExitAtomicityTest.xml` y `P2/TEST-adapters.out.JsonProductRepositoryTest.xml` | P1: informe de actualización/revisión de código; P2: pruebas de atomicidad y persistencia aprobadas. |Alta |Salida incorrecta de la IA | X | — |
| DEF-014 | RF03 (v0.1) | Editar un producto en P1 lo mueve al final del archivo de persistencia. | `prototipos/P2-claude/docs/INFORME_ACTUALIZACION_V2.md`; revisión de `P1-gemini-v1/src/main/java/adapters/out/JsonProductRepository.java`; `P2/TEST-adapters.out.JsonProductRepositoryTest.xml` | P1: informe de actualización/revisión de código; P2: pruebas de persistencia aprobadas. |Alta |Salida incorrecta de la IA | X | — |



## Limitaciones declaradas del Prototipo 2 Claude (candidatos a defecto de P2)


| ID | Requisito | Descripción | Fuente | Evidencia | Severidad | Origen |
|---|---|---|---|---|---|---|
| DEF-015 | — | El informe de actualización indica que los botones de fila de P2 se manejan con el ratón, no con teclado. No se cuenta como incumplimiento confirmado: la SDD no exige explícitamente uso por teclado. | `prototipos/P2-claude/docs/INFORME_ACTUALIZACION_V2.md` | Limitación declarada, sin requisito incumplido identificado. |Baja |No aplica / limitación declarada |
| DEF-016 | RF-UI15 | P2 valida los campos numéricos al guardar en vez de bloquear teclas; no es defecto porque RF-UI15 permite expresamente rechazar o bloquear valores inválidos. | `prototipos/P2-claude/docs/INFORME_ACTUALIZACION_V2.md`; `P2/TEST-adapters.in.SwingAppTest.xml` | No contar como defecto: el enfoque implementado está permitido por la SDD y las pruebas de formulario pasan. |Baja |No es incumplimiento |


## Resumen 

| | P1 Gemini | P2 Claude |
|---|---|---|
| Total de defectos presentes | 14 | 0 |
| Críticos |0 | 0|
| Altos |12 |0 |
| Medios |2 |0 |
| Bajos |0 |0 |
| Defectos de P1 que persisten en P2 | n/a |0 |
| Defectos nuevos en P2 | n/a |0 |
| Brechas nuevas de SDD v0.4 no descritas previamente en B |6 |0|
| Defectos de salida de la IA frente a requisitos recibidos |7 |0|
| Problemas observados y formalizados por el equipo |1 |0 |
