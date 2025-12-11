package pe.edu.utp.sistemaimprenta.util;

import java.io.*;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigUtil {
    private static final String CONFIG_FILE_NAME = "config.properties"; 
    private static final Properties properties = new Properties();
    private static final Logger log = LoggerFactory.getLogger(ConfigUtil.class);

    static {
        loadConfig();
    }

    private static void loadConfig() {
        File externalFile = new File(CONFIG_FILE_NAME);
        if (externalFile.exists()) {
            try (InputStream input = new FileInputStream(externalFile)) {
                properties.load(input);
                log.info("Configuración cargada desde archivo externo.");
            } catch (IOException e) {
                log.error("Error cargando config externa", e);
            }
        } else {
       
            log.warn("No se encontró config.properties externo. Usando valores internos por defecto.");
            try (InputStream input = ConfigUtil.class.getResourceAsStream("/config/config.properties")) {
                if (input != null) {
                    properties.load(input);
                  
                    save(); 
                }
            } catch (IOException e) {
                log.error("Error cargando configuración interna", e);
            }
        }
    }

    public static String get(String key) {
        return properties.getProperty(key);
    }

    public static void set(String key, String value) {
        properties.setProperty(key, value);
    }

    public static void save() {
       
        try (OutputStream output = new FileOutputStream(CONFIG_FILE_NAME)) {
            properties.store(output, "Configuración del sistema");
        } catch (IOException e) {
            log.error("Error guardando configuración", e);
        }
    }

    public static Properties getAll() {
        return properties;
    }
}