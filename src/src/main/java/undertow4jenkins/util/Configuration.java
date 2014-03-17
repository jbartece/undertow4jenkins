package undertow4jenkins.util;

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

}
