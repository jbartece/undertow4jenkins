package undertow4jenkins.parser;

import java.util.ArrayList;
import java.util.List;

public class WebXmlContent {

    public String webAppVersion;

    public String displayName;

    public String description;

    public List<Servlet> servlets = new ArrayList<Servlet>();

    public List<ServletMapping> servletsMapping = new ArrayList<ServletMapping>();

    public List<Filter> filters = new ArrayList<Filter>();

    public List<FilterMapping> filterMappings = new ArrayList<FilterMapping>();

    public List<Listener> listeners = new ArrayList<Listener>();

    public List<SecurityRole> securityRoles = new ArrayList<SecurityRole>();

    public List<SecurityConstraint> securityConstraints = new ArrayList<SecurityConstraint>();

    public LoginConfig loginConfig;

    public List<EnvEntry> envEntries = new ArrayList<EnvEntry>();

    public List<MimeMapping> mimeMappings = new ArrayList<MimeMapping>();

    public List<ErrorPage> errorPages = new ArrayList<ErrorPage>();

    public static class Servlet {

        public String servletName;

        public String servletClass;

        public List<InitParam> initParams = new ArrayList<InitParam>();
    }

    public static class InitParam {

        public String paramName;

        public String paramValue;
    }

    public static class ServletMapping {

        public String servletName;

        public String urlPattern;
    }

    public static class Filter {

        public String filterName;

        public String filterClass;
    }

    public static class FilterMapping {

        public String filterName;

        public String urlPattern;
    }

    public static class Listener {

        public String listenerClass;
    }

    public static class SecurityRole {

        public String roleName;
    }

    public static class SecurityConstraint {

        public WebResourceCollection webResourceCollection;

        public AuthConstraint authConstraint;
    }

    public static class WebResourceCollection {

        public String webResourceName;

        public String urlPattern;
    }

    public static class AuthConstraint {

        public String roleName;
    }

    public static class LoginConfig {

        public String authMethod;

        public String formLoginPage;

        public String formErrorPage;
    }

    public static class EnvEntry {

        public String entryName;

        public String entryType;

        public String entryValue;
    }

    public static class MimeMapping {

        public String extension;

        public String mimeType;
    }

    public static class ErrorPage {

        public String exceptionType;

        public String location;
    }

    @Override
    public String toString() {
        return "WebXmlContent [webAppVersion=" + webAppVersion + ", \ndisplayName=" + displayName
                + ", \ndescription=" + description + ", \nservlets=" + servlets
                + ", \nservletsMapping=" + servletsMapping + ", \nfilters=" + filters
                + ", \nfilterMappings=" + filterMappings + ", \nlisteners=" + listeners
                + ", \nsecurityRoles=" + securityRoles + ", \nsecurityConstraints="
                + securityConstraints + ", \nloginConfig=" + loginConfig + ", \nenvEntries="
                + envEntries + ", \nmimeMappings=" + mimeMappings + ", \nerrorPages=" + errorPages
                + "]";
    }

}
