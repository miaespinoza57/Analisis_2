package taberna.eventos;

import static taberna.util.Validaciones.exigirNoNulo;
import static taberna.util.Validaciones.exigirPositivo;
import static taberna.util.Validaciones.exigirTextoNoVacio;

import java.time.Instant;
import java.util.UUID;

/**
 * Se publica cuando la Barra termina de servir una bebida en una mesa.
 */
public record BebidaServidaEvent(
        UUID identificadorDelEvento,
        Instant momentoDeOcurrencia,
        int numeroDeMesa,
        String bebida) implements Evento {

    public BebidaServidaEvent {
        exigirNoNulo(identificadorDelEvento, "identificadorDelEvento");
        exigirNoNulo(momentoDeOcurrencia, "momentoDeOcurrencia");
        exigirPositivo(numeroDeMesa, "numeroDeMesa");
        exigirTextoNoVacio(bebida, "bebida");
    }

    /** Constructor cómodo: genera automáticamente el identificador y el momento del evento. */
    public BebidaServidaEvent(int numeroDeMesa, String bebida) {
        this(UUID.randomUUID(), Instant.now(), numeroDeMesa, bebida);
    }

    @Override
    public String toString() {
        return "BebidaServidaEvent[mesa=" + numeroDeMesa + ", bebida=" + bebida + "]";
    }
}
