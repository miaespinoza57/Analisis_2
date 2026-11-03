# La Taberna del Bus de Eventos - "El Gólem Alquímico"

Implementación en **Java** del patrón arquitectónico **Publisher/Subscriber** por medio de un **Event Bus**.
Los componentes de la taberna (pedidos, barra, cocina, banda, sonido, panel LED...) se comunican
únicamente a través del bus, sin conocerse entre sí.

**Autora:** Mia Montserrat Espinoza Rosales (matrícula 1199574) - Ingeniería en Computación, UABC Mexicali.

## Requisitos

- Java 17 o superior (probado con JDK 21 compilando con `--release 17`).
- No usa librerías externas ni Maven/Gradle.
- Para generar los diagramas: [PlantUML](https://plantuml.com) (opcional).

## Cómo compilar y ejecutar

### Opción rápida (scripts)

```bash
# Linux / macOS / Git Bash
./compilar_y_ejecutar.sh

# Windows (cmd o PowerShell)
compilar_y_ejecutar.bat
```

### Opción manual

```bash
# 1) Compilar
mkdir out
javac --release 17 -encoding UTF-8 -d out $(find src -name "*.java")     # Linux / macOS
# Windows: dir /s /b src\*.java > fuentes.txt  &&  javac -encoding UTF-8 -d out @fuentes.txt

# 2) Pruebas del bus (18 verificaciones)
java -cp out taberna.PruebasDelBusDeEventos

# 3) Simulación de la taberna (dura unos 15 segundos)
java -Dfile.encoding=UTF-8 -cp out taberna.Main
```

> En Windows, si ves caracteres raros, ejecuta antes `chcp 65001` y usa una terminal moderna
> (Windows Terminal) para que se vean los colores ANSI.

## Estructura del proyecto

```
src/main/java/taberna/
├── Main.java                      Raíz de composición: crea y conecta todo
├── bus/
│   ├── BusDeEventos.java          Interfaz del bus (suscribir, desuscribir, publicar)
│   ├── BusDeEventosEnMemoria.java Implementación thread-safe
│   └── Suscriptor.java            Interfaz genérica de los suscriptores
├── eventos/                       Eventos inmutables (records)
│   ├── Evento.java, CategoriaDeItem.java, ItemDelPedido.java
│   ├── PedidoRealizadoEvent.java, BebidaServidaEvent.java
│   └── ComidaPreparadaEvent.java, BandaTocandoEvent.java
├── componentes/                   Componentes originales de la taberna
│   ├── SistemaDePedidos.java      Publicador
│   ├── Barra.java, Cocina.java    Suscriptores y publicadores
│   ├── Banda.java                 Publicador
│   └── SistemaDeSonido.java, PanelLED.java   Suscriptores
├── extensiones/                   Componentes agregados SIN modificar el código anterior
│   ├── SistemaDeHumo.java, SistemaDeIluminacion.java, RegistroDeAuditoria.java
│   └── DemostracionDeExtensibilidad.java
└── util/                          ConsolaDeLaTaberna, ColorAnsi, Validaciones, HilosNombrados
src/test/java/taberna/PruebasDelBusDeEventos.java
docs/diagramas/*.puml              Código PlantUML de los diagramas
docs/imagenes/*.png                Diagramas generados
```

## Cómo funciona

1. `SistemaDePedidos` publica un `PedidoRealizadoEvent` (mesa, pedido, items).
2. El bus lo entrega a `Barra` y `Cocina`. Cada una filtra los items que le tocan, simula su preparación
   con un `ScheduledExecutorService` (**bebida: 2 s, comida: 5 s**, sin bloquear el bus) y publica
   `BebidaServidaEvent` / `ComidaPreparadaEvent`.
3. `PanelLED` está suscrito a **todos** los eventos (`Evento.class`) e imprime anuncios coloridos.
4. `Banda` corre en su propio hilo, toca una canción cada pocos segundos y publica `BandaTocandoEvent`;
   `SistemaDeSonido` ajusta el ecualizador y `PanelLED` anuncia la canción.

La salida lleva marca de tiempo relativa (`[00:02.5]`), un color por componente y se imprime de forma
sincronizada para que las líneas de distintos hilos nunca se mezclen.

## Decisiones de diseño (y por qué cumplen la rúbrica)

### Event Bus robusto
- `ConcurrentHashMap<Class, CopyOnWriteArrayList<Suscriptor>>`: seguro con varios hilos.
- Se puede **suscribir y desuscribir mientras se publica** (las listas son copy-on-write).
- No se duplican suscriptores; los argumentos nulos se rechazan con mensajes claros.
- Un suscriptor que lanza una excepción se registra y **los demás siguen recibiendo el evento**.
- Un suscriptor del tipo base `Evento.class` recibe todos los eventos.
- La conversión genérica está encapsulada en un solo método (`notificar`) con `@SuppressWarnings` justificado.

### Eventos inmutables
- Son `record` de Java: atributos finales y sin setters.
- Validación en el constructor compacto (no nulos, mesa > 0, duración > 0) y copia defensiva de las listas
  con `List.copyOf`, por lo que `items()` no se puede modificar.
- `toString()` legible y nombres expresivos.

### Desacoplamiento total
- Cada componente recibe un `BusDeEventos` (la **interfaz**) por constructor (inyección de dependencias).
- Ningún componente tiene atributos ni imports de otro componente: solo conocen el bus, `Suscriptor` y los eventos.
- `Main` es el único lugar donde se crean los objetos y se conectan.

### Extensibilidad (Open/Closed)
`DemostracionDeExtensibilidad` agrega **tres componentes nuevos** sin tocar `Banda`, `BusDeEventosEnMemoria`
ni ningún componente existente:

- `SistemaDeHumo`: reacciona a `BandaTocandoEvent` solo si la canción es *"Through the Fire and Flames"*.
- `SistemaDeIluminacion`: cambia el modo de las luces según la duración de la canción.
- `RegistroDeAuditoria`: se suscribe a todos los eventos y cuenta cuántos hubo de cada tipo.

El sistema está **abierto a extensión** (se agregan suscriptores nuevos) y **cerrado a modificación**
(el código existente no cambia). Además se demuestra la **desuscripción dinámica**: a los 10 s las luces se
desconectan del bus y dejan de reaccionar.

## Salida de ejemplo

Ver [`salida_ejemplo.txt`](salida_ejemplo.txt) (captura real de una ejecución, sin colores).

```
[00:00.5] [PEDIDOS]      Mesa 5 pide [cerveza, hamburguesa] (pedido_123)
[00:00.5] [BARRA]        Preparando cerveza para la Mesa 5...
[00:00.5] [COCINA]       Cocinando hamburguesa del pedido_123...
[00:00.5] [PANEL LED]    ¡Nuevo pedido en la Mesa 5! [cerveza, hamburguesa]
[00:01.0] [BANDA]        Empieza a tocar "El Algoritmo del Amor" (Los Seguidores de Dijkstra)
[00:01.0] [SONIDO]       Ajustando ecualizador para: El Algoritmo del Amor
[00:02.5] [BARRA]        Bebida lista para la Mesa 5: cerveza
[00:02.5] [PANEL LED]    ¡Nueva bebida servida en la Mesa 5! (cerveza)
[00:05.5] [COCINA]       Platillo terminado para el pedido_123: hamburguesa
[00:05.5] [PANEL LED]    ¡Comida preparada para el pedido_123! (hamburguesa)
```

## Diagramas UML (PlantUML)

Los archivos fuente están en `docs/diagramas/`:

| Archivo | Contenido |
|---|---|
| `01a-clases-bus-y-eventos.puml` | Clases del bus y de los eventos |
| `01b-clases-componentes.puml` | Clases de los componentes y las extensiones |
| `01c-clases-utilidades-y-main.puml` | Utilidades y `Main` |
| `02-diagrama-de-secuencia.puml` | Flujo completo: pedido de la Mesa 5 y la banda |
| `03-diagrama-de-componentes.puml` | Arquitectura: todos dependen solo del bus |

Para generar las imágenes:

```bash
# Con el .jar de PlantUML (necesita Graphviz instalado)
PLANTUML_LIMIT_SIZE=16384 java -jar plantuml.jar -tpng -charset UTF-8 -o ../imagenes docs/diagramas/*.puml
```

O pega el contenido de cada `.puml` en <https://www.plantuml.com/plantuml>.
