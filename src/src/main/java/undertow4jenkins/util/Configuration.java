package undertow4jenkins.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Configuration {

    private static final Logger log = LoggerFactory
            .getLogger("undertow4jenkins.util.Configuration");

    private static String PROPERTY_PATH = "undertow4jenkins.util.undertow4jenkins";

    private static ResourceBundle propertiesResource = initConfiguration();

    private static ResourceBundle initConfiguration() {
        return ResourceBundle.getBundle(PROPERTY_PATH);
    }

    public static String getProperty(String propertyName) {
        return propertiesResource.getString(propertyName).trim();
    }

    public static Integer getIntProperty(String propertyName) {
        try {
            return Integer.parseInt(getProperty(propertyName).trim());
        } catch (NumberFormatException e) {
            log.error("Property is not Integer!", e);
            return null;
        }
    }

    public static Boolean getBoolProperty(String propertyName) {
        try {
            return Boolean.parseBoolean(getProperty(propertyName).trim());
        } catch (NumberFormatException e) {
            log.error("Property is not Boolean!", e);
            return null;
        }
    }

    public static boolean isPropertySet(String propertyName) {
        return propertiesResource.containsKey(propertyName);
    }

    /**
     * Create Properties from file from the folder of this class
     * 
     * @param propertyFileName Name of property file
     * @return New property entity
     * @throws IOException Thrown if the file is not found or file is malformed
     */
    public static Properties loadPropertiesFromFile(String propertyFileName) throws IOException {
        InputStream input = null;
        Properties properties;
        
        try {
            input = new FileInputStream(propertyFileName);
            properties = new Properties();
            properties.load(input);
        } finally {
            if (input != null)
                input.close();
        }

        return properties;
    }

}
