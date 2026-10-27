# Sistema de Control de Inventario y Alertas (SCI) — versión 2

Aplicación de escritorio en Java 17 con arquitectura hexagonal e interfaz Swing.
Cumple la especificación SDD v0.4 (secciones 1 a 8).

## Requisitos
- Java 17 o superior (JDK). No hace falta instalar Maven: el proyecto incluye Maven Wrapper.

## Ejecutar la aplicación
- **Desde el IDE:** abrir el proyecto como proyecto Maven y ejecutar la clase `Main` (método `main`).
- **Desde la terminal:** `./mvnw compile exec:java` (Windows: `mvnw.cmd compile exec:java`).

El archivo de datos `inventory.json` se crea solo, en la carpeta desde donde se ejecuta la aplicación.

## Ejecutar las pruebas
- **Terminal:** `./mvnw test` (Windows: `mvnw.cmd test`). La primera vez descarga Maven y las dependencias.
- **IDE:** ejecutar la carpeta `src/test/java` como pruebas JUnit 5.

Las pruebas de interfaz (`SwingAppTest`) necesitan entorno gráfico; en modo headless se omiten
(no fallan). Resto de la suite: dominio, casos de uso, persistencia y arquitectura.

## Estructura
- `src/main/java/domain/`: entidades, reglas, FEFO, estados derivados y puertos (`ProductRepository`, `ClockPort`).
- `src/main/java/application/`: casos de uso (`InventoryUseCase`, `InventoryService`).
- `src/main/java/adapters/in/`: interfaz Swing (tema, componentes, formularios y las seis pantallas).
- `src/main/java/adapters/out/`: persistencia JSON (guardado seguro, copia `.bak`) y reloj de Bogotá.
- `src/test/java/`: pruebas JUnit 5 (con repositorio en memoria y reloj fijo en `testsupport/`).
- `docs/`: informe de la actualización a la versión 2.

## Datos
- Formato JSON compatible con la versión 1 (se cargan sin pérdida).
- Si `inventory.json` está dañado, se guarda una copia (`inventory.json.bak`), se avisa y se inicia vacío.
- Cada guardado escribe primero `inventory.json.tmp` y luego reemplaza el archivo de datos.
