package taberna.bus;

import taberna.eventos.Evento;

/**
 * Abstracción del Event Bus. Los componentes de la taberna solo conocen esta
 * interfaz, nunca a otros componentes: ese es el desacoplamiento del patrón
 * Publisher/Subscriber.
 */
public interface BusDeEventos {

    /**
     * Registra un suscriptor para que sea notificado cuando se publique un evento
     * de {@code tipoDeEvento} (o de cualquiera de sus subtipos). Si el mismo
     * suscriptor ya estaba registrado para ese tipo, no se duplica.
     */
    <T extends Evento> void suscribir(Class<T> tipoDeEvento, Suscriptor<? super T> suscriptor);

    /**
     * Quita un suscriptor de un tipo de evento.
     *
     * @return {@code true} si el suscriptor estaba registrado y fue removido
     */
    <T extends Evento> boolean desuscribir(Class<T> tipoDeEvento, Suscriptor<? super T> suscriptor);

    /** Notifica el evento a todos los suscriptores registrados para su tipo. */
    void publicar(Evento evento);

    /** Cantidad de suscriptores registrados exactamente para ese tipo de evento. */
    int cantidadDeSuscriptores(Class<? extends Evento> tipoDeEvento);
}
