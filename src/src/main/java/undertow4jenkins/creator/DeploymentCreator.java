package undertow4jenkins.creator;

import static io.undertow.servlet.Servlets.deployment;
import io.undertow.security.idm.IdentityManager;
import io.undertow.server.HandlerWrapper;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.servlet.api.DeploymentInfo;

import java.io.Closeable;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import undertow4jenkins.CustomException;
import undertow4jenkins.handlers.AccessLoggerHandler;
import undertow4jenkins.handlers.SimpleAccessLogger;
import undertow4jenkins.loader.ErrorPageLoader;
import undertow4jenkins.loader.FilterLoader;
import undertow4jenkins.loader.ListenerLoader;
import undertow4jenkins.loader.MimeLoader;
import undertow4jenkins.loader.SecurityLoader;
import undertow4jenkins.loader.ServletLoader;
import undertow4jenkins.option.Options;
import undertow4jenkins.parser.WebXmlContent;
import undertow4jenkins.security.ArgumentsIdentityManager;
import undertow4jenkins.security.FileIdentityManager;

public class DeploymentCreator {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private ClassLoader classLoader;

    private Options options;

    private String pathToTmpDir;

    private String applicationContextPath;

    private List<Closeable> objToClose;

    public DeploymentCreator(ClassLoader classLoader, Options options, String pathToTmpDir,
            List<Closeable> objToClose, String applicationContextPath) {
        this.classLoader = classLoader;
        this.options = options;
        this.pathToTmpDir = pathToTmpDir;
        this.objToClose = objToClose;
        this.applicationContextPath = applicationContextPath;
    }

    public DeploymentInfo createServletContainerDeployment(WebXmlContent webXmlContent)
            throws ClassNotFoundException {
        DeploymentInfo servletContainerBuilder = deployment()
                // .setIgnoreFlush(true) TODO test performance of this option
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
                        SecurityLoader
                                .createLoginConfig(webXmlContent.loginConfig, "Jenkins Realm"))
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

        setAccessLogger(servletContainerBuilder);

        return servletContainerBuilder;
    }

    private void setAccessLogger(DeploymentInfo servletContainerBuilder) {
        if (options.accessLoggerClassName != null) {
            HandlerWrapper accessLoggerWrapper = createAccessLogger();
            if (accessLoggerWrapper != null)
                servletContainerBuilder.addInnerHandlerChainWrapper(accessLoggerWrapper);
        }
    }

    private HandlerWrapper createAccessLogger() {
        return new HandlerWrapper() {

            @Override
            public HttpHandler wrap(final HttpHandler handler) {
                try {
                    return createAccessLoggerHandler(handler);
                } catch (CustomException e) {
                    log.warn("Access logger could not be created. "
                            + "This feature is disabled! Reason: " + e.getMessage());
                    return handler;
                }
            }
        };
    }

    private HttpHandler createAccessLoggerHandler(final HttpHandler handler) throws CustomException {
        try {
            // Compatibility with old winstone class name
            if ("winstone.accesslog.SimpleAccessLogger".equals(options.accessLoggerClassName))
                options.accessLoggerClassName = SimpleAccessLogger.class.getName();

            Class<? extends AccessLoggerHandler> loggerClass = Class.forName(
                    options.accessLoggerClassName,
                    true, classLoader).asSubclass(AccessLoggerHandler.class);

            Constructor<? extends AccessLoggerHandler> loggerConstructor = loggerClass
                    .getConstructor(HttpHandler.class, String.class, String.class,
                            String.class);

            AccessLoggerHandler accessLoggerHandler = loggerConstructor.newInstance(handler,
                    "webapp", // same value of app name as in winstone
                    options.simpleAccessLogger_file, options.simpleAccessLogger_format);
            objToClose.add(accessLoggerHandler);

            return accessLoggerHandler;
        } catch (InvocationTargetException e) {
            throw new CustomException(e.getCause().getMessage());
        } catch (Throwable e) {
            throw new CustomException(e.getMessage());
        }
    }

    private void setSecurityActions(DeploymentInfo servletContainerBuilder) {
        if (!options.argumentsRealmPasswd.isEmpty() || 
                !options.realmClassName.equals("undertow4jenkins.security.ArgumentsIdentityManager")) {
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
            options.realmClassName = ArgumentsIdentityManager.class.getName();
        }
        else {
            if ("winstone.realm.FileRealm".equals(options.realmClassName)) {
                options.realmClassName = FileIdentityManager.class.getName();
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
