# Bitácora A: Ficha del caso y fichas por prototipo

Equipo CodeMind Lab · Metodología de la Investigación · Ingeniería de Software, Universidad de Cartagena

**Prototipos del caso**

| ID | Herramienta | Carpeta | Etiqueta |
|---|---|---|---|
| P1 | Gemini | `prototipos/P1-gemini-v1` | `P1` |
| P2 | Claude (versión final) | `prototipos/P2-claude` | `P2` |

## 1. Ficha del caso

| Campo | Registro |
|---|---|
| Título del estudio | Uso de la inteligencia artificial en el desarrollo de un prototipo de software mediante Spec-Driven Development (SDD): un caso de estudio |
| Pregunta de investigación | ¿Cómo se incorpora la IA en el proceso de desarrollo de un prototipo de software mediante SDD y en qué medida el prototipo resultante cumple las especificaciones definidas? (tomada del artículo; verificar contra el FO-IN-001) |
| Prototipo objeto de estudio | Sistema de Control de Inventario y Alertas (SCI): Java 17, Swing, FEFO, arquitectura hexagonal, persistencia en archivo local (JSON) |
| Repositorio de GitHub | https://github.com/adrianasmoraleso-maker/sci-inventario-alertas |
| Especificación SDD | Versiones 0.3 y 0.4. Archivos `especificacion/Especificacion_SDD_SCI_v0.1.md` y `especificacion/Especificacion_SDD_SCI_v0.2.md`; sus contenidos declaran las versiones SDD 0.3 y 0.4, respectivamente. La carpeta está actualmente sin versionar en Git |
| Herramientas y modelo exacto | Gemini: Gemini 3.1 Pro (14/10/2026, implementación) y Gemini 3.6 Flash (22/10/2026, imagen de GUI). Claude: Claude Sonnet 5.5, nivel medio (10/10/2026 y 22/10/2026). Planes: Gemini Pro, Claude gratuito |
| Entorno de pruebas | IntelliJ IDEA 2026.2.3 (Build 262.10968.63), JUnit 5.10.2, Windows. Ejecución con Maven: Apache Maven 3.9.9 (obtenido mediante el Maven Wrapper de P2); Java 25.0.2, Eclipse Adoptium jdk-25.0.2.10-hotspot; Windows 10 amd64; codificación de plataforma UTF-8; configuración regional en_US. Proyecto com.sci:inventory-system. El equipo anotó 17:43 (UTC-5) como hora de ejecución del 27/10/2026; los logs archivados terminan a las 17:50 para P1 y 18:57 para P2. Nota: el informe del 22/10/2026 indica que ese día las suites se corrieron desde el IDE porque Maven no estaba disponible. La especificación requiere JDK 17 o superior |
| Autor de las pruebas JUnit | Adriana Morales y Said Alejandro |
| Fecha de inicio y fin del registro | Inicio: 10/10/2026. Fin: 27/10/2026 |
| Criterios de comparación congelados | 27/10/2026, aprox. 18:00 según el registro del equipo. El log archivado de P2 termina a las 18:57; confirmar si 17:43 corresponde al inicio de esa ejecución antes de afirmar que los criterios se congelaron después de ejecutarla |

### Desviaciones respecto al protocolo del FO-IN-001

| Fecha | Qué cambió respecto al FO-IN-001 | Motivo | Quién lo decidió | Impacto |
|---|---|---|---|---|
| 22/10/2026 | Se estudian dos prototipos (P1 Gemini y P2 Claude) en lugar de una primera versión y una versión corregida con Gemini | Comparar cuál herramienta funcionaba mejor | El equipo | Cambia el alcance y la comparabilidad: se comparan dos herramientas (Gemini y Claude) en lugar de una versión inicial y una corregida con la misma herramienta |
| 22/10/2026 | No se usó ChatGPT en los prototipos (solo como prueba exploratoria); el FO-IN-001 le asignaba la imagen de referencia de la GUI | ChatGPT se probó solo para ver si era mejor que Gemini o Claude; la imagen de referencia la generó Gemini (INT-P1-002) | El equipo | No afecta a P1 ni a P2: ChatGPT no intervino en ningún prototipo |
| 22/10/2026 | La interfaz gráfica la generó Gemini (confirmar si también en P2) | En P2 la interfaz la generó Claude, como parte de generar el prototipo (ver fila siguiente), siguiendo la imagen de referencia de Gemini | El equipo | Confirmado en Bitácora B: en P1 la GUI y la imagen de referencia las hizo Gemini (INT-P1-001, INT-P1-002); en P2 la GUI la generó Claude (INT-P2-001) siguiendo la imagen de Gemini |
| 22/10/2026 | Claude, además de redactar requisitos, generó el prototipo final P2 (confirmar) | El prototipo de Gemini (P1) era deficiente e incumplía requerimientos | El equipo | Confirmado (INT-P2-001): Claude entregó la versión 2. El mismo modelo redactó los requisitos (INT-P0-002) y generó P2; afecta la comparabilidad |
| 27/10/2026 | Los criterios de comparación (Bitácora E) se registraron como congelados alrededor de las 18:00. El log archivado de P2 termina a las 18:57; falta confirmar si 17:43 fue el inicio de esa ejecución para establecer si los criterios se fijaron durante o después de la corrida | Falta de tiempo: el equipo concluye el estudio el 27/10/2026 | El equipo | La cronología debe aclararse antes de afirmar que los criterios se fijaron después de conocer el resultado final de P2; en cualquier caso, no se congelaron antes de iniciar las pruebas |

## 2. Ficha del prototipo P1 (Gemini)

| Campo | Registro |
|---|---|
| Herramienta y modelo | Gemini 3.1 Pro (implementación, INT-P1-001); Gemini 3.6 Flash (imagen de GUI, INT-P1-002) |
| Fecha de generación | 14/10/2026 (INT-P1-001) |
| Enlaces a los chats | https://gemini.google.com/share/941418b42705?social_preview=false&skid=400bd28a-a9aa-40d0-87ba-aa8e377825df (INT-P1-001); https://gemini.google.com/share/d/1jQxbI4ZUr1uXprgh59U8G1SQVBQyUZ6r?usp=sharing (INT-P1-002) |
| Chats exportados | Pendiente |
| Código en GitHub | `prototipos/P1-gemini-v1`, etiqueta `P1`. Commit: `498cb73a9006b9f48275eb81b2f1855ebb5d7c2f` |
| Entrada recibida | Documento de especificación resultante de INT-P0-001 y la estructura de carpetas indicada en el prompt; sin imagen de GUI. Texto exacto en Bitácora B, INT-P1-001 |
| N.º de interacciones hasta P1 | 1 (INT-P1-001). INT-P1-002 (imagen de GUI, 22/10/2026) se generó después de P1 y se usó como entrada de P2 |
| ¿Compila y corre desde IntelliJ sin cambios del equipo? | El informe de pruebas del 22/10/2026 indica que el build del IDE finalizó correctamente y que las suites se ejecutaron desde IntelliJ. Sí, sin cambios del equipo |
| Cambios del equipo sobre lo entregado por la IA | Ninguno |
| Pruebas | 6 archivos de prueba JUnit 5 en el prototipo evaluado, escritos por el equipo. El `ProductTest.java` original (1 prueba) fue reemplazado; el original queda en el commit `8368776` |
| Estado resumido | Informe histórico del equipo (22/10/2026): 92 pruebas, 82 aprobadas y 10 fallidas; esa corrida no debe confundirse con la evidencia Maven archivada del 27/10/2026, que registra 88 pruebas, 76 aprobadas y 12 fallidas. Las dos fallas adicionales actuales son pruebas directas de dominio RN01; el servicio rechaza cantidades no positivas. Las 10 fallas RN14–RN16 corresponden a requisitos agregados después de generar P1. La revisión manual histórica también señaló campos numéricos sin validar y tablas editables; consultar Bitácoras C y D para la clasificación y trazabilidad actuales |
| Observaciones |  |

## 3. Ficha del prototipo P2 (Claude)

| Campo | Registro |
|---|---|
| Herramienta y modelo | Claude Sonnet 5.5, nivel medio |
| Fecha de generación | 22/10/2026 (INT-P2-001) |
| Enlaces a los chats | https://claude.ai/share/11508f6c-db1b-43a6-8399-34885a743f54 (INT-P2-001); https://claude.ai/share/1431cafe-a1c0-4e5a-bf2e-24a99097891a (requisitos y prompt, INT-P0-001 e INT-P0-002) |
| Chats exportados | Pendiente |
| Código en GitHub | `prototipos/P2-claude`, etiqueta `P2`, commit `4a8e528` |
| Entrada recibida | 4 archivos: versión 1 del proyecto, requisitos actualizados, informe de pruebas de la v1 y el texto generado en INT-P0-002. Texto exacto en Bitácora B, INT-P2-001 |
| ¿Recibió información de los defectos de P1? | Sí: informe de pruebas de la v1 y dos defectos listados en el prompt (números negativos y lista editable) |
| N.º de interacciones hasta P2 | 1 (INT-P2-001), más 1 de actualización de requisitos (INT-P0-002). Se detuvo dos veces y se le indicó "continuar" |
| ¿Compila y corre desde IntelliJ sin cambios del equipo? | Sí, sin cambios del equipo |
| Cambios del equipo sobre lo entregado por la IA | Ninguno: el código solo fue modificado por la IA |
| Pruebas | 9 archivos en `src/test/java`: los 7 de P1 más `testsupport/FixedClock.java` y `testsupport/InMemoryProductRepository.java`. Sí se adaptaron a P2: las adaptó Said Alejandro y Adriana sube todo al repositorio |
| Contenido de `docs/` | `.gitkeep` e `INFORME_ACTUALIZACION_V2.md`. En la raíz de P2 también están `mvnw`, `mvnw.cmd` y `pom.xml`. Quién lo generó: Claude (INT-P2-001); subido al repositorio por el equipo |
| Estado resumido | El informe previo de actualización describe 76 pruebas aprobadas y 25 de interfaz omitidas en modo headless; corresponde a otra corrida. El informe Maven archivado de la ejecución del equipo registra 101 pruebas, 101 aprobadas, 0 fallidas, 0 errores y 0 omitidas (`evidencia/pruebas/P2/test-run.txt`, finalizada a las 18:57 del 27/10/2026). Inspección estática del informe de actualización: 43 clases de producción y 3.235 líneas |
| Observaciones | Claude se detuvo dos veces durante la generación |
