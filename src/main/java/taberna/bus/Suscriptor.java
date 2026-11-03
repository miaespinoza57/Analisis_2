package taberna.bus;

import taberna.eventos.Evento;

/**
 * Cualquier objeto interesado en un tipo de evento. Al ser una interfaz con un
 * solo método también puede implementarse con una expresión lambda.
 *
 * @param <T> tipo de evento que sabe recibir
 */
@FunctionalInterface
public interface Suscriptor<T extends Evento> {

    void alRecibirEvento(T evento);
}
