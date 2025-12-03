package pe.edu.utp.sistemaimprenta.controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import pe.edu.utp.sistemaimprenta.dao.CustomerDao;
import pe.edu.utp.sistemaimprenta.model.Customer;
import pe.edu.utp.sistemaimprenta.model.User;
import pe.edu.utp.sistemaimprenta.util.Export;
import pe.edu.utp.sistemaimprenta.util.Message;
import pe.edu.utp.sistemaimprenta.util.Notification;
import pe.edu.utp.sistemaimprenta.util.NotificationType;
import pe.edu.utp.sistemaimprenta.util.UserAware;
import pe.edu.utp.sistemaimprenta.util.Validator;

public class CustomerController implements Initializable, UserAware {

    @FXML
    private Button btnActualizar;

    @FXML
    private Button btnCsv;

    @FXML
    private Button btnEliminar;

    @FXML
    private Button btnExcel;

    @FXML
    private Button btnLimpiar;

    @FXML
    private Button btnRegistrar;

    @FXML
    private ImageView btnBuscar;

    @FXML
    private TableColumn<Customer, String> colNombre;

    @FXML
    private TableColumn<Customer, String> colDireccion;

    @FXML
    private TableColumn<Customer, String> colDni;

    @FXML
    private TableColumn<Customer, String> colEmail;

    @FXML
    private TableColumn<Customer, Integer> colId;

    @FXML
    private TableColumn<Customer, String> colTelefono;

    @FXML
    private TableColumn<Customer, String> colFecha;

    @FXML
    private ComboBox<String> cmbFiltro;

    @FXML
    private Label lblFiltro;

    @FXML
    private TableView<Customer> tablaDatos;

    @FXML
    private TextField txtApellidos;

    @FXML
    private TextField txtBuscar;

    @FXML
    private TextField txtDireccion;

    @FXML
    private TextField txtDni;

    @FXML
    private TextField txtEmail;

    @FXML
    private TextField txtNombres;

    @FXML
    private TextField txtTelefono;

    @FXML
    private Label lblError;

    private ObservableList<Customer> listaClientes;
    private CustomerDao customerDao;

    public CustomerController() {
        customerDao = new CustomerDao();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Validator.validarSoloNumeros(txtTelefono);
        Validator.validarSoloNumeros(txtDni);
        Validator.limitarCaracteres(txtTelefono, 9);
        Validator.limitarCaracteres(txtDni, 8);
        Validator.limitarCaracteres(txtEmail, 30);
        Validator.limitarCaracteres(txtNombres, 30);
        Validator.limitarCaracteres(txtApellidos, 30);
        Validator.limitarCaracteres(txtDireccion, 30);
        btnRegistrar.setOnAction(e -> registrar());
        btnActualizar.setOnAction(e -> actualizar());
        btnEliminar.setOnAction(e -> eliminar());
        btnLimpiar.setOnAction(e -> limpiarCampos());
        btnExcel.setOnAction(this::exportarClientesExcel);
        btnCsv.setOnAction(this::exportarClientesCsv);
        listaClientes = FXCollections.observableArrayList(customerDao.findAll());
        tablaDatos.setItems(listaClientes);

        colId.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getId()).asObject());
        colNombre.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getLastName() + " " + data.getValue().getName()));
        colEmail.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getEmail()));
        colTelefono.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getTelephoneNumber()));
        colDni.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getDni()));
        colDireccion.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getAddress()));
        colFecha.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getCreatedAt().toString()));

        tablaDatos.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldSel, newSel) -> mostrarClienteSeleccionado(newSel));

        cmbFiltro.setItems(FXCollections.observableArrayList("DNI", "Nombre", "Apellido", "Email", "Teléfono"));
        cmbFiltro.getSelectionModel().selectFirst();
        lblFiltro.setText(cmbFiltro.getValue());

        cmbFiltro.valueProperty().addListener((obs, oldVal, newVal) -> lblFiltro.setText(newVal));

        btnBuscar.setOnMouseClicked(e -> buscarCliente());

    }

    private void mostrarClienteSeleccionado(Customer c) {
        if (c == null) {
            return;
        }
        txtDni.setText(c.getDni());
        txtApellidos.setText(c.getLastName());
        txtNombres.setText(c.getName());
        txtTelefono.setText(c.getTelephoneNumber());
        txtEmail.setText(c.getEmail());
        txtDireccion.setText(c.getAddress());
    }

    private void buscarCliente() {
        String filtro = cmbFiltro.getValue();
        String texto = txtBuscar.getText().trim().toLowerCase();

        if (texto.isEmpty()) {
            refrescarTabla();
            return;
        }

        var filtrados = customerDao.findAll().stream().filter(c
                -> switch (filtro) {
            case "DNI" ->
                c.getDni().toLowerCase().contains(texto);
            case "Nombre" ->
                c.getName().toLowerCase().contains(texto);
            case "Apellido" ->
                c.getLastName().toLowerCase().contains(texto);
            case "Email" ->
                c.getEmail().toLowerCase().contains(texto);
            case "Teléfono" ->
                c.getTelephoneNumber().toLowerCase().contains(texto);
            default ->
                false;
        }).toList();

        listaClientes.setAll(filtrados);
    }

    private void registrar() {
        String error = validateCustomerFields();
        if (error != null) {
            Message.showMessage(lblError, error, "red");
            Notification.showNotification("Validación", "", 4, NotificationType.ERROR);
            return;
        }

        Customer c = new Customer();
        llenarDatosCliente(c);
        customerDao.save(c, usuarioActual);
        refrescarTabla();
        limpiarCampos();
    }

    private void actualizar() {
        Customer seleccionado = tablaDatos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            Message.showMessage(lblError, "Debe seleccionar un registro", "red");
            Notification.showNotification("USUARIO", "", 4, NotificationType.WARNING);
        }

        String error = validateCustomerFields();
        if (error != null) {
            Message.showMessage(lblError, error, "red");
            Notification.showNotification("Validación", "", 4, NotificationType.ERROR);
            return;
        }

        llenarDatosCliente(seleccionado);
        customerDao.update(seleccionado, usuarioActual);
        Notification.showNotification("REGISTRO CLIENTE", "Con éxito", 4, NotificationType.SUCCESS);
        refrescarTabla();
        limpiarCampos();
    }

    private void eliminar() {
        Customer seleccionado = tablaDatos.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            customerDao.delete(seleccionado.getId(), usuarioActual);
            refrescarTabla();
            limpiarCampos();

        }
    }

    private void refrescarTabla() {
        listaClientes.setAll(customerDao.findAll());
    }

    private void llenarDatosCliente(Customer c) {
        c.setDni(txtDni.getText());
        c.setName(txtNombres.getText());
        c.setLastName(txtApellidos.getText());
        c.setEmail(txtEmail.getText());
        c.setTelephoneNumber(txtTelefono.getText());
        c.setAddress(txtDireccion.getText());
    }

    private void limpiarCampos() {
        TextField[] fields = {txtDni, txtNombres, txtApellidos, txtEmail, txtTelefono, txtDireccion};
        for (TextField f : fields) {
            f.clear();
        }
    }

    private String validateCustomerFields() {
        if (txtDni.getText().trim().isEmpty()) {
            return "El DNI es obligatorio";
        }
        if (!Validator.isValidDNI(txtDni.getText())) {
            return "El DNI no es válido";
        }
        if (txtNombres.getText().trim().isEmpty()) {
            return "El nombre es obligatorio";
        }
        if (txtApellidos.getText().trim().isEmpty()) {
            return "El apellido es obligatorio";
        }
        if (txtTelefono.getText().trim().isEmpty()) {
            return "El teléfono es obligatorio";
        }

        if (!txtTelefono.getText().matches("\\d+")) {
            return "El teléfono solo debe contener números";
        }

        if (txtDireccion.getText().trim().isEmpty()) {
            return "La dirección es obligatoria";
        }

        if (txtEmail.getText().trim().isEmpty()) {
            return "El correo es obligatorio";
        }
        if (!Validator.isValidEmail(txtEmail.getText())) {
            return "El correo no es válido";
        }

        return null;
    }

    private void exportarClientesExcel(ActionEvent e) {
        String[] headers = {"ID", "DNI", "Apellidos", "Nombres", "Email", "Teléfono", "Dirección", "Fecha"};
        Export.exportToExcel(
                tablaDatos.getScene().getWindow(),
                tablaDatos.getItems(),
                "Clientes",
                headers,
                c -> new Object[]{
                    c.getId(),
                    c.getDni(),
                    c.getLastName(),
                    c.getName(),
                    c.getEmail(),
                    c.getTelephoneNumber(),
                    c.getAddress(),
                    c.getCreatedAt()
                }
        );
    }

    private void exportarClientesCsv(ActionEvent e) {
        String[] headers = {"ID", "DNI", "Apellidos", "Nombres", "Email", "Teléfono", "Dirección", "Fecha"};
        Export.exportToCsv(
                tablaDatos.getScene().getWindow(),
                tablaDatos.getItems(),
                "Clientes",
                headers,
                c -> new Object[]{
                    c.getId(),
                    c.getDni(),
                    c.getLastName(),
                    c.getName(),
                    c.getEmail(),
                    c.getTelephoneNumber(),
                    c.getAddress(),
                    c.getCreatedAt()
                }
        );
    }

    private User usuarioActual;

    @Override
    public void setUsuarioActual(User usuarioActual) {
        this.usuarioActual = usuarioActual;
    }
}
