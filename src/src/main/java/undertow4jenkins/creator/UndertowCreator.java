package undertow4jenkins.creator;

import static io.undertow.servlet.Servlets.defaultContainer;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.Undertow.Builder;
import io.undertow.UndertowOptions;
import io.undertow.server.HttpHandler;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;

import java.io.Closeable;
import java.util.List;

import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import undertow4jenkins.listener.HttpsListenerBuilder;
import undertow4jenkins.listener.SimpleListenerBuilder;
import undertow4jenkins.option.Options;
import undertow4jenkins.parser.WebXmlContent;
import undertow4jenkins.util.Configuration;

/**
 * This class ensures initialization of Undertow webserver
 * and delegates creating of servlet container configuration do DeploymentCreator.
 * 
 * 
 * @author Jakub Bartecek <jbartece@redhat.com>
 * 
 */
public class UndertowCreator {

    private final Logger log = LoggerFactory.getLogger(getClass());

    /** ClassLoader for web application */
    private ClassLoader classLoader;

    private Options options;

    /** Path to root directory of web application */
    private String webrootDir;

    /** Context path of application. Has to be set in constructor */
    private String applicationContextPath;

    /**
     * Initializes creator
     * 
     * @param classLoader ClassLoader for web application
     * @param options Options of Undertow4Jenkins
     * @param webrootDir Path to root directory of web application
     */
    public UndertowCreator(ClassLoader classLoader, Options options, String webrootDir) {
        this.classLoader = classLoader;
        this.options = options;
        this.webrootDir = webrootDir;
        setContextPath(options.prefix);
    }

    /**
     * Process initialization of webserver and servlet container
     * 
     * @param webXmlContent Content of web.xml
     * @param objToClose List of objects to be closed on application end
     * @return Created server instance
     * @throws ServletException Thrown if there are problems with servlet
     * @throws ClassNotFoundException Thrown if some specified class in web.xml or as parameter is not found
     */
    public Undertow initUndertow(WebXmlContent webXmlContent, List<Closeable> objToClose)
            throws ServletException,
            ClassNotFoundException {
        DeploymentCreator deploymentCreator = new DeploymentCreator(classLoader, options,
                webrootDir, objToClose, applicationContextPath);

        DeploymentInfo deploymentInfo = deploymentCreator
                .createServletContainerDeployment(webXmlContent);
        DeploymentManager manager = defaultContainer().addDeployment(deploymentInfo);
        manager.deploy();

        Undertow.Builder serverBuilder = createUndertowInstance(manager);
        return serverBuilder.build();
    }

    /**
     * Set context path from prefix 
     * @param prefix Prefix of application URL
     */
    private void setContextPath(String prefix) {
        if (prefix.isEmpty() || (prefix.startsWith("/") && prefix.length() == 1)) {
            applicationContextPath = "";
        }
        else {
            if (prefix.startsWith("/"))
                applicationContextPath = prefix;
            else
                applicationContextPath = "/" + prefix;
        }
    }

    /**
     * Prepares instance of undertow webserver instance
     * 
     * @param manager Prepared servlet container instance
     * @return Prepared instance of undertow
     * @throws ServletException Thrown if there are problems with servlet
     */
    private Undertow.Builder createUndertowInstance(DeploymentManager manager)
            throws ServletException {
        Undertow.Builder serverBuilder = Undertow.builder();

        createHandlerChain(serverBuilder, manager.start());
        createListeners(serverBuilder);
        setCustomOptions(serverBuilder);

        return serverBuilder;
    }

    /**
     * Prepares handler chain for Undertow
     * 
     * Currently creates only redirect handler
     * 
     * @param serverBuilder Builder of Undertow
     * @param containerManagerHandler First handler of servlet container
     */
    private void createHandlerChain(Builder serverBuilder, HttpHandler containerManagerHandler) {
        HttpHandler redirectHandler = createRedirectHandlerForContainer(containerManagerHandler);

        serverBuilder.setHandler(redirectHandler);
    }

    /**
     * Creates all listeners for specified protocols 
     * 
     * @param serverBuilder Builder of Undertow
     */
    private void createListeners(Undertow.Builder serverBuilder) {
        SimpleListenerBuilder simpleListenerBuilder = new SimpleListenerBuilder(options);
        simpleListenerBuilder.setHttpListener(serverBuilder);
        simpleListenerBuilder.setAjpListener(serverBuilder);

        new HttpsListenerBuilder(options).setHttpsListener(serverBuilder);
    }

    /**
     * Edits options of Undertow
     * @param serverBuilder Builder of Undertow
     */
    private void setCustomOptions(Builder serverBuilder) {
        setIdleTimeout(serverBuilder);
        serverBuilder.setWorkerThreads(options.handlerCountMax);
        serverBuilder.setServerOption(UndertowOptions.MAX_PARAMETERS, options.maxParamCount);
    }
    
    /**
     * Sets option keep-alive timeout to Undertow from parameters httpsKeepAliveTimeout, httpKeepAliveTimeout.
     * 
     * 
     * It is too much complicated to set it separately for HTTP and HTTPS,
     * so there war chosen compromise to set it together.
     * If both httpsKeepAliveTimeout and httpKeepAliveTimeout are set,
     * the HTTP parameter has higher priority.
     * 
     * @param serverBuilder Builder of Undertow
     */
    private void setIdleTimeout(Builder serverBuilder) {
        boolean defaultHttpValue = isDefaultHttpTimeout();
        boolean defaultHttpsValue = isDefaultHttpsTimeout();

        Integer timeoutValue;

        // There are two options, which can have this value, but Undertow supports
        // only same value for all listeners
        if (defaultHttpsValue && defaultHttpValue)
            timeoutValue = options.httpKeepAliveTimeout;
        else {
            if (!defaultHttpValue && !defaultHttpsValue) {
                log.warn("Undertow does not support different keepAliveTimeout "
                        + "for HTTP and HTTPS. httpKeepAliveTimeout value used.");
                timeoutValue = options.httpKeepAliveTimeout;
            }
            else {
                if (defaultHttpValue)
                    timeoutValue = options.httpKeepAliveTimeout;
                else {
                    timeoutValue = options.httpsKeepAliveTimeout;
                }
            }
        }

        serverBuilder.setServerOption(
                UndertowOptions.IDLE_TIMEOUT,
                timeoutValue.longValue());

    }

    /**
     * @return True if httpsKeepAliveTimeout was not specified, otherwise false.
     */
    private boolean isDefaultHttpsTimeout() {
        if (options.httpsKeepAliveTimeout.equals(Configuration
                .getIntProperty("Options.defaultValue.httpsKeepAliveTimeout"))) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * @return True if httpKeepAliveTimeout was not specified, otherwise false.
     */
    private boolean isDefaultHttpTimeout() {
        if (options.httpKeepAliveTimeout.equals(Configuration
                .getIntProperty("Options.defaultValue.httpKeepAliveTimeout"))) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Creates redirect handler
     * 
     * @param handler Next handler of chain
     * @return Created handler
     */
    private HttpHandler createRedirectHandlerForContainer(HttpHandler handler) {
        if (applicationContextPath.isEmpty()) {
            return handler;
        }
        else {
            return Handlers.path(Handlers.redirect(applicationContextPath))
                    .addPrefixPath(applicationContextPath, handler);
        }
    }

}
