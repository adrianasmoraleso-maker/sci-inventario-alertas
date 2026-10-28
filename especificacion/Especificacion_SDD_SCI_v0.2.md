# Especificación SDD: Sistema de Control de Inventario y Alertas (SCI)

Versión 0.4 (simplificada). Define un sistema de inventario pequeño en Java con un único fin: evaluar la calidad general del software desarrollado con inteligencia artificial.

## 1. Restricciones técnicas

- [ ] **Lenguaje:** Java 17 o superior.

**Tipo de aplicación:** escritorio con interfaz gráfica hecha con Swing. No es una aplicación web: sin servidor HTTP, sin API REST, sin páginas web.

- [ ] **Ejecución:** desde el mismo IDE, con una clase Main que tenga el método main y sin comandos ni configuración manual. Si se usa Maven o Gradle, las dependencias se descargan solas al abrir el proyecto. Lo ideal es usar solo el JDK.

- [ ] **Persistencia:** un archivo local (JSON, TXT o CSV, a elección de la IA) que hace de base de datos. Se crea solo en la primera ejecución, dentro de la carpeta del proyecto. No se usa ningún motor de base de datos.

- [ ] **Sin** autenticación, red ni concurrencia.

## 2. Alcance

**Dentro:** productos con CRUD completo; lotes con fecha de vencimiento; entradas adicionales; salidas con asignación FEFO automática; consulta de existencias; alertas de stock bajo, próximo a caducar y caducado; eliminación de lotes caducados. Un único actor: el operador de inventario.

**Fuera:** usuarios y roles, varios almacenes, precios, proveedores, historial de movimientos, reportes, notificaciones, multiidioma, versión web o API.

**Modelo de datos:** una sola entidad, **Producto**, con código, nombre, stock mínimo, umbral de alerta y su lista de lotes. Cada lote es un objeto de valor dentro del producto, sin identidad propia: código de lote, fecha de vencimiento y saldo. Todo el inventario se guarda en un único archivo, con un registro por producto. Las alertas y las existencias se calculan y no son entidades.

## 3. Requisitos funcionales

| ID   | Requisito                                                                                                                               |
|------|-----------------------------------------------------------------------------------------------------------------------------------------|
| RF01 | Crear un producto con código único, nombre, stock mínimo y umbral de alerta de caducidad en días (por defecto 180).                     |
| RF02 | Listar productos y consultar uno.                                                                                                       |
| RF03 | Editar nombre, stock mínimo y umbral de alerta de un producto.                                                                          |
| RF04 | Eliminar un producto, solo si ninguno de sus lotes tiene saldo mayor que 0.                                                             |
| RF05 | Registrar un lote de un producto con código, fecha de vencimiento y cantidad inicial.                                                   |
| RF06 | Registrar una entrada adicional sobre un lote no caducado.                                                                              |
| RF07 | Registrar la salida de una cantidad de un producto, asignada automáticamente a lotes según FEFO y repartida entre varios si hace falta. |
| RF08 | Consultar existencias de un producto: stock disponible, stock caducado y lotes con estado y días restantes.                             |
| RF09 | Consultar alertas: stock bajo, próximo a caducar y caducado.                                                                            |
| RF10 | Eliminar un lote caducado, descartando su saldo.                                                                                        |
| RF11 | Guardar todos los cambios en el archivo de persistencia y cargarlos al iniciar la aplicación.                                           |

## 4. Reglas de negocio

| ID   | Regla                                                                                                                                                                                             |
|------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| RN01 | Toda cantidad de entrada, salida o lote inicial es un entero mayor que 0.                                                                                                                         |
| RN02 | El saldo de un lote nunca es negativo.                                                                                                                                                            |
| RN03 | El código de producto es único; el código de lote es único dentro de su producto.                                                                                                                 |
| RN04 | El lote tiene fecha de vencimiento obligatoria, y al registrarlo no puede ser anterior a hoy.                                                                                                     |
| RN05 | Un lote está caducado si hoy \> fecha de vencimiento (el día de vencimiento aún es utilizable).                                                                                                   |
| RN06 | Un lote caducado no recibe entradas ni participa en salidas.                                                                                                                                      |
| RN07 | Stock disponible = suma de saldos de lotes no caducados. Stock caducado = suma de saldos de lotes caducados.                                                                                      |
| RN08 | FEFO: la salida consume primero el lote no caducado con vencimiento más próximo; empates por orden de registro; se omiten lotes con saldo 0; si el lote no alcanza, se continúa con el siguiente. |
| RN09 | Si el stock disponible es menor que la cantidad pedida, la salida se rechaza por completo y no cambia ningún saldo.                                                                               |
| RN10 | Hay stock bajo cuando stock disponible \< stock mínimo. Con mínimo 0 nunca hay alerta.                                                                                                            |
| RN11 | Un lote está próximo a caducar si no está caducado, tiene saldo \> 0 y hoy \>= vencimiento − umbral en días.                                                                                      |
| RN12 | Las alertas se calculan al consultar; no se guardan.                                                                                                                                              |
| RN13 | Un producto con saldo \> 0 no se elimina; solo se eliminan lotes caducados.                                                                                                                       |

## 5. Frontend (UI)

La interfaz es una ventana de escritorio con varios menús claros. Cada pantalla tiene título y su acción principal en una posición consistente.

| ID      | Requisito                                                                                                                                                                 |
|---------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| RF-UI01 | Menú de navegación siempre visible (barra lateral o pestañas) con seis secciones: Inicio, Productos, Lotes, Salidas, Existencias y Alertas. La sección activa se resalta. |

| ID      | Pantalla    | Contenido                                                                                                                                     | Cubre            |
|---------|-------------|-----------------------------------------------------------------------------------------------------------------------------------------------|------------------|
| RF-UI02 | Inicio      | Número de productos, número de alertas por tipo y accesos rápidos a Registrar producto, Registrar salida y Alertas.                           | RF02, RF09       |
| RF-UI03 | Productos   | Tabla con código, nombre, stock mínimo, umbral y stock disponible; formularios para crear, editar y eliminar; búsqueda por código o nombre.   | RF01 a RF04      |
| RF-UI04 | Lotes       | Por producto: tabla de lotes (código, vencimiento, saldo, estado) y acciones para registrar lote, entrada adicional y eliminar lote caducado. | RF05, RF06, RF10 |
| RF-UI05 | Salidas     | Producto y cantidad. Al confirmar, muestra la asignación FEFO por lote y un aviso si queda stock bajo.                                        | RF07             |
| RF-UI06 | Existencias | Selector de producto; indicadores de disponible, caducado y mínimo; tabla de lotes en orden FEFO con estado y días restantes.                 | RF08             |
| RF-UI07 | Alertas     | Lista con tipo, producto, lote y mensaje; filtro por tipo.                                                                                    | RF09             |

| ID      | Comportamiento común                                                                                                                                                                                       |
|---------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| RF-UI08 | Validación en los formularios: campos obligatorios, cantidad entera \> 0, stock mínimo ≥ 0, umbral \> 0, fecha no pasada. El error se muestra junto al campo.                                              |
| RF-UI09 | Los errores de reglas (código duplicado, stock insuficiente, lote caducado, lote no caducado al eliminar, producto con saldo) se muestran como mensajes claros en español, nunca como trazas de excepción. |
| RF-UI10 | Confirmación antes de eliminar y antes de registrar una salida.                                                                                                                                            |
| RF-UI11 | Estados por color y por texto: Vigente (verde), Próximo a caducar (ámbar), Caducado (rojo). Las alertas usan los mismos colores.                                                                           |
| RF-UI12 | Tras cada operación exitosa se muestra una notificación y las tablas se actualizan sin reiniciar.                                                                                                          |
| RF-UI13 | Toda tabla o lista vacía muestra un mensaje que orienta al usuario.                                                                                                                                        |
| RF-UI14 | Textos en español y fechas en formato dd/mm/aaaa.                                                                                                                                                          |

## 6. Arquitectura hexagonal y calidad de código

| ID    | Requisito                                                                                                                                                                                                                  |
|-------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| RNF01 | **Arquitectura hexagonal (puertos y adaptadores).** Tres zonas: dominio (entidades, reglas, FEFO), aplicación (casos de uso y puertos) y adaptadores (entrada: interfaz gráfica; salida: archivo de persistencia y reloj). |
| RNF02 | **Regla de dependencias.** Las dependencias apuntan hacia adentro. El dominio no importa Swing, java.io, java.nio.file ni librerías JSON. La aplicación depende solo de interfaces (puertos).                              |
| RNF03 | **Puertos explícitos.** Entrada: un caso de uso por operación (RF01 a RF10). Salida: repositorio de productos y proveedor de fecha actual. Las interfaces viven en dominio o aplicación, nunca en los adaptadores.         |
| RNF04 | **La interfaz gráfica no contiene reglas de negocio.** Solo recoge datos, llama a casos de uso y muestra resultados.                                                                                                       |
| RNF05 | **Pruebas unitarias (JUnit 5)** del dominio y de los casos de uso, con repositorios en memoria y reloj fijo, sin archivos ni interfaz gráfica.                                                                             |
| RNF06 | **Archivo de datos.** Si no existe, se crea vacío. Si no se puede leer o escribir, se muestra un mensaje claro y la aplicación no se cierra. Una salida repartida entre lotes se guarda completa o no se guarda.           |
| RNF07 | **Responsabilidades separadas.** Ninguna clase mezcla interfaz, reglas y acceso a archivos; FEFO y caducidad existen en un solo lugar.                                                                                     |

## 7. Supuestos

| ID    | Supuesto                                                                                      |
|-------|-----------------------------------------------------------------------------------------------|
| SP-01 | Un lote está caducado solo si hoy \> vencimiento.                                             |
| SP-02 | Las alertas se calculan al consultar y no se guardan.                                         |
| SP-03 | El usuario no elige el lote de una salida; FEFO es automático.                                |
| SP-04 | El stock mínimo se compara con el stock disponible, sin lotes caducados.                      |
| SP-05 | La fecha de hoy se obtiene de un puerto de reloj, en zona horaria America/Bogota.             |
| SP-06 | La interfaz gráfica se implementa con Swing, que viene con el JDK y no requiere dependencias. |

## 8. Requisitos nuevos (versión 0.4)

Origen: el informe de pruebas de la versión anterior (10 pruebas fallidas por falta de validaciones), dos errores de interfaz detectados en la revisión manual y el rediseño de la interfaz a partir de una plantilla visual entregada como imagen. Esta sección es una actualización de un proyecto ya implementado (versión 1): las secciones 1 a 7 ya fueron implementadas y no se rehacen, y los datos que la versión 1 guardó en el archivo JSON deben seguir cargándose sin pérdida. Los requisitos de esta sección complementan a los anteriores; si alguno entra en conflicto con otra sección, prevalece esta.

### 8.1 Validaciones del dominio

| ID   | Requisito                                                                                                                                                                                                                                                               |
|------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| RN14 | El nombre de un producto no puede estar vacío ni contener solo espacios.                                                                                                                                                                                                |
| RN15 | El stock mínimo es un entero ≥ 0, tanto al crear como al editar un producto.                                                                                                                                                                                            |
| RN16 | El umbral de alerta es un entero \> 0, tanto al crear como al editar un producto.                                                                                                                                                                                       |
| RN17 | Estas validaciones viven en el dominio (al construir y al modificar el producto) y los casos de uso las respetan. Ante un valor inválido se lanza IllegalArgumentException y no se modifica ni se guarda nada. La interfaz valida además, pero no sustituye al dominio. |

### 8.2 Correcciones de la interfaz

| ID      | Requisito                                                                                                                                                                                                                                                                                                                                                             |
|---------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| RF-UI15 | **Campos numéricos.** Todos los campos numéricos (cantidad del lote, entrada, salida, stock mínimo, umbral) solo aceptan enteros. Se rechazan o bloquean los valores negativos, decimales, texto y vacío, con un mensaje junto al campo. Cantidades y umbral deben ser \> 0; stock mínimo ≥ 0. Un valor inválido nunca llega al caso de uso. Aplica a crear y editar. |
| RF-UI16 | **Tablas de solo lectura.** Ninguna celda de ninguna tabla o lista se puede editar directamente (ni con doble clic ni al escribir sobre ella). Los datos solo cambian mediante acciones explícitas (Editar, Eliminar, Entrada, Registrar).                                                                                                                            |
| RF-UI17 | **Edición por formulario.** La acción Editar abre un formulario con los valores actuales y las mismas validaciones de la creación, con botones Guardar y Cancelar. Cancelar descarta los cambios.                                                                                                                                                                     |

### 8.3 Rediseño de la interfaz

La plantilla visual (imagen) es solo una referencia de estilo: no se copia su contenido, datos ni nombres, y se adapta a las pantallas de este sistema.

| ID      | Requisito                                                                                                                                                                                                                                                                                                                                                                                                                  |
|---------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| RF-UI18 | **Estructura general.** Barra lateral oscura a la izquierda con el nombre de la aplicación arriba y los ítems del menú con ícono; el ítem activo se resalta. A la derecha, un área de contenido de fondo claro con el título de la sección y el contenido dentro de tarjetas blancas de bordes redondeados y sombra suave.                                                                                                 |
| RF-UI19 | **Menús.** Solo los seis existentes: Inicio, Productos, Lotes, Salidas, Existencias y Alertas. No se agrega nada de la plantilla que el sistema no tenga: Dashboard como ítem aparte, Pedidos, Proveedores, Reportes, Ajustes, perfil o cuenta de usuario, notificaciones, búsqueda global, imágenes, precios, valor de inventario, categorías, ubicaciones, importar CSV, exportar PDF, selección múltiple ni paginación. |
| RF-UI20 | **Pantallas de lista (Productos, Lotes, Alertas y tablas de Existencias).** Tarjeta principal con barra de herramientas (búsqueda o selector a la izquierda, filtros desplegables, botón primario a la derecha), tabla con encabezado diferenciado y filas con separadores finos. Cada fila tiene sus acciones como botones pequeños con borde. La columna Estado se muestra como etiqueta de color tipo píldora.          |
| RF-UI21 | **Tarjetas de indicadores.** Fila de tarjetas con ícono, título y cifra, como en la plantilla, solo en Inicio (total de productos, stock bajo, próximos a caducar, caducados) y en Existencias (disponible, caducado, mínimo).                                                                                                                                                                                             |
| RF-UI22 | **Estados por color y texto.** Lote: Vigente (verde), Próximo a caducar (ámbar), Caducado (rojo). Producto, solo como presentación derivada de reglas existentes: Disponible (verde), Stock bajo según RN10 (ámbar), Sin stock cuando el disponible es 0 (rojo); Sin stock prevalece sobre Stock bajo.                                                                                                                     |
| RF-UI23 | **Formularios y diálogos** con el mismo estilo: bordes redondeados, botón primario azul marino, botón secundario con borde, errores en rojo bajo el campo y confirmación antes de eliminar y de registrar una salida.                                                                                                                                                                                                      |
| RF-UI24 | **Tema centralizado.** Colores, tipografía y espaciados se definen en un solo lugar (clase de tema), sin valores sueltos en cada pantalla. Paleta: barra lateral azul marino oscuro, fondo gris muy claro, tarjetas blancas, botón primario azul marino, verde, ámbar y rojo para estados. Una sola tipografía sans-serif. Se permite usar FlatLaf mediante Maven; si no, se personaliza con UIManager y Java2D.           |
| RF-UI25 | **Ventana.** Redimensionable, con tamaño mínimo de 1000 × 650 px. Las tablas ocupan el ancho disponible y tienen scroll vertical propio.                                                                                                                                                                                                                                                                                   |
| RF-UI26 | **Sin cambios funcionales.** El rediseño no cambia reglas, casos de uso ni puertos. La interfaz sigue siendo un adaptador de entrada sin reglas de negocio (RNF04).                                                                                                                                                                                                                                                        |

**Adaptación por pantalla:**

| Pantalla    | Barra de herramientas                                                                                        | Contenido                                                                                                                                | Acciones                                               |
|-------------|--------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------|
| Inicio      | Ninguna                                                                                                      | Cuatro tarjetas de indicadores y accesos rápidos como botones (Registrar producto, Registrar salida, Ver alertas)                        | Accesos rápidos                                        |
| Productos   | Búsqueda por código o nombre; filtro Estado (Todos, Disponible, Stock bajo, Sin stock); botón Nuevo producto | Tabla: Código, Nombre, Stock mínimo, Umbral (días), Stock disponible, Estado                                                             | Editar, Eliminar, Ver lotes                            |
| Lotes       | Selector de producto; filtro Estado (Todos, Vigente, Próximo a caducar, Caducado); botón Registrar lote      | Tabla: Código de lote, Vencimiento, Saldo, Días restantes, Estado                                                                        | Entrada; Eliminar (solo habilitado en lotes caducados) |
| Salidas     | Ninguna                                                                                                      | Tarjeta con formulario (producto y cantidad) y botón Registrar salida; debajo, tabla con la asignación FEFO: Lote, Vencimiento, Cantidad | Registrar salida                                       |
| Existencias | Selector de producto                                                                                         | Tres tarjetas (disponible, caducado, mínimo) y tabla de lotes en orden FEFO: Código de lote, Vencimiento, Saldo, Días restantes, Estado  | Ninguna                                                |
| Alertas     | Filtro Tipo (Todas, Stock bajo, Próximo a caducar, Caducado); filtro Producto                                | Tabla: Tipo (píldora), Producto, Lote, Mensaje                                                                                           | Ninguna                                                |

### 8.4 Robustez de persistencia y pruebas

| ID    | Requisito                                                                                                                                                                                                                                                                                                                                                                                                          |
|-------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| RNF08 | **Archivo JSON ilegible o malformado.** Se muestra un mensaje claro y la aplicación no se cierra. Antes de iniciar con datos vacíos se conserva una copia del archivo ilegible (por ejemplo, con extensión .bak) para no perder información.                                                                                                                                                                       |
| RNF09 | **Guardado seguro.** Se escribe primero en un archivo temporal y luego se reemplaza el archivo de datos, de modo que una escritura interrumpida no lo deje corrupto. Un fallo de permisos o de disco muestra un mensaje claro y la operación se informa como fallida.                                                                                                                                              |
| RNF10 | **Pruebas nuevas.** Pruebas unitarias del dominio y del servicio para RN14 a RN17 (creación y edición); pruebas de persistencia para JSON malformado y fallo de escritura; pruebas de interfaz para tablas de solo lectura y rechazo de números negativos en formularios (se omiten en modo headless). Las 10 pruebas que hoy fallan deben pasar sin modificarlas, y la suite completa debe quedar con 0 fallidas. |
| RNF11 | **Ejecución de pruebas sin instalar Maven.** El proyecto incluye Maven Wrapper (mvnw) para ejecutar las pruebas desde la terminal, además de ejecutarlas desde el IDE.                                                                                                                                                                                                                                             |
