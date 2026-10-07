package ui.app;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import service.ProductoService;
import model.Producto;
import service.ValidadorProducto;
import javafx.scene.control.Alert.AlertType;
import java.math.BigDecimal;

/**
 * Controlador de la interfaz de usuario para la gestión de productos.
 * Construye la escena, maneja los eventos y coordina con el servicio.
 */
public class ProductoController {

    private final ProductoService servicio;
    private final TableView<Producto> tablaProductos;
    private final ObservableList<Producto> datosTabla;

    // Campos del formulario
    private final TextField fieldNombre;
    private final ComboBox<String> fieldCategoria;
    private final TextField fieldCantidad;
    private final TextField fieldPrecio;
    private final ComboBox<Producto.Estado> fieldEstado;

    public ProductoController() {
        this.servicio = new ProductoService();
        this.datosTabla = FXCollections.observableArrayList();

        // Inicializar componentes de UI
        fieldNombre = new TextField();
        fieldNombre.setPromptText("Nombre del producto");

        fieldCategoria = new ComboBox<>();
        fieldCategoria.setEditable(true); // Permite escribir nuevas categorías
        fieldCategoria.setPromptText("Categoria");
        // Podemos agregar algunas sugerencias, pero no es obligatorio
        fieldCategoria.getItems().addAll("Electrónica", "Ropa", "Alimentos", "Hogar", "Otros");

        fieldCantidad = new TextField();
        fieldCantidad.setPromptText("Cantidad (entero >= 0)");

        fieldPrecio = new TextField();
        fieldPrecio.setPromptText("Precio ( > 0)");

        fieldEstado = new ComboBox<>();
        fieldEstado.setItems(FXCollections.observableArrayList(
                Producto.Estado.DISPONIBLE,
                Producto.Estado.AGOTADO,
                Producto.Estado.DESCONTINUADO
        ));
        fieldEstado.setPlaceholder(new Label("Seleccione estado"));

        // Tabla
        tablaProductos = new TableView<>();
        tablaProductos.setItems(datosTabla);
        tablaProductos.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        // Definir columnas
        TableColumn<Producto, Long> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setVisible(false); // Opcional: ocultar ID si no se quiere mostrar

        TableColumn<Producto, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));

        TableColumn<Producto, String> colCategoria = new TableColumn<>("Categoría");
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));

        TableColumn<Producto, Integer> colCantidad = new TableColumn<>("Cantidad");
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));

        TableColumn<Producto, BigDecimal> colPrecio = new TableColumn<>("Precio");
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        // Formatear el precio para mostrar con dos decimales
        colPrecio.setCellFactory(column -> {
            return new TableCell<Producto, BigDecimal>() {
                @Override
                protected void updateItem(BigDecimal precio, boolean empty) {
                    super.updateItem(precio, empty);
                    if (empty || precio == null) {
                        setText(null);
                    } else {
                        setText(String.format("%.2f", precio));
                    }
                }
            };
        });

        TableColumn<Producto, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        tablaProductos.getColumns().addAll(colId, colNombre, colCategoria, colCantidad, colPrecio, colEstado);

        // Ajustar ancho de columnas (opcional)
        colNombre.setMinWidth(150);
        colCategoria.setMinWidth(100);
        colCantidad.setMinWidth(80);
        colPrecio.setMinWidth(80);
        colEstado.setMinWidth(100);

        // Cargar datos iniciales
        cargarDatosIniciales();

        // Configurar evento de selección en la tabla
        tablaProductos.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                cargarProductoEnFormulario(newSelection);
            }
        });

        // Configurar atajos de teclado (opcional): Enter en campos para agregar/editar
        configurarAtajosTeclado();
    }

    /**
     * Carga la lista de productos desde el servicio y la muestra en la tabla.
     */
    private void cargarDatosIniciales() {
        try {
            datosTabla.clear();
            datosTabla.addAll(servicio.listarProductos());
        } catch (Exception e) {
            mostrarError("Error al cargar los datos", "No se pudieron cargar los productos desde el archivo: " + e.getMessage());
        }
    }

    /**
     * Llena el formulario con los datos del producto seleccionado.
     * @param producto el producto a mostrar en el formulario
     */
    private void cargarProductoEnFormulario(Producto producto) {
        fieldNombre.setText(producto.getNombre());
        fieldCategoria.setValue(producto.getCategoria());
        fieldCantidad.setText(producto.getCantidad() != null ? producto.getCantidad().toString() : "");
        fieldPrecio.setText(producto.getPrecio() != null ? producto.getPrecio().toString() : "");
        fieldEstado.setValue(producto.getEstado());
    }

    /**
     * Limpia el formulario y descarta la selección en la tabla.
     */
    private void limpiarFormulario() {
        fieldNombre.clear();
        fieldCategoria.getSelectionModel().clearSelection();
        fieldCategoria.getEditor().clear(); // Limpiar el editor si es editable
        fieldCantidad.clear();
        fieldPrecio.clear();
        fieldEstado.getSelectionModel().clearSelection();
        tablaProductos.getSelectionModel().clearSelection();
    }

    /**
     * Configura atajos de teclado: Enter en los campos de texto para agregar o editar.
     */
    private void configurarAtajosTeclado() {
        // Cuando se presiona Enter en cualquier campo, intentar agregar o editar
        KeyEventHandler enterHandler = (KeyEvent event) -> {
            if (event.getCode() == KeyCode.ENTER) {
                if (tablaProductos.getSelectionModel().getSelectedItem() == null) {
                    manejarAgregar();
                } else {
                    manejarEditar();
                }
                event.consume();
            }
        };

        fieldNombre.addEventFilter(KeyEvent.KEY_PRESSED, enterHandler);
        fieldCategoria.addEventFilter(KeyEvent.KEY_PRESSED, enterHandler);
        fieldCantidad.addEventFilter(KeyEvent.KEY_PRESSED, enterHandler);
        fieldPrecio.addEventFilter(KeyEvent.KEY_PRESSED, enterHandler);
        fieldEstado.addEventFilter(KeyEvent.KEY_PRESSED, enterHandler);
    }

    /**
     * Maneja el botón Agregar.
     */
    private void manejarAgregar() {
        // Recoger datos del formulario
        String nombre = fieldNombre.getText();
        String categoria = fieldCategoria.getValue() != null ? fieldCategoria.getValue().trim() : fieldCategoria.getEditor().getText() != null ? fieldCategoria.getEditor().getText().trim() : "";
        String cantidadTexto = fieldCantidad.getText();
        String precioTexto = fieldPrecio.getText();
        Producto.Estado estado = fieldEstado.getValue();

        // Validar usando ValidadorProducto
        String error = ValidadorProducto.validarProducto(nombre, categoria, cantidadTexto, precioTexto, estado);
        if (error != null) {
            mostrarError("Error de validación", error);
            return;
        }

        // Crear objeto Producto
        Producto nuevo = new Producto();
        nuevo.setNombre(nombre);
        nuevo.setCategoria(categoria);
        nuevo.setCantidad(Integer.parseInt(cantidadTexto.trim()));
        nuevo.setPrecio(new java.math.BigDecimal(precioTexto.trim()));
        nuevo.setEstado(estado);

        try {
            servicio.agregarProducto(nuevo);
            // Actualizar tabla
            datosTabla.add(nuevo);
            // Seleccionar la fila recién agregada
            tablaProductos.getSelectionModel().select(nuevo);
            tablaProductos.scrollTo(nuevo);
            limpiarFormulario();
            mostrarInformación("Producto agregado", "El producto ha sido agregado correctamente.");
        } catch (Exception e) {
            mostrarError("Error al agregar", "No se pudo agregar el producto: " + e.getMessage());
        }
    }

    /**
     * Maneja el botón Editar.
     */
    private void manejarEditar() {
        Producto seleccionado = tablaProductos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError("Error de selección", "Seleccione un producto de la tabla para editar.");
            return;
        }

        // Recoger datos del formulario
        String nombre = fieldNombre.getText();
        String categoria = fieldCategoria.getValue() != null ? fieldCategoria.getValue().trim() : fieldCategoria.getEditor().getText() != null ? fieldCategoria.getEditor().getText().trim() : "";
        String cantidadTexto = fieldCantidad.getText();
        String precioTexto = fieldPrecio.getText();
        Producto.Estado estado = fieldEstado.getValue();

        // Validar
        String error = ValidadorProducto.validarProducto(nombre, categoria, cantidadTexto, precioTexto, estado);
        if (error != null) {
            mostrarError("Error de validación", error);
            return;
        }

        // Actualizar el producto seleccionado con los nuevos datos
        seleccionado.setNombre(nombre);
        seleccionado.setCategoria(categoria);
        seleccionado.setCantidad(Integer.parseInt(cantidadTexto.trim()));
        seleccionado.setPrecio(new java.math.BigDecimal(precioTexto.trim()));
        seleccionado.setEstado(estado);

        try {
            servicio.editarProducto(seleccionado);
            // La tabla se actualiza automáticamente porque es observable y el objeto es el mismo
            tablaProductos.refresh(); // Forzar refresh para que se vean los cambios
            limpiarFormulario();
            mostrarInformación("Producto editado", "El producto ha sido actualizado correctamente.");
        } catch (Exception e) {
            mostrarError("Error al editar", "No se pudo editar el producto: " + e.getMessage());
        }
    }

    /**
     * Maneja el botón Eliminar.
     */
    private void manejarEliminar() {
        Producto seleccionado = tablaProductos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError("Error de selección", "Seleccione un producto de la tabla para eliminar.");
            return;
        }

        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("¿Eliminar producto seleccionado?");
        alert.setContentText("Producto: " + seleccionado.getNombre() + " (ID: " + seleccionado.getId() + ")");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    servicio.eliminarProducto(seleccionado.getId());
                    datosTabla.remove(seleccionado);
                    limpiarFormulario();
                    mostrarInformación("Producto eliminado", "El producto ha sido eliminado correctamente.");
                } catch (Exception e) {
                    mostrarError("Error al eliminar", "No se pudo eliminar el producto: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Maneja el botón Limpiar.
     */
    private void manejarLimpiar() {
        limpiarFormulario();
    }

    /**
     * Muestra una ventana de error.
     */
    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    /**
     * Muestra una ventana de información.
     */
    private void mostrarInformación(String titulo, String mensaje) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    /**
     * Devuelve la escena principal para ser mostrada en el escenario.
     * @return la escena completa con todos los componentes
     */
    public Scene getScene() {
        // --- Formulario ---
        GridPane formulario = new GridPane();
        formulario.setHgap(10);
        formulario.setVgap(10);
        formulario.setPadding(new Insets(15, 15, 0, 15));

        formulario.add(new Label("Nombre:"), 0, 0);
        formulario.add(fieldNombre, 1, 0);
        formulario.add(new Label("Categoría:"), 0, 1);
        formulario.add(fieldCategoria, 1, 1);
        formulario.add(new Label("Cantidad:"), 0, 2);
        formulario.add(fieldCantidad, 1, 2);
        formulario.add(new Label("Precio:"), 0, 3);
        formulario.add(fieldPrecio, 1, 3);
        formulario.add(new Label("Estado:"), 0, 4);
        formulario.add(fieldEstado, 1, 4);

        // --- Botones ---
        HBox botones = new HBox(10);
        botones.setPadding(new Insets(15));
        botones.setStyle("-fx-background-color: #f0f0f0;");

        Button btnAgregar = new Button("Agregar");
        Button btnEditar = new Button("Editar");
        Button btnEliminar = new Button("Eliminar");
        Button btnLimpiar = new Button("Limpiar");

        btnAgregar.setOnAction(e -> manejarAgregar());
        btnEditar.setOnAction(e -> manejarEditar());
        btnEliminar.setOnAction(e -> manejarEliminar());
        btnLimpiar.setOnAction(e -> manejarLimpiar());

        // Estilo uniforme para botones
        btnAgregar.setPrefWidth(100);
        btnEditar.setPrefWidth(100);
        btnEliminar.setPrefWidth(100);
        btnLimpiar.setPrefWidth(100);

        botones.getChildren().addAll(btnAgregar, btnEditar, btnEliminar, btnLimpiar);

        // --- Layout principal ---
        BorderPane root = new BorderPane();
        root.setTop(formulario);
        root.setCenter(tablaProductos);
        root.setBottom(botones);

        // Dar prioridad a la tabla para que expanda
        BorderPane.setMargin(tablaProductos, new Insets(10, 10, 0, 10));
        tablaProductos.setPrefHeight(300);
        BorderPane.setMargin(botones, new Insets(0, 10, 10, 10));
        BorderPane.setMargin(formulario, new Insets(10, 10, 0, 10));

        Scene scene = new Scene(root, 900, 600);
        return scene;
    }
}