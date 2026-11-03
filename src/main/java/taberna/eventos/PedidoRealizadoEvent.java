package taberna.eventos;

import static taberna.util.Validaciones.exigirNoNulo;
import static taberna.util.Validaciones.exigirPositivo;
import static taberna.util.Validaciones.exigirTextoNoVacio;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Se publica cuando una mesa hace un pedido. Es inmutable: la lista de items
 * se copia de forma defensiva y no puede modificarse después de crear el evento.
 */
public record PedidoRealizadoEvent(
        UUID identificadorDelEvento,
        Instant momentoDeOcurrencia,
        int numeroDeMesa,
        String identificadorDePedido,
        List<ItemDelPedido> items) implements Evento {

    public PedidoRealizadoEvent {
        exigirNoNulo(identificadorDelEvento, "identificadorDelEvento");
        exigirNoNulo(momentoDeOcurrencia, "momentoDeOcurrencia");
        exigirPositivo(numeroDeMesa, "numeroDeMesa");
        exigirTextoNoVacio(identificadorDePedido, "identificadorDePedido");
        exigirNoNulo(items, "items");
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Un pedido debe incluir al menos un item");
        }
        items = List.copyOf(items);
    }

    /** Constructor cómodo: genera automáticamente el identificador y el momento del evento. */
    public PedidoRealizadoEvent(int numeroDeMesa, String identificadorDePedido, List<ItemDelPedido> items) {
        this(UUID.randomUUID(), Instant.now(), numeroDeMesa, identificadorDePedido, items);
    }

    public List<ItemDelPedido> bebidas() {
        return items.stream().filter(ItemDelPedido::esBebida).toList();
    }

    public List<ItemDelPedido> comidas() {
        return items.stream().filter(ItemDelPedido::esComida).toList();
    }

    @Override
    public String toString() {
        return "PedidoRealizadoEvent[mesa=" + numeroDeMesa
                + ", pedido=" + identificadorDePedido
                + ", items=" + items + "]";
    }
}
