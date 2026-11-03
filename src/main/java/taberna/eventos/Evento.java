package taberna.eventos;

import java.time.Instant;
import java.util.UUID;

/**
 * Contrato común de todos los eventos del bus. Todo evento tiene un
 * identificador único y el momento en que ocurrió. Las implementaciones
 * deben ser inmutables.
 */
public interface Evento {

    UUID identificadorDelEvento();

    Instant momentoDeOcurrencia();

    /** Nombre legible del tipo de evento (por ejemplo, "BebidaServidaEvent"). */
    default String nombreDelTipoDeEvento() {
        return getClass().getSimpleName();
    }
}
