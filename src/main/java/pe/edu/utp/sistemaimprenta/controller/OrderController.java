package pe.edu.utp.sistemaimprenta.controller;

import java.io.IOException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import pe.edu.utp.sistemaimprenta.dao.*;
import pe.edu.utp.sistemaimprenta.model.*;
import pe.edu.utp.sistemaimprenta.util.*;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class OrderController implements Initializable, UserAware {

    @FXML
    private Button btnActualizar;

    @FXML
    private Button btnAgregar;

    @FXML
    private Button btnAgregarDetalle;

    @FXML
    private ImageView btnBuscar;

    @FXML
    private Button btnCancelarPedido;

    @FXML
    private Button btnVerBoleta;

    @FXML
    private Button btnEliminar;

    @FXML
    private Button btnGuardarPedido;

    @FXML
    private Button btnQuitarDetalle;

    @FXML
    private ComboBox<Customer> cmbCliente;

    @FXML
    private ComboBox<OrderState> cmbEstado;

    @FXML
    private ComboBox<String> cmbFiltro;

    @FXML
    private ComboBox<Product> cmbProducto;

    @FXML
    private Label lblErrorDetalle;

    @FXML
    private Label lblErrorPedido;

    @FXML
    private Pane paneNuevoPedido;

    @FXML
    private TableView<Order> tablaDatos;

    @FXML
    private TableView<OrderDetail> tablaDetalle;

    @FXML
    private TextField txtBuscar;

    @FXML
    private TextField txtCantidad;

    @FXML
    private DatePicker dateEntrega;
    @FXML
    private Label lblTotal;

    @FXML
    private TableColumn<Order, Integer> colId;
    @FXML
    private TableColumn<Order, String> colVendedor;
    @FXML
    private TableColumn<Order, String> colCliente;
    @FXML
    private TableColumn<Order, String> colEstado;

    @FXML
    private TableColumn<Order, String> colFechaEntrega;
    @FXML
    private TableColumn<Order, String> colFechaRegistro;
    @FXML
    private TableColumn<Order, Double> colTotal;
    @FXML
    private TableColumn<OrderDetail, String> colProductoDetalle;
    @FXML
    private TableColumn<OrderDetail, Integer> colCantidadDetalle;
    @FXML
    private TableColumn<OrderDetail, Double> colPrecioDetalle;
    @FXML
    private TableColumn<OrderDetail, Double> colSubtotalDetalle;

    @FXML
    private TableColumn<Order, String> colEstadoPago;

    @FXML
    private Pane panePago;
    @FXML
    private Label lblSaldoPendiente;
    @FXML
    private Label lblTotalPagado;
    @FXML
    private Label lblTotalPedidoPago;
    @FXML
    private Label lblClientePago;
    @FXML
    private ComboBox<PaymentMethod> cmbMetodoPago;
    @FXML
    private TextField txtMontoPago;
    @FXML
    private Label lblErrorPago;
    @FXML
    private Button btnGuardarPago;
    @FXML
    private Button btnCancelarPago;

    @FXML
    private Button btnRegistrarPago;

    private final PaymentDao paymentDAO = new PaymentDao();
    private Order pedidoSeleccionado;
    private double montoPendiente;

    private ObservableList<Order> listaPedidos;
    private final ObservableList<OrderDetail> detalles = FXCollections.observableArrayList();

    private final OrderDao orderDao = new OrderDao();
    private final CustomerDao customerDao = new CustomerDao();
    private final ProductDao productDao = new ProductDao();

    private User usuarioActual;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarTabla();
        configurarDetalle();
        configurarCombos();
        configurarBotones();
        refrescarTabla();
        paneNuevoPedido.setVisible(false);
        panePago.setVisible(false);
    }

    @Override
    public void setUsuarioActual(User user) {
        this.usuarioActual = user;

        if (this.usuarioActual != null) {
            if (this.usuarioActual.getType().equals(UserType.ADMINISTRADOR)) 
                btnAgregar.setVisible(false);
        }
    }

    private void configurarTabla() {
        colId.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getId()).asObject());
        colVendedor.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getUser().getUsername()));
        colCliente.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getCustomer().getName()));
        colEstado.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getState().name()));
        colFechaEntrega.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getDeliveryDate() != null ? data.getValue().getDeliveryDate().toLocalDate().toString() : ""));
        colFechaRegistro.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getCreatedAt() != null ? data.getValue().getCreatedAt().toLocalDate().toString() : ""));
        colTotal.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty(data.getValue().getTotalAmount()).asObject());
        colEstadoPago.setCellValueFactory(data -> {
            Order pedido = data.getValue();
            double totalPagado = paymentDAO.obtenerTotalPagadoPorPedido(pedido.getId());
            double totalPedido = pedido.getTotalAmount();

            String estado;
            if (totalPagado <= 0) {
                estado = "Pendiente";
            } else if (totalPagado < totalPedido) {
                estado = "Parcial";
            } else {
                estado = "Pagado";
            }

            return new javafx.beans.property.SimpleStringProperty(estado);
        });
    }

    private void configurarDetalle() {
        colProductoDetalle.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getProduct().getName()));
        colCantidadDetalle.setCellValueFactory(d -> new javafx.beans.property.SimpleIntegerProperty(d.getValue().getQuantity()).asObject());
        colPrecioDetalle.setCellValueFactory(d -> new javafx.beans.property.SimpleDoubleProperty(d.getValue().getUnitPrice()).asObject());
        colSubtotalDetalle.setCellValueFactory(d -> new javafx.beans.property.SimpleDoubleProperty(d.getValue().getSubtotal()).asObject());
        tablaDetalle.setItems(detalles);
    }

    private void configurarCombos() {
        cmbFiltro.setItems(FXCollections.observableArrayList("Cliente", "Vendedor", "Estado"));
        cmbFiltro.getSelectionModel().selectFirst();

        cmbCliente.setItems(FXCollections.observableArrayList(customerDao.findAll()));
        cmbProducto.setItems(FXCollections.observableArrayList(productDao.findAll()));
        cmbEstado.setItems(FXCollections.observableArrayList(OrderState.values()));
        cmbMetodoPago.setItems(FXCollections.observableArrayList(PaymentMethod.values()));
    }

    private void configurarBotones() {

        btnVerBoleta.setOnAction(e -> mostrarBoleta());
        btnBuscar.setOnMouseClicked(e -> buscarPedido());
        btnActualizar.setOnAction(e -> refrescarTabla());
        btnAgregar.setOnAction(e -> paneNuevoPedido.setVisible(true));
        btnEliminar.setOnAction(e -> eliminarPedido());
        btnGuardarPedido.setOnAction(e -> registrarPedido());
        btnCancelarPedido.setOnAction(e -> paneNuevoPedido.setVisible(false));
        btnAgregarDetalle.setOnAction(e -> agregarDetalle());
        btnQuitarDetalle.setOnAction(e -> quitarDetalle());
        btnGuardarPago.setOnAction(e -> registrarPago());
        btnCancelarPago.setOnAction(e -> cancelarPago());

        btnRegistrarPago.setOnAction(e -> {
            pedidoSeleccionado = tablaDatos.getSelectionModel().getSelectedItem();

            if (pedidoSeleccionado == null) {
                Message.showMessage(lblErrorPedido, "Seleccione un pedido antes de registrar un pago", "red");
                return;
            }

            double totalPagado = paymentDAO.obtenerTotalPagadoPorPedido(pedidoSeleccionado.getId());
            double totalPedido = pedidoSeleccionado.getTotalAmount();

            if (totalPagado >= totalPedido) {
                Message.showMessage(lblErrorPedido, "Este pedido ya está completamente pagado. No puede registrar más pagos.", "red");
                return;
            }

            cargarDatosPedido(pedidoSeleccionado);
        });

    }

    private void agregarDetalle() {
        Product p = cmbProducto.getValue();
        if (p == null) {
            Message.showMessage(lblErrorDetalle, "Seleccione un producto", "red");
            return;
        }

        int cantidad;
        try {
            cantidad = Integer.parseInt(txtCantidad.getText());
            if (cantidad <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            Message.showMessage(lblErrorDetalle, "Cantidad inválida", "red");
            return;
        }

        double precio = p.getBasePrice();

        OrderDetail detalle = new OrderDetail(p, cantidad, precio);
        detalles.add(detalle);
        tablaDetalle.refresh();
        actualizarTotal();
    }

    private void quitarDetalle() {
        OrderDetail seleccionado = tablaDetalle.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            detalles.remove(seleccionado);
            actualizarTotal();
        } else {
            Message.showMessage(lblErrorDetalle, "Seleccione un detalle", "red");
        }
    }

    private void actualizarTotal() {
        double total = detalles.stream().mapToDouble(OrderDetail::getSubtotal).sum();
        lblTotal.setText(String.format("%.2f", total));
    }

    private void registrarPedido() {
        try {
            Customer cliente = cmbCliente.getValue();
            LocalDate fechaEntrega = dateEntrega.getValue();
            OrderState estado = cmbEstado.getValue();

            if (cliente == null || fechaEntrega == null || detalles.isEmpty() || estado == null) {
                Message.showMessage(lblErrorDetalle, "Complete todos los campos", "red");
                return;
            }

            Order pedido = new Order();
            pedido.setCustomer(cliente);
            pedido.setUser(usuarioActual);
            pedido.setState(estado);
            pedido.setDeliveryDate(fechaEntrega.atStartOfDay());
            pedido.setDetails(detalles);
            pedido.setTotalAmount(detalles.stream().mapToDouble(OrderDetail::getSubtotal).sum());

            boolean exito = orderDao.save(pedido, usuarioActual);
            if (exito) {
                Notification.showNotification("PEDIDO", "Pedido registrado con éxito", 4, NotificationType.SUCCESS);
                paneNuevoPedido.setVisible(false);
                refrescarTabla();
                detalles.clear();
                actualizarTotal();
            } else {
                Message.showMessage(lblErrorDetalle, "Error al registrar pedido", "red");
            }

        } catch (Exception e) {
            Message.showMessage(lblErrorDetalle, "Error: " + e.getMessage(), "red");
        }
    }

    private void eliminarPedido() {
        Order seleccionado = tablaDatos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            Message.showMessage(lblErrorPedido, "Seleccione un pedido", "red");
            return;
        }

        boolean exito = orderDao.delete(seleccionado.getId(), usuarioActual);
        if (exito) {
            Notification.showNotification("PEDIDO", "Pedido eliminado", 4, NotificationType.SUCCESS);
            refrescarTabla();
        } else {
            Message.showMessage(lblErrorPedido, "Error al eliminar pedido", "red");
        }
    }

    private void refrescarTabla() {
        List<Order> pedidos = orderDao.findAll();
        listaPedidos = FXCollections.observableArrayList(pedidos);
        tablaDatos.setItems(listaPedidos);
    }

    private void buscarPedido() {
        String texto = txtBuscar.getText().trim().toLowerCase();
        if (texto.isEmpty()) {
            refrescarTabla();
            return;
        }

        String filtro = cmbFiltro.getValue();
        List<Order> pedidos = orderDao.findAll();

        listaPedidos.setAll(pedidos.stream().filter(p -> {
            switch (filtro) {
                case "Cliente":
                    return p.getCustomer().getName().toLowerCase().contains(texto);
                case "Vendedor":
                    return p.getUser().getUsername().toLowerCase().contains(texto);
                case "Estado":
                    return p.getState().name().toLowerCase().contains(texto);
                default:
                    return false;
            }
        }).toList());
    }

    private void mostrarBoleta() {
        Order seleccionado = tablaDatos.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            Message.showMessage(lblErrorPedido, "Seleccione un pedido para ver la boleta", "red");
            return;
        }

        double totalPagado = paymentDAO.obtenerTotalPagadoPorPedido(seleccionado.getId());
        double totalPedido = seleccionado.getTotalAmount();

        if (totalPagado < totalPedido) {
            double pendiente = totalPedido - totalPagado;
            Message.showMessage(lblErrorPedido,
                    String.format("No puede generar la boleta. Aún hay un saldo pendiente de S/ %.2f", pendiente),
                    "red");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/TicketView.fxml"));
            Parent root = loader.load();

            TicketController controller = loader.getController();
            controller.cargarDatos(seleccionado);

            Stage stage = new Stage();
            stage.setTitle("Boleta - Pedido N° " + seleccionado.getId());
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            Message.showMessage(lblErrorPedido, "Error al mostrar boleta", "red");
        }
    }

    public void cargarDatosPedido(Order pedido) {
        this.pedidoSeleccionado = pedido;

        if (pedido == null) {
            return;
        }

        double totalPagado = paymentDAO.obtenerTotalPagadoPorPedido(pedido.getId());
        double totalPedido = pedido.getTotalAmount();
        montoPendiente = Math.max(0, totalPedido - totalPagado);

        lblClientePago.setText(pedido.getCustomer().getLastName() + " " + pedido.getCustomer().getName());
        lblTotalPedidoPago.setText(String.format("S/ %.2f", totalPedido));
        lblTotalPagado.setText(String.format("S/ %.2f", totalPagado));
        lblSaldoPendiente.setText(String.format("S/ %.2f", montoPendiente));

        txtMontoPago.clear();
        cmbMetodoPago.getSelectionModel().clearSelection();

        panePago.setVisible(true);
    }

    private boolean validarPago() {
        String montoTexto = txtMontoPago.getText().trim();
        PaymentMethod metodo = cmbMetodoPago.getValue();

        if (montoTexto.isEmpty()) {
            Message.showMessage(lblErrorPago, "Ingrese el monto del pago.", "red");
            return false;
        }

        double monto;
        try {
            monto = Double.parseDouble(montoTexto);
        } catch (NumberFormatException e) {
            Message.showMessage(lblErrorPago, "Monto inválido. Use solo números.", "red");
            return false;
        }

        if (monto <= 0) {
            Message.showMessage(lblErrorPago, "El monto debe ser mayor a cero", "red");
            return false;
        }

        if (monto > montoPendiente) {
            Message.showMessage(lblErrorPago, "El monto no puede ser mayor al saldo pendiente", "red");
            return false;
        }

        if (metodo == null) {
            Message.showMessage(lblErrorPago, "Seleccione un método de pago", "red");
            return false;
        }

        if (montoPendiente <= 0) {
            Message.showMessage(lblErrorPago, "El pedido ya está completamente pagado.", "red");
            return false;
        }

        return true;
    }

    private void registrarPago() {
        if (!validarPago()) {
            return;
        }

        double monto = Double.parseDouble(txtMontoPago.getText().trim());
        PaymentMethod metodo = cmbMetodoPago.getValue();

        Payment nuevoPago = new Payment();
        nuevoPago.setOrder(pedidoSeleccionado);
        nuevoPago.setMetodoPago(metodo);
        nuevoPago.setMonto(monto);
        nuevoPago.setFechaPago(LocalDateTime.now());

        boolean exito = paymentDAO.registrarPago(nuevoPago, usuarioActual);

        if (exito) {
            Message.showMessage(lblErrorPago, "Pago registrado correctamente.", "green");
            actualizarMontos();
        } else {
            Message.showMessage(lblErrorPago, "Error al registrar el pago", "red");
        }
    }

    private void actualizarMontos() {
        double totalPagado = paymentDAO.obtenerTotalPagadoPorPedido(pedidoSeleccionado.getId());
        double totalPedido = pedidoSeleccionado.getTotalAmount();
        montoPendiente = Math.max(0, totalPedido - totalPagado);

        lblTotalPagado.setText(String.format("S/ %.2f", totalPagado));
        lblSaldoPendiente.setText(String.format("S/ %.2f", montoPendiente));

        txtMontoPago.clear();
        cmbMetodoPago.getSelectionModel().clearSelection();

        if (montoPendiente <= 0) {
            Message.showMessage(lblErrorPago, "El pedido ha sido pagado completamente", "green");
            panePago.setVisible(false);
        }
    }

    private void cancelarPago() {
        limpiarFormulario();
        panePago.setVisible(false);
    }

    private void limpiarFormulario() {
        txtMontoPago.clear();
        cmbMetodoPago.getSelectionModel().clearSelection();
    }
}
