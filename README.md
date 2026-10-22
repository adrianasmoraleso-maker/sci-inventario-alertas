# Sistema de Control de Inventario y Alertas (SCI)

Sistema desarrollado en Java 17 con Arquitectura Hexagonal y Java Swing.
Cumple con las especificaciones SDD v0.3.

## Requisitos
- Java 17 o superior.
- Maven.

## Ejecución
Puede compilar y ejecutar la clase `Main.java` desde su IDE, o usar Maven:
```
mvn clean compile exec:java -Dexec.mainClass="Main"
```

## Estructura
- `docs/`: Especificaciones SDD y decisiones del equipo.
- `src/main/java/domain/`: Entidades y reglas de negocio.
- `src/main/java/application/`: Casos de uso.
- `src/main/java/adapters/in/`: Interfaz gráfica (Swing).
- `src/main/java/adapters/out/`: Persistencia (JSON) y Reloj (Bogotá).

## Pruebas
Ejecute la suite con `mvn test`. Las pruebas de persistencia usan archivos temporales; las pruebas de arquitectura verifican las dependencias entre capas; y las pruebas de Swing requieren un entorno gráfico (se omiten en modo headless).
