package undertow4jenkins;

import static io.undertow.servlet.Servlets.defaultContainer;
import static io.undertow.servlet.Servlets.deployment;
import io.undertow.Undertow;
import io.undertow.Undertow.Builder;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;

import java.io.File;

import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import undertow4jenkins.loader.ErrorPageLoader;
import undertow4jenkins.loader.FilterLoader;
import undertow4jenkins.loader.ListenerLoader;
import undertow4jenkins.loader.MimeLoader;
import undertow4jenkins.loader.SecurityLoader;
import undertow4jenkins.loader.ServletLoader;
import undertow4jenkins.option.Options;
import undertow4jenkins.parser.WebXmlContent;

public class UndertowInitiator {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private ClassLoader classLoader;

    private Options options;

    private String pathToTmpDir;

    /**
     * IP adress, which should be set to bind listener in Undertow to listen on all interfaces
     */
    private final String hostAllInterfacesString = "0.0.0.0";

    public UndertowInitiator(ClassLoader classLoader, Options options, String pathToTmpDir) {
        this.classLoader = classLoader;
        this.options = options;
        this.pathToTmpDir = pathToTmpDir;
    }

    public Undertow initUndertow(WebXmlContent webXmlContent) throws ServletException,
            ClassNotFoundException {

        DeploymentManager manager = defaultContainer().addDeployment(
                createServletContainerDeployment(webXmlContent));
        manager.deploy();

        Undertow.Builder serverBuilder = Undertow.builder()
                .setHandler(manager.start())
                .setWorkerThreads(options.handlerCountMax); // TODO

        setHttpListener(serverBuilder);
        setHttpsListener(serverBuilder);
        setAjpListener(serverBuilder);
        createControlPort(serverBuilder);

        return serverBuilder.build();
    }

    // "   --httpKeepAliveTimeout   = how long idle HTTP keep-alive connections are kept around (in ms; default 5000)?\n" +
    /**
     * Creates HTTP listener based on values from options.httpPort, options.httpListenAdress.
     * 
     * @param serverBuilder Prepared Undertow instance to which listener will be added
     */
    private void setHttpListener(Builder serverBuilder) {
        if (options.httpPort == -1) {
            log.info("Http listener is disabled.");
        }
        else {
            if (options.httpPort < -1) {
                log.warn("Unallowed httpPort value. Http listener is disabled!");
            }
            else {
                if (options.httpListenAdress != null)
                    serverBuilder.addHttpListener(options.httpPort, options.httpListenAdress);
                else {
                    // Listen on all interfaces
                    serverBuilder.addHttpListener(options.httpPort, hostAllInterfacesString);
                }
                log.debug("Created HTTP listener");
            }
        }
    }

    // "   --httpsPort              = set the https listening port. -1 to disable, Default is disabled\n" +
    // "                              if neither --httpsCertificate nor --httpsKeyStore are specified,\n" +
    // "                              https is run with one-time self-signed certificate.\n" +
    // "   --httpsListenAddress     = set the https listening address. Default is all interfaces\n" +
    // "   --httpsDoHostnameLookups = enable host name lookups on incoming https connections (true/false). Default is false\n" +
    // "   --httpsKeepAliveTimeout   = how long idle HTTPS keep-alive connections are kept around (in ms; default 5000)?\n" +
    // "   --httpsKeyStore          = the location of the SSL KeyStore file.\n" +
    // "   --httpsKeyStorePassword  = the password for the SSL KeyStore file. Default is null\n" +
    // "   --httpsCertificate       = the location of the PEM-encoded SSL certificate file.\n" +
    // "                              (the one that starts with '-----BEGIN CERTIFICATE-----')\n" +
    // "                              must be used with --httpsPrivateKey.\n" +
    // "   --httpsPrivateKey        = the location of the PEM-encoded SSL private key.\n" +
    // "                              (the one that starts with '-----BEGIN RSA PRIVATE KEY-----')\n" +
    // "   --httpsKeyManagerType    = the SSL KeyManagerFactory type (eg SunX509, IbmX509). Default is SunX509\n" +
    private void setHttpsListener(Builder serverBuilder) {
        if(options.httpsPort == -1)
            return;

    }

    /**
     * Creates AJP listener based on values from options.ajpPort, options.ajpListenAdress.
     * 
     * @param serverBuilder Prepared Undertow instance to which listener will be added
     */
    private void setAjpListener(Builder serverBuilder) {
        if(options.ajp13Port == -1) 
            return;
        
        if(options.ajp13Port < -1) {
            log.warn("Unallowed ajpPort value. Ajp listener is disabled!");
        }
        else {
            if(options.ajp13ListenAdress != null)
                serverBuilder.addAjpListener(options.ajp13Port, options.ajp13ListenAdress);
            else {
                //Listen on all interfaces
                serverBuilder.addAjpListener(options.ajp13Port, hostAllInterfacesString);
            }
            
            log.debug("Created AJP listener");
        }
    }

    // "   --controlPort            = set the shutdown/control port. -1 to disable, Default disabled\n" +
    private void createControlPort(Builder serverBuilder) {
        if(options.controlPort == -1)
            return;
        //TODO

    }

    private DeploymentInfo createServletContainerDeployment(WebXmlContent webXmlContent)
            throws ClassNotFoundException {
        DeploymentInfo servletContainerBuilder = deployment()
                .setClassLoader(classLoader)
                .setContextPath("")
                .setDeploymentName(options.warfile)
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
                .addMimeMappings(MimeLoader.createMimeMappings(webXmlContent.mimeMappings));

        FilterLoader.addFilterMappings(webXmlContent.filterMappings, servletContainerBuilder);
        setServletAppVersion(webXmlContent.webAppVersion, servletContainerBuilder);
        setDisplayName(webXmlContent.displayName, servletContainerBuilder);

        // Load static resources from extracted war archive
        servletContainerBuilder.setResourceManager(
                new FileResourceManager(new File(pathToTmpDir), 0L));

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
