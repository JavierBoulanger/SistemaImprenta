package pe.edu.utp.sistemaimprenta.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import pe.edu.utp.sistemaimprenta.util.ConfigUtil;

import java.io.File;
import java.net.URL;
import java.util.*;

public class ConfigController implements Initializable {

    @FXML
    private TextField txtNombre;
    @FXML
    private TextField txtRuc;
    @FXML
    private TextField txtDireccion;
    @FXML
    private VBox vboxImagenes;
    @FXML
    private Button btnGuardarDatos;
    @FXML
    private Button btnGuardarImagenes;

    private final Map<String, ComboBox<String>> imageSelectors = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadData();
        loadImageSelectors();
        btnGuardarDatos.setOnAction(e -> saveCompanyData());
        btnGuardarImagenes.setOnAction(e -> saveImages());
    }

    private void loadData() {
        txtNombre.setText(ConfigUtil.get("empresa.nombre"));
        txtRuc.setText(ConfigUtil.get("empresa.ruc"));
        txtDireccion.setText(ConfigUtil.get("empresa.direccion"));
    }

    private void loadImageSelectors() {
        String dashboardIconsPath = "src/main/resources/images/icons/dashboard";
        File folder = new File(dashboardIconsPath);
        if (!folder.exists()) {
            return;
        }

        Map<String, String> keys = Map.ofEntries(
                Map.entry("img.logo", "Logo del Sistema"),
                Map.entry("img.user", "Imagen de Usuario"),
                Map.entry("img.iconClientes", "Ícono Clientes"),
                Map.entry("img.iconPedidos", "Ícono Pedidos"),
                Map.entry("img.iconProductos", "Ícono Productos"),
                Map.entry("img.iconConfiguracion", "Ícono Configuración")
        );

        List<String> images = listImages(folder);

        for (Map.Entry<String, String> entry : keys.entrySet()) {
            String key = entry.getKey();
            String label = entry.getValue();
            String currentValue = ConfigUtil.get(key);

            ComboBox<String> comboBox = new ComboBox<>();
            comboBox.setPrefWidth(500);
            comboBox.getItems().addAll(images);

            if (currentValue != null && images.contains(currentValue)) {
                comboBox.setValue(currentValue);
            } else if (!images.isEmpty()) {
                comboBox.setValue(images.get(0));
            }

            Label lbl = new Label(label);
            HBox row = new HBox(20, lbl, comboBox);
            vboxImagenes.getChildren().add(row);

            imageSelectors.put(key, comboBox);
        }
    }

    private List<String> listImages(File folder) {
        List<String> images = new ArrayList<>();
        for (File file : Objects.requireNonNull(folder.listFiles())) {
            if (file.isFile() && file.getName().matches(".*\\.(png|jpg|jpeg|gif)$")) {
                images.add("/images/icons/dashboard/" + file.getName());
            }
        }
        return images;
    }

    private void saveCompanyData() {
        ConfigUtil.set("empresa.nombre", txtNombre.getText());
        ConfigUtil.set("empresa.ruc", txtRuc.getText());
        ConfigUtil.set("empresa.direccion", txtDireccion.getText());
        ConfigUtil.save();
        showAlert("Datos de empresa guardados correctamente.");
    }

    private void saveImages() {
        for (var entry : imageSelectors.entrySet()) {
            ConfigUtil.set(entry.getKey(), entry.getValue().getValue());
        }
        ConfigUtil.save();
        showAlert("Imágenes guardadas correctamente. Reinicie para aplicar los cambios.");
    }

    private void showAlert(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
    }
}
