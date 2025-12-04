package pe.edu.utp.sistemaimprenta.integration;

import org.junit.jupiter.api.*;
import pe.edu.utp.sistemaimprenta.dao.ProductDao;
import pe.edu.utp.sistemaimprenta.model.Product;
import pe.edu.utp.sistemaimprenta.model.ProductType;
import pe.edu.utp.sistemaimprenta.model.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProductDaoIT {

    private static ProductDao dao;
    private static User user;
    private static int generatedId;

    @BeforeAll
    static void init() {
        dao = new ProductDao();
        user = new User();
        user.setId(1); 
    }

    @Test
    @Order(1)
    @DisplayName("Debe guardar producto exitosamente y capturar ID")
    void testGuardarProducto() {
        Product p = new Product();
        p.setName("IT Producto");
        p.setDescription("Producto de integración");
        p.setType(ProductType.fromId(1)); 
        p.setBasePrice(10.50);
        p.setActive(true);

        boolean resultado = dao.save(p, user);

        assertTrue(resultado);
        assertTrue(p.getId() > 0);

        generatedId = p.getId();
        System.out.println("ID generado: " + generatedId);
    }

    @Test
    @Order(2)
    @DisplayName("Debe buscar el producto recién registrado")
    void testBuscarProductoPorId() {
        Product p = dao.findById(generatedId);
        assertNotNull(p);
        assertEquals(generatedId, p.getId());
    }

    @Test
    @Order(3)
    @DisplayName("Debe listar productos")
    void testListarProductos() {
        List<Product> lista = dao.findAll();
        assertNotNull(lista);
        assertFalse(lista.isEmpty());
    }

    @Test
    @Order(4)
    @DisplayName("Debe actualizar el producto insertado")
    void testActualizarProducto() {
        Product p = dao.findById(generatedId);

        assertNotNull(p);

        p.setName("Producto Modificado IT");
        p.setBasePrice(20.00);

        boolean resultado = dao.update(p, user);
        assertTrue(resultado);

        Product actualizado = dao.findById(generatedId);
        assertEquals("Producto Modificado IT", actualizado.getName());
        assertEquals(20.00, actualizado.getBasePrice());
    }

    @Test
    @Order(5)
    @DisplayName("Debe eliminar el producto insertado")
    void testEliminarProducto() {
        boolean resultado = dao.delete(generatedId, user);
        assertTrue(resultado);

        Product p = dao.findById(generatedId);
        assertNull(p);
    }

    @Test
    @Order(6)
    @DisplayName("Debe regresar null si se busca ID inexistente")
    void testBuscarIdInexistente() {
        Product p = dao.findById(999999);
        assertNull(p);
    }
}
