package ui.app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ui.app.ProductoController;

/**
 * Clase principal de la aplicación JavaFX.
 * Configura el escenario y muestra la interfaz de usuario.
 */
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Inventario de Productos");

        // Crear el controlador y la vista
        ProductoController controller = new ProductoController();
        Scene scene = controller.getScene();

        // Establecer el escenario
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}