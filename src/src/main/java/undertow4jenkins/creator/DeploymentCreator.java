package undertow4jenkins.creator;

import static io.undertow.servlet.Servlets.deployment;
import io.undertow.security.idm.IdentityManager;
import io.undertow.server.HandlerWrapper;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.cache.DirectBufferCache;
import io.undertow.server.handlers.resource.CachingResourceManager;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.server.handlers.resource.ResourceManager;
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

/**
 * This class ensures initialization of Undertow servlet container 
 * 
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */
public class DeploymentCreator {

    private final Logger log = LoggerFactory.getLogger(getClass());

    /** ClassLoader for web application  */
    private ClassLoader classLoader;

    private Options options;

    /** Path to root directory of web application */
    private String webrootDir;

    /** Prefix of application URL */
    private String applicationContextPath;

    /** List of objects to be closed on  application end */
    private List<Closeable> objToClose;

    /**
     * Initializes creator
     * 
     * @param classLoader ClassLoder for web application
     * @param options Options of Undertow4Jenkins
     * @param webrootDir Path to root directory of web application
     * @param objToClose List of objects to be closed on application end
     * @param applicationContextPath Prefix of application URL
     */
    public DeploymentCreator(ClassLoader classLoader, Options options, String webrootDir,
            List<Closeable> objToClose, String applicationContextPath) {
        this.classLoader = classLoader;
        this.options = options;
        this.webrootDir = webrootDir;
        this.objToClose = objToClose;
        this.applicationContextPath = applicationContextPath;
    }

    /**
     * Creates whole configuration of servlet container
     * Process setting of configuration from web.xml and creating of identity manager 
     * and access logger (if specified) 
     * 
     * @param webXmlContent Content of web.xml
     * @return Created configuration of servlet container
     * @throws ClassNotFoundException Thrown if some specified class in web.xml or as parameter is not found
     */
    public DeploymentInfo createServletContainerDeployment(WebXmlContent webXmlContent)
            throws ClassNotFoundException {
        DeploymentInfo servletContainerBuilder = deployment()
                .setIgnoreFlush(true)
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
        servletContainerBuilder.setResourceManager(createResourceManager());
        
        // Set session timeout for application
        servletContainerBuilder.setSessionManagerFactory(new JenkinsSessionManagerFactory(
                options.sessionTimeout * 60));

        //Creates access logger
        setAccessLogger(servletContainerBuilder);

        return servletContainerBuilder;
    }

    /**
     * Creates caching resource manager to serve static resources from webroot directory
     * @return Created resource manager
     */
    private ResourceManager createResourceManager() {
        //Used values, which are used in some parts of Undertow 
        DirectBufferCache bufferCache = new DirectBufferCache(1024, 10, 10480);
        ResourceManager resourceManager = new FileResourceManager(new File(webrootDir), 10485760L);
        return new CachingResourceManager(100, 10000, bufferCache, resourceManager, -1);
    }

    /**
     * Add access logger to servlet container
     * @param servletContainerBuilder Container configuration object
     */
    private void setAccessLogger(DeploymentInfo servletContainerBuilder) {
        if (options.accessLoggerClassName != null) {
            HandlerWrapper accessLoggerWrapper = createAccessLogger();
            if (accessLoggerWrapper != null)
                servletContainerBuilder.addInnerHandlerChainWrapper(accessLoggerWrapper);
        }
    }

    /**
     * Creates handler wrapper for access logger
     * @return Created handler wrapper
     */
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

    /**
     * Creates handler for access logger
     * @param handler Next handler of chain
     * @return Created handler
     * @throws CustomException Thrown if specified AccessLoggerHandler class could not be loaded and started
     */
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

    /**
     * Prepares servlet container to support authentication 
     * @param servletContainerBuilder Container configuration object
     */
    private void setSecurityActions(DeploymentInfo servletContainerBuilder) {
        if (!options.argumentsRealmPasswd.isEmpty() || 
                !options.realmClassName.equals("undertow4jenkins.security.ArgumentsIdentityManager")) {
            // Initialization of Arguments Identity Manager
            mapWinstoneRealmNamesToIdentityManager(); // Compatibility with old winstone class names

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

    /**
     * Maps old winstone class names of security realms to identity manager classes 
     * created in this project (with analogous function)
     */
    private void mapWinstoneRealmNamesToIdentityManager() {
        if ("winstone.realm.ArgumentsRealm".equals(options.realmClassName)) {
            options.realmClassName = ArgumentsIdentityManager.class.getName();
        }
        else {
            if ("winstone.realm.FileRealm".equals(options.realmClassName)) {
                options.realmClassName = FileIdentityManager.class.getName();
            }
        }
    }

    /**
     * Sets display name
     * @param displayName Display name
     * @param servletContainerBuilder Container configuration object
     */
    private void setDisplayName(String displayName, DeploymentInfo servletContainerBuilder) {
        if (displayName != null)
            servletContainerBuilder.setDisplayName(displayName);
    }

    /**
     * Sets version of web application from web.xml
     * @param version Version string from web.xml
     * @param servletContainerBuilder Container configuration object
     */
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
