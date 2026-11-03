package taberna.eventos;

import static taberna.util.Validaciones.exigirNoNulo;
import static taberna.util.Validaciones.exigirTextoNoVacio;

/**
 * Un renglón del pedido: qué se pidió y si es bebida o comida. Es inmutable.
 */
public record ItemDelPedido(String nombre, CategoriaDeItem categoria) {

    public ItemDelPedido {
        exigirTextoNoVacio(nombre, "nombre");
        exigirNoNulo(categoria, "categoria");
    }

    public static ItemDelPedido bebida(String nombre) {
        return new ItemDelPedido(nombre, CategoriaDeItem.BEBIDA);
    }

    public static ItemDelPedido comida(String nombre) {
        return new ItemDelPedido(nombre, CategoriaDeItem.COMIDA);
    }

    public boolean esBebida() {
        return categoria == CategoriaDeItem.BEBIDA;
    }

    public boolean esComida() {
        return categoria == CategoriaDeItem.COMIDA;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
