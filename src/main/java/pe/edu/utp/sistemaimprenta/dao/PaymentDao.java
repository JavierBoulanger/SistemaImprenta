package pe.edu.utp.sistemaimprenta.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pe.edu.utp.sistemaimprenta.db.DBConnection;
import pe.edu.utp.sistemaimprenta.model.*;
import pe.edu.utp.sistemaimprenta.util.AuditUtil;

public class PaymentDao {

    private static final Logger log = LoggerFactory.getLogger(PaymentDao.class);

    private Connection conn;

    public PaymentDao() {
        this.conn = DBConnection.getInstance().getConnection();
    }

    public List<Payment> listarPagos() {
        List<Payment> lista = new ArrayList<>();
        String sql = """
                        SELECT p.id_pago, p.id_pedido, p.id_metodo_pago, p.id_estado_pago, 
                               p.monto, p.fecha_pago,
                               c.nombres AS cliente
                        FROM Pago p
                        INNER JOIN Pedido pe ON pe.id_pedido = p.id_pedido
                        INNER JOIN Cliente c ON c.id_cliente = pe.id_cliente
                        ORDER BY p.fecha_pago DESC
                    """;

        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {

            log.info("Ejecutando consulta listarPagos...");

            while (rs.next()) {
                Payment pago = new Payment();
                pago.setId(rs.getInt("id_pago"));
                pago.setMetodoPago(PaymentMethod.fromId(rs.getInt("id_metodo_pago")));
                pago.setEstadoPago(PaymentStatus.fromId(rs.getInt("id_estado_pago")));
                pago.setMonto(rs.getDouble("monto"));
                pago.setFechaPago(rs.getTimestamp("fecha_pago").toLocalDateTime());
                lista.add(pago);
            }

            log.info("Pagos listados correctamente: {}", lista.size());
        } catch (SQLException e) {
            log.error("Error al listar pagos", e);
        }

        return lista;
    }

    public boolean registrarPago(Payment pago, User u) {
        String sql = """
        INSERT INTO Pago (id_pedido, id_metodo_pago, id_estado_pago, monto, fecha_pago)
        VALUES (?, ?, ?, ?, ?)
    """;

        try {
            double totalPagadoActual = obtenerTotalPagadoPorPedido(pago.getOrder().getId());
            double totalPedido = pago.getOrder().getTotalAmount();
            double nuevoTotal = totalPagadoActual + pago.getMonto();

            PaymentStatus estadoPago = (nuevoTotal < totalPedido) ? PaymentStatus.PARCIAL : PaymentStatus.PAGADO;

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, pago.getOrder().getId());
                ps.setInt(2, pago.getMetodoPago().getId());
                ps.setInt(3, estadoPago.getId());
                ps.setDouble(4, pago.getMonto());
                ps.setTimestamp(5, Timestamp.valueOf(pago.getFechaPago()));

                int rows = ps.executeUpdate();

                AuditUtil.registrar(
                        u,
                        "Registró pago por Pedido " + pago.getOrder().getId() + " por S/ " + pago.getMonto(),
                        AuditType.CREACION
                );

                log.info("Pago registrado: Pedido={}, Monto={}, Estado={}", pago.getOrder().getId(), pago.getMonto(), estadoPago);
                return rows > 0;
            }

        } catch (SQLException e) {
            log.error("Error al registrar pago: {}", pago, e);
            return false;
        }
    }

    public double obtenerTotalIngresosMes() {
        String sql = """
            SELECT ISNULL(SUM(monto),0) AS total
            FROM Pago 
            WHERE MONTH(fecha_pago) = MONTH(GETDATE()) 
              AND YEAR(fecha_pago) = YEAR(GETDATE()) 
              AND id_estado_pago = 2
        """;

        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {

            if (rs.next()) {
                double total = rs.getDouble("total");
                log.info("Total ingresos del mes: S/ {}", total);
                return total;
            }

        } catch (SQLException e) {
            log.error("Error al obtener total de ingresos del mes", e);
        }

        return 0.0;
    }

    public int contarPorEstado(PaymentStatus estado) {
        String sql = "SELECT COUNT(*) AS cantidad FROM Pago WHERE id_estado_pago = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, estado.getId());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int count = rs.getInt("cantidad");
                log.info("Pagos con estado {}: {}", estado.getNombre(), count);
                return count;
            }

        } catch (SQLException e) {
            log.error("Error al contar pagos por estado {}", estado, e);
        }

        return 0;
    }

    public double obtenerTotalPagadoPorPedido(int idPedido) {
        String sql = """
        SELECT ISNULL(SUM(monto), 0) AS total_pagado
        FROM Pago
        WHERE id_pedido = ?
          AND id_estado_pago IN (2, 3) -- PAGADO o PARCIAL
    """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPedido);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                double totalPagado = rs.getDouble("total_pagado");
                log.info("Total pagado del pedido {}: S/ {}", idPedido, totalPagado);
                return totalPagado;
            }

        } catch (SQLException e) {
            log.error("Error al obtener total pagado del pedido {}", idPedido, e);
        }

        return 0.0;
    }

    public boolean anularPago(int idPago, User u) {
        String sql = "UPDATE Pago SET id_estado_pago = 3 WHERE id_pago = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPago);
            int rows = ps.executeUpdate();

            AuditUtil.registrar(u, "Anuló el pago #" + idPago, AuditType.ELIMINACION);

            log.info("Pago anulado: ID={} ({} filas afectadas)", idPago, rows);
            return rows > 0;

        } catch (SQLException e) {
            log.error("Error al anular pago ID={}", idPago, e);
            return false;
        }
    }

}
