package pe.edu.utp.sistemaimprenta.unit;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import pe.edu.utp.sistemaimprenta.controller.LoginController;

public class LoginControllerTest {

    @Test
    void testUsernameVacio() {
        String error = LoginController.validateInputFieldsLogin("", "1234");
        assertEquals("Debe ingresar su nombre de usuario", error);
    }

    @Test
    void testPasswordVacia() {
        String error = LoginController.validateInputFieldsLogin("user", "");
        assertEquals("Debe ingresar su contraseña", error);
    }

    @Test
    void testValidacionOk() {
        String error = LoginController.validateInputFieldsLogin("user", "1234");
        assertNull(error);
    }
}
