package undertow4jenkins;

import static io.undertow.servlet.Servlets.defaultContainer;
import static io.undertow.servlet.Servlets.deployment;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.Undertow.Builder;
import io.undertow.UndertowOptions;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;

import java.io.File;

import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import undertow4jenkins.listener.HttpsListenerBuilder;
import undertow4jenkins.listener.SimpleListenerBuilder;
import undertow4jenkins.loader.ErrorPageLoader;
import undertow4jenkins.loader.FilterLoader;
import undertow4jenkins.loader.ListenerLoader;
import undertow4jenkins.loader.MimeLoader;
import undertow4jenkins.loader.SecurityLoader;
import undertow4jenkins.loader.ServletLoader;
import undertow4jenkins.option.Options;
import undertow4jenkins.parser.WebXmlContent;
import undertow4jenkins.util.Configuration;

public class UndertowInitiator {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private ClassLoader classLoader;

    private Options options;

    private String pathToTmpDir;

    // Has to be set in constructor
    private String applicationContextPath;

    public UndertowInitiator(ClassLoader classLoader, Options options, String pathToTmpDir) {
        this.classLoader = classLoader;
        this.options = options;
        this.pathToTmpDir = pathToTmpDir;
        setContextPath(options.prefix);
    }

    public Undertow initUndertow(WebXmlContent webXmlContent) throws ServletException,
            ClassNotFoundException {

        DeploymentManager manager = defaultContainer().addDeployment(
                createServletContainerDeployment(webXmlContent));
        manager.deploy();

        Undertow.Builder serverBuilder = createUndertowInstance(manager);
        return serverBuilder.build();
    }

    private void setContextPath(String prefix) {
        if (prefix.isEmpty()) {
            applicationContextPath = "";
        }
        else {
            applicationContextPath = "/" + prefix;
        }
    }

    private Undertow.Builder createUndertowInstance(DeploymentManager manager)
            throws ServletException {

        HttpHandler containerHandler = createPathHandlerForContainer(manager.start());
        Undertow.Builder serverBuilder = Undertow.builder()
                .setHandler(containerHandler);

        // Create listeners
        SimpleListenerBuilder simpleListenerBuilder = new SimpleListenerBuilder(options);
        simpleListenerBuilder.setHttpListener(serverBuilder);
        simpleListenerBuilder.setAjpListener(serverBuilder);

        new HttpsListenerBuilder(options).setHttpsListener(serverBuilder);

        // Set specific options
        setCustomOptions(serverBuilder);

        return serverBuilder;
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
        if (options.httpsKeepAliveTimeout.equals(Configuration.getIntProperty("Options.defaultValue.httpsKeepAliveTimeout"))) {
            return true;
        }
        else {
            return false;
        }
    }

    private boolean isDefaultHttpTimeout() {
        if (options.httpKeepAliveTimeout.equals(Configuration.getIntProperty("Options.defaultValue.httpKeepAliveTimeout"))) {
            return true;
        }
        else {
            return false;
        }
    }

    private HttpHandler createPathHandlerForContainer(HttpHandler containerManagerHandler) {
        if (applicationContextPath.isEmpty()) {
            return containerManagerHandler;
        }
        else {
            return Handlers.path(Handlers.redirect(applicationContextPath))
                    .addPrefixPath(applicationContextPath, containerManagerHandler);
        }
    }

    private DeploymentInfo createServletContainerDeployment(WebXmlContent webXmlContent)
            throws ClassNotFoundException {
        DeploymentInfo servletContainerBuilder = deployment()
                .setClassLoader(classLoader)
                .setDeploymentName(options.warfile)
                .setContextPath(applicationContextPath)
                .addListeners(ListenerLoader.createListener(webXmlContent.listeners, classLoader))
                .addServlets(
                        ServletLoader.createServlets(webXmlContent.servlets,
                                webXmlContent.servletsMapping, classLoader))
                .addFilters(FilterLoader.createFilters(webXmlContent.filters, classLoader))
                .addSecurityRoles(SecurityLoader.createSecurityRoles(webXmlContent.securityRoles))
                .addSecurityConstraints(
                        SecurityLoader.createSecurityConstraints(webXmlContent.securityConstraints))
                .setLoginConfig(SecurityLoader.createLoginConfig(webXmlContent.loginConfig))
                .addErrorPages(ErrorPageLoader.createErrorPage(webXmlContent.errorPages))
                .addMimeMappings(
                        MimeLoader
                                .createMimeMappings(webXmlContent.mimeMappings, options.mimeTypes));

        FilterLoader.addFilterMappings(webXmlContent.filterMappings, servletContainerBuilder);
        setServletAppVersion(webXmlContent.webAppVersion, servletContainerBuilder);
        setDisplayName(webXmlContent.displayName, servletContainerBuilder);

        // Load static resources from extracted war archive
        servletContainerBuilder.setResourceManager(
                new FileResourceManager(new File(pathToTmpDir), 0L));

        // Set session timeout for application
        servletContainerBuilder.setSessionManagerFactory(new JenkinsSessionManagerFactory(
                options.sessionTimeout * 60));

        // TODO solve env-entry

        return servletContainerBuilder;
    }

    private void setDisplayName(String displayName, DeploymentInfo servletContainerBuilder) {
        if (displayName != null)
            servletContainerBuilder.setDisplayName(displayName);
    }

    private void setServletAppVersion(String version, DeploymentInfo servletContainerBuilder) {
        if (version == null)
            return;

        String[] versionArray = version.split("\\.");

        if (versionArray.length == 2) {
            try {
                int majorVersion = Integer.parseInt(versionArray[0]);
                int minorVersion = Integer.parseInt(versionArray[1]);
                servletContainerBuilder.setMajorVersion(majorVersion);
                servletContainerBuilder.setMinorVersion(minorVersion);
                return;
            } catch (NumberFormatException e) {
            }
        }

        log.warn("Version of web-app is not set properly!");
    }

}
