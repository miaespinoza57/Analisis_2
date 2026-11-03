package taberna.util;

import java.util.Objects;

/**
 * Validaciones reutilizables con mensajes claros en español.
 * Se usan sobre todo en los constructores de los eventos para garantizar que
 * un evento, una vez creado, siempre sea válido e inmutable.
 */
public final class Validaciones {

    private Validaciones() {
        // Clase utilitaria: no se instancia.
    }

    public static <T> T exigirNoNulo(T valor, String nombreDelCampo) {
        return Objects.requireNonNull(valor, "El campo '" + nombreDelCampo + "' no puede ser nulo");
    }

    public static String exigirTextoNoVacio(String texto, String nombreDelCampo) {
        exigirNoNulo(texto, nombreDelCampo);
        if (texto.isBlank()) {
            throw new IllegalArgumentException("El campo '" + nombreDelCampo + "' no puede estar vacío");
        }
        return texto;
    }

    public static int exigirPositivo(int numero, String nombreDelCampo) {
        if (numero <= 0) {
            throw new IllegalArgumentException(
                    "El campo '" + nombreDelCampo + "' debe ser mayor que cero (recibido: " + numero + ")");
        }
        return numero;
    }
}
