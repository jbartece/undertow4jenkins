package undertow4jenkins.option;

import java.lang.reflect.Field;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OptionParser {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private org.apache.commons.cli.Options optionsDefinition;

    public OptionParser() {
        optionsDefinition = createOptionsDefinition();
    }

    public Options parse(String[] args) {
        CommandLineParser parser = new GnuParser();
        CommandLine parsedOptions;
        try {
            parsedOptions = parser.parse(optionsDefinition, args);

        } catch (ParseException e) {
            log.error("Command line options could not be parsed!\n Reason: " , e.getMessage());
            return null;
        }

        return loadParsedOptions(parsedOptions);
    }

    private org.apache.commons.cli.Options createOptionsDefinition() {
        org.apache.commons.cli.Options options = new org.apache.commons.cli.Options();

        for (Field field : Options.class.getFields()) {
            if (field.getType().equals(Boolean.class)) {
                // Boolean arguments does not expect additional value
                options.addOption(new Option(field.getName(), false, ""));
            }
            else {
                //Other types expects arguments with value (Integer/String)
                options.addOption(new Option(field.getName(), true, ""));
            }
        }

        return options;
    }

    private Options loadParsedOptions(CommandLine parsedOptions) {
        Options options = new Options();

        for (Field field : Options.class.getFields()) {
            Class<?> fieldClass = field.getType();
            if (parsedOptions.hasOption(field.getName())) {
                saveOptionValue(parsedOptions, options, field, fieldClass);
            }
        }

        return options;
    }

    private void saveOptionValue(CommandLine parsedOptions, Options options, Field field,
            Class<?> fieldClass) {
        try {
            if (fieldClass.equals(String.class)) {
                field.set(options, parsedOptions.getOptionValue(field.getName()));
            }
            else {
                if (fieldClass.equals(Integer.class)) {
                    Integer value = Integer.parseInt(parsedOptions.getOptionValue(field
                            .getName()));
                    field.set(options, value);
                }
                else {
                    if (fieldClass.equals(Boolean.class)) {
                        field.set(options, Boolean.TRUE);
                    }
                    else {
                        log.warn("Class undertow4jenkins.option.Options has option with type, "
                                + "which is not supported!");
                    }
                }
            }

            // TODO set properly error messages
        } catch (NumberFormatException e) {
            log.error(e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            log.error(e.getMessage(), e);
        }
    }
}
