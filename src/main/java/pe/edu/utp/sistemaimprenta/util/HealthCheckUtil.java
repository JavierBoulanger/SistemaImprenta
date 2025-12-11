package pe.edu.utp.sistemaimprenta.util;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import pe.edu.utp.sistemaimprenta.db.DBConnection;

public class HealthCheckUtil {

    public static boolean verificarConexionBD() {
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            if (conn == null || conn.isClosed()) {
                return false;
            }   
            return conn.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }

    public static String obtenerEspacioDisco() {
        File disco = new File("."); 
        long libreBytes = disco.getFreeSpace();
        long libreGB = libreBytes / (1024 * 1024 * 1024);
        return libreGB + " GB Libres";
    }
    
    public static boolean esEspacioCritico() {
        File disco = new File(".");
        long libreMB = disco.getFreeSpace() / (1024 * 1024);
        return libreMB < 500; 
    }

    public static String obtenerUsoMemoria() {
        Runtime rt = Runtime.getRuntime();
        long totalMB = rt.totalMemory() / (1024 * 1024);
        long libreMB = rt.freeMemory() / (1024 * 1024);
        long usadoMB = totalMB - libreMB;
        
        return usadoMB + " MB / " + totalMB + " MB";
    }
    
    public static double obtenerPorcentajeMemoria() {
        Runtime rt = Runtime.getRuntime();
        return (double) (rt.totalMemory() - rt.freeMemory()) / rt.totalMemory();
    }
}