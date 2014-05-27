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
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import undertow4jenkins.CustomException;

/**
 * Process parsing of command line options.
 * Based on Apache Commons CLI
 * 
 * @author Jakub Bartecek <jbartece@redhat.com>
 * 
 */
public class OptionParser {

    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Options, which cannot be parsed with automatic
     * loading of options based on Reflection API
     */
    private static final String[] customParsingOptions = { "argumentsRealmPasswd",
            "argumentsRealmRoles" };

    /** Definition of options */
    private org.apache.commons.cli.Options optionsDefinition;

    /** Properties from config file */
    private Properties configFileProperties;

    /** Pattern to replace "_" with dot */
    private Pattern dotReplacePattern;

    /**
     * Initializes parser
     */
    public OptionParser() {
        this.dotReplacePattern = Pattern.compile("_");
        optionsDefinition = createOptionsDefinition();
    }

    /**
     * Parse all command line arguments to internal options
     * 
     * @param args Command line arguments
     * @return Loaded options from command line aruments
     */
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

            return loadParsedOptions(parsedOptions, options);
        } catch (Exception e) {
            log.error("Command line options could not be parsed!\nReason: " + e.toString());
            return null;
        }

    }

    /**
     * Process options, which has to be treated manually (those in field customParsingOptions)
     * 
     * @param args Command line arguments
     * @param options Initialized options object
     * @return Command line arguments without options from field customParsingOptions
     * @throws ParseException Thrown if arguments could not be parsed
     */
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

    /**
     * Loads options from properties
     * @param configFile File with options
     * @param options Options object
     * @throws Exception Thrown if arguments from config file could not be parsed
     */
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

    /**
     * Parse usernames and roles for arguments realm
     * @param options Options object
     * @param arg Command line argument
     * @throws Exception Thrown if arguments some error during parsing occurs
     */
    private void parseArgumentsRealmRoles(Options options, String arg) throws Exception {
        String[] data = arg.substring(23).split("=");
        if (data.length != 2)
            throw new Exception();

        List<String> rolesList = new ArrayList<String>(3);
        for (String role : data[1].split(","))
            rolesList.add(role.trim());

        options.argumentsRealmRoles.put(data[0], rolesList.toArray(new String[0]));
    }

    /**
     * 
     * Parse usernames and passwords for arguments realm
     * @param options Options object
     * @param arg Command line argument
     * @throws Exception Thrown if arguments some error during parsing occurs
     */
    private void parseArgumentsRealmPasswd(Options options, String arg) throws Exception {
        String[] data = arg.substring(24).split("=");
        if (data.length != 2)
            throw new Exception();

        options.argumentsRealmPasswd.put(data[0], data[1]);
    }

    /**
     * Prepares options for arguments parsing
     * @return Prepared options
     */
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

    /**
     * Skip option if it should be treated in custom way
     * @param fieldName Name of field
     * @return True if option should be skipped, otherwise false
     */
    private boolean skipOption(String fieldName) {
        if (customParsingOptions[0].equals(fieldName) || customParsingOptions[1].equals(fieldName))
            return true;
        else
            return false;
    }

    /**
     * Replace "_" with . in field name
     * 
     * @param name Field name
     * @return Edited field name
     */
    private String fieldNameToOptionName(String name) {
        if (name.indexOf("_") != -1) {
            return dotReplacePattern.matcher(name).replaceFirst(".");
        }
        else
            return name;
    }

    /**
     * Load options from parsed command line arguments
     * @param parsedOptions Result of parsing
     * @param options Options object
     * @return Loaded options from command line arguments
     * @throws Exception Thrown if arguments some error during loading results occurs
     */
    private Options loadParsedOptions(CommandLine parsedOptions, Options options)
            throws CustomException {
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

    /**
     * Save option value to field
     * @param parsedOptions Result of parsing
     * @param options Options object
     * @param field Target field
     * @param optionName Name of option
     * @param fieldClass Type of field
     * @throws Exception Thrown if arguments some error during loading results occurs
     */
    private void saveOptionValue(CommandLine parsedOptions, Options options, Field field,
            String optionName, Class<?> fieldClass) throws CustomException {
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
        } catch (Exception e) {
            throw new CustomException("Wrong value of option" + optionName);
        }
    }
}
