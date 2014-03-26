package undertow4jenkins;

import static io.undertow.servlet.Servlets.defaultContainer;
import static io.undertow.servlet.Servlets.deployment;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import undertow4jenkins.loader.FilterLoader;
import undertow4jenkins.loader.ServletLoader;
import undertow4jenkins.option.OptionParser;
import undertow4jenkins.option.Options;

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

        // Create class loader to load classed from jenkins.war archive.
        // It is needed to load servlet classes such as Stapler.
        createJarsClassloader();

        log.info(options.toString());
    }

    private void createJarsClassloader() {
        byte buffer[] = new byte[8192];
        try {
            JarFile warArchive = new JarFile(options.warfile);

            List<URL> jarUrls = new ArrayList<URL>();

            for (Enumeration<JarEntry> e = warArchive.entries(); e.hasMoreElements();) {
                JarEntry element = (JarEntry) e.nextElement();

                if (element.getName().startsWith("WEB-INF/lib/") &&
                        element.getName().endsWith(".jar")) {
                    log.trace("Jar entry: " + element.getName());

                    File outFile = new File("/tmp/undertow4jenkins/" + element.getName());

                    if (!outFile.exists()) {
                        outFile.getParentFile().mkdirs();
                        // Copy out the extracted file
                        InputStream inContent = warArchive.getInputStream(element);
                        OutputStream outStream = new FileOutputStream(outFile);
                        int readBytes = inContent.read(buffer);
                        while (readBytes != -1) {
                            outStream.write(buffer, 0, readBytes);
                            readBytes = inContent.read(buffer);
                        }
                        inContent.close();
                        outStream.close();
                    }

                    jarUrls.add(outFile.toURI().toURL());
                }

            }
            warArchive.close();

            log.trace("Jar URLs: " + jarUrls.toString());
            // URL[] warFileURL = new URL[] { new File(options.warfile + File.separator
            // + "WEB-INF/lib/stapler-1.223.jar").toURI().toURL() };

            // this.jenkinsWarClassLoader = new URLClassLoader(warFileURL,
            // getClass().getClassLoader());
            // log.debug("Created ClassLoader for jenkins.war: " + warFileURL[0].toString());
            this.jenkinsWarClassLoader = new URLClassLoader(
                    jarUrls.toArray(new URL[jarUrls.size()]),
                    getClass().getClassLoader());
        } catch (MalformedURLException e) {
            log.error("Bad path to jenkins.war file!", e);
        } catch (IOException e) {
            log.error("War archive", e);
        }
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
