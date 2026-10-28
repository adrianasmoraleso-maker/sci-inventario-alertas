# Bitácora E: Comparación de herramientas (Gemini vs Claude)

Como el equipo es ejecutor y analista a la vez, **los criterios y la escala debían fijarse antes de ejecutar las pruebas de P2**. En este caso se registraron alrededor de las 18:00 del 27/10/2026, pero el log Maven de P2 termina a las 18:57 y la hora 17:43 se anotó como referencia de la ejecución. La cronología debe confirmarse; no afirmar que los criterios se congelaron después de conocer el resultado final hasta aclarar si 17:43 fue el inicio o el fin de la corrida.

**Fecha de congelación de los criterios:** 27 / 10 / 2026 (aprox. 18:00, según el registro del equipo; cronología respecto al resultado final de P2 pendiente de confirmar).

## 1. Criterios

| Criterio | Cómo se mide | Fuente de datos |
|---|---|---|
| Cumplimiento de la especificación | % de RF y RN cumplidos | Bitácora C |
| Defectos presentes | N.º y severidad | Bitácora D |
| Persistencia de defectos de P1 en P2 | Defectos que se arrastran | Bitácora D |
| Defectos nuevos | Defectos introducidos en P2 | Bitácora D |
| Esfuerzo de iteración | N.º de interacciones hasta la versión evaluada | Bitácora B |
| Intervención del equipo | % de salidas aceptadas, modificadas o rechazadas | Bitácora B |
| Calidad arquitectónica | Cumplimiento de la arquitectura hexagonal y de los RNF | Bitácora C |
| Fidelidad de la interfaz | Cumplimiento de RI y rúbrica de estética | Bitácora C |
| Ejecutabilidad | ¿Compila y corre en IntelliJ sin cambios del equipo? | Bitácora A |

La evaluación se mide mediante conteos directos, porcentajes de cumplimiento sobre las matrices de pruebas y la tasa de defectos identificados.

## 2. Resultados comparativos

| Criterio | P1 Gemini | P2 Claude | Evidencia |
|---|---|---|---|
| Cumplimiento de la especificación | Sobre v0.2: RF 10/11 (90,9 %), RN 10/17 (58,8 %), RF-UI 0/26 (0 %), RNF 4/11 (36,4 %). Sobre v0.1: RN 10/13 (76,9 %), RF-UI 0/14, RNF 4/7 (57,1 %) | Sobre v0.2: RF 10/11 (90,9 %), RN 16/17 (94,1 %), RF-UI 13/26 (50 %), RNF 10/11 (90,9 %) | Bitácora C, sección 4. Solo `✔` cuenta; lo no evaluable (NE) cuenta como no cumplido. P1 tuvo 3 pruebas automatizadas Swing y verificaciones manuales reportadas, pero estas últimas no tienen registro detallado ni evidencia archivada |
| Defectos presentes (por severidad) | 14 defectos sobre v0.2: 0 críticos, 12 altos, 2 medios, 0 bajos. Sobre v0.1: 8 (7 altos, 1 medio) | 0 defectos confirmados; D registra 2 limitaciones/candidatos de severidad baja (DEF-015 y DEF-016), que no se cuentan como defectos | Bitácoras C y D |
| Persistencia de defectos | n/a | 0 reportados en D; la columna P2 usa `—` para indicar que no se registran como presentes. P2 obtuvo 101 aprobadas en la ejecución Maven archivada | Bitácoras C y D |
| Defectos nuevos | n/a | 0 confirmados; DEF-015 y DEF-016 son limitaciones/candidatos no contados como incumplimientos | Bitácora D |
| Esfuerzo de iteración | 1 interacción (INT-P1-001). La imagen de GUI (INT-P1-002) se generó después y alimentó P2 | 1 interacción (INT-P2-001), más 1 de actualización de requisitos (INT-P0-002) | Bitácora B |
| Intervención del equipo | 1 de 1 aceptada (100%); 0 modificadas; 0 rechazadas | 1 de 1 aceptada (100%); 0 modificadas; 0 rechazadas. INT-P0-002 (requisitos) fue modificada | Bitácora B |
| Calidad arquitectónica | RNF01–RNF03 y RNF05 cumplen; RNF04 no evaluable; RNF06 y RNF07 parciales. Sin importaciones prohibidas en dominio ni aplicación | RNF01–RNF06 cumplen (y RNF08–RNF11); RNF07 parcial. 7 pruebas de arquitectura aprobadas | Bitácora C, matriz RNF01–RNF07 |
| Fidelidad de la interfaz | 0/26 cumplidos: 3 parciales, 5 incumplidos (RF-UI05, 12, 14, 15 y 16) y 18 no evaluables | 13/26 cumplidos, 6 parciales y 7 no evaluables | Bitácora C, matriz RF-UI. Rúbrica de estética: no evaluada |
| Ejecutabilidad | Sí: compila y corre desde IntelliJ sin cambios del equipo. Maven (27/10/2026): 88 pruebas ejecutadas, 76 aprobadas, 12 fallidas | Sí: compila y corre desde IntelliJ sin cambios del equipo. Maven (27/10/2026): 101 ejecutadas, 101 aprobadas | Bitácoras A y C |

## 3. Factores que podrían explicar las diferencias (amenazas a la validez)

Antes de atribuir una diferencia a la herramienta, registren qué más cambió entre P1 y P2.

| Factor | P1 Gemini | P2 Claude | Comentario |
|---|---|---|---|
| Entrada entregada a la IA (¿igual o más detallada?) | Documento de especificación (INT-P0-001) y estructura de carpetas indicada en el prompt | Más detallada: versión 1, requisitos actualizados, informe de pruebas y prompt generado en INT-P0-002 | P2 recibió más contexto que P1 |
| Información de los defectos de P1 que ya conocía el equipo | Ninguna (primera versión) | Informe de pruebas de la v1 y dos defectos conocidos (números negativos y lista editable) | Ventaja de P2 |
| Versión o modelo de la IA y fecha de uso | Gemini 3.1 Pro (14/10/2026); Gemini 3.6 Flash para la GUI (22/10/2026) | Claude Sonnet 5.5, nivel medio (22/10/2026) | Modelos de distinta generación y tamaño |
| Plan y capacidades usadas (gratuito, razonamiento extendido, archivos adjuntos) | Gemini Pro; 1 archivo adjunto (especificación) | Claude gratuito, Sonnet 5.5 nivel medio; 4 archivos adjuntos | Planes distintos: Gemini de pago y Claude gratuito |
| Ajustes manuales del equipo antes de evaluar | Ninguno en el código | Ninguno en el código; Said adaptó las pruebas a P2 | El código de P2 solo lo modificó la IA |
| ¿Las pruebas JUnit son idénticas en los dos prototipos? | El paquete inicial incluía 3 archivos y 73 pruebas; la ejecución archivada actual abarca 6 suites y 88 pruebas | 7 suites y 101 pruebas; 12 nombres de prueba coinciden con P1 | No son idénticas: P2 incluye pruebas de persistencia, arquitectura e interfaz que P1 no tiene; C, sección 1, lista las suites y los conteos de la ejecución evaluada |
| Imagen o descripción de la GUI recibida | Sin imagen en la implementación inicial; la imagen de referencia se generó después (INT-P1-002) | A registra que P2 siguió la imagen generada en INT-P1-002; no consta si se adjuntó de nuevo como archivo separado en INT-P2-001 | Bitácoras A y B; el modo exacto de entrega de la imagen a P2 no está documentado |

P2 se desarrolló después de conocer los problemas de P1, lo que puede favorecerlo; debe quedar escrito como limitación.

## 4. Conclusiones

A partir de los resultados observados, **P2 presenta mejores resultados registrados que P1 en esta comparación**, pero no se puede atribuir causalmente la diferencia solo a la herramienta:

1. **Mayor cumplimiento observado en P2:** P2 registró porcentajes superiores a P1 en reglas de negocio (94,1 % frente al 58,8 %), requisitos no funcionales (90,9 % frente al 36,4 %) y requisitos de interfaz (50 % frente al 0 %). Son resultados descriptivos de estos prototipos, no una estimación estadística ni una comparación aislada entre herramientas.
2. **Resultados de pruebas y defectos:** En las ejecuciones Maven archivadas, P2 aprobó 101/101 pruebas y P1 76/88; D registra 0 defectos confirmados en P2 y 14 en P1 contra v0.2. Los conjuntos de pruebas no son idénticos y P2 recibió más contexto, incluidos requisitos actualizados y problemas conocidos de P1.

Por tanto, estos datos describen los prototipos y las condiciones concretas de este caso; no demuestran que Claude sea en general más precisa ni que P2 esté libre de fallas. La evolución de la especificación, el orden de desarrollo, las diferencias entre suites y la cronología de congelación de criterios limitan la comparación.