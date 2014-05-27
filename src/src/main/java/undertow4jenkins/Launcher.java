package undertow4jenkins;

import io.undertow.Undertow;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
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
 * This class is entry point to the servlet container.
 * Coordinates starting of whole application and run the control port.
 * 
 * @author Jakub Bartecek <jbartece@redhat.com>
 * 
 */
public class Launcher implements Runnable {

    private final Logger log = LoggerFactory.getLogger(getClass());

    /** Code for control port message with type shutdown */
    private static final byte SHUTDOWN_REQUEST_TYPE = (byte) '0';

    /** Code for control port message with type request */
    private static final byte RELOAD_REQUEST_TYPE = (byte) '4';

    /** Container options */
    private Options options;

    /** Classloader to load classes from war of Jenkins */
    private ClassLoader jenkinsWarClassLoader;

    /** Created instance of undertow */
    private Undertow undertowInstance;

    /** List of objects to be closed on application end */
    private List<Closeable> objectsToClose = new ArrayList<Closeable>();

    /** Duration of timeout in control thread. Used for sleep and socket accept  */
    private static int CONTROL_TIMEOUT_DURATION = 2000;

    /** Control port */
    private int controlPort;

    /** Thread processing control */
    private Thread controlThread;
    
    /** Max port value */
    private static final int MAX_PORT = 65535;

    /**
     * Field for usage, which can be overridden outside this class (from extras-executable-war)
     */
    public static String USAGE;

    /**
     * Creates instance and store options
     * 
     * @param options Loaded command-line options for servlet container
     */
    public Launcher(Options options) {
        this.options = options;
        log.debug(options.toString());
    }

    /**
     * This method is entry point to initialize server Undertow and run
     * the Jenkins CI.
     */
    public void startApplication() {

        if (checkHelpParams() || checkAppConfig())
            return;

        try {
            String webRootPath = WarWorker.createWebApplicationRoot(options.warfile,
                    options.webroot);

            // Create class loader to load classed from jenkins.war archive.
            // It is needed to load servlet classes such as Stapler.
            this.jenkinsWarClassLoader = WarWorker.createJarsClassloader(
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

        runControlThread(options.controlPort);
    }

    /**
     * Runs control thread of this application
     * @param port control port
     */
    private void runControlThread(int port) {
        this.controlPort = port;
        this.controlThread =
                new Thread(this, "Undertow4Jenkins control thread");
        this.controlThread.setDaemon(false);
        this.controlThread.start();
    }

    
    /**
     * Processing life-cycle of control port thread
     */
    @Override
    public void run() {
        if (this.controlPort < -1 || this.controlPort > MAX_PORT) 
            log.warn("Unallowed controlPort value. Control port is disabled!");

        boolean interrupted = false;
        ServerSocket controlSocket = null;
        try {
            if(this.controlPort > 0 && this.controlPort < MAX_PORT) {
                controlSocket = new ServerSocket(this.controlPort);
                controlSocket.setSoTimeout(CONTROL_TIMEOUT_DURATION);
                log.info("Control port initializated. Port: " + this.controlPort);
            }

            while (!interrupted) {
                Socket acceptedSocket = null;
                
                try {
                    if(controlSocket != null) {
                        acceptedSocket = controlSocket.accept();
                        if (acceptedSocket != null)
                            handleControlRequest(acceptedSocket);
                    }
                    else 
                        Thread.sleep(CONTROL_TIMEOUT_DURATION);
                } catch (InterruptedIOException e) {
                } catch(InterruptedException e) {
                    // If program is killed with sigterm - OK
                    interrupted = true;
                } catch(Throwable e) {
                    log.error("Error occured in control thread. Reason: " + e.getMessage());
                } finally {
                    if(acceptedSocket != null)
                        closeSource(acceptedSocket);
                    if(Thread.interrupted())
                        interrupted = true;
                }
            }
            
            if(controlSocket != null)
                closeSource(controlSocket);

        } catch (IOException e) {
            log.error("Error occured on control port. Control port is disabled.");
        } catch (Throwable e) {
            log.error("Initialization of control thread failed! Reason: " + e.getMessage());
        }
        
        log.info("Control thread of undertow4jenkins was terminated.");
    }

    /**
     * Closes source and ignores possible exceptions
     * @param s Socket to close
     */
    private void closeSource(Closeable  s) {
        try {
            s.close();
        } catch (IOException e) {
        }
    }
    
    /**
     * Process received request on control port
     * 
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
                    internalShutdown();
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

    /**
     * Reload whole application - stops it and start is again
     */
    private void reloadApplication() {
        undertowInstance.stop();
        undertowInstance.start();
    }

    /**
     * Shutdown application - stops undertow, releases sources and stop JVM
     */
    private void internalShutdown() {
        undertowInstance.stop();
        undertowInstance = null;

        for (Closeable o : objectsToClose)
            closeSource(o);
        
        System.exit(0);
    }
    
    /**
     * Shutdown application and close objects with opened resources
     * Used by tests (not stopping JVM).
     */
    public void shutdownApplication() {
        undertowInstance.stop();
        undertowInstance = null;

        for (Closeable o : objectsToClose)
            closeSource(o);

        if (this.controlThread != null) 
            this.controlThread.interrupt();
        
        Thread.yield();
    }

    /**
     * Checks app options
     * 
     * @return False if webroot or warfile option is specified, otherwise true
     */
    private boolean checkAppConfig() {
        if (options.warfile != null || options.webroot != null)
            return false;
        else {
            log.error("Warfile or webroot has to be specified!");
            return true;
        }
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
     * Main method for Undertow4Jenkins
     * 
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        Logger log = LoggerFactory.getLogger("Main");
        log.info("Undertow4Jenkins is starting...");

        OptionParser optionParser = new OptionParser();
        Options options = optionParser.parse(args);
        if (options == null)
            return;

        Launcher launcher = new Launcher(options);
        launcher.startApplication();
    }
}
