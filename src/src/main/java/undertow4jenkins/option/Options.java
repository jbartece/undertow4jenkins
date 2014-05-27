package undertow4jenkins.option;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import undertow4jenkins.util.Configuration;

/**
 * Stores values of all available options of Undertow4Jenkins.
 * 
 * Names of options are related to those specified in Jenkins CI help 
 *  
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */
public class Options {

    //
    public String prefix;
    public String webroot;
    public String warfile;

    //
    public Integer httpPort;
    public String httpListenAddress;
    public Integer httpKeepAliveTimeout;

    //
    public Integer httpsPort;
    public String httpsListenAddress;
    public Integer httpsKeepAliveTimeout;
    public String httpsKeyStore;
    public String httpsKeyStorePassword;
    public String httpsCertificate;
    public String httpsPrivateKey;
    public String httpsKeyManagerType;

    //
    public Integer ajp13Port;
    public String ajp13ListenAddress;
    public Integer controlPort;
    public String commonLibFolder;

    //
    public Integer handlerCountMax;
    public Integer sessionTimeout;
    public String mimeTypes; 
    public Integer maxParamCount;
    
    //
    public Boolean usage;
    public Boolean help;
    public Boolean version;
    
    //
    public String realmClassName;
    public Map<String, String> argumentsRealmPasswd = new HashMap<String, String>();     
    public Map<String, String[]> argumentsRealmRoles = new HashMap<String, String[]>();      
    public String fileRealm_configFile;       
    
    //
    public String accessLoggerClassName;
    public String simpleAccessLogger_format; 
    public String simpleAccessLogger_file; 
    
    //Not supported options - only for compatibility with old versions of Winstone   
    public Boolean spdy;
    public String webappsDir;
    public String hostsDir;
    public Integer handlerCountStartup;
    public Integer handlerCountMaxIdle;
    public Boolean httpDoHostnameLookups;
    public Boolean httpsDoHostnameLookups;
    public Boolean logThrowingLineNo;
    public Boolean logThrowingThread;
    public Integer debug;
    

    /**
     * @return Set of unsupported options
     */
    public static Set<String> getUnsupportedOptions() {
        Set<String> set =  new HashSet<String>();
        set.add("webappsDir");
        set.add("hostsDir");
        set.add("handlerCountStartup");
        set.add("handlerCountMaxIdle");
        set.add("httpDoHostnameLookups");
        set.add("logThrowingLineNo");
        set.add("logThrowingThread");
        set.add("debug");
        set.add("spdy");
        return set;
    }
    
    /**
     * Creates object and initialize options with default values.
     */
    public Options() {
        loadOptionsDefaultValues();
    }

    /**
     * Loads all default values of options
     */
    private void loadOptionsDefaultValues() {
        String propertyPrefix = "Options.defaultValue.";
        
        for(Field f : getClass().getFields()) {
            String propertyName = propertyPrefix + f.getName();
            if(Configuration.isPropertySet(propertyName)) {
                setFieldDefaultValue(f, propertyName);
            }
        }
    }

    /**
     * Sets default value to specified field
     * @param f Target field
     * @param propertyName Name of property with default value
     */
    private void setFieldDefaultValue(Field f, String propertyName) {
        Logger log = LoggerFactory.getLogger(getClass());
        Class<?> fieldClass = f.getType();
        
        try {
            if (fieldClass.equals(String.class)) {
                f.set(this, Configuration.getProperty(propertyName));
            }
            else {
                if(fieldClass.equals(Integer.class)) {
                    f.set(this, Configuration.getIntProperty(propertyName));
                }
                else {
                    if(fieldClass.equals(Boolean.class)) {
                        f.set(this, Configuration.getBoolProperty(propertyName));
                    }
                    else {
                        log.warn("Class undertow4jenkins.option.Options has option with type, "
                                + "which is not supported!");
                    }
                }
            }
        } catch (Exception e) {
            log.error("Loading of default options failed!");
        }
    }
    
    @Override
    public String toString() {
        return "Options ["
                + (webroot != null ? "webroot=" + webroot + ", " : "")
                + (warfile != null ? "warfile=" + warfile + ", " : "")
                + (webappsDir != null ? "webappsDir=" + webappsDir + ", " : "")
                + (hostsDir != null ? "hostsDir=" + hostsDir + ", " : "")
                + (prefix != null ? "prefix=" + prefix + ", " : "")
                + (commonLibFolder != null ? "commonLibFolder=" + commonLibFolder + ", " : "")
                + (logThrowingLineNo != null ? "logThrowingLineNo=" + logThrowingLineNo + ", " : "")
                + (logThrowingThread != null ? "logThrowingThread=" + logThrowingThread + ", " : "")
                + (debug != null ? "debug=" + debug + ", " : "")
                + (httpPort != null ? "httpPort=" + httpPort + ", " : "")
                + (httpListenAddress != null ? "httpListenAdress=" + httpListenAddress + ", " : "")
                + (httpDoHostnameLookups != null ? "httpDoHostnameLookups=" + httpDoHostnameLookups
                        + ", " : "")
                + (httpKeepAliveTimeout != null ? "httpKeepAliveTimeout=" + httpKeepAliveTimeout
                        + ", " : "")
                + (httpsPort != null ? "httpsPort=" + httpsPort + ", " : "")
                + (httpsListenAddress != null ? "httpsListenAdress=" + httpsListenAddress + ", " : "")
                + (httpsKeepAliveTimeout != null ? "httpsKeepAliveTimeout=" + httpsKeepAliveTimeout
                        + ", " : "")
                + (httpsKeyStore != null ? "httpsKeyStore=" + httpsKeyStore + ", " : "")
                + (httpsKeyStorePassword != null ? "httpsKeyStorePassword=" + httpsKeyStorePassword
                        + ", " : "")
                + (httpsCertificate != null ? "httpsCertificate=" + httpsCertificate + ", " : "")
                + (httpsPrivateKey != null ? "httpsPrivateKey=" + httpsPrivateKey + ", " : "")
                + (httpsKeyManagerType != null ? "httpsKeyManagerType=" + httpsKeyManagerType
                        + ", " : "")
                + (spdy != null ? "spdy=" + spdy + ", " : "")
                + (ajp13Port != null ? "ajp13Port=" + ajp13Port + ", " : "")
                + (ajp13ListenAddress != null ? "ajp13ListenAdress=" + ajp13ListenAddress + ", " : "")
                + (controlPort != null ? "controlPort=" + controlPort + ", " : "")
                + (handlerCountStartup != null ? "handlerCountStartup=" + handlerCountStartup
                        + ", " : "")
                + (handlerCountMax != null ? "handlerCountMax=" + handlerCountMax + ", " : "")
                + (handlerCountMaxIdle != null ? "handlerCountMaxIdle=" + handlerCountMaxIdle
                        + ", " : "")
                + (sessionTimeout != null ? "sessionTimeout=" + sessionTimeout + ", " : "")
                + (mimeTypes != null ? "mimeTypes=" + mimeTypes + ", " : "")
                + (maxParamCount != null ? "maxParamCount=" + maxParamCount + ", " : "")
                + (usage != null ? "usage=" + usage + ", " : "")
                + (help != null ? "help=" + help + ", " : "")
                + (version != null ? "version=" + version + ", " : "")
                + (realmClassName != null ? "realmClassName=" + realmClassName + ", " : "")
                + (argumentsRealmPasswd != null ? "argumentsRealmPasswd=" + argumentsRealmPasswd
                        + ", " : "")
                + (argumentsRealmRoles != null ? "argumentsRealmRoles=" + argumentsRealmRoles
                        + ", " : "")
                + (fileRealm_configFile != null ? "fileRealm_configFile=" + fileRealm_configFile
                        + ", " : "")
                + (accessLoggerClassName != null ? "accessLoggerClassName=" + accessLoggerClassName
                        + ", " : "")
                + (simpleAccessLogger_format != null ? "simpleAccessLogger_format="
                        + simpleAccessLogger_format + ", " : "")
                + (simpleAccessLogger_file != null ? "simpleAccessLogger_file="
                        + simpleAccessLogger_file : "") + "]";
    }

    

    
}
