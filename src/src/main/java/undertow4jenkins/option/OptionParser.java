package undertow4jenkins.option;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OptionParser {

    private final Logger log = LoggerFactory.getLogger(getClass());

    public final String oHttp = "httpPort";

    public final String oWarFile = "warfile";

    public final String oWebRoot = "webroot";

    private org.apache.commons.cli.Options optionsDefinition;

    public OptionParser() {
        optionsDefinition = createOptionsDefinition();
    }

    private org.apache.commons.cli.Options createOptionsDefinition() {
        org.apache.commons.cli.Options options = new org.apache.commons.cli.Options();
        
        
        options.addOption(new Option("warfile", true, "description warfile"));
        options.addOption(new Option("webroot", true, "description webroot"));
        
        return options;
    }

    public Options parse(String[] args) {
        CommandLineParser parser = new GnuParser();
        CommandLine parsedOptions;
        try {
            parsedOptions = parser.parse(optionsDefinition, args);

        } catch (ParseException e) {
            log.error("Command line options could not be parsed!");
            return null;
        }

        return loadParsedOptions(parsedOptions);
    }

    private Options loadParsedOptions(CommandLine parsedOptions) {
        Options options = new Options();

        if (parsedOptions.hasOption("warfile")) {
            log.info("warfile: " + parsedOptions.getOptionValue("warfile"));
            options.warfile = parsedOptions.getOptionValue("warfile");
        }
        else {
            log.info("warfile not specified");
        }

        if (parsedOptions.hasOption("webroot")) {
            log.info("webroot: " + parsedOptions.getOptionValue("webroot"));
            options.warfile = parsedOptions.getOptionValue("webroot");
        }
        else {
            log.info("webroot not specified");
        }

        return options;
    }

}
