package taberna.componentes;

import static taberna.util.Validaciones.exigirNoNulo;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import taberna.bus.BusDeEventos;
import taberna.bus.Suscriptor;
import taberna.eventos.BebidaServidaEvent;
import taberna.eventos.ItemDelPedido;
import taberna.eventos.PedidoRealizadoEvent;
import taberna.util.ColorAnsi;
import taberna.util.ConsolaDeLaTaberna;

/**
 * Suscriptor y publicador: escucha {@link PedidoRealizadoEvent}, prepara las bebidas
 * (2 segundos cada una, sin bloquear el bus) y publica un {@link BebidaServidaEvent}.
 */
public final class Barra implements Suscriptor<PedidoRealizadoEvent> {

    public static final int SEGUNDOS_DE_PREPARACION_DE_BEBIDA = 2;
    private static final String ETIQUETA = "BARRA";

    private final BusDeEventos busDeEventos;
    private final ConsolaDeLaTaberna consola;
    private final ScheduledExecutorService planificadorDeTareas;

    public Barra(BusDeEventos busDeEventos, ConsolaDeLaTaberna consola, ScheduledExecutorService planificadorDeTareas) {
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
        for (ItemDelPedido bebida : pedido.bebidas()) {
            consola.imprimir(ColorAnsi.AMARILLO, ETIQUETA,
                    "Preparando " + bebida.nombre() + " para la Mesa " + pedido.numeroDeMesa() + "...");
            planificadorDeTareas.schedule(
                    () -> servirBebida(pedido.numeroDeMesa(), bebida),
                    SEGUNDOS_DE_PREPARACION_DE_BEBIDA,
                    TimeUnit.SECONDS);
        }
    }

    private void servirBebida(int numeroDeMesa, ItemDelPedido bebida) {
        consola.imprimir(ColorAnsi.AMARILLO, ETIQUETA,
                "Bebida lista para la Mesa " + numeroDeMesa + ": " + bebida.nombre());
        busDeEventos.publicar(new BebidaServidaEvent(numeroDeMesa, bebida.nombre()));
    }
}
