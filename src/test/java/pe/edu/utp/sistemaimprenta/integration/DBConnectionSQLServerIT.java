package pe.edu.utp.sistemaimprenta.integration;

import org.junit.jupiter.api.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;
import pe.edu.utp.sistemaimprenta.db.DBConfig;
import pe.edu.utp.sistemaimprenta.db.DBConnection;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DBConnectionSQLServerIT {

    private static DBConnection dbConnection;

    @BeforeAll
    static void init() {
        dbConnection = DBConnection.getInstance();
    }

    @Test
    @Order(1)
    @DisplayName("Debe establecer conexión exitosa con SQL Server")
    void testConexionExitosa() throws SQLException {
        Connection conn = dbConnection.getConnection();
        assertNotNull(conn, "La conexión no debe ser nula");
        assertFalse(conn.isClosed(), "La conexión no debe estar cerrada");
    }

    @Test
    @Order(2)
    @DisplayName("Debe ejecutar una consulta simple SELECT 1")
    void testConsultaBasica() throws SQLException {
        Connection conn = dbConnection.getConnection();
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT 1 AS resultado");
            assertTrue(rs.next(), "Debe devolver al menos una fila");
            assertEquals(1, rs.getInt("resultado"), "El resultado de SELECT 1 debe ser 1");
        }
    }

    @Test
    @Order(3)
    @DisplayName("Debe mantener patrón Singleton (misma instancia)")
    void testSingleton() {
        DBConnection otraInstancia = DBConnection.getInstance();
        assertSame(dbConnection, otraInstancia, "Debe ser la misma instancia Singleton");
    }

    @Test
    @Order(4)
    @DisplayName("Debe cerrar correctamente la conexión")
    void testCerrarConexion() throws SQLException {
        dbConnection.closeConnection();
        Connection conn = dbConnection.getConnection();
        assertTrue(conn.isClosed(), "La conexión debe estar cerrada tras closeConnection()");
    }

    @Test
    @Order(5)
    @DisplayName("Debe manejar claves inexistentes en DBConfig correctamente")
    void testConfigInexistente() {
        String valor = DBConfig.get("clave.que.no.existe");
        assertNull(valor, "Debe devolver null si la clave no existe");
    }
}




