package taberna.eventos;

import static taberna.util.Validaciones.exigirNoNulo;
import static taberna.util.Validaciones.exigirTextoNoVacio;

import java.time.Instant;
import java.util.UUID;

/**
 * Se publica cuando la Cocina termina de preparar un platillo de un pedido.
 */
public record ComidaPreparadaEvent(
        UUID identificadorDelEvento,
        Instant momentoDeOcurrencia,
        String identificadorDePedido,
        String plato) implements Evento {

    public ComidaPreparadaEvent {
        exigirNoNulo(identificadorDelEvento, "identificadorDelEvento");
        exigirNoNulo(momentoDeOcurrencia, "momentoDeOcurrencia");
        exigirTextoNoVacio(identificadorDePedido, "identificadorDePedido");
        exigirTextoNoVacio(plato, "plato");
    }

    /** Constructor cómodo: genera automáticamente el identificador y el momento del evento. */
    public ComidaPreparadaEvent(String identificadorDePedido, String plato) {
        this(UUID.randomUUID(), Instant.now(), identificadorDePedido, plato);
    }

    @Override
    public String toString() {
        return "ComidaPreparadaEvent[pedido=" + identificadorDePedido + ", plato=" + plato + "]";
    }
}
