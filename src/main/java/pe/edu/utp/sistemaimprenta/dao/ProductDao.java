package pe.edu.utp.sistemaimprenta.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pe.edu.utp.sistemaimprenta.db.DBConnection;
import pe.edu.utp.sistemaimprenta.model.AuditType;
import pe.edu.utp.sistemaimprenta.model.Product;
import pe.edu.utp.sistemaimprenta.model.ProductType;
import pe.edu.utp.sistemaimprenta.model.User;
import pe.edu.utp.sistemaimprenta.util.AuditUtil;

public class ProductDao implements CrudDao<Product> {

    private static final Logger log = LoggerFactory.getLogger(ProductDao.class);

    private Connection getConnection() throws SQLException {
        return DBConnection.getInstance().getConnection();
    }

    @Override
    public List<Product> findAll() {
        List<Product> productos = new ArrayList<>();
        String query = "SELECT * FROM Producto";

        try (Statement stmt = getConnection().createStatement(); ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Product p = new Product();
                p.setId(rs.getInt("id_producto"));
                p.setName(rs.getString("nombre"));
                p.setDescription(rs.getString("descripcion"));
                p.setType(ProductType.fromId(rs.getInt("id_tipo_producto")));
                p.setBasePrice(rs.getDouble("precio_unitario"));
                p.setActive(rs.getBoolean("estado"));
                productos.add(p);
            }

        } catch (SQLException e) {
            log.error("No se pudo listar productos de la base de datos", e);
        }
        return productos;
    }

    @Override
    public boolean save(Product producto, User u) {
        String query = "INSERT INTO Producto (nombre, descripcion, id_tipo_producto, precio_unitario, estado) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = getConnection().prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, producto.getName());
            stmt.setString(2, producto.getDescription());
            stmt.setInt(3, producto.getType().getId());
            stmt.setDouble(4, producto.getBasePrice());
            stmt.setBoolean(5, producto.isActive());

            int rows = stmt.executeUpdate();

            if (rows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        producto.setId(rs.getInt(1)); 
                    }
                }

                AuditUtil.registrar(u, "Creó nuevo producto: " + producto.getName(), AuditType.CREACION);
                return true;
            }

        } catch (SQLException e) {
            log.error("No se pudo registrar producto en la base de datos", e);
        }

        return false;
    }

    @Override
    public boolean delete(int id, User u) {
        String query = "DELETE FROM Producto WHERE id_producto = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();

            AuditUtil.registrar(u, "Eliminó el producto PRO-" + id, AuditType.ELIMINACION);
            return true;
        } catch (SQLException e) {
            log.error("No se pudo eliminar producto en la base de datos", e);
            return false;
        }
    }

    @Override
    public Product findById(int id) {
        String query = "SELECT * FROM Producto WHERE id_producto = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Product p = new Product();
                    p.setId(rs.getInt("id_producto"));
                    p.setName(rs.getString("nombre"));
                    p.setDescription(rs.getString("descripcion"));
                    p.setType(ProductType.fromId(rs.getInt("id_tipo_producto")));
                    p.setBasePrice(rs.getDouble("precio_unitario"));
                    p.setActive(rs.getBoolean("estado"));
                    return p;
                }
            }
        } catch (SQLException e) {
            log.error("No se pudo buscar producto en la base de datos", e);
        }
        return null;
    }

    @Override
    public boolean update(Product producto, User u) {
        String query = "UPDATE Producto SET nombre = ?, descripcion = ?, id_tipo_producto = ?, "
                + "precio_unitario = ?, estado = ? WHERE id_producto = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setString(1, producto.getName());
            stmt.setString(2, producto.getDescription());
            stmt.setInt(3, producto.getType().getId());
            stmt.setDouble(4, producto.getBasePrice());
            stmt.setBoolean(5, producto.isActive());
            stmt.setInt(6, producto.getId());

            stmt.executeUpdate();

            AuditUtil.registrar(u, "Actualizó el producto PRO-" + producto.getId(), AuditType.MODIFICACION);
            return true;
        } catch (SQLException e) {
            log.error("No se pudo actualizar producto en la base de datos", e);
            return false;
        }
    }
}
