package undertow4jenkins.loader;

import static io.undertow.servlet.Servlets.servlet;
import io.undertow.servlet.api.ServletInfo;

import java.util.ArrayList;
import java.util.List;

import undertow4jenkins.parser.WebXmlContent.InitParam;
import undertow4jenkins.parser.WebXmlContent.Servlet;
import undertow4jenkins.parser.WebXmlContent.ServletMapping;

/**
 * Utility class to create servlets and servlet mappings from web.xml data
 * 
 * @author Jakub Bartecek <jbartece@redhat.com>
 * 
 */
public class ServletLoader {

    /**
     * Creates servlet entity from web.xml data
     * 
     * @param servletDataCol Data about servlet from web.xml
     * @param mappingDataCol Data about servlet mapping from web.xml
     * @param classLoader Web archive classloader
     * @return Created entity
     * @throws ClassNotFoundException Thrown if some class
     *         could not be loaded by current class loader
     */
    public static List<ServletInfo> createServlets(List<Servlet> servletDataCol,
            List<ServletMapping> mappingDataCol, ClassLoader classLoader)
            throws ClassNotFoundException {
        List<ServletInfo> servlets = new ArrayList<ServletInfo>(3);

        for (Servlet servletData : servletDataCol) {
            Class<? extends javax.servlet.Servlet> servletClass = loadServletClass(
                    servletData.servletClass, classLoader);
            servlets.add(createServletInfo(servletClass, servletData, mappingDataCol));
        }

        return servlets;
    }

    /**
     * Create undertow servlet entity
     * 
     * @param servletClass Servlet class
     * @param servletData Servlet data
     * @param mappingData Servlet mapping
     * @return Created entity
     */
    private static ServletInfo createServletInfo(
            Class<? extends javax.servlet.Servlet> servletClass,
            Servlet servletData, List<ServletMapping> mappingData) {
        ServletInfo servlet = servlet(servletData.servletName, servletClass);

        for (ServletMapping servletMapping : mappingData) {
            if (servletData.servletName.equals(servletMapping.servletName))
                servlet.addMapping(servletMapping.urlPattern);
        }

        // if (mapping == null)
        // throw new RuntimeException("Servlet has to have specified servlet-mapping entry!");

        for (InitParam param : servletData.initParams)
            servlet.addInitParam(param.paramName, param.paramValue);

        return servlet;
    }

    /**
     * Loads servlet class
     * 
     * @param servletClassName Servlet class name
     * @param classLoader Web archive classloader
     * @return Loaded class
     * @throws ClassNotFoundException Thrown if class could not be loaded by current class loader
     */
    private static Class<? extends javax.servlet.Servlet> loadServletClass(String servletClassName,
            ClassLoader classLoader) throws ClassNotFoundException {
        Class<?> clazz = Class.forName(servletClassName, true, classLoader);
        return clazz.asSubclass(javax.servlet.Servlet.class);
    }

}
