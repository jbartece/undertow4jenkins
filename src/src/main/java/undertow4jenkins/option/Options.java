package undertow4jenkins.option;

public class Options {

    public String webroot;
    public String warfile;

    //
    public String webappsDir;
    public String hostsDir;

    //
    public Integer httpPort;
    public Integer httpListenAdress;
    public Boolean httpDoHostnameLookups;
    public Integer httpKeepAliceTimeout;

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
    public Integer ajp13ListenAdress;
    public Boolean controlPort;

    //
    public Integer handlerCountStartup;
    public Integer handlerCountMax;
    public Integer handlerCountMaxIdle;
    public Integer sessionTimeout;
    public String mimeTypes;

    //
    public String maxParamCount;
    public Boolean usage;
    public Boolean help;
    public Boolean version;
    
    //Currently ignored (not properly defined)
    public String realmClassName;
    public String argumentsRealmPasswd;     //TODO - temporary implementation (has to be checked later)
    public String argumentsRealmRoles;      //TODO - temporary implementation (has to be checked later)
    public String fileRealConfigFile;       //TODO - temporary implementation (has to be checked later)
    
    //Currently ignored (not properly defined)
    public String accessLoggerClassName;
    public String simpleAccessLoggerFormat;
    public String simpleAccessLoggerFile;
    
    
    /**
     * Prints values of all non null options
     */
    @Override
    public String toString() {
        return "Options ["
                + (webroot != null ? "webroot=" + webroot + ", " : "")
                + (warfile != null ? "warfile=" + warfile + ", " : "")
                + (webappsDir != null ? "webappsDir=" + webappsDir + ", " : "")
                + (hostsDir != null ? "hostsDir=" + hostsDir + ", " : "")
                + (httpPort != null ? "httpPort=" + httpPort + ", " : "")
                + (httpListenAdress != null ? "httpListenAdress=" + httpListenAdress + ", " : "")
                + (httpDoHostnameLookups != null ? "httpDoHostnameLookups=" + httpDoHostnameLookups
                        + ", " : "")
                + (httpKeepAliceTimeout != null ? "httpKeepAliceTimeout=" + httpKeepAliceTimeout
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
                + (fileRealConfigFile != null ? "fileRealConfigFile=" + fileRealConfigFile + ", "
                        : "")
                + (accessLoggerClassName != null ? "accessLoggerClassName=" + accessLoggerClassName
                        + ", " : "")
                + (simpleAccessLoggerFormat != null ? "simpleAccessLoggerFormat="
                        + simpleAccessLoggerFormat + ", " : "")
                + (simpleAccessLoggerFile != null ? "simpleAccessLoggerFile="
                        + simpleAccessLoggerFile : "") + "]";
    }
    
}
