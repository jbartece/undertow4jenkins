package undertow4jenkins.loader;

import io.undertow.servlet.api.MimeMapping;

import java.util.ArrayList;
import java.util.List;

public class MimeLoader {

    public static List<MimeMapping> createMimeMappings(
            List<undertow4jenkins.parser.WebXmlContent.MimeMapping> mappingsDataCol) {
        List<MimeMapping> mappingList = new ArrayList<MimeMapping>();

        for (undertow4jenkins.parser.WebXmlContent.MimeMapping mappingData : mappingsDataCol) {
            mappingList.add(new MimeMapping(mappingData.extension, mappingData.mimeType));
        }

        return mappingList;
    }

}
