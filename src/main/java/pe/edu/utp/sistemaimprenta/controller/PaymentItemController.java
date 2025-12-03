package pe.edu.utp.sistemaimprenta.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pe.edu.utp.sistemaimprenta.model.*;

import java.time.format.DateTimeFormatter;

public class PaymentItemController {

    @FXML
    private ImageView iconView;
    @FXML
    private Label lblTitulo;
    @FXML
    private Label lblDetalle;
    @FXML
    private Label lblMonto;
    @FXML
    private Label lblEstado;

    private static final Logger log = LoggerFactory.getLogger(PaymentItemController.class);

    public void setPago(Payment pago) {
        if (pago == null) {
            log.warn("Se intentó cargar un pago nulo en PaymentItemController");
            return;
        }

        Order order = pago.getOrder();

        String clienteNombre = (order != null && order.getCustomer() != null)
                ? order.getCustomer().getName()
                : "Cliente desconocido";

        String codigoPedido = (order != null)
                ? "PED-" + order.getId()
                : "PED-???";

        lblTitulo.setText("Pago de " + clienteNombre + " - " + codigoPedido);

        String detalle = "";
        if (order != null && order.getDetails() != null && !order.getDetails().isEmpty()) {
            detalle = order.getDetails().get(0).getProduct().getName();
            if (order.getDetails().size() > 1) {
                detalle += " y " + (order.getDetails().size() - 1) + " más";
            }
        } else {
            detalle = "Sin productos";
        }

        lblDetalle.setText(detalle + " • "
                + pago.getFechaPago().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        lblMonto.setText(String.format("S/ %.2f", pago.getMonto()));

        configurarEstado(pago.getEstadoPago());
        configurarIcono(pago.getMetodoPago());

        log.debug("Cargado ítem de pago: id={}, cliente={}, estado={}",
                pago.getId(), clienteNombre, pago.getEstadoPago());
    }

    private void configurarEstado(PaymentStatus estado) {
        if (estado == null) {
            lblEstado.setText("Desconocido");
            return;
        }

        lblEstado.setText(estado.name());
        lblEstado.getStyleClass().clear();
        lblEstado.getStyleClass().addAll("estado-label");

        switch (estado) {
            case PAGADO ->
                lblEstado.getStyleClass().add("estado-completado");
            case PENDIENTE ->
                lblEstado.getStyleClass().add("estado-pendiente");
            case ANULADO ->
                lblEstado.getStyleClass().add("estado-anulado");
            case PARCIAL ->
                lblEstado.getStyleClass().add("estado-parcial"); 
            default ->
                lblEstado.getStyleClass().add("estado-desconocido");
        }
    }

    private void configurarIcono(PaymentMethod metodo) {
        if (metodo == null) {
            return;
        }

        String iconPath;
        switch (metodo) {
            case EFECTIVO ->
                iconPath = "/images/icons/money.png";
            case TARJETA ->
                iconPath = "/images/icons/card.png";
            case TRANSFERENCIA ->
                iconPath = "/images/icons/transfer.png";
            default ->
                iconPath = "/images/icons/interrogative.png";
        }

        try {
            iconView.setImage(new Image(getClass().getResourceAsStream(iconPath)));
        } catch (Exception e) {
            log.error("Error al cargar icono para método de pago {}: {}", metodo, e.getMessage());
        }
    }
}
