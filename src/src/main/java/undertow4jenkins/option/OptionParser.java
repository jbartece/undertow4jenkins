package undertow4jenkins.option;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OptionParser {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final String[] customParsingOptions = { "argumentsRealmPasswd",
            "argumentsRealmRoles" };

    private org.apache.commons.cli.Options optionsDefinition;

    public OptionParser() {
        optionsDefinition = createOptionsDefinition();
    }

    public Options parse(String[] args) {
        Options options = new Options();

        CommandLineParser parser = new GnuParser();
        CommandLine parsedOptions;
        try {
            args = removeRealmOptions(args, options);
            parsedOptions = parser.parse(optionsDefinition, args);

        } catch (ParseException e) {
            log.error("Command line options could not be parsed!\nReason: " + e.getMessage());
            return null;
        }

        return loadParsedOptions(parsedOptions, options);
    }

    private String[] removeRealmOptions(String[] args, Options options) throws ParseException {
        List<String> editedArgs = new ArrayList<String>();

        try {
            for (String arg : args) {
                if (arg.startsWith("--argumentsRealm.passwd.")) {
                    String[] data = arg.substring(24).split("=");
                    if(data.length != 2)
                        throw new Exception();
                    
                    options.argumentsRealmPasswd.put(data[0], data[1]);
                    continue;
                }

                if (arg.startsWith("--argumentsRealm.roles.")) {
                    String[] data = arg.substring(23).split("=");
                    if(data.length != 2)
                        throw new Exception();
                    
                    options.argumentsRealmRoles.put(data[0], data[1].split(","));
                    continue;
                }

                editedArgs.add(arg);
            }
        } catch (Throwable e) {
            throw new ParseException("Not properly specified argumentsRealm options!");
        }

        return editedArgs.toArray(new String[0]);
    }

    private org.apache.commons.cli.Options createOptionsDefinition() {
        org.apache.commons.cli.Options options = new org.apache.commons.cli.Options();

        for (Field field : Options.class.getFields()) {
            if (skipOption(field.getName()))
                continue;

            String optionName = fieldNameToOptionName(field.getName());
            if (field.getType().equals(Boolean.class)) {
                // Boolean arguments does not expect additional value
                options.addOption(new Option(null, optionName, false, ""));
            }
            else {
                // Other types expects arguments with value (Integer/String)
                options.addOption(new Option(null, optionName, true, ""));
            }
        }

        return options;
    }

    private boolean skipOption(String fieldName) {
        if (customParsingOptions[0].equals(fieldName) || customParsingOptions[1].equals(fieldName))
            return true;
        else
            return false;
    }

    private String fieldNameToOptionName(String name) {
        return name.replaceFirst("_", "."); // TODO optimize
    }

    private Options loadParsedOptions(CommandLine parsedOptions, Options options) {
        for (Field field : Options.class.getFields()) {
            if (skipOption(field.getName()))
                continue;

            Class<?> fieldClass = field.getType();
            String optionNameOfField = fieldNameToOptionName(field.getName());

            if (parsedOptions.hasOption(optionNameOfField)) {
                saveOptionValue(parsedOptions, options, field, optionNameOfField, fieldClass);
            }
        }

        return options;
    }

    private void saveOptionValue(CommandLine parsedOptions, Options options, Field field,
            String optionName, Class<?> fieldClass) {
        try {
            if (fieldClass.equals(String.class)) {
                field.set(options, parsedOptions.getOptionValue(optionName));
            }
            else {
                if (fieldClass.equals(Integer.class)) {
                    Integer value = Integer.parseInt(parsedOptions.getOptionValue(optionName));
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
