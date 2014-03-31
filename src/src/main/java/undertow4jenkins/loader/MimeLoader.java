package undertow4jenkins.loader;

import io.undertow.servlet.api.MimeMapping;

import java.util.ArrayList;
import java.util.List;

public class MimeLoader {

    // <mime-mapping>
    // <extension>xml</extension>
    // <mime-type>application/xml</mime-type>
    // </mime-mapping>
    private static String[][] mappingConfig = { { "xml", "application/xml" },
            { "log", "text/plain" }, { "war", "application/octet-stream" },
            { "ear", "application/octet-stream" }, { "rar", "application/octet-stream" },
            { "webm", "video/webm" } };

    public static List<MimeMapping> createMimeMappings(ClassLoader jenkinsWarClassLoader) {
        List<MimeMapping> mappingList = new ArrayList<MimeMapping>(); 
        
        for(String[] arr : mappingConfig)  {
            mappingList.add(new MimeMapping(arr[0], arr[1]));
        }
        
        return mappingList;
    }

}
