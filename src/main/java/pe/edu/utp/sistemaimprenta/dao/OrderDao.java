package pe.edu.utp.sistemaimprenta.dao;

import pe.edu.utp.sistemaimprenta.db.DBConnection;
import pe.edu.utp.sistemaimprenta.model.*;
import pe.edu.utp.sistemaimprenta.util.AuditUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class OrderDao implements CrudDao<Order> {

    private static final Logger log = LoggerFactory.getLogger(OrderDao.class);

    private Connection getConnection() {
        return DBConnection.getInstance().getConnection();
    }

    @Override
    public boolean save(Order order, User user) {
        String sqlPedido = """
            INSERT INTO Pedido (id_cliente, id_usuario, id_estado_pedido, fechaEntrega, montoTotal)
            VALUES (?, ?, ?, ?, ?)
        """;

        String sqlDetalle = """
            INSERT INTO DetallePedido (id_pedido, id_producto, cantidad, precioUnitario, subtotal)
            VALUES (?, ?, ?, ?, ?)
        """;

        Connection conn = getConnection();

        try {
            conn.setAutoCommit(false);

            PreparedStatement psPedido = conn.prepareStatement(sqlPedido, Statement.RETURN_GENERATED_KEYS);
            psPedido.setInt(1, order.getCustomer().getId());
            psPedido.setInt(2, user.getId());
            psPedido.setInt(3, order.getState().getId());
            psPedido.setTimestamp(4, Timestamp.valueOf(order.getDeliveryDate()));
            psPedido.setDouble(5, order.getTotalAmount());
            psPedido.executeUpdate();

            ResultSet rs = psPedido.getGeneratedKeys();
            if (rs.next()) {
                order.setId(rs.getInt(1));
            }

            PreparedStatement psDetalle = conn.prepareStatement(sqlDetalle);
            for (OrderDetail d : order.getDetails()) {
                psDetalle.setInt(1, order.getId());
                psDetalle.setInt(2, d.getProduct().getId());
                psDetalle.setInt(3, d.getQuantity());
                psDetalle.setDouble(4, d.getUnitPrice());
                psDetalle.setDouble(5, d.getSubtotal());
                psDetalle.addBatch();
            }
            psDetalle.executeBatch();

            conn.commit();
            AuditUtil.registrar(user, "Registró el pedido PED-" + order.getId(), AuditType.CREACION);
            return true;

        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ignored) {
            }
            log.error("Error al registrar pedido", e);
            return false;
        }
    }

    public List<OrderDetail> findDetailsByOrderId(int orderId) {
        List<OrderDetail> detalles = new ArrayList<>();
        String sql = """
            SELECT d.id_detalle_pedido, d.cantidad, d.precioUnitario, d.subtotal,
                    p.id_producto, p.nombre AS producto, p.precio_unitario
            FROM DetallePedido d
            INNER JOIN Producto p ON d.id_producto = p.id_producto
            WHERE d.id_pedido = ?
        """;

        Connection conn = getConnection();
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Product producto = new Product();
                producto.setId(rs.getInt("id_producto"));
                producto.setName(rs.getString("producto"));
                producto.setBasePrice(rs.getDouble("precio_unitario"));

                OrderDetail detalle = new OrderDetail();
                detalle.setId(rs.getInt("id_detalle_pedido"));
                detalle.setProduct(producto);
                detalle.setQuantity(rs.getInt("cantidad"));
                detalle.setUnitPrice(rs.getDouble("precioUnitario"));
                detalle.setSubtotal(rs.getDouble("subtotal"));

                detalles.add(detalle);
            }

        } catch (SQLException e) {
            log.error("Error al obtener detalles del pedido " + orderId, e);
        }

        return detalles;
    }

    @Override
    public List<Order> findAll() {
        String sql = """
            SELECT p.id_pedido, p.fechaRegistro, p.fechaEntrega, p.montoTotal,
                    p.id_estado_pedido,
                    c.id_cliente, c.nombres AS clienteNom, c.apellidos AS clienteAp, 
                    u.id_usuario, u.nombre AS usuario
            FROM Pedido p
            INNER JOIN Cliente c ON p.id_cliente = c.id_cliente
            INNER JOIN Usuario u ON p.id_usuario = u.id_usuario
        """;

        List<Order> lista = new ArrayList<>();
        Connection conn = getConnection();

        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Order o = new Order();
                o.setId(rs.getInt("id_pedido"));

                Customer c = new Customer();
                c.setId(rs.getInt("id_cliente"));
                c.setName(rs.getString("clienteNom"));
                c.setLastName(rs.getString("clienteAp"));
                o.setCustomer(c);

                User u = new User();
                u.setId(rs.getInt("id_usuario"));
                u.setUsername(rs.getString("usuario"));
                o.setUser(u);

                o.setState(OrderState.fromId(rs.getInt("id_estado_pedido")));
                o.setCreatedAt(rs.getTimestamp("fechaRegistro").toLocalDateTime());
                o.setDeliveryDate(rs.getTimestamp("fechaEntrega").toLocalDateTime());
                o.setTotalAmount(rs.getDouble("montoTotal"));

                o.setDetails(findDetailsByOrderId(o.getId()));

                lista.add(o);
            }

        } catch (SQLException e) {
            log.error("Error al listar pedidos", e);
        }

        return lista;
    }

    @Override
    public Order findById(int id) {
        String sqlPedido = """
        SELECT p.*, 
               c.id_cliente, c.nombres AS clienteNom, c.apellidos AS clienteAp,
               u.id_usuario, u.nombre AS usuario
        FROM Pedido p
        INNER JOIN Cliente c ON p.id_cliente = c.id_cliente
        INNER JOIN Usuario u ON p.id_usuario = u.id_usuario
        WHERE p.id_pedido = ?
    """;

        Connection conn = getConnection();
        try {
            PreparedStatement psPedido = conn.prepareStatement(sqlPedido);
            psPedido.setInt(1, id);
            ResultSet rs = psPedido.executeQuery();

            if (!rs.next()) {
                return null;
            }

            Order o = new Order();
            o.setId(rs.getInt("id_pedido"));

            Customer c = new Customer();
            c.setId(rs.getInt("id_cliente"));
            c.setName(rs.getString("clienteNom"));
            c.setLastName(rs.getString("clienteAp"));
            o.setCustomer(c);

            User u = new User();
            u.setId(rs.getInt("id_usuario"));
            u.setUsername(rs.getString("usuario"));
            o.setUser(u);

            o.setState(OrderState.fromId(rs.getInt("id_estado_pedido")));
            o.setCreatedAt(rs.getTimestamp("fechaRegistro").toLocalDateTime());
            o.setDeliveryDate(rs.getTimestamp("fechaEntrega").toLocalDateTime());
            o.setTotalAmount(rs.getDouble("montoTotal"));

            o.setDetails(findDetailsByOrderId(id));

            return o;

        } catch (SQLException e) {
            log.error("Error al buscar pedido por ID", e);
            return null;
        }
    }

    @Override
    public boolean update(Order order, User user) {
        String sql = """
            UPDATE Pedido
            SET id_estado_pedido = ?, fechaEntrega = ?, montoTotal = ?
            WHERE id_pedido = ?
        """;

        Connection conn = getConnection();
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, order.getState().getId());
            ps.setTimestamp(2, Timestamp.valueOf(order.getDeliveryDate()));
            ps.setDouble(3, order.getTotalAmount());
            ps.setInt(4, order.getId());
            ps.executeUpdate();

            AuditUtil.registrar(user, "Actualizó el pedido PED-" + order.getId(), AuditType.MODIFICACION);
            return true;

        } catch (SQLException e) {
            log.error("Error al actualizar pedido", e);
            return false;
        }
    }

    @Override
    public boolean delete(int id, User user) {
        String sqlDetalle = "DELETE FROM DetallePedido WHERE id_pedido = ?";
        String sqlPedido = "DELETE FROM Pedido WHERE id_pedido = ?";

        Connection conn = getConnection();
        try {
            conn.setAutoCommit(false);

            PreparedStatement psD = conn.prepareStatement(sqlDetalle);
            psD.setInt(1, id);
            psD.executeUpdate();

            PreparedStatement psP = conn.prepareStatement(sqlPedido);
            psP.setInt(1, id);
            psP.executeUpdate();

            conn.commit();
            AuditUtil.registrar(user, "Eliminó el pedido PED-" + id, AuditType.ELIMINACION);
            return true;

        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ignored) {
            }
            log.error("Error al eliminar pedido", e);
            return false;
        }
    }

    // metodos para reportes
    public List<Object[]> findTopSellingProducts(LocalDate fechaInicio, LocalDate fechaFin, int limit) {
        List<Object[]> resultados = new ArrayList<>();

        String query = """
        SELECT TOP (?) P.nombre, SUM(DP.cantidad) AS UnidadesVendidas
        FROM DetallePedido DP
        INNER JOIN Producto P ON P.id_producto = DP.id_producto
        INNER JOIN Pedido PE ON PE.id_pedido = DP.id_pedido
        WHERE PE.fechaRegistro BETWEEN ? AND ?
        GROUP BY P.nombre
        ORDER BY UnidadesVendidas DESC
    """;

        Connection conn = getConnection();

        try (PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, limit);

            ps.setTimestamp(2, Timestamp.valueOf(fechaInicio.atStartOfDay()));
            ps.setTimestamp(3, Timestamp.valueOf(fechaFin.atTime(23, 59, 59)));

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    String nombre = rs.getString("nombre");
                    int unidades = rs.getInt("UnidadesVendidas");
                    resultados.add(new Object[]{nombre, unidades});
                }

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return resultados;
    }

    public List<Object[]> findIncomeByProductType() {
        List<Object[]> resultados = new ArrayList<>();

        String query = """
            SELECT TP.nombre AS TipoProducto, SUM(P.montoTotal) AS IngresoTotal
            FROM Pedido P
            INNER JOIN DetallePedido DP ON DP.id_pedido = P.id_pedido
            INNER JOIN Producto PR ON PR.id_producto = DP.id_producto
            INNER JOIN TipoProducto TP ON TP.id_tipo_producto = PR.id_tipo_producto
            GROUP BY TP.nombre
            ORDER BY IngresoTotal DESC
        """;

        Connection conn = getConnection();
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                resultados.add(new Object[]{
                    rs.getString("TipoProducto"),
                    rs.getDouble("IngresoTotal")
                });
            }

        } catch (SQLException e) {
            log.error("Error al obtener ingresos por tipo de producto", e);
        }
        return resultados;
    }
}
