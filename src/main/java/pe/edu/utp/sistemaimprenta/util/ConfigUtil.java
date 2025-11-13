package pe.edu.utp.sistemaimprenta.util;

import java.io.*;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigUtil {
    private static final String CONFIG_PATH = "/config/config.properties";
    private static final Properties properties = new Properties();
    private static final Logger log = LoggerFactory.getLogger(ConfigUtil.class);
    
    static {
        try (InputStream input = ConfigUtil.class.getResourceAsStream(CONFIG_PATH)) {
            if (input != null) {
                properties.load(input);
            } else {
                System.err.println("No se encontró config.properties");
            }
        } catch (IOException e) {
            log.error("Error al cargar configuración del sistema",e);
        }
    }

    public static String get(String key) {
        return properties.getProperty(key);
    }

    public static void set(String key, String value) {
        properties.setProperty(key, value);
    }

    public static void save() {
        try (OutputStream output = new FileOutputStream("src/main/resources/config/config.properties")) {
            properties.store(output, "Configuración del sistema");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Properties getAll() {
        return properties;
    }
}
