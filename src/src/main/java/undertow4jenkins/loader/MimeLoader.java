package undertow4jenkins.loader;

import io.undertow.servlet.api.MimeMapping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import undertow4jenkins.util.Configuration;

/**
 * Utility class to create MIME mappings from web.xml data
 * 
 * @author Jakub Bartecek <jbartece@redhat.com>
 * 
 */
public class MimeLoader {

    private static final Logger log = LoggerFactory.getLogger("undertow4jenkins.loader.MimeLoader");

    /** Path to file with built in MIME mappings */
    private static final String DEFAULT_MIMES_FILENAME = "undertow4jenkins/util/mimeMappings.properties";

    /**
     * Create mime mappings from web.xml data
     * 
     * @param mappingsDataCol Data from web.xml
     * @param optionMimes Mime mappings from command line arguments
     * @return Lost of created MIME mappings
     */
    public static List<MimeMapping> createMimeMappings(
            List<undertow4jenkins.parser.WebXmlContent.MimeMapping> mappingsDataCol,
            String optionMimes) {
        List<MimeMapping> mappingList = new ArrayList<MimeMapping>();

        loadMimesFromOption(optionMimes, mappingList);

        for (undertow4jenkins.parser.WebXmlContent.MimeMapping mappingData : mappingsDataCol) {
            mappingList.add(new MimeMapping(mappingData.extension, mappingData.mimeType));
        }

        loadBuiltinMimes(mappingList);

        return mappingList;
    }

    /**
     * Loads mime mappings from properties file
     * 
     * @param mappingList In-out list of MIME mappings
     */
    private static void loadBuiltinMimes(List<MimeMapping> mappingList) {
        try {
            Properties properties = Configuration.loadPropertiesFromFile(DEFAULT_MIMES_FILENAME);

            for (Entry<Object, Object> entry : properties.entrySet()) {
                mappingList.add(
                        new MimeMapping(entry.getKey().toString(), entry.getValue().toString()));
            }

        } catch (IOException e) {
            log.warn("Loading of built-in mime mappings failed! Reason:" + e.getMessage());
        }
    }

    /**
     * Loads MIME mappings from command line argument
     * 
     * @param optionMimes String of passed argument
     * @param mappingList In-out list of MIME mappings
     */
    private static void loadMimesFromOption(String optionMimes, List<MimeMapping> mappingList) {
        if (optionMimes != null) {
            String[] mimePairs = optionMimes.split(":");

            for (String singleMimeStr : mimePairs) {
                String[] singleMime = singleMimeStr.split("=");
                if (singleMime.length == 2) {
                    mappingList.add(new MimeMapping(singleMime[0], singleMime[1]));
                }
                else
                    log.warn("Wrong additional mime definition. Caused by: " + singleMimeStr);
            }
        }
    }

}
