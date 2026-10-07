package service;

import java.math.BigDecimal;
import model.Producto;

/**
 * Clase de validación para el producto.
 * Contiene métodos estáticos que validan los campos del formulario y devuelven
 * un mensaje de error si la validación falla, o null si es válida.
 * <p>
 * Esta clase evita que la lógica de validación se encuentre dentro del controlador UI.
 */
public class ValidadorProducto {

    /**
     * Valida el nombre del producto.
     * @param nombre el nombre a validar (puede ser null)
     * @return null si es válido, mensaje de error si no lo es
     */
    public static String validarNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return "El nombre del producto es obligatorio";
        }
        return null;
    }

    /**
     * Valida la categoría del producto.
     * @param categoria la categoría a validar (puede ser null)
     * @return null si es válido, mensaje de error si no lo es
     */
    public static String validarCategoria(String categoria) {
        if (categoria == null || categoria.trim().isEmpty()) {
            return "La categoría es obligatoria";
        }
        return null;
    }

    /**
     * Valida la cantidad del producto.
     * @param cantidadTexto el texto de la cantidad (puede ser null o vacío)
     * @return null si es válido, mensaje de error si no lo es
     */
    public static String validarCantidad(String cantidadTexto) {
        if (cantidadTexto == null || cantidadTexto.trim().isEmpty()) {
            return "La cantidad es obligatoria";
        }
        try {
            int valor = Integer.parseInt(cantidadTexto.trim());
            if (valor < 0) {
                return "La cantidad debe ser un número entero mayor o igual a cero";
            }
        } catch (NumberFormatException e) {
            return "La cantidad debe ser un número entero válido";
        }
        return null;
    }

    /**
     * Valida el precio del producto.
     * @param precioTexto el texto del precio (puede ser null o vacío)
     * @return null si es válido, mensaje de error si no lo es
     */
    public static String validarPrecio(String precioTexto) {
        if (precioTexto == null || precioTexto.trim().isEmpty()) {
            return "El precio es obligatorio";
        }
        try {
            BigDecimal valor = new BigDecimal(precioTexto.trim());
            if (valor.compareTo(BigDecimal.ZERO) <= 0) {
                return "El precio debe ser un número positivo mayor que cero";
            }
        } catch (NumberFormatException e) {
            return "El precio debe ser un número válido";
        }
        return null;
    }

    /**
     * Valida el estado del producto.
     * @param estado el estado a validar (puede ser null)
     * @return null si es válido, mensaje de error si no lo es
     */
    public static String validarEstado(Producto.Estado estado) {
        if (estado == null) {
            return "Debe seleccionar un estado";
        }
        return null;
    }

    /**
     * Valida todos los campos del producto obtenidos del formulario.
     * @param nombre texto del nombre
     * @param categoria texto de la categoría
     * @param cantidadTexto texto de la cantidad
     * @param precioTexto texto del precio
     * @param estado estado seleccionado (puede ser null)
     * @return null si todos son válidos, o el primer mensaje de error encontrado
     */
    public static String validarProducto(String nombre, String categoria,
                                         String cantidadTexto, String precioTexto,
                                         Producto.estado estado) {
        String error = validarNombre(nombre);
        if (error != null) return error;
        error = validarCategoria(categoria);
        if (error != null) return error;
        error = validarCantidad(cantidadTexto);
        if (error != null) return error;
        error = validarPrecio(precioTexto);
        if (error != null) return error;
        error = validarEstado(estado);
        if (error != null) return error;
        return null;
    }
}