package taberna.componentes;

import static taberna.util.Validaciones.exigirNoNulo;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import taberna.bus.BusDeEventos;
import taberna.bus.Suscriptor;
import taberna.eventos.ComidaPreparadaEvent;
import taberna.eventos.ItemDelPedido;
import taberna.eventos.PedidoRealizadoEvent;
import taberna.util.ColorAnsi;
import taberna.util.ConsolaDeLaTaberna;

/**
 * Suscriptor y publicador: escucha {@link PedidoRealizadoEvent}, cocina los platillos
 * (5 segundos cada uno, sin bloquear el bus) y publica un {@link ComidaPreparadaEvent}.
 */
public final class Cocina implements Suscriptor<PedidoRealizadoEvent> {

    public static final int SEGUNDOS_DE_PREPARACION_DE_COMIDA = 5;
    private static final String ETIQUETA = "COCINA";

    private final BusDeEventos busDeEventos;
    private final ConsolaDeLaTaberna consola;
    private final ScheduledExecutorService planificadorDeTareas;

    public Cocina(BusDeEventos busDeEventos, ConsolaDeLaTaberna consola, ScheduledExecutorService planificadorDeTareas) {
        this.busDeEventos = exigirNoNulo(busDeEventos, "busDeEventos");
        this.consola = exigirNoNulo(consola, "consola");
        this.planificadorDeTareas = exigirNoNulo(planificadorDeTareas, "planificadorDeTareas");
    }

    public void activar() {
        busDeEventos.suscribir(PedidoRealizadoEvent.class, this);
    }

    public void desactivar() {
        busDeEventos.desuscribir(PedidoRealizadoEvent.class, this);
    }

    @Override
    public void alRecibirEvento(PedidoRealizadoEvent pedido) {
        for (ItemDelPedido plato : pedido.comidas()) {
            consola.imprimir(ColorAnsi.NARANJA, ETIQUETA,
                    "Cocinando " + plato.nombre() + " del " + pedido.identificadorDePedido() + "...");
            planificadorDeTareas.schedule(
                    () -> terminarPlato(pedido.identificadorDePedido(), plato),
                    SEGUNDOS_DE_PREPARACION_DE_COMIDA,
                    TimeUnit.SECONDS);
        }
    }

    private void terminarPlato(String identificadorDePedido, ItemDelPedido plato) {
        consola.imprimir(ColorAnsi.NARANJA, ETIQUETA,
                "Platillo terminado para el " + identificadorDePedido + ": " + plato.nombre());
        busDeEventos.publicar(new ComidaPreparadaEvent(identificadorDePedido, plato.nombre()));
    }
}
