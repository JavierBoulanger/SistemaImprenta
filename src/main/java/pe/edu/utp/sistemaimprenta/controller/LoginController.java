package pe.edu.utp.sistemaimprenta.controller;

import javafx.fxml.Initializable;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.stage.Stage;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import pe.edu.utp.sistemaimprenta.dao.UserDao;
import pe.edu.utp.sistemaimprenta.model.User;
import pe.edu.utp.sistemaimprenta.model.UserType;
import pe.edu.utp.sistemaimprenta.util.FxmlPath;
import pe.edu.utp.sistemaimprenta.util.Message;
import pe.edu.utp.sistemaimprenta.util.Notification;
import pe.edu.utp.sistemaimprenta.util.NotificationType;
import pe.edu.utp.sistemaimprenta.util.ViewLoader;

public class LoginController implements Initializable {

    @FXML
    private Button btnEnter;

    @FXML
    private Label lblError;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private TextField txtUsername;
    
    private UserDao userDao;

    public LoginController() {
        userDao = new UserDao();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        btnEnter.setOnAction(e -> handleLogin());
    }

    private void handleLogin() {
        String u = txtUsername.getText();
        String p = txtPassword.getText();

        String error = validateInputFieldsLogin(u, p);
        if (error != null) {
            Message.showMessage(lblError, error, "red");
            Notification.showNotification("Login", "ERROR!", 4, NotificationType.ERROR);
            return;
        }
        /*
          User user = new User("Javier","ads","dsa",UserType.ADMINISTRADOR);
            
            Notification.showNotification("Login", "Ingreso exitoso", 4, NotificationType.SUCCESS);
            openDashboard(user);
            closeCurrentStage();
        */
        
        if (userDao.validateLogin(u, p)) {
            User user = userDao.getUser();
            Message.showMessage(lblError,"Inicio de sesión exitoso", "green");
            Notification.showNotification("Login", "Ingreso exitoso", 4, NotificationType.SUCCESS);
            openDashboard(user);
            closeCurrentStage();
        }else{
            Message.showMessage(lblError, "Credenciales incorrectas", "red");
            Notification.showNotification("Login", "ERROR!", 4, NotificationType.ERROR);
        }
        
        
    }
    
    private void openDashboard(User user) {
        DashboardController controller = ViewLoader.openWindowGetController(FxmlPath.DASHBOARD.getPath(), "Dashboard", false);
        if (controller != null) {
            controller.setUser(user);
        }
    }
    
    
    private void closeCurrentStage() {
        Stage currentStage = (Stage) txtUsername.getScene().getWindow();
        currentStage.close();
    }
    
    public static String validateInputFieldsLogin(String username ,String password) {
        if (username == null || username.trim().isEmpty()) {
            return "Debe ingresar su nombre de usuario";
        }
        if (password == null || password.trim().isEmpty()) {
            return "Debe ingresar su contraseña";
        }
        return null;
    }
}
