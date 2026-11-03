package taberna.bus;

import static taberna.util.Validaciones.exigirNoNulo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import taberna.eventos.Evento;

/**
 * Implementación del Event Bus en memoria, segura para uso concurrente.
 *
 * <ul>
 *   <li>Los suscriptores se guardan por tipo de evento en un {@link ConcurrentHashMap}.</li>
 *   <li>Cada lista es un {@link CopyOnWriteArrayList}: se puede suscribir o desuscribir
 *       mientras se está publicando sin que se produzcan errores de concurrencia.</li>
 *   <li>Un suscriptor registrado en un tipo base (por ejemplo {@code Evento.class})
 *       recibe también los eventos de todos sus subtipos.</li>
 *   <li>Si un suscriptor lanza una excepción, se registra y el bus sigue notificando
 *       a los demás: un componente defectuoso no tumba a los otros.</li>
 * </ul>
 */
public final class BusDeEventosEnMemoria implements BusDeEventos {

    private static final Logger REGISTRO = Logger.getLogger(BusDeEventosEnMemoria.class.getName());

    private final Map<Class<? extends Evento>, CopyOnWriteArrayList<Suscriptor<?>>> suscriptoresPorTipoDeEvento =
            new ConcurrentHashMap<>();

    @Override
    public <T extends Evento> void suscribir(Class<T> tipoDeEvento, Suscriptor<? super T> suscriptor) {
        exigirNoNulo(tipoDeEvento, "tipoDeEvento");
        exigirNoNulo(suscriptor, "suscriptor");
        suscriptoresPorTipoDeEvento
                .computeIfAbsent(tipoDeEvento, tipo -> new CopyOnWriteArrayList<>())
                .addIfAbsent(suscriptor);
    }

    @Override
    public <T extends Evento> boolean desuscribir(Class<T> tipoDeEvento, Suscriptor<? super T> suscriptor) {
        exigirNoNulo(tipoDeEvento, "tipoDeEvento");
        exigirNoNulo(suscriptor, "suscriptor");
        List<Suscriptor<?>> suscriptoresDelTipo = suscriptoresPorTipoDeEvento.get(tipoDeEvento);
        return suscriptoresDelTipo != null && suscriptoresDelTipo.remove(suscriptor);
    }

    @Override
    public void publicar(Evento evento) {
        exigirNoNulo(evento, "evento");
        for (Class<? extends Evento> tipoRegistrado : tiposRegistradosCompatiblesCon(evento)) {
            List<Suscriptor<?>> suscriptoresDelTipo = suscriptoresPorTipoDeEvento.get(tipoRegistrado);
            if (suscriptoresDelTipo == null) {
                continue;
            }
            for (Suscriptor<?> suscriptor : suscriptoresDelTipo) {
                notificar(suscriptor, evento);
            }
        }
    }

    @Override
    public int cantidadDeSuscriptores(Class<? extends Evento> tipoDeEvento) {
        exigirNoNulo(tipoDeEvento, "tipoDeEvento");
        List<Suscriptor<?>> suscriptoresDelTipo = suscriptoresPorTipoDeEvento.get(tipoDeEvento);
        return suscriptoresDelTipo == null ? 0 : suscriptoresDelTipo.size();
    }

    /**
     * Tipos con suscriptores a los que les interesa este evento: primero el tipo exacto
     * y después los tipos base (por ejemplo, {@code Evento.class}).
     */
    private List<Class<? extends Evento>> tiposRegistradosCompatiblesCon(Evento evento) {
        Class<? extends Evento> tipoExacto = evento.getClass();
        List<Class<? extends Evento>> tiposCompatibles = new ArrayList<>();
        if (suscriptoresPorTipoDeEvento.containsKey(tipoExacto)) {
            tiposCompatibles.add(tipoExacto);
        }
        for (Class<? extends Evento> tipoRegistrado : suscriptoresPorTipoDeEvento.keySet()) {
            if (tipoRegistrado != tipoExacto && tipoRegistrado.isInstance(evento)) {
                tiposCompatibles.add(tipoRegistrado);
            }
        }
        return tiposCompatibles;
    }

    /**
     * Único lugar donde se hace la conversión genérica. Es segura porque un suscriptor
     * solo se notifica cuando el evento es instancia del tipo con el que se registró.
     */
    @SuppressWarnings("unchecked")
    private static void notificar(Suscriptor<?> suscriptor, Evento evento) {
        try {
            ((Suscriptor<Evento>) suscriptor).alRecibirEvento(evento);
        } catch (RuntimeException error) {
            REGISTRO.log(Level.WARNING,
                    "El suscriptor " + suscriptor.getClass().getSimpleName()
                            + " falló al procesar " + evento.nombreDelTipoDeEvento(), error);
        }
    }
}
