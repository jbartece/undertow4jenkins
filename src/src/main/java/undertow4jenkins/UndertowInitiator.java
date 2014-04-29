package undertow4jenkins;

import static io.undertow.servlet.Servlets.defaultContainer;
import static io.undertow.servlet.Servlets.deployment;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.Undertow.Builder;
import io.undertow.UndertowOptions;
import io.undertow.security.idm.IdentityManager;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;

import java.io.Closeable;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import undertow4jenkins.handlers.AccessLoggerHandler;
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

    /** Has to be set in constructor */
    private String applicationContextPath;

    List<Closeable> objToClose;

    public UndertowInitiator(ClassLoader classLoader, Options options, String pathToTmpDir) {
        this.classLoader = classLoader;
        this.options = options;
        this.pathToTmpDir = pathToTmpDir;
        setContextPath(options.prefix);
    }

    public Undertow initUndertow(WebXmlContent webXmlContent, List<Closeable> objToClose)
            throws ServletException,
            ClassNotFoundException {
        this.objToClose = objToClose;

        DeploymentManager manager = defaultContainer().addDeployment(
                createServletContainerDeployment(webXmlContent));
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
        HttpHandler next = containerManagerHandler;

        if (options.accessLoggerClassName != null) {
            next = createAccessLogger(next); // TODO move to container handlers
        }

        HttpHandler redirectHandler = createRedirectHandlerForContainer(next);

        serverBuilder.setHandler(redirectHandler);
    }

    private HttpHandler createAccessLogger(HttpHandler next) {
        try {
            // TODO Map name to old winstone class name
            Class<? extends AccessLoggerHandler> loggerClass = Class.forName(
                    options.accessLoggerClassName,
                    true, classLoader).asSubclass(AccessLoggerHandler.class);

            Constructor<? extends AccessLoggerHandler> loggerConstructor = loggerClass
                    .getConstructor(HttpHandler.class, String.class, String.class,
                            String.class);

            AccessLoggerHandler accessLoggerHandler = loggerConstructor.newInstance(next,
                    "webapp", // same value of app name as in winstone
                    options.simpleAccessLogger_file, options.simpleAccessLogger_format);
            objToClose.add(accessLoggerHandler);

            next = accessLoggerHandler;
        } catch (InvocationTargetException e) {
            log.error("Access logger could not be created. "
                    + "This feature is disabled! Reason: " + e.getCause().getMessage());
        } catch (Throwable e) {
            log.error("Access logger could not be created. "
                    + "This feature is disabled! Reason: " + e.getMessage());
        }

        return next;
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
                .setLoginConfig(
                        SecurityLoader.createLoginConfig(webXmlContent.loginConfig, "Jenkins"))
                .addErrorPages(ErrorPageLoader.createErrorPage(webXmlContent.errorPages))
                .addMimeMappings(
                        MimeLoader
                                .createMimeMappings(webXmlContent.mimeMappings, options.mimeTypes));

        setSecurityActions(servletContainerBuilder);

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

        // servletContainerBuilder.
        return servletContainerBuilder;
    }

    private void setSecurityActions(DeploymentInfo servletContainerBuilder) {
        if (!options.argumentsRealmPasswd.isEmpty() || options.fileRealm_configFile != null) {
            // Initialization of Arguments Identity Manager
            mapWinstoneRealmNamesToIdentityManager(options); // Compatibility with old winstone class names

            try {
                // Locate identity manager class
                Class<? extends IdentityManager> managerClass =
                        Class.forName(options.realmClassName, true, classLoader)
                                .asSubclass(IdentityManager.class);
                Constructor<? extends IdentityManager> managerConstructor = managerClass
                        .getConstructor(Options.class);

                // Create and add classloader to deployment
                IdentityManager idManager = managerConstructor.newInstance(options);
                servletContainerBuilder.setIdentityManager(idManager);
            } catch (InvocationTargetException e) {
                log.error("Security support could not be created. "
                        + "This feature is disabled! Reason: " + e.getCause().getMessage());
            } catch (Throwable e) {
                log.error("Security support could not be created. "
                        + "This feature is disabled! Reason: " + e.getMessage());
            }
        }
    }

    private void mapWinstoneRealmNamesToIdentityManager(Options options2) {
        if ("winstone.realm.ArgumentsRealm".equals(options.realmClassName)) {
            options.realmClassName = "undertow4jenkins.security.ArgumentsIdentityManager";
        }
        else {
            if ("winstone.realm.FileRealm".equals(options.realmClassName)) {
                options.realmClassName = "undertow4jenkins.security.FileIdentityManager";
            }
        }
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
