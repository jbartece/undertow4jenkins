package undertow4jenkins.option;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import undertow4jenkins.util.Configuration;

public class Options {
    
    public String webroot;
    public String warfile;

    //
    public String webappsDir;
    public String hostsDir;
    public String prefix;
    
    //TODO
    public String config;
    public String commonLibFolder;
    public Boolean logThrowingLineNo;
    public Boolean logThrowingThread;
    public Integer debug;

    //
    public Integer httpPort;
    public String httpListenAdress;
    public Boolean httpDoHostnameLookups;
    public Integer httpKeepAliveTimeout;

    //
    public Integer httpsPort;
    public String httpsListenAdress;
    public Integer httpsKeepAliveTimeout;
    public String httpsKeyStore;
    public String httpsKeyStorePassword;
    public String httpsCertificate;
    public String httpsPrivateKey;
    public String httpsKeyManagerType;

    //
    public Boolean spdy;
    public Integer ajp13Port;
    public String ajp13ListenAdress;
    public Integer controlPort;

    //
    public Integer handlerCountStartup;
    public Integer handlerCountMax;
    public Integer handlerCountMaxIdle;
    
    //
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
    
    public Options() {
        loadOptionsDefaultValues();
    }

    private void loadOptionsDefaultValues() {
        String propertyPrefix = "Options.defaultValue.";
        
        for(Field f : getClass().getFields()) {
            String propertyName = propertyPrefix + f.getName();
            if(Configuration.isPropertySet(propertyName)) {
                setFieldDefaultValue(f, propertyName);
            }
        }
    }

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

            // TODO set properly error messages
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            log.error(e.getMessage(), e);
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
                + (config != null ? "config=" + config + ", " : "")
                + (commonLibFolder != null ? "commonLibFolder=" + commonLibFolder + ", " : "")
                + (logThrowingLineNo != null ? "logThrowingLineNo=" + logThrowingLineNo + ", " : "")
                + (logThrowingThread != null ? "logThrowingThread=" + logThrowingThread + ", " : "")
                + (debug != null ? "debug=" + debug + ", " : "")
                + (httpPort != null ? "httpPort=" + httpPort + ", " : "")
                + (httpListenAdress != null ? "httpListenAdress=" + httpListenAdress + ", " : "")
                + (httpDoHostnameLookups != null ? "httpDoHostnameLookups=" + httpDoHostnameLookups
                        + ", " : "")
                + (httpKeepAliveTimeout != null ? "httpKeepAliveTimeout=" + httpKeepAliveTimeout
                        + ", " : "")
                + (httpsPort != null ? "httpsPort=" + httpsPort + ", " : "")
                + (httpsListenAdress != null ? "httpsListenAdress=" + httpsListenAdress + ", " : "")
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
                + (ajp13ListenAdress != null ? "ajp13ListenAdress=" + ajp13ListenAdress + ", " : "")
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
