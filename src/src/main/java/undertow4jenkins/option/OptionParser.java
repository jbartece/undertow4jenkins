package undertow4jenkins.option;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
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

    private Properties configFileProperties;

    public OptionParser() {
        optionsDefinition = createOptionsDefinition();
    }

    public Options parse(String[] args) {
        Options options = new Options();

        GnuParser parser = new GnuParser();
        CommandLine parsedOptions;
        try {
            args = preprocessOptions(args, options);

            if (configFileProperties == null)
                parsedOptions = parser.parse(optionsDefinition, args);
            else
                parsedOptions = parser.parse(optionsDefinition, args, configFileProperties);

        } catch (ParseException e) {
            log.error("Command line options could not be parsed!\nReason: " + e.toString());
            return null;
        }

        return loadParsedOptions(parsedOptions, options);
    }

    private String[] preprocessOptions(String[] args, Options options) throws ParseException {
        List<String> editedArgs = new ArrayList<String>();

        try {
            for (String arg : args) {
                if (arg.startsWith("--config=")) {
                    String configFile = arg.substring(9);
                    createPropertiesLoadCustomOptions(configFile, options);
                    continue;
                }

                if (arg.startsWith("--argumentsRealm.passwd.")) {
                    parseArgumentsRealmPasswd(options, arg);
                    continue;
                }

                if (arg.startsWith("--argumentsRealm.roles.")) {
                    parseArgumentsRealmRoles(options, arg);
                    continue;
                }

                editedArgs.add(arg);
            }

            if (configFileProperties == null) {
                // Option --config was not set - try default./winstone.properties or undertow4jenkins.properties
                File winstoneF = new File("winstone.properties");
                File undertowF = new File("undertow4jenkins.properties");

                if (undertowF.exists()) {
                    createPropertiesLoadCustomOptions(undertowF.getName(), options);
                }
                else {
                    if (winstoneF.exists()) {
                        createPropertiesLoadCustomOptions(winstoneF.getName(), options);
                    }
                }
            }
            
        } catch (IOException e) {
            throw new ParseException("Not properly set config file!");
        } catch (Throwable e) {
            throw new ParseException("Not properly specified argumentsRealm options!");
        }

        return editedArgs.toArray(new String[0]);
    }

    private void createPropertiesLoadCustomOptions(String configFile, Options options)
            throws Exception {
        InputStream configStream = new FileInputStream(configFile);
        this.configFileProperties = new Properties();
        configFileProperties.load(configStream);

        List<String> propertiesToRemove = new ArrayList<String>(3);
        for (Entry<Object, Object> entry : configFileProperties.entrySet()) {
            String optionName = entry.getKey().toString();
            if (optionName.startsWith("argumentsRealm.passwd.")) {
                parseArgumentsRealmPasswd(options, "--" + optionName + "="
                        + entry.getValue().toString());
                propertiesToRemove.add(optionName);
                continue;
            }

            if (optionName.startsWith("argumentsRealm.roles.")) {
                parseArgumentsRealmRoles(options, "--" + optionName + "="
                        + entry.getValue().toString());
                propertiesToRemove.add(optionName);
                continue;
            }
        }

        for (String opt : propertiesToRemove)
            configFileProperties.remove(opt);
    }

    private void parseArgumentsRealmRoles(Options options, String arg) throws Exception {
        String[] data = arg.substring(23).split("=");
        if (data.length != 2)
            throw new Exception();

        List<String> rolesList = new ArrayList<String>(3);
        for(String role : data[1].split(",")) 
            rolesList.add(role.trim());
        
        options.argumentsRealmRoles.put(data[0], rolesList.toArray(new String[0]));
    }

    private void parseArgumentsRealmPasswd(Options options, String arg) throws Exception {
        String[] data = arg.substring(24).split("=");
        if (data.length != 2)
            throw new Exception();

        options.argumentsRealmPasswd.put(data[0], data[1]);
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
        Set<String> unsupportedOptions = Options.getUnsupportedOptions();

        for (Field field : Options.class.getFields()) {
            if (skipOption(field.getName()))
                continue;

            Class<?> fieldClass = field.getType();
            String optionNameOfField = fieldNameToOptionName(field.getName());

            if (parsedOptions.hasOption(optionNameOfField)) {
                if (unsupportedOptions.contains(field.getName())) {
                    log.warn("Option " + field.getName()
                            + " is not supported in this version of container. "
                            + "Its usage is not essential for this container.");
                }
                else {
                    saveOptionValue(parsedOptions, options, field, optionNameOfField, fieldClass);
                }
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
