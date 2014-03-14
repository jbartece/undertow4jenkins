package undertow4jenkins;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import undertow4jenkins.option.OptionParser;

/**
 * @author Jakub Bartecek <jbartece@redhat.com>
 * 
 */
public class Launcher {

    private final Logger log = LoggerFactory.getLogger(getClass());
    
    @SuppressWarnings("unused")
    private Map<String, String> options;

    /**
     * Field for usage, which can be overridden outside this class
     */
    public static String USAGE;

    public Launcher(Map<String, String> options) {
        log.debug("constructor");
        this.options = options;
    }

    public void run() {
        log.debug("run");
        
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        Logger log = LoggerFactory.getLogger("Main");
        log.info("Undertow4Jenkins is starting...");

        OptionParser optionParser = new OptionParser();
        Map<String, String> options = optionParser.parse(args);

        Launcher launcher = new Launcher(options);
        launcher.run();
    }

}
