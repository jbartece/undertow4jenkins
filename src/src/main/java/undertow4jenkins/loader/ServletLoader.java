package undertow4jenkins.loader;

import static io.undertow.servlet.Servlets.servlet;
import io.undertow.servlet.api.ServletInfo;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.Servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ServletLoader {

    private final Logger log = LoggerFactory.getLogger(getClass());
    
    private ClassLoader classLoader;
    
    private String[] servletClasses = {"org.kohsuke.stapler.Stapler"};
    
    public ServletLoader(ClassLoader cl) {
        this.classLoader = cl;
        
    }
    
    public List<ServletInfo> getServlets() {
        List<ServletInfo> servlets = new ArrayList<ServletInfo>();
        
        for(String servletStr : servletClasses) {
            Class<? extends Servlet> servletClazz = loadServletClass(servletStr);
            servlets.add(createServletInfo(servletClazz));
        }

        
        return servlets;
    }
    

    private ServletInfo createServletInfo(Class<? extends Servlet> clazzServlet) {
        return servlet("Stapler", clazzServlet)
                .addInitParam("default-encodings", "text/html=UTF-8")
                .addMapping("/*");
    }

    private Class<? extends Servlet> loadServletClass(String servletClassName) {
        try {
            ClassLoader classLoader = this.classLoader;
            Class<?> clazz = Class.forName(servletClassName, true, classLoader);
            return clazz.asSubclass(Servlet.class);

            // return (Class<Servlet>) Class.forName(
            // servletClassName, true, classLoader);
        } catch (ClassNotFoundException e) {
            log.error("Loading of servlet class failed!", e);
        } catch (RuntimeException e) {
            log.error("Loading of servlet class failed!", e);
        }

        return null;
    }

}
