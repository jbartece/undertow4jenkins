package undertow4jenkins.loader;

import static io.undertow.servlet.Servlets.servlet;
import io.undertow.servlet.api.ServletInfo;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.Servlet;

public class ServletLoader {

    private static String[] servletClasses = { "org.kohsuke.stapler.Stapler" };

    public static List<ServletInfo> getServlets(ClassLoader classLoader)
            throws ClassNotFoundException {
        List<ServletInfo> servlets = new ArrayList<ServletInfo>();

        for (String servletStr : servletClasses) {
            Class<? extends Servlet> servletClass = loadServletClass(servletStr, classLoader);
            servlets.add(createServletInfo(servletClass));
        }

        return servlets;
    }

    private static ServletInfo createServletInfo(Class<? extends Servlet> clazzServlet) {
        return servlet("Stapler", clazzServlet)
                .addInitParam("default-encodings", "text/html=UTF-8")
                .addMapping("/*");
    }

    private static Class<? extends Servlet> loadServletClass(String servletClassName,
            ClassLoader classLoader) throws ClassNotFoundException {
        Class<?> clazz = Class.forName(servletClassName, true, classLoader);
        return clazz.asSubclass(Servlet.class);
    }

}
