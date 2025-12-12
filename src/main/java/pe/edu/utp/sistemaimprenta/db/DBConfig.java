package pe.edu.utp.sistemaimprenta.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBConfig {

    private static final String CONFIG_FILE = "db.properties";
    private static Properties properties;
    private static final Logger log = LoggerFactory.getLogger(DBConfig.class);

    static {
        properties = new Properties();
        loadProperties();
    }

    private static void loadProperties() {
        File externalFile = new File(CONFIG_FILE);
        
        if (externalFile.exists()) {
            try (InputStream is = new FileInputStream(externalFile)) {
                properties.load(is);
                log.info("Configuración cargada desde archivo EXTERNO: {}", externalFile.getAbsolutePath());
                return; 
            } catch (IOException e) {
                log.error("Error al leer archivo de configuración externo, intentando interno...", e);
            }
        } else {
            log.warn("No se encontró archivo de configuración externo '{}'. Buscando en el classpath...", CONFIG_FILE);
        }

        try (InputStream is = DBConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (is != null) {
                properties.load(is);
                log.info("Configuración cargada desde recursos INTERNOS (dentro del JAR).");
            } else {
                log.error("FATAL: No se encontró '{}' ni fuera ni dentro del JAR.", CONFIG_FILE);
            }
        } catch (IOException e) {
            log.error("No se pudo cargar configuración de la base de datos interna", e);
        }
    }

    public static String get(String key) {
        return properties.getProperty(key);
    }
}