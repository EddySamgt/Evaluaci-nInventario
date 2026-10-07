package service;

import model.Producto;
import repository.ProductoRepository;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Servicio que encapsula la lógica de negocio para el gestión de productos.
 * <p>
 * Coordina entre el repositorio (persistencia) y valida las operaciones.
 * No conoce nada de la capa de presentación (UI).
 */
public class ProductoService {

    private final ProductoRepository repositorio;
    // Contador atómico para generar IDs únicos de forma thread-safe (útil si en futuro se usa concurrencia)
    private final AtomicLong idCounter;

    public ProductoService() {
        this.repositorio = new ProductoRepository();
        // Inicializamos el contador basado en el ID máximo existente o 0 si no hay productos
        this.idCounter = new AtomicLong(obtenerIdMaximoExistente() + 1);
    }

    /**
     * Obtiene el ID máximo actual de los productos almacenados.
     * <p>
     * Se usa para inicializar el contador al crear el servicio.
     *
     * @return el ID mayor encontrado, o 0 si no hay productos o error al cargar
     */
    private long obtenerIdMaximoExistente() {
        try {
            List<Producto> productos = repositorio.cargarProductos();
            return productos.stream()
                    .mapToLong(Producto::getId)
                    .max()
                    .orElse(0L);
        } catch (ProductoRepository.PersistenciaException e) {
            // Si no podemos leer, asumimos que no hay productos y empezamos en 1
            // En una aplicación real, podríamos logar este error
            return 0L;
        }
    }

    /**
     * Genera un nuevo ID único para un producto.
     *
     * @return el próximo ID disponible
     */
    private Long generarId() {
        return idCounter.getAndIncrement();
    }

    /**
     * Agrega un nuevo producto al repositorio.
     * <p>
     * Asigna un ID generado automáticamente antes de guardar.
     *
     * @param producto producto a agregar (su ID será ignorado y se asignará uno nuevo)
     * @throws ProductoRepository.PersistenciaException si hay error al guardar
     */
    public void agregarProducto(Producto producto) throws ProductoRepository.PersistenciaException {
        Objects.requireNonNull(producto, "El producto no puede ser nulo");
        // Delegamos la validación a la clase ValidadorProducto
        String error = ValidadorProducto.validarProducto(
                producto.getNombre(),
                producto.getCategoria(),
                producto.getCantidad() == null ? null : producto.getCantidad().toString(),
                producto.getPrecio() == null ? null : producto.getPrecio().toString(),
                producto.getEstado()
        );
        if (error != null) {
            throw new IllegalArgumentException(error);
        }

        // Asignamos un ID nuevo (sobrescribiendo cualquiera que tuviera)
        producto.setId(generarId());

        // Cargamos la lista actual, agregamos el producto y guardamos
        List<Producto> productos = repositorio.cargarProductos();
        productos.add(producto);
        repositorio.guardarProductos(productos);
    }

    /**
     * Edita un producto existente identificado por su ID.
     * <p>
     * Reemplaza el producto completo con el proporcionado (debe tener el mismo ID).
     *
     * @param producto producto con los datos actualizados (debe contener un ID válido)
     * @throws ProductoRepository.PersistenciaException si hay error al guardar
     * @throws IllegalArgumentException si el producto es nulo, falta ID o no existe un producto con ese ID
     */
    public void editarProducto(Producto producto) throws ProductoRepository.PersistenciaException {
        Objects.requireNonNull(producto, "El producto no puede ser nulo");
        Long id = producto.getId();
        if (id == null) {
            throw new IllegalArgumentException("El producto debe tener un ID para poder editarlo");
        }
        // Validamos usando ValidadorProducto
        String error = ValidadorProducto.validarProducto(
                producto.getNombre(),
                producto.getCategoria(),
                producto.getCantidad() == null ? null : producto.getCantidad().toString(),
                producto.getPrecio() == null ? null : producto.getPrecio().toString(),
                producto.getEstado()
        );
        if (error != null) {
            throw new IllegalArgumentException(error);
        }

        List<Producto> productos = repositorio.cargarProductos();
        boolean encontrado = false;
        for (int i = 0; i < productos.size(); i++) {
            if (Objects.equals(productos.get(i).getId(), id)) {
                productos.set(i, producto);
                encontrado = true;
                break;
            }
        }
        if (!encontrado) {
            throw new IllegalArgumentException("No existe un producto con el ID: " + id);
        }

        repositorio.guardarProductos(productos);
    }

    /**
     * Elimina un producto por su ID.
     *
     * @param id identificador del producto a eliminar
     * @throws ProductoRepository.PersistenciaException si hay error al guardar
     * @throws IllegalArgumentException si el ID es nulo o no existe un producto con ese ID
     */
    public void eliminarProducto(Long id) throws ProductoRepository.PersistenciaException {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser nulo");
        }
        List<Producto> productos = repositorio.cargarProductos();
        boolean eliminado = productos.removeIf(p -> Objects.equals(p.getId(), id));
        if (!eliminado) {
            throw new IllegalArgumentException("No existe un producto con el ID: " + id);
        }
        repositorio.guardarProductos(productos);
    }

    /**
     * Obtiene la lista completa de productos.
     *
     * @return lista de productos (puede estar vacía)
     * @throws ProductoRepository.PersistenciaException si hay error al leer
     */
    public List<Producto> listarProductos() throws ProductoRepository.PersistenciaException {
        return repositorio.cargarProductos();
    }

    /**
     * Busca un producto por su ID.
     *
     * @param id identificador del producto a buscar
     * @return el producto encontrado o null si no existe
     * @throws ProductoRepository.PersistenciaException si hay error al leer
     */
    public Producto buscarPorId(Long id) throws ProductoRepository.PersistenciaException {
        if (id == null) {
            return null;
        }
        List<Producto> productos = repositorio.cargarProductos();
        for (Producto p : productos) {
            if (Objects.equals(p.getId(), id)) {
                return p;
            }
        }
        return null;
    }

    /**
     * Método main temporal para probar la persistencia.
     * <p>
     * Agrega un producto de ejemplo, luego cierra y vuelve a cargar la lista para verificar que se guardó correctamente.
     * <p>
     * NOTA: Este método es solo para prueba y debe eliminarse o comentarse antes de integrar con la UI.
     */
    public static void main(String[] args) {
        System.out.println("=== Inicio de prueba temporal de ProductoService ===");
        ProductoService servicio = new ProductoService();

        try {
            // Limpiamos cualquier dato previo para una prueba limpia (opcional)
            // En un escenario real, no haríamos esto
            List<Producto> productosIniciales = servicio.listarProductos();
            System.out.println("Productos iniciales: " + productosIniciales.size());

            // Creamos un producto de prueba
            Producto prueba = new Producto();
            prueba.setNombre("Producto de prueba");
            prueba.setCategoria("Categoria de prueba");
            prueba.setCantidad(10);
            prueba.setPrecio(new java.math.BigDecimal("99.99"));
            prueba.setEstado(Producto.Estado.DISPONIBLE);
            // Nota: no establecemos ID porque el servicio lo generará

            // Agregamos el producto
            servicio.agregarProducto(prueba);
            System.out.println("Producto agregado con ID: " + prueba.getId());

            // Volvemos a cargar la lista para verificar persistencia
            List<Producto> productosDespues = servicio.listarProductos();
            System.out.println("Productos después de agregar: " + productosDespues.size());
            for (Producto p : productosDespues) {
                System.out.println(" - " + p);
            }

            // Prueba de edición (opcional)
            Producto paraEditar = servicio.buscarPorId(prueba.getId());
            if (paraEditar != null) {
                paraEditar.setCantidad(15);
                servicio.editarProducto(paraEditar);
                System.out.println("Producto editado: cantidad actualizada a 15");
            }

            // Prueba de eliminación (opcional)
            // servicio.eliminarProducto(prueba.getId());
            // System.out.println("Producto eliminado");

        } catch (ProductoRepository.PersistenciaException e) {
            System.err.println("ERROR de persistencia: " + e.getMessage());
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            System.err.println("ERROR de validación: " + e.getMessage());
        }

        System.out.println("=== Fin de prueba temporal ===");
    }
}