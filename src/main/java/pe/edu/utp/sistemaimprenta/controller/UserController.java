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
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import pe.edu.utp.sistemaimprenta.dao.UserDao;
import pe.edu.utp.sistemaimprenta.model.User;
import pe.edu.utp.sistemaimprenta.model.UserType;
import pe.edu.utp.sistemaimprenta.util.EncryptPassword;
import pe.edu.utp.sistemaimprenta.util.Export;
import pe.edu.utp.sistemaimprenta.util.Message;
import pe.edu.utp.sistemaimprenta.util.Notification;
import pe.edu.utp.sistemaimprenta.util.NotificationType;
import pe.edu.utp.sistemaimprenta.util.UserAware;
import pe.edu.utp.sistemaimprenta.util.Validator;

public class UserController implements Initializable, UserAware {

    @FXML
    private Button btnActualizar;

    @FXML
    private ImageView btnBuscar;

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
    private ComboBox<String> cmbFiltro;

    @FXML
    private ComboBox<UserType> cmbRol;

    @FXML
    private TableColumn<User, String> colContrasena;

    @FXML
    private TableColumn<User, String> colEmail;

    @FXML
    private TableColumn<User, String> colFechaRegistro;

    @FXML
    private TableColumn<User, Integer> colId;

    @FXML
    private TableColumn<User, String> colNombre;

    @FXML
    private TableColumn<User, String> colTipo;

    @FXML
    private Label lblError;

    @FXML
    private Label lblFiltro;

    @FXML
    private TableView<User> tablaDatos;

    @FXML
    private TextField txtBuscar;

    @FXML
    private PasswordField txtContrasena;

    @FXML
    private TextField txtEmail;

    @FXML
    private TextField txtNombre;

    private ObservableList<User> listaUsuarios;
    private UserDao userDao;

    public UserController() {
        userDao = new UserDao();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Validator.limitarCaracteres(txtNombre, 30);
        Validator.limitarCaracteres(txtEmail, 30);
        btnRegistrar.setOnAction(e -> registrar());
        btnActualizar.setOnAction(e -> actualizar());
        btnEliminar.setOnAction(e -> eliminar());
        btnLimpiar.setOnAction(e -> limpiarCampos());
        btnBuscar.setOnMouseClicked(e -> buscarUsuario());

        btnExcel.setOnAction(this::exportarUsuariosExcel);
        btnCsv.setOnAction(this::exportarUsuariosCsv);
        cmbRol.setItems(FXCollections.observableArrayList(UserType.values()));

        colId.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getId()).asObject());
        colNombre.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getUsername()));
        colEmail.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getEmail()));
        colContrasena.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getPassword()));
        colTipo.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getType().toString()));
        colFechaRegistro.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getCreatedAt().toString()));

        listaUsuarios = FXCollections.observableArrayList(userDao.findAll());
        tablaDatos.setItems(listaUsuarios);

        tablaDatos.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                txtNombre.setText(newSel.getUsername());
                txtEmail.setText(newSel.getEmail());
                txtContrasena.setText(newSel.getPassword());
                cmbRol.setValue(newSel.getType());
            }
        });

        cmbFiltro.setItems(FXCollections.observableArrayList("Nombre", "Email", "Rol"));
        cmbFiltro.getSelectionModel().selectFirst();
        lblFiltro.setText(cmbFiltro.getValue());
        cmbFiltro.valueProperty().addListener((obs, o, n) -> lblFiltro.setText(n));
    }

    private void registrar() {
        String error = validateUserFields();
        if (error != null) {
            Message.showMessage(lblError, error, "red");
            Notification.showNotification("Validación", "", 4, NotificationType.ERROR);
            return;
        }

        User u = new User();
        llenarDatosUsuario(u);
        userDao.save(u,usuarioActual);
        Notification.showNotification("USUARIO", "Registrado con éxito", 4, NotificationType.SUCCESS);
        refrescarTabla();
        limpiarCampos();
    }

    private void actualizar() {
        User seleccionado = tablaDatos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            Message.showMessage(lblError, "Debe seleccionar un registro", "red");
            Notification.showNotification("USUARIO", "", 4, NotificationType.WARNING);
            return;
        }

        String error = validateUserFields();
        if (error != null) {
            Message.showMessage(lblError, error, "red");
            Notification.showNotification("Validación", "", 4, NotificationType.ERROR);
            return;
        }

        llenarDatosUsuario(seleccionado);
        userDao.update(seleccionado,usuarioActual);
        Notification.showNotification("USUARIO", "Actualizado con éxito", 4, NotificationType.SUCCESS);
        refrescarTabla();
        limpiarCampos();
    }

    private void eliminar() {
        User seleccionado = tablaDatos.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            userDao.delete(seleccionado.getId(),usuarioActual);
            refrescarTabla();
            limpiarCampos();
        }
    }

    private void buscarUsuario() {
        String filtro = cmbFiltro.getValue();
        String texto = txtBuscar.getText().trim().toLowerCase();

        if (texto.isEmpty()) {
            refrescarTabla();
            return;
        }

        var filtrados = userDao.findAll().stream().filter(u -> switch (filtro) {
            case "Nombre" ->
                u.getUsername().toLowerCase().contains(texto);
            case "Email" ->
                u.getEmail().toLowerCase().contains(texto);
            case "Rol" ->
                u.getType().toString().toLowerCase().contains(texto);
            default ->
                false;
        }).toList();

        listaUsuarios.setAll(filtrados);
    }

    private void llenarDatosUsuario(User u) {
        u.setUsername(txtNombre.getText());
        u.setEmail(txtEmail.getText());
        u.setPassword(EncryptPassword.encrypt(txtContrasena.getText()));
        u.setType(cmbRol.getValue());
    }

    private void limpiarCampos() {
        txtNombre.clear();
        txtEmail.clear();
        txtContrasena.clear();
        cmbRol.getSelectionModel().clearSelection();
    }

    private void refrescarTabla() {
        listaUsuarios.setAll(userDao.findAll());
    }

    private String validateUserFields() {
        if (txtNombre.getText().trim().isEmpty()) {
            return "El nombre de usuario es obligatorio";
        }
        if (txtEmail.getText().trim().isEmpty()) {
            return "El correo electrónico es obligatorio";
        }
        if (!Validator.isValidEmail(txtEmail.getText())) {
            return "El correo electrónico no es válido";
        }
        if (txtContrasena.getText().trim().isEmpty()) {
            return "La contraseña es obligatoria";
        }
        if (cmbRol.getValue() == null) {
            return "Debe seleccionar un rol";
        }
        return null;
    }

    private void exportarUsuariosExcel(ActionEvent e) {
        String[] headers = {"ID", "Usuario", "Email", "Tipo", "Fecha Registro"};
        Export.exportToExcel(
                tablaDatos.getScene().getWindow(),
                tablaDatos.getItems(),
                "Usuarios",
                headers,
                u -> new Object[]{
                    u.getId(),
                    u.getUsername(),
                    u.getEmail(),
                    u.getType(),
                    u.getCreatedAt()
                }
        );
    }

    private void exportarUsuariosCsv(ActionEvent e) {
        String[] headers = {"ID", "Usuario", "Email", "Tipo", "Fecha Registro"};
        Export.exportToCsv(
                tablaDatos.getScene().getWindow(),
                tablaDatos.getItems(),
                "Usuarios",
                headers,
                u -> new Object[]{
                    u.getId(),
                    u.getUsername(),
                    u.getEmail(),
                    u.getType(),
                    u.getCreatedAt()
                }
        );
    }

    private User usuarioActual;

    @Override
    public void setUsuarioActual(User usuarioActual) {
        this.usuarioActual = usuarioActual;
    }
}
