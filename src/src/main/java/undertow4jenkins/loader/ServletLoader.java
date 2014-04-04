package undertow4jenkins.loader;

import static io.undertow.servlet.Servlets.servlet;
import io.undertow.servlet.api.ServletInfo;

import java.util.ArrayList;
import java.util.List;

import undertow4jenkins.parser.WebXmlContent.InitParam;
import undertow4jenkins.parser.WebXmlContent.Servlet;
import undertow4jenkins.parser.WebXmlContent.ServletMapping;

public class ServletLoader {

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

    private static ServletInfo createServletInfo(
            Class<? extends javax.servlet.Servlet> servletClass,
            Servlet servletData, List<ServletMapping> mappingData) {
        String mapping = null;

        for (ServletMapping servletMapping : mappingData) {
            if (servletData.servletName.equals(servletMapping.servletName))
                mapping = servletMapping.urlPattern;
        }

        if (mapping == null)
            throw new RuntimeException("Servlet has to have specified servlet-mapping entry!");

        ServletInfo servlet = servlet(servletData.servletName, servletClass)
                .addMapping(mapping);

        for(InitParam param : servletData.initParams) 
            servlet.addInitParam(param.paramName, param.paramValue); 
        
        return servlet;
    }

    private static Class<? extends javax.servlet.Servlet> loadServletClass(String servletClassName,
            ClassLoader classLoader) throws ClassNotFoundException {
        Class<?> clazz = Class.forName(servletClassName, true, classLoader);
        return clazz.asSubclass(javax.servlet.Servlet.class);
    }

}
