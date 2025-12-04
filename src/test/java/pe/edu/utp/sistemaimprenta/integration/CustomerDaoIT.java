package pe.edu.utp.sistemaimprenta.integration;

import org.junit.jupiter.api.*;
import pe.edu.utp.sistemaimprenta.dao.CustomerDao;
import pe.edu.utp.sistemaimprenta.model.Customer;
import pe.edu.utp.sistemaimprenta.model.User;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CustomerDaoIT {

    private static CustomerDao dao;
    private static int generatedId;
    private static final User user = new User();

    @BeforeAll
    static void init() {
        dao = new CustomerDao();
        user.setId(1);
    }

    @Test
    @Order(1)
    @DisplayName("Debe guardar cliente en la base de datos")
    void testGuardarCliente() {
        Customer c = new Customer();
        c.setDni("99999999");
        c.setLastName("Prueba");
        c.setName("Cliente");
        c.setTelephoneNumber("987654321");
        c.setEmail("prueba@test.com");
        c.setAddress("Lima");

        boolean resultado = dao.save(c, user);
        assertTrue(resultado);

        generatedId = c.getId();

        assertTrue(generatedId > 0, "El ID generado debe ser mayor a 0");
        System.out.println("ID generado en prueba: " + generatedId);
    }

    @Test
    @Order(2)
    @DisplayName("Debe buscar cliente por ID")
    void testBuscarPorId() {
        Customer c = dao.findById(generatedId);
        assertNotNull(c);
        assertEquals("99999999", c.getDni());
    }

    @Test
    @Order(3)
    @DisplayName("Debe actualizar cliente existente")
    void testActualizarCliente() {
        Customer c = dao.findById(generatedId);
        assertNotNull(c);

        c.setName("Actualizado");

        boolean resultado = dao.update(c, user);
        assertTrue(resultado);

        Customer actualizado = dao.findById(generatedId);
        assertEquals("Actualizado", actualizado.getName());
    }

    @Test
    @Order(4)
    @DisplayName("Debe listar clientes sin errores")
    void testListarClientes() {
        List<Customer> lista = dao.findAll();
        assertNotNull(lista);
        assertTrue(lista.size() > 0);
    }

    @Test
    @Order(5)
    @DisplayName("Debe eliminar cliente por ID")
    void testEliminarCliente() {
        boolean resultado = dao.delete(generatedId, user);
        assertTrue(resultado);

        Customer eliminado = dao.findById(generatedId);
        assertNull(eliminado, "El cliente debe ser null tras eliminarlo");
    }

    @Test
    @Order(6)
    @DisplayName("Buscar ID inexistente debe devolver null")
    void testBuscarIdNoExistente() {
        Customer c = dao.findById(-1);
        assertNull(c);
    }
}
