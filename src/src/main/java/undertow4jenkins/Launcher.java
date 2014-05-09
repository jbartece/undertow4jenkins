package undertow4jenkins;

import io.undertow.Undertow;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import undertow4jenkins.creator.UndertowCreator;
import undertow4jenkins.option.OptionParser;
import undertow4jenkins.option.Options;
import undertow4jenkins.parser.WebXmlContent;
import undertow4jenkins.parser.WebXmlParser;
import undertow4jenkins.util.Configuration;
import undertow4jenkins.util.WarWorker;

/**
 * @author Jakub Bartecek <jbartece@redhat.com>
 * 
 */
public class Launcher {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final byte SHUTDOWN_REQUEST_TYPE = (byte) '0';

    private static final byte RELOAD_REQUEST_TYPE = (byte) '4';

    private Options options;

    private ClassLoader jenkinsWarClassLoader;

    private Undertow undertowInstance;

    private List<Closeable> objectsToClose = new ArrayList<Closeable>();

    /**
     * Field for usage, which can be overridden outside this class
     */
    public static String USAGE;

    public Launcher(Options options) {
        this.options = options;
        log.debug(options.toString());
    }

    public void run() {

        if (checkHelpParams() || checkAppConfig())
            return;

        try {
            String webRootPath = WarWorker.createWebApplicationRoot(options.warfile, 
                    options.webroot);

            // Create class loader to load classed from jenkins.war archive.
            // It is needed to load servlet classes such as Stapler.
            this.jenkinsWarClassLoader = WarWorker.createJarsClassloader(options.warfile,
                    options.commonLibFolder, webRootPath, getClass().getClassLoader());

            WebXmlParser parser = new WebXmlParser();
            WebXmlContent webXmlContent = parser.parse(webRootPath + "WEB-INF/web.xml");

            log.info("Webroot directory: " + webRootPath);
            UndertowCreator undertowInitiator = new UndertowCreator(jenkinsWarClassLoader,
                    options, webRootPath);
            undertowInstance = undertowInitiator.initUndertow(webXmlContent, objectsToClose);
            undertowInstance.start();

        } catch (Throwable e) {
            log.error("Initialization of servlet container failed! Reason: " + e.getMessage());
        } 

        listenOnControlPort(options.controlPort);
    }
    
    // private static int controlSocketTimeout = 2000;


    private void listenOnControlPort(int port) {
        if (port == -1)
            return;

        if (port < -1 || port > 65535) {
            log.warn("Unallowed controlPort value. Control port is disabled!");
            return;
        }

        boolean interrupted = false;
        ServerSocket controlSocket = null;
        try {
            controlSocket = new ServerSocket(port);
            // TODO check if timeout is needed
            // controlSocket.setSoTimeout(controlSocketTimeout);
            log.info("Control port initializated. Port: " + port);

            while (!interrupted) {
                Socket acceptedSocket = controlSocket.accept();
                handleControlRequest(acceptedSocket);   //TODO solve interruption
            }

        } catch (IOException e) {
            log.error("Error occured on control port. Control port is disabled.");
        } catch (Throwable e) {
            // If program is killed with sigterm - OK
        } finally {
            try {
                if (controlSocket != null)
                    controlSocket.close();
            } catch (IOException e) {
            }
        }

    }

    /**
     * @return true if shutdown request was accepted, otherwise false
     */
    private void handleControlRequest(Socket acceptedSocket) throws IOException {
        InputStream inputStream = null;

        try {
            inputStream = acceptedSocket.getInputStream();
            int requestType = inputStream.read();
            log.debug("Accepted control request with type: " + requestType + " [byte value]");

            switch ((byte) requestType) {
                case SHUTDOWN_REQUEST_TYPE:
                    log.info("Accepted shutdown request on control port");
                    shutdownApplication();
                    break;

                case RELOAD_REQUEST_TYPE:
                    log.info("Accepted reload request on control port");
                    reloadApplication();
                    break;

                default:
                    log.info("Accepted unknown request on control port");
                    break;
            }
        } finally {
            acceptedSocket.close();

            if (inputStream != null)
                inputStream.close();
        }

    }

    private void reloadApplication() {
        undertowInstance.stop();
        undertowInstance.start();
    }

    public void shutdownApplication() {
        undertowInstance.stop();

        try {
            for (Closeable o : objectsToClose)
                o.close();
        } catch (IOException e) {
        }
        // System.exit(0);
    }

    /**
     * Checks app options
     * 
     * @return False if webroot or warfile option is specified, otherwise true
     */
    private boolean checkAppConfig() {
        if (options.warfile != null || options.webroot != null)
            return false;
        else
            return true;
    }

    /**
     * Checks one of help/usage/version parameters was set. If so, print proper information.
     * 
     * @return True if help/usage/version was specified, otherwise false
     */
    private boolean checkHelpParams() {
        if ((options.help != null && options.help) || (options.usage != null && options.usage)) {
            System.out.println(Launcher.USAGE);
            return true;
        }

        if (options.version != null && options.version) {
            System.out.println("Undertow4jenkins version: "
                    + Configuration.getProperty("App.version"));
            return true;
        }

        return false;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        Logger log = LoggerFactory.getLogger("Main");
        log.info("Undertow4Jenkins is starting...");

        OptionParser optionParser = new OptionParser();
        Options options = optionParser.parse(args);
        if (options == null)
            return;

        Launcher launcher = new Launcher(options);
        launcher.run();
    }
}
