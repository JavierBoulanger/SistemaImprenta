package pe.edu.utp.sistemaimprenta.controller;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RegisterControllerTest {

    @Test
    void testUsuarioVacio() {
        String error = RegisterController.validateInputFieldsRegister("", "test@mail.com", "1234", "1234");
        assertEquals("El nombre de usuario es obligatorio", error);
    }

    @Test
    void testEmailVacio() {
        String error = RegisterController.validateInputFieldsRegister("user", "", "1234", "1234");
        assertEquals("El correo electrónico es obligatorio", error);
    }

    @Test
    void testEmailInvalido() {
        String error = RegisterController.validateInputFieldsRegister("user", "malcorreo", "1234", "1234");
        assertEquals("El correo electrónico no es válido", error);
    }

    @Test
    void testPasswordVacia() {
        String error = RegisterController.validateInputFieldsRegister("user", "test@mail.com", "", "1234");
        assertEquals("La contraseña es obligatoria", error);
    }

    @Test
    void testPasswordsNoCoinciden() {
        String error = RegisterController.validateInputFieldsRegister("user", "test@mail.com", "1234", "0000");
        assertEquals("Las contraseñas no coinciden", error);
    }

    @Test
    void testValidacionOk() {
        String error = RegisterController.validateInputFieldsRegister("user", "test@mail.com", "1234", "1234");
        assertNull(error);
    }
}
