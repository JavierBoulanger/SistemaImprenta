package pe.edu.utp.sistemaimprenta.dao;

import pe.edu.utp.sistemaimprenta.model.Customer;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import pe.edu.utp.sistemaimprenta.db.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pe.edu.utp.sistemaimprenta.model.AuditType;
import pe.edu.utp.sistemaimprenta.model.User;
import pe.edu.utp.sistemaimprenta.util.AuditUtil;

public class CustomerDao implements CrudDao<Customer> {

    private static final Logger log = LoggerFactory.getLogger(CustomerDao.class);

    private Connection getConnection() throws SQLException {
        return DBConnection.getInstance().getConnection();
    }

    @Override
    public boolean save(Customer c, User u) {
        String sql = "INSERT INTO Cliente (dni, apellidos, nombres, telefono, correo_electronico, direccion) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getDni());
            ps.setString(2, c.getLastName());
            ps.setString(3, c.getName());
            ps.setString(4, c.getTelephoneNumber());
            ps.setString(5, c.getEmail());
            ps.setString(6, c.getAddress());

            int filas = ps.executeUpdate();
            AuditUtil.registrar(u, "Creó nuevo cliente: " + c.getName() + " " + c.getLastName(), AuditType.CREACION);

            if (filas > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    c.setId(rs.getInt(1)); // id generado
                }
            }
            return filas > 0;
        } catch (SQLException e) {
            log.error("No se pudo guardar cliente en la base de datos", e);
            return false;
        }
    }

    @Override
    public Customer findById(int id) {
        String sql = "SELECT * FROM Cliente WHERE id_cliente=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Customer c = new Customer();
                    c.setId(rs.getInt("id_cliente"));
                    c.setDni(rs.getString("dni"));
                    c.setLastName(rs.getString("apellidos"));
                    c.setName(rs.getString("nombres"));
                    c.setTelephoneNumber(rs.getString("telefono"));
                    c.setEmail(rs.getString("correo_electronico"));
                    c.setAddress(rs.getString("direccion"));
                    c.setCreatedAt(rs.getTimestamp("fecha_registro").toLocalDateTime());
                    return c;
                }
            }
        } catch (SQLException e) {
            log.error("Error al buscar cliente por ID", e);
        }
        return null;
    }

    @Override
    public boolean delete(int id, User u) {
        String sql = "DELETE FROM Cliente WHERE id_cliente=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            AuditUtil.registrar(u, "Eliminó cliente CLI-" + id, AuditType.ELIMINACION);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("Error al eliminar cliente por ID", e);
            return false;
        }
    }

    @Override
    public boolean update(Customer c, User u) {
        String sql = "UPDATE Cliente SET dni=?, apellidos=?, nombres=?, telefono=?, correo_electronico=?, direccion=? "
                + "WHERE id_cliente=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, c.getDni());
            ps.setString(2, c.getLastName());
            ps.setString(3, c.getName());
            ps.setString(4, c.getTelephoneNumber());
            ps.setString(5, c.getEmail());
            ps.setString(6, c.getAddress());
            ps.setInt(7, c.getId());
            AuditUtil.registrar(u, "Actualizó el usuario CLI-" + c.getId(), AuditType.MODIFICACION);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("Error al actualizar cliente ", e);
            return false;
        }
    }

    @Override
    public List<Customer> findAll() {
        List<Customer> lista = new ArrayList<>();
        String sql = "SELECT * FROM Cliente";
        try (Statement st = getConnection().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Customer c = new Customer();
                c.setId(rs.getInt("id_cliente"));
                c.setDni(rs.getString("dni"));
                c.setLastName(rs.getString("apellidos"));
                c.setName(rs.getString("nombres"));
                c.setTelephoneNumber(rs.getString("telefono"));
                c.setEmail(rs.getString("correo_electronico"));
                c.setAddress(rs.getString("direccion"));
                c.setCreatedAt(rs.getTimestamp("fecha_registro").toLocalDateTime());
                lista.add(c);
            }
        } catch (SQLException e) {
            log.error("No se pudo listar clientes de la base de datos", e);
        }
        return lista;
    }
}
