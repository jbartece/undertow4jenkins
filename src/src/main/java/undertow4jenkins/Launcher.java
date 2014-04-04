package undertow4jenkins;

import io.undertow.Undertow;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.xml.stream.XMLStreamException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import undertow4jenkins.option.OptionParser;
import undertow4jenkins.option.Options;
import undertow4jenkins.parser.WebXmlContent;
import undertow4jenkins.parser.WebXmlFormatException;
import undertow4jenkins.parser.WebXmlParser;
import undertow4jenkins.util.WarWorker;

/**
 * @author Jakub Bartecek <jbartece@redhat.com>
 * 
 */
public class Launcher {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private Options options;

    private ClassLoader jenkinsWarClassLoader;

    private final String pathToTmpDir = "/tmp/undertow4jenkins/extractedWar/";

    /**
     * Field for usage, which can be overridden outside this class
     */
    public static String USAGE;

    public Launcher(Options options) {
        this.options = options;
        log.info(options.toString());
    }

    public void run() {
        try {
            WarWorker.extractFilesFromWar(options.warfile, pathToTmpDir);
            // Create class loader to load classed from jenkins.war archive.
            // It is needed to load servlet classes such as Stapler.
            this.jenkinsWarClassLoader = WarWorker.createJarsClassloader(options.warfile,
                    pathToTmpDir);

            WebXmlParser parser = new WebXmlParser();
            WebXmlContent webXmlContent = parser.parse(pathToTmpDir + "WEB-INF/web.xml");

            if (log.isDebugEnabled())
                log.debug("Loaded content of web.xml:\n" + webXmlContent.toString());

            UndertowInitiator undertowInitiator = new UndertowInitiator(jenkinsWarClassLoader,
                    options, pathToTmpDir);
            Undertow undertowInstance = undertowInitiator.initUndertow(webXmlContent);
            undertowInstance.start();
        } catch (ServletException e) {
            log.error("Start of embedded Undertow server failed!", e);
        } catch (IOException e) {
            log.error("War archive extraction failed!", e);
        } catch (ClassNotFoundException e) {
            log.error("Initiating servlet container failed!", e);
        } catch (XMLStreamException e) {
            log.error("Parsing web.xml failed!", e);
        } catch (WebXmlFormatException e) {
            log.error("Parsing web.xml failed!", e);
        }
        // ClassCastException and RuntimeException also should be catched
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
