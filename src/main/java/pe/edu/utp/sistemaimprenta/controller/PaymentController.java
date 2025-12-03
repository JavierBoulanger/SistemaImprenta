package pe.edu.utp.sistemaimprenta.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pe.edu.utp.sistemaimprenta.dao.PaymentDao;
import pe.edu.utp.sistemaimprenta.model.*;
import pe.edu.utp.sistemaimprenta.util.FxmlPath;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PaymentController {

    @FXML
    private Label lblTotalIngresos;
    @FXML
    private Label lblPagosPendientes;
    @FXML
    private Label lblPagosCompletados;
    @FXML
    private VBox listaPagos;
    @FXML
    private Button btnFiltroTodos;
    @FXML
    private Button btnFiltroCompletados;
    @FXML
    private Button btnFiltroPendientes;

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentDao paymentDao = new PaymentDao();
    private List<Payment> todosPagos = new ArrayList<>();

    @FXML
    public void initialize() {
        log.info("Inicializando PaymentController con datos desde la base de datos...");
        cargarPagosDesdeBD();
        actualizarResumen();
        cargarPagos(todosPagos);

        btnFiltroTodos.setOnAction(e -> cargarPagos(todosPagos));
        btnFiltroCompletados.setOnAction(e -> cargarPagos(filtrarPorEstado(PaymentStatus.PAGADO)));
        btnFiltroPendientes.setOnAction(e -> cargarPagos(filtrarPorEstado(PaymentStatus.PENDIENTE)));
    }

    private void cargarPagosDesdeBD() {
        todosPagos = paymentDao.listarPagos();
        log.info("Pagos cargados desde BD: {}", todosPagos.size());
    }

    private void cargarPagos(List<Payment> pagos) {
        listaPagos.getChildren().clear();
        for (Payment pago : pagos) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(FxmlPath.PAYMENT_ITEM.getPath()));
                HBox item = loader.load();
                PaymentItemController controller = loader.getController();
                controller.setPago(pago);
                listaPagos.getChildren().add(item);
            } catch (IOException e) {
                log.error("Error al cargar PaymentItem.fxml", e);
            }
        }
    }

    private void actualizarResumen() {
        double totalIngresos = paymentDao.obtenerTotalIngresosMes();
        int pendientes = paymentDao.contarPagosPendientesReales();
        int completados = paymentDao.contarPorEstado(PaymentStatus.PAGADO);

        lblTotalIngresos.setText(String.format("S/ %.2f", totalIngresos));
        lblPagosPendientes.setText(pendientes+ "");
        lblPagosCompletados.setText(completados+ "");

        log.debug("Resumen actualizado desde BD: ingresos=S/{}, pendientes={}, completados={}",
                totalIngresos, pendientes, completados);
    }

    private List<Payment> filtrarPorEstado(PaymentStatus estado) {
        List<Payment> filtrados = new ArrayList<>();

        for (Payment p : todosPagos) {
            if (estado == PaymentStatus.PENDIENTE) {
                if (p.getEstadoPago() == PaymentStatus.PENDIENTE
                        || p.getEstadoPago() == PaymentStatus.PARCIAL) {
                    filtrados.add(p);
                }
            } else {
                if (p.getEstadoPago() == estado) {
                    filtrados.add(p);
                }
            }
        }

        log.info("Filtrados {} pagos con estado {}", filtrados.size(), estado);
        return filtrados;
    }

}
