package pe.edu.utp.sistemaimprenta.controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import pe.edu.utp.sistemaimprenta.dao.UserDao;
import pe.edu.utp.sistemaimprenta.model.User;
import pe.edu.utp.sistemaimprenta.model.UserType;
import pe.edu.utp.sistemaimprenta.util.EncryptPassword;
import pe.edu.utp.sistemaimprenta.util.Message;
import pe.edu.utp.sistemaimprenta.util.Notification;
import pe.edu.utp.sistemaimprenta.util.NotificationType;
import pe.edu.utp.sistemaimprenta.util.Validator;


public class RegisterController implements Initializable {
    
    @FXML
    private Button btnEnter;
    
    @FXML
    private Label lblError;
    
    @FXML
    private PasswordField txtConfirmPassword;
    
    @FXML
    private TextField txtEmail;
    
    @FXML
    private PasswordField txtPassword;
    
    @FXML
    private TextField txtUsername;
    
    private UserDao userDao;
    
    public RegisterController() {
        userDao = new UserDao();
    }
  
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        btnEnter.setOnAction(e -> handleRegister());
    }
    
    private void handleRegister() {
        String username = txtUsername.getText();
        String email = txtEmail.getText();
        String password = txtPassword.getText();
        String confirmPassword = txtConfirmPassword.getText();
        
        String error = validateInputFieldsRegister(username, email, password, confirmPassword);
        if (error != null) {
            Message.showMessage(lblError, error, "red");
            Notification.showNotification("REGISTER", "ERROR!", 4, NotificationType.ERROR);
            return;
        }

        UserType defaultType = UserType.VENDEDOR;
        
        if (userDao.existsUser(username)) {
            Message.showMessage(lblError, "Nombre de usuario en uso", "red");
            Notification.showNotification("REGISTER", "ERROR!", 4, NotificationType.ERROR);
        } else if (userDao.existsEmail(email)) {
            Message.showMessage(lblError, "Correo electronico en uso", "red");
            Notification.showNotification("REGISTER", "ERROR!", 4, NotificationType.ERROR);
        } else if (userDao.save(new User(username, EncryptPassword.encrypt(password),email , defaultType),null)) {
            Message.showMessage(lblError, "Registro exitoso", "green");
            Notification.showNotification("Register", "Registro exitoso", 4, NotificationType.SUCCESS);
            clear();
        } else {
            Message.showMessage(lblError, "Error al registrar usuario", "red");
            Notification.showNotification("REGISTER", "ERROR!", 4, NotificationType.ERROR);
        }
        
    }
    
    private void clear() {
        txtUsername.clear();
        txtEmail.clear();
        txtPassword.clear();
        txtConfirmPassword.clear();
    }
    
    public static String validateInputFieldsRegister(String username, String email, String password, String confirmPassword) {
        if (username == null || username.trim().isEmpty()) {
            return "El nombre de usuario es obligatorio";
        }
        if (email == null || email.trim().isEmpty()) {
            return "El correo electrónico es obligatorio";
        }
        if (!Validator.isValidEmail(email)) {
            return "El correo electrónico no es válido";
        }
        if (password == null || password.trim().isEmpty()) {
            return "La contraseña es obligatoria";
        }
        if (!password.equals(confirmPassword)) {
            return "Las contraseñas no coinciden";
        }
        return null;
    }
}
