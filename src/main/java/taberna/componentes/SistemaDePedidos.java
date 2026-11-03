package taberna.componentes;

import static taberna.util.Validaciones.exigirNoNulo;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import taberna.bus.BusDeEventos;
import taberna.eventos.ItemDelPedido;
import taberna.eventos.PedidoRealizadoEvent;
import taberna.util.ColorAnsi;
import taberna.util.ConsolaDeLaTaberna;

/**
 * Publicador: recibe los pedidos de las mesas y publica un {@link PedidoRealizadoEvent}.
 * No sabe quién atenderá el pedido (Barra, Cocina, Panel...): solo conoce el bus.
 */
public final class SistemaDePedidos {

    private static final String ETIQUETA = "PEDIDOS";
    private static final int NUMERO_INICIAL_DE_PEDIDO = 123;

    private final BusDeEventos busDeEventos;
    private final ConsolaDeLaTaberna consola;
    private final AtomicInteger contadorDePedidos = new AtomicInteger(NUMERO_INICIAL_DE_PEDIDO);

    public SistemaDePedidos(BusDeEventos busDeEventos, ConsolaDeLaTaberna consola) {
        this.busDeEventos = exigirNoNulo(busDeEventos, "busDeEventos");
        this.consola = exigirNoNulo(consola, "consola");
    }

    /**
     * Registra un pedido y lo publica en el bus.
     *
     * @return el identificador asignado al pedido (por ejemplo "pedido_123")
     */
    public String realizarPedido(int numeroDeMesa, List<ItemDelPedido> items) {
        String identificadorDePedido = "pedido_" + contadorDePedidos.getAndIncrement();
        PedidoRealizadoEvent pedidoRealizado = new PedidoRealizadoEvent(numeroDeMesa, identificadorDePedido, items);
        consola.imprimir(ColorAnsi.VERDE, ETIQUETA,
                "Mesa " + numeroDeMesa + " pide " + pedidoRealizado.items() + " (" + identificadorDePedido + ")");
        busDeEventos.publicar(pedidoRealizado);
        return identificadorDePedido;
    }
}
