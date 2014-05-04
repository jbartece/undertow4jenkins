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

public class UndertowCreator {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private ClassLoader classLoader;

    private Options options;

    private String pathToTmpDir;

    /** Has to be set in constructor */
    private String applicationContextPath;

    public UndertowCreator(ClassLoader classLoader, Options options, String pathToTmpDir) {
        this.classLoader = classLoader;
        this.options = options;
        this.pathToTmpDir = pathToTmpDir;
        setContextPath(options.prefix);
    }

    public Undertow initUndertow(WebXmlContent webXmlContent, List<Closeable> objToClose)
            throws ServletException,
            ClassNotFoundException {
        DeploymentCreator deploymentCreator = new DeploymentCreator(classLoader, options, 
                pathToTmpDir, objToClose, applicationContextPath);
        
        DeploymentInfo deploymentInfo = deploymentCreator.createServletContainerDeployment(webXmlContent);
        DeploymentManager manager = defaultContainer().addDeployment(deploymentInfo);
        manager.deploy();

        Undertow.Builder serverBuilder = createUndertowInstance(manager);
        return serverBuilder.build();
    }

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

    private Undertow.Builder createUndertowInstance(DeploymentManager manager)
            throws ServletException {
        Undertow.Builder serverBuilder = Undertow.builder();

        createHandlerChain(serverBuilder, manager.start());
        createListeners(serverBuilder);
        setCustomOptions(serverBuilder);

        return serverBuilder;
    }

    private void createHandlerChain(Builder serverBuilder, HttpHandler containerManagerHandler) {
        HttpHandler redirectHandler = createRedirectHandlerForContainer(containerManagerHandler);

        serverBuilder.setHandler(redirectHandler);
    }

    private void createListeners(Undertow.Builder serverBuilder) {
        SimpleListenerBuilder simpleListenerBuilder = new SimpleListenerBuilder(options);
        simpleListenerBuilder.setHttpListener(serverBuilder);
        simpleListenerBuilder.setAjpListener(serverBuilder);

        new HttpsListenerBuilder(options).setHttpsListener(serverBuilder);
    }

    private void setCustomOptions(Builder serverBuilder) {
        setIdleTimeout(serverBuilder);
        serverBuilder.setWorkerThreads(options.handlerCountMax);
        serverBuilder.setServerOption(UndertowOptions.MAX_PARAMETERS, options.maxParamCount);
    }

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

    private boolean isDefaultHttpsTimeout() {
        if (options.httpsKeepAliveTimeout.equals(Configuration
                .getIntProperty("Options.defaultValue.httpsKeepAliveTimeout"))) {
            return true;
        }
        else {
            return false;
        }
    }

    private boolean isDefaultHttpTimeout() {
        if (options.httpKeepAliveTimeout.equals(Configuration
                .getIntProperty("Options.defaultValue.httpKeepAliveTimeout"))) {
            return true;
        }
        else {
            return false;
        }
    }

    private HttpHandler createRedirectHandlerForContainer(HttpHandler containerManagerHandler) {
        if (applicationContextPath.isEmpty()) {
            return containerManagerHandler;
        }
        else {
            return Handlers.path(Handlers.redirect(applicationContextPath))
                    .addPrefixPath(applicationContextPath, containerManagerHandler);
        }
    }

}
