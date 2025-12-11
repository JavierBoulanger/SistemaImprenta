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
        String pathProduccion = "images/icons/dashboard";
        File folder = new File(pathProduccion);

        if (!folder.exists()) {
             folder = new File("src/main/resources/images/icons/dashboard");
        }

        if (!folder.exists()) {
            System.err.println("Carpeta no encontrada: " + folder.getAbsolutePath());
            return;
        }

        Map<String, String> keys = Map.ofEntries(
                Map.entry("img.logo", "Logo del Sistema"),
                Map.entry("img.audit", "Ícono Auditoria"),
                Map.entry("img.configuration", "Ícono Configuración"),
                Map.entry("img.customers", "Ícono Clientes"),
                Map.entry("img.users", "Ícono Usuarios"),
                Map.entry("img.orders", "Ícono Pedidos"),
                Map.entry("img.products", "Ícono Productos"),
                Map.entry("img.reports", "Ícono Reportes"),
                Map.entry("img.payments", "Ícono Pagos"),
                Map.entry("img.health", "Ícono Rendimiento")
                
        );

        List<String> images = listImages(folder);

        for (Map.Entry<String, String> entry : keys.entrySet()) {
            String key = entry.getKey();
            String label = entry.getValue();
            String currentValue = ConfigUtil.get(key);

            ComboBox<String> comboBox = new ComboBox<>();
            comboBox.setPrefWidth(350);
            comboBox.getItems().addAll(images);

            String selectedImage = null;

            
            if (currentValue != null && images.contains(currentValue)) {
                selectedImage = currentValue;
            } 
            
            else if (currentValue != null) {
                
                String fileNameSaved = new File(currentValue).getName(); 
                
                for (String imgOption : images) {
                    String fileNameOption = new File(imgOption).getName();
                    
                    if (fileNameOption.equalsIgnoreCase(fileNameSaved)) {
                        selectedImage = imgOption;
                        break; 
                    }
                }
            }
            
            if (selectedImage == null) {
                
                String keyword = key.replace("img.", "");
                for (String img : images) {
                    if (img.toLowerCase().contains(keyword)) {
                        selectedImage = img;
                        break;
                    }
                }
            }


            if (selectedImage != null) {
                comboBox.setValue(selectedImage);
            } else if (!images.isEmpty()) {
                comboBox.setValue(images.get(0));
            }
 

            Label lbl = new Label(label);
            lbl.setPrefWidth(150);
            HBox row = new HBox(10, lbl, comboBox);
            vboxImagenes.getChildren().add(row);

            imageSelectors.put(key, comboBox);
        }
    }

    private List<String> listImages(File folder) {
        List<String> images = new ArrayList<>();
        File[] files = folder.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().matches(".*\\.(png|jpg|jpeg|gif)$")) {
                    images.add("file:images/icons/dashboard/" + file.getName());
                }
            }
        }
        Collections.sort(images);
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
