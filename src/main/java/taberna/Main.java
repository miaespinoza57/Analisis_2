package taberna;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import taberna.bus.BusDeEventos;
import taberna.bus.BusDeEventosEnMemoria;
import taberna.componentes.Banda;
import taberna.componentes.Barra;
import taberna.componentes.Cocina;
import taberna.componentes.PanelLED;
import taberna.componentes.SistemaDePedidos;
import taberna.componentes.SistemaDeSonido;
import taberna.eventos.ItemDelPedido;
import taberna.extensiones.DemostracionDeExtensibilidad;
import taberna.extensiones.SistemaDeHumo;
import taberna.util.ConsolaDeLaTaberna;
import taberna.util.HilosNombrados;

/**
 * Raíz de composición: es el ÚNICO lugar donde se crean los componentes y se
 * conectan al bus. Ningún componente conoce a otro.
 */
public final class Main {

    private static final String NOMBRE_DE_LA_BANDA = "Los Seguidores de Dijkstra";
    private static final int SEGUNDOS_ANTES_DE_LA_PRIMERA_CANCION = 1;
    private static final int SEGUNDOS_ENTRE_CANCIONES = 4;
    private static final int SEGUNDOS_HASTA_DESCONECTAR_LAS_LUCES = 10;
    private static final int SEGUNDOS_DE_DURACION_DE_LA_DEMOSTRACION = 15;
    private static final int SEGUNDOS_MAXIMOS_PARA_CERRAR = 5;

    private Main() {
    }

    public static void main(String[] argumentos) throws InterruptedException {
        ConsolaDeLaTaberna consola = new ConsolaDeLaTaberna();
        BusDeEventos busDeEventos = new BusDeEventosEnMemoria();

        ScheduledExecutorService planificadorDeBarraYCocina =
                Executors.newScheduledThreadPool(4, HilosNombrados.conPrefijo("barra-cocina"));
        ScheduledExecutorService planificadorDeLaBanda =
                Executors.newSingleThreadScheduledExecutor(HilosNombrados.conPrefijo("banda"));
        ScheduledExecutorService planificadorDeClientes =
                Executors.newSingleThreadScheduledExecutor(HilosNombrados.conPrefijo("clientes"));

        consola.imprimirSeparador("Abre la taberna \"El Gólem Alquímico\"");

        // 1) Componentes originales de la taberna
        SistemaDePedidos sistemaDePedidos = new SistemaDePedidos(busDeEventos, consola);
        Barra barra = new Barra(busDeEventos, consola, planificadorDeBarraYCocina);
        Cocina cocina = new Cocina(busDeEventos, consola, planificadorDeBarraYCocina);
        SistemaDeSonido sistemaDeSonido = new SistemaDeSonido(busDeEventos, consola);
        PanelLED panelLED = new PanelLED(busDeEventos, consola);
        Banda banda = new Banda(busDeEventos, consola, planificadorDeLaBanda, NOMBRE_DE_LA_BANDA, crearRepertorio());

        barra.activar();
        cocina.activar();
        sistemaDeSonido.activar();
        panelLED.activar();

        // 2) Extensión: componentes nuevos, sin modificar nada de lo anterior
        DemostracionDeExtensibilidad extensiones = new DemostracionDeExtensibilidad(busDeEventos, consola);
        extensiones.conectarNuevosComponentes();

        // 3) Simulación concurrente: clientes, banda y tiempos de respuesta
        consola.imprimirSeparador("Comienza la noche");
        banda.iniciarActuacion(SEGUNDOS_ANTES_DE_LA_PRIMERA_CANCION, SEGUNDOS_ENTRE_CANCIONES);
        programarPedidosDeLosClientes(planificadorDeClientes, sistemaDePedidos);
        planificadorDeClientes.schedule(
                extensiones::desconectarIluminacion, SEGUNDOS_HASTA_DESCONECTAR_LAS_LUCES, TimeUnit.SECONDS);

        TimeUnit.SECONDS.sleep(SEGUNDOS_DE_DURACION_DE_LA_DEMOSTRACION);

        // 4) Cierre ordenado
        consola.imprimirSeparador("Cierra la taberna");
        banda.detenerActuacion();
        cerrar(planificadorDeClientes, planificadorDeLaBanda, planificadorDeBarraYCocina);
        extensiones.imprimirResumenDeAuditoria();
    }

    private static List<Banda.Cancion> crearRepertorio() {
        return List.of(
                new Banda.Cancion("El Algoritmo del Amor", 180),
                new Banda.Cancion(SistemaDeHumo.CANCION_QUE_ACTIVA_EL_HUMO, 444),
                new Banda.Cancion("Recursión en Mi Menor", 200));
    }

    private static void programarPedidosDeLosClientes(ScheduledExecutorService planificadorDeClientes,
                                                      SistemaDePedidos sistemaDePedidos) {
        planificadorDeClientes.schedule(() -> sistemaDePedidos.realizarPedido(5,
                List.of(ItemDelPedido.bebida("cerveza"), ItemDelPedido.comida("hamburguesa"))),
                500, TimeUnit.MILLISECONDS);
        planificadorDeClientes.schedule(() -> sistemaDePedidos.realizarPedido(2,
                List.of(ItemDelPedido.bebida("limonada"), ItemDelPedido.bebida("cerveza"), ItemDelPedido.comida("tacos"))),
                2, TimeUnit.SECONDS);
        planificadorDeClientes.schedule(() -> sistemaDePedidos.realizarPedido(8,
                List.of(ItemDelPedido.bebida("refresco"), ItemDelPedido.comida("pizza"))),
                3500, TimeUnit.MILLISECONDS);
    }

    private static void cerrar(ExecutorService... ejecutores) throws InterruptedException {
        for (ExecutorService ejecutor : ejecutores) {
            ejecutor.shutdown();
        }
        for (ExecutorService ejecutor : ejecutores) {
            if (!ejecutor.awaitTermination(SEGUNDOS_MAXIMOS_PARA_CERRAR, TimeUnit.SECONDS)) {
                ejecutor.shutdownNow();
            }
        }
    }
}
