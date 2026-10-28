# Bitácora F: Análisis y cierre

## 1. Codificación temática

Agrupen las observaciones y motivos de la Bitácora B en códigos. Cada código necesita al menos una cita (ID de interacción).

| Código | Definición | Interacciones que lo evidencian | Herramienta (P1 / P2) | Frecuencia |
|---|---|---|---|---|
| Aclaración del alcance | La primera respuesta requiere una precisión del equipo para producir lo esperado | INT-P0-002, INT-P1-002 | Claude / Gemini | 2 |
| Desajuste entre solicitud y salida | La respuesta inicial no conserva el carácter de actualización de P1 | INT-P0-002 | Claude | 1 |
| Iteración durante la generación | La generación se interrumpe y el equipo solicita que continúe | INT-P2-001 | Claude | 1 |
| Diferencia de calidad observada con pruebas | P1 presenta fallos documentados; la ejecución actual archivada de P2 pasa | INT-P1-001, INT-P2-001 | Gemini / Claude | 2 |

## 2. Indicadores del proceso

| Indicador | P1 Gemini | P2 Claude | Total |
|---|---|---|---|
| N.º de interacciones registradas en B | 2 | 3 | 5 |
| Salidas aceptadas | 2 | 2 | 4 |
| Salidas modificadas | 0 | 1 | 1 |
| Salidas rechazadas | 0 | 0 | 0 |
| Interacciones sobre la especificación (INT-P0) | 0 | 2 | 2 |
| Decisiones del equipo registradas en B | 2 | 3 | 5 |

## 3. Relación entre proceso y resultado

Respondan en 5 a 10 líneas cada pregunta, citando IDs.

1. **¿Qué intervenciones o decisiones del equipo se asocian con menos defectos?**  
   La actualización de requisitos después de revisar P1 hizo explícitas las validaciones de nombre, stock mínimo y umbral (INT-P0-002; SDD v0.4, RN14–RN17).  
   El prompt se corrigió para aclarar que se debía modificar la v1 y no empezar un producto nuevo (INT-P0-002).  
   La ejecución archivada actual de P1 registra 12 fallos en 88 pruebas.  
   La ejecución archivada actual de P2 registra 101 aprobadas y ninguna fallida (C, `evidencia/pruebas/P1/` y `P2/`).  
   La diferencia es una asociación observada, no prueba que una sola intervención causara el resultado.

2. **¿Qué limitaciones de cada IA se asocian con incumplimientos?**  
   Gemini entregó una versión descrita como básica (INT-P1-001); sus reportes actuales tienen 12 fallos, incluidos casos RN01 y RN14–RN16.  
   La primera respuesta de Claude a la actualización planteó un proyecto nuevo y requirió corregir el prompt (INT-P0-002).  
   Durante INT-P2-001 la generación se interrumpió dos veces y se pidió continuar.  
   No se debe afirmar que los defectos de validación persisten en P2.  
   La corrida archivada actual de P2 pasa las pruebas asociadas y totaliza 101/101.

3. **¿Qué defectos se originaron en la especificación y no en la IA? ¿Qué cambió entre versiones?**  
   No hay evidencia de que los defectos identificados se deban a ambigüedad de la especificación: RN01 ya exigía cantidades mayores que cero.  
   RF-UI16 (tablas de solo lectura) se incorporó en la SDD v0.4 después de la generación de P1.  
   Esa versión también añadió RN14–RN17, RF-UI15–RF-UI17 y requisitos adicionales de persistencia, rediseño y ejecución de pruebas.  
   D registra las brechas de P1 contra esas reglas como requisitos posteriores, no como instrucciones recibidas originalmente por Gemini.  
   P2 se generó con esos requisitos y sus pruebas actuales pasan.

4. **¿Qué aportó cada herramienta de forma distintiva y en qué falló?**  
   Gemini produjo el prototipo P1 (INT-P1-001).  
   También entregó una imagen de referencia visual tras aclararse que se pedía una imagen (INT-P1-002).  
   Claude contribuyó a la especificación y al prompt de actualización (INT-P0-001, INT-P0-002), y generó P2 (INT-P2-001).  
   La ejecución actual archivada de P2 suma 101 pruebas aprobadas.  
   Claude necesitó aclaración de alcance y continuación; los fallos del informe histórico no se atribuyen a la corrida actual.

5. **¿Qué amenazas a la validez se identifican?**  
   El mismo equipo participó en la generación, ejecución y análisis, con riesgo de sesgo de expectativa.  
   P2 se desarrolló después de P1 y reutilizó aprendizajes, requisitos y resultados de pruebas; el orden no fue contrabalanceado.  
   Las especificaciones y el alcance evolucionaron entre las versiones evaluadas.  
   Los conjuntos de pruebas no son idénticos: P2 incluye persistencia y más pruebas de interfaz/arquitectura; P1 no tiene reporte ejecutado de `JsonProductRepositoryTest`.  
   El informe narrativo headless de P2 (76 aprobadas y 25 omitidas) no equivale a la corrida Maven archivada (101 aprobadas y 0 omitidas).

6. **¿Qué se podría repetir en un estudio multicaso o comparativo?**  
   Congelar antes de generar cada prototipo la especificación, los criterios de aceptación y la versión de las herramientas.  
   Usar el mismo conjunto de pruebas automatizadas y manuales para cada caso, conservando reportes completos y entorno de ejecución.  
   Registrar prompts, iteraciones, modelo, fecha y cambios introducidos por el equipo.  
   Incorporar revisión independiente o ciega de los defectos y distinguir requisitos iniciales de los añadidos posteriormente.  
   Para comparar el efecto de especificar, incluir casos equivalentes con y sin SDD y variar el orden de las herramientas.

