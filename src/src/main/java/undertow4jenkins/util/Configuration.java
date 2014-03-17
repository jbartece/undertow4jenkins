package undertow4jenkins.util;

import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Configuration {

    private static final Logger log = LoggerFactory
            .getLogger("undertow4jenkins.configuration.Configuration");

    private static String PROPERTY_FILE_NAME = "undertow4jenkins.properties";

    private static Properties propertiesResource = initConfiguration();

    private static Properties initConfiguration() {
        try {
            Properties properties = new Properties();
            properties.load(Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream(PROPERTY_FILE_NAME));
            return properties;
        } catch (IOException e) {
            log.error("Configuration could not be initialized!", e);
            return null;
        }

    }

    public static String getProperty(String propertyName) {
        return propertiesResource.getProperty(propertyName);
    }

    public static Integer getIntProperty(String propertyName) {
        try {
            return Integer.parseInt(getProperty(propertyName));
        } catch (NumberFormatException e) {
            log.error("Property is not Integer!", e);
            return null;
        }
    }

    public static Boolean getBoolProperty(String propertyName) {
        try {
            return Boolean.parseBoolean(getProperty(propertyName));
        } catch (NumberFormatException e) {
            log.error("Property is not Boolean!", e);
            return null;
        }
    }
    
    public static boolean isPropertySet(String propertyName) {
        return propertiesResource.containsKey(propertyName);
    }

}
