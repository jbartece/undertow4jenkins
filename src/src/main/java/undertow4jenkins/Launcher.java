package undertow4jenkins;

import static io.undertow.servlet.Servlets.defaultContainer;
import static io.undertow.servlet.Servlets.deployment;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;

import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import undertow4jenkins.loader.ListenerLoader;
import undertow4jenkins.loader.ServletLoader;
import undertow4jenkins.option.OptionParser;
import undertow4jenkins.option.Options;
import undertow4jenkins.util.WarClassLoader;

/**
 * @author Jakub Bartecek <jbartece@redhat.com>
 * 
 */
public class Launcher {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private Options options;

    private ClassLoader jenkinsWarClassLoader;

    /**
     * Field for usage, which can be overridden outside this class
     */
    public static String USAGE;

    public Launcher(Options options) {
        log.debug("constructor");

        this.options = options;
        log.info(options.toString());

        // Create class loader to load classed from jenkins.war archive.
        // It is needed to load servlet classes such as Stapler.
        this.jenkinsWarClassLoader = WarClassLoader.createJarsClassloader(options.warfile);

    }

    public void run() {
        log.debug("run");

        try {
            startUndertow();
        } catch (ServletException e) {
            log.error("Start of embedded Undertow server failed!", e);
        }

    }

    private void startUndertow() throws ServletException {

        DeploymentInfo servletBuilder = deployment()
                .setClassLoader(Launcher.class.getClassLoader())
                .setContextPath("/")
                .addListener(
                        new ListenerLoader(this.jenkinsWarClassLoader)
                                .createListener("hudson.WebAppMain"))
                // TODO - check
                .setDeploymentName("Jenkins CI")
                .addServlets(new ServletLoader(jenkinsWarClassLoader).getServlets());

        DeploymentManager manager = defaultContainer().addDeployment(servletBuilder);
        manager.deploy();

        HttpHandler servletHandler = manager.start();
        PathHandler pathHandler = Handlers.path(Handlers.redirect("/"))
                .addPrefixPath("/", servletHandler);

        Undertow server = Undertow.builder()
                .addHttpListener(options.httpPort, "localhost")
                // .addAjpListener(options.ajp13Port, options.ajp13ListenAdress)
                .setHandler(pathHandler)
                .setWorkerThreads(options.handlerCountMax) // TODO
                .build();
        server.start();

    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        Logger log = LoggerFactory.getLogger("Main");
        log.info("Undertow4Jenkins is starting...");

        OptionParser optionParser = new OptionParser();
        Options options = optionParser.parse(args);

        Launcher launcher = new Launcher(options);
        launcher.run();
    }

}
