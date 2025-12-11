package pe.edu.utp.sistemaimprenta.integration;

import java.sql.Connection;
import org.junit.jupiter.api.*;
import pe.edu.utp.sistemaimprenta.model.OrderDetail;
import pe.edu.utp.sistemaimprenta.model.Customer;
import pe.edu.utp.sistemaimprenta.model.Product;
import pe.edu.utp.sistemaimprenta.model.User;
import pe.edu.utp.sistemaimprenta.model.OrderState;
import pe.edu.utp.sistemaimprenta.dao.OrderDao;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import pe.edu.utp.sistemaimprenta.db.DBConnection;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OrderDaoIT {

    private static Connection conn;
    private static OrderDao dao;
    private static User userTest = new User();
    private static pe.edu.utp.sistemaimprenta.model.Order pedidoCreado;

    @BeforeAll
    void setup() throws Exception {
        conn = DBConnection.getInstance().getConnection();
        conn.setAutoCommit(false); 
        dao = new OrderDao();
        userTest.setId(1);
    }

    @AfterAll
    void cleanup() throws Exception {
        conn.rollback(); 
        conn.setAutoCommit(true);
    }

    @Test
    @Order(1)
    void testGuardarPedido() {
        pe.edu.utp.sistemaimprenta.model.Order o = new pe.edu.utp.sistemaimprenta.model.Order();

        Customer c = new Customer();
        c.setId(1);

        o.setCustomer(c);
        o.setUser(userTest);
        o.setState(OrderState.fromId(1));
        o.setDeliveryDate(LocalDateTime.now().plusDays(3));
        o.setTotalAmount(120.50);

        OrderDetail det = new OrderDetail();
        Product pr = new Product();
        pr.setId(1);

        det.setProduct(pr);
        det.setQuantity(2);
        det.setUnitPrice(60.25);
        det.setSubtotal(120.50);

        o.setDetails(List.of(det));

        boolean ok = dao.save(o, userTest);
        assertTrue(ok);
        assertTrue(o.getId() > 0);

        pedidoCreado = o;
    }

    @Test
    @Order(2)
    void testBuscarPorId() {
        var o = dao.findById(pedidoCreado.getId());
        assertNotNull(o);
        assertEquals(pedidoCreado.getId(), o.getId());
    }
}

