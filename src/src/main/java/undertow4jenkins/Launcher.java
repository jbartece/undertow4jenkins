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
import undertow4jenkins.util.Configuration;
import undertow4jenkins.util.WarWorker;
import undertow4jenkins.parser.WebXmlContent.MimeMapping;

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
        log.debug(options.toString());
    }

    public void run() {

        if (checkHelpParams())
            return;

        try {
            WarWorker.extractFilesFromWar(options.warfile, pathToTmpDir);
            // Create class loader to load classed from jenkins.war archive.
            // It is needed to load servlet classes such as Stapler.
            this.jenkinsWarClassLoader = WarWorker.createJarsClassloader(options.warfile,
                    pathToTmpDir);

            WebXmlParser parser = new WebXmlParser();
            WebXmlContent webXmlContent = parser.parse(pathToTmpDir + "WEB-INF/web.xml");

            editXmlContentWithOptions(webXmlContent);
            
            // if (log.isDebugEnabled())
            // log.debug("Loaded content of web.xml:\n" + webXmlContent.toString());

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


    private void editXmlContentWithOptions(WebXmlContent webXmlContent) {
        if(options.mimeTypes != null) {
            String[] mimePairs = options.mimeTypes.split(":");
            for(String singleMimeStr : mimePairs) {
                String[] singleMime = singleMimeStr.split("=");
                if(singleMime.length == 2) {
                    MimeMapping newMime = new MimeMapping();
                    newMime.extension = singleMime[0];
                    newMime.mimeType = singleMime[1];
                    webXmlContent.mimeMappings.add(newMime);
                }
                else 
                    log.warn("Wrong additional mime definition. Caused by: " + singleMimeStr );
            }
        }
        
    }

    /**
     * Checks one of help/usage/version parameters was set. If so, print proper information.
     * @return True if help/usage/version was specified, otherwise false
     */
    private boolean checkHelpParams() {
        if ( (options.help != null && options.help )|| (options.usage != null && options.usage)) {
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

        Launcher launcher = new Launcher(options);
        launcher.run();
    }
}
