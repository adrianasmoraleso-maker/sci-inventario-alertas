# Sistema de Control de Inventario y Alertas (SCI): evidencia del estudio de caso

Repositorio del estudio de caso **«Uso de la inteligencia artificial en el desarrollo de un prototipo de software mediante Spec-Driven Development (SDD): un caso de estudio»**, del equipo CodeMind Lab (Ingeniería de Software, Universidad de Cartagena, asignatura Metodología de la Investigación).

El SCI es una aplicación de escritorio en **Java 17** con **arquitectura hexagonal** e interfaz **Swing**, que gestiona productos, lotes con fecha de vencimiento, entradas y salidas con asignación FEFO y alertas de stock bajo, próximo a caducar y caducado. Es un prototipo pequeño a propósito: **su único fin es servir de objeto de estudio**, no es un producto comercial.

## Los dos prototipos

| ID | Herramienta de IA | Carpeta | Etiqueta | Especificación |
|---|---|---|---|---|
| P1 | Gemini | [`prototipos/P1-gemini-v1`](prototipos/P1-gemini-v1) | `P1` | SDD v0.1 (contenido 0.3) |
| P2 | Claude | [`prototipos/P2-claude`](prototipos/P2-claude) | `P2` | SDD v0.2 (contenido 0.4) |

Cada carpeta es un proyecto Maven completo, con su código y sus pruebas JUnit 5. Los archivos de especificación del repositorio son `especificacion/Especificacion_SDD_SCI_v0.1.md` y `especificacion/Especificacion_SDD_SCI_v0.2.md`; sus contenidos declaran las versiones SDD 0.3 y 0.4, respectivamente.

Las etiquetas fijan cada versión: para ver el código exacto de un prototipo, abran la etiqueta en GitHub (`P1` o `P2`) o ejecuten `git checkout P1`.

## Requisitos

- Java 17 o superior (JDK); las ejecuciones de evidencias archivadas se realizaron con Java 25.0.2, pero el proyecto compila y se ejecuta con nivel 17.
- IntelliJ IDEA (o cualquier IDE con soporte Maven).
- Maven: solo para P1. **P2 incluye Maven Wrapper** (`mvnw`, `mvnw.cmd`) y no necesita instalarlo.

## Cómo ejecutar

Abran la carpeta del prototipo (no la raíz del repositorio) como proyecto Maven en el IDE y ejecuten la clase `Main`. También desde la terminal:

```powershell
# P1 (Gemini)
cd prototipos\P1-gemini-v1
mvn clean compile exec:java -Dexec.mainClass="Main"

# P2 (Claude)
cd prototipos\P2-claude
.\mvnw.cmd compile exec:java
```

En Linux o macOS, para P2: `./mvnw compile exec:java`.

La aplicación crea `inventory.json` en la carpeta desde donde se ejecuta. Ese archivo es el de datos y no se sube al repositorio (`.gitignore`).

## Cómo ejecutar las pruebas

```powershell
# P1 (Gemini)
cd prototipos\P1-gemini-v1
mvn test

# P2 (Claude)
cd prototipos\P2-claude
.\mvnw.cmd test
```

También desde IntelliJ, ejecutando la carpeta `src/test/java` como pruebas JUnit 5. Las pruebas de interfaz (`SwingAppTest`) necesitan entorno gráfico y se omiten en modo headless.

Los resultados de cada ejecución, con fecha y quién la hizo, están en [`bitacoras/C-verificacion-trazabilidad.md`](bitacoras/C-verificacion-trazabilidad.md) y en `evidencia/pruebas/`. Las pruebas de P1 y de P2 **no son idénticas**; la Bitácora C explica las diferencias.

## Estructura del repositorio

```
prototipos/
  P1-gemini-v1/        Prototipo 1 (Gemini): código y pruebas
  P2-claude/           Prototipo 2 (Claude): código y pruebas
especificacion/        Especificaciones SDD v0.1 y v0.2
bitacoras/             Bitácoras A-F del estudio de caso
evidencia/
  pruebas/
    P1/                Reportes JUnit del prototipo 1
    P2/                Reportes JUnit del prototipo 2
  capturas-gui/        Capturas de la interfaz de P1
```

Dentro de cada prototipo:

- `src/main/java/domain/`: entidades y reglas de negocio (FEFO, caducidad, estados).
- `src/main/java/application/`: casos de uso (`InventoryUseCase`, `InventoryService`).
- `src/main/java/adapters/in/`: interfaz gráfica Swing (adaptador de entrada).
- `src/main/java/adapters/out/`: persistencia en JSON y reloj de Bogotá (adaptadores de salida).
- `src/test/java/`: pruebas JUnit 5.

La rama `pruebas-junit` conserva el historial de las pruebas originales de P1.

## Bitácoras

| Bitácora | Contenido |
|---|---|
| [A](bitacoras/A-ficha-caso-y-prototipos.md) | Ficha del caso, desviaciones respecto al proyecto y ficha de cada prototipo |
| [B](bitacoras/B-interacciones-ia.md) | Registro de interacciones con la IA: instrucción, salida y decisión del equipo |
| [C](bitacoras/C-verificacion-trazabilidad.md) | Pruebas, resultados y matriz de trazabilidad especificación-prueba-resultado |
| [D](bitacoras/D-defectos.md) | Defectos: severidad, origen y prototipos en que aparecen |
| [E](bitacoras/E-comparacion-herramientas.md) | Comparación entre Gemini y Claude |
| [F](bitacoras/F-analisis-cierre.md) | Análisis, codificación temática y cierre |

## Herramientas de IA utilizadas

| Herramienta | Uso en el caso |
|---|---|
| Gemini | Generación del código del prototipo P1 dentro del estudio de caso. |
| Claude | Generación del código del prototipo P2 dentro del estudio de caso. |
| Copilot | Asistencia documental, revisión y normalización del repositorio. |

La Bitácora B registra el contexto de las interacciones y decisiones del equipo; la evidencia de la ejecución de pruebas queda en `evidencia/pruebas/`.

## Limitaciones

El equipo es a la vez ejecutor y analista del caso. Es un estudio de caso único, descriptivo-evaluativo, y sus resultados no son generalizables más allá de este caso. Las diferencias entre los prototipos pueden deberse a la herramienta, pero también a la especificación entregada, a la información previa que recibió cada IA y a que los conjuntos de pruebas no son equivalentes; esos factores se registran en la Bitácora E.

## Cómo citar


> Equipo CodeMind Lab. (2026). Evidencia del estudio de caso: desarrollo del SCI mediante Spec-Driven Development con apoyo de IA (v1.0) [Conjunto de datos]. Zenodo. https://doi.org/10.5281/zenodo.23720620

## Autoría

- Equipo CodeMind Lab, Programa de Ingeniería de Software, Universidad de Cartagena.
- Asistencia documental y revisión de redacción: Copilot.
- Autoría de las pruebas JUnit y validación de resultados: Adriana Morales, Said Martelo y el equipo de ejecución que registró la evidencia del caso.
- Separación de responsabilidades: la asistencia documental no implica autoría de pruebas ni validación del resultado de ejecución; esas tareas corresponden al equipo del estudio.
