package undertow4jenkins.option;

import java.util.HashMap;
import java.util.Map;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OptionParser {
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    public final String oHttp= "httpPort";
    public final String oWarFile = "warfile";
    public final String oWebRoot = "webroot";

    public Map<String, String> parse(String[] args) {
        Map<String, String> options = new HashMap<String, String>();
        
        for(String arg: args) {
            if(arg.startsWith("--")) {
//                switch(arg) {
//                    case "oHttp":
//                        break;
//                }  
            } 
            log.info(arg);
        }
        return options;
    }

}
