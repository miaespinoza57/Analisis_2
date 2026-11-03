package taberna.eventos;

import static taberna.util.Validaciones.exigirNoNulo;
import static taberna.util.Validaciones.exigirPositivo;
import static taberna.util.Validaciones.exigirTextoNoVacio;

import java.time.Instant;
import java.util.UUID;

/**
 * Se publica cada vez que la banda empieza a tocar una canción.
 */
public record BandaTocandoEvent(
        UUID identificadorDelEvento,
        Instant momentoDeOcurrencia,
        String nombreDeLaBanda,
        String nombreDeLaCancion,
        int duracionEnSegundos) implements Evento {

    public BandaTocandoEvent {
        exigirNoNulo(identificadorDelEvento, "identificadorDelEvento");
        exigirNoNulo(momentoDeOcurrencia, "momentoDeOcurrencia");
        exigirTextoNoVacio(nombreDeLaBanda, "nombreDeLaBanda");
        exigirTextoNoVacio(nombreDeLaCancion, "nombreDeLaCancion");
        exigirPositivo(duracionEnSegundos, "duracionEnSegundos");
    }

    /** Constructor cómodo: genera automáticamente el identificador y el momento del evento. */
    public BandaTocandoEvent(String nombreDeLaBanda, String nombreDeLaCancion, int duracionEnSegundos) {
        this(UUID.randomUUID(), Instant.now(), nombreDeLaBanda, nombreDeLaCancion, duracionEnSegundos);
    }

    @Override
    public String toString() {
        return "BandaTocandoEvent[banda=" + nombreDeLaBanda
                + ", cancion=" + nombreDeLaCancion
                + ", duracion=" + duracionEnSegundos + "s]";
    }
}
