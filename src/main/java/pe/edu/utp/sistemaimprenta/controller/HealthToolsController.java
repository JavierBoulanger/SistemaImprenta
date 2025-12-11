package pe.edu.utp.sistemaimprenta.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import java.net.URL;
import java.util.ResourceBundle;
import pe.edu.utp.sistemaimprenta.util.HealthCheckUtil;

public class HealthToolsController implements Initializable {

    @FXML private Circle circleDB;
    @FXML private Label lblEstadoDB;
    
    @FXML private Circle circleDisk;
    @FXML private Label lblEspacioDisco;
    
    @FXML private ProgressBar progressRam;
    @FXML private Label lblRamInfo;
    @FXML private Label lblSystemTime;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ejecutarDiagnostico();
    }

    @FXML
    private void onRefresh() {
        ejecutarDiagnostico();
    }

    private void ejecutarDiagnostico() {

        boolean dbOk = HealthCheckUtil.verificarConexionBD();
        if (dbOk) {
            circleDB.setFill(Color.GREEN);
            lblEstadoDB.setText("Conexión Establecida (ONLINE)");
            lblEstadoDB.setStyle("-fx-text-fill: green;");
        } else {
            circleDB.setFill(Color.RED);
            lblEstadoDB.setText("Sin Conexión (OFFLINE)");
            lblEstadoDB.setStyle("-fx-text-fill: red;");
        }

        lblEspacioDisco.setText(HealthCheckUtil.obtenerEspacioDisco());
        if (HealthCheckUtil.esEspacioCritico()) {
            circleDisk.setFill(Color.RED);
            lblEspacioDisco.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        } else {
            circleDisk.setFill(Color.GREEN);
            lblEspacioDisco.setStyle("-fx-text-fill: black;");
        }

        progressRam.setProgress(HealthCheckUtil.obtenerPorcentajeMemoria());
        lblRamInfo.setText(HealthCheckUtil.obtenerUsoMemoria());
        
        lblSystemTime.setText(java.time.LocalDateTime.now().toString());
    }
}