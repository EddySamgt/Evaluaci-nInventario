package repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.Producto;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Repositorio responsable de la persistencia de productos en formato JSON.
 * <p>
 * Maneja la lectura y escritura de {@code productos.json} en el directorio de trabajo.
 * Si el archivo no existe, se crea vacío al guardar.
 */
public class ProductoRepository {

    private static final String NOMBRE_ARCHIVO = "productos.json";
    private final ObjectMapper objectMapper;

    public ProductoRepository() {
        this.objectMapper = new ObjectMapper();
        // Configurar para aceptar propiedades desconocidas y serializar fechas si fuera necesario
        // (no necesario para este caso, pero buena práctica)
    }

    /**
     * Carga la lista de productos desde el archivo JSON.
     * <p>
     * Si el archivo no existe, devuelve una lista vacía y no lanza excepción.
     * Si existe pero está corrupto o no se puede leer, lanza PersistenciaException.
     *
     * @return lista de productos (puede estar vacía)
     * @throws PersistenciaException si ocurre un error de E/S o de parseo JSON
     */
    public List<Producto> cargarProductos() throws PersistenciaException {
        Path path = Paths.get(NOMBRE_ARCHIVO);
        if (!Files.exists(path)) {
            // Archivo no existe: devolvemos lista vacía
            return new ArrayList<>();
        }

        try {
            byte[] jsonData = Files.readAllBytes(path);
            // Si el archivo está vacío, devolvemos lista vacía
            if (jsonData.length == 0) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(jsonData, new TypeReference<List<Producto>>() {});
        } catch (IOException e) {
            throw new PersistenciaException("Error al leer el archivo de productos: " + e.getMessage(), e);
        }
    }

    /**
     * Guarda la lista de productos en el archivo JSON.
     * <p>
     * Sobrescribe el archivo existente o lo crea si no existe.
     * <p>
     * El directorio de trabajo es el desde donde se ejecuta la aplicación.
     *
     * @param productos lista de productos a guardar
     * @throws PersistenciaException si ocurre un error de E/S al escribir
     */
    public void guardarProductos(List<Producto> productos) throws PersistenciaException {
        Objects.requireNonNull(productos, "La lista de productos no puede ser nula");

        try {
            // Crear el archivo si no existe (Files.write crea el archivo si no existe y sobrescribe si existe)
            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(productos);
            Path path = Paths.get(NOMBRE_ARCHIVO);
            Files.write(path, json.getBytes());
        } catch (IOException e) {
            throw new PersistenciaException("Error al guardar el archivo de productos: " + e.getMessage(), e);
        }
    }

    /**
     * Excepción personalizada para errores de persistencia.
     * <p>
     * La capa de servicio o UI puede capturar esta excepción y mostrar un Alert al usuario.
     */
    public static class PersistenciaException extends Exception {
        public PersistenciaException(String message, Throwable cause) {
            super(message, cause);
        }

        public PersistenciaException(String message) {
            super(message);
        }
    }
}