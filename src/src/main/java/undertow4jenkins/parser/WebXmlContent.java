package undertow4jenkins.parser;

import java.util.ArrayList;
import java.util.List;

public class WebXmlContent {

    public String webAppVersion;

    public String displayName;

    public String description;

    public List<Servlet> servlets = new ArrayList<Servlet>(3);

    public List<ServletMapping> servletsMapping = new ArrayList<ServletMapping>(3);

    public List<Filter> filters = new ArrayList<Filter>();

    public List<FilterMapping> filterMappings = new ArrayList<FilterMapping>();

    public List<Listener> listeners = new ArrayList<Listener>(3);

    public List<String> securityRoles = new ArrayList<String>(7);

    public List<SecurityConstraint> securityConstraints = new ArrayList<SecurityConstraint>(3);

    public LoginConfig loginConfig;

    public List<EnvEntry> envEntries = new ArrayList<EnvEntry>(5);

    public List<MimeMapping> mimeMappings = new ArrayList<MimeMapping>();

    public List<ErrorPage> errorPages = new ArrayList<ErrorPage>(3);

    public static class Servlet {

        public String servletName;

        public String servletClass;

        public List<InitParam> initParams = new ArrayList<InitParam>();

        @Override
        public String toString() {
            return "Servlet [servletName=" + servletName + ", servletClass=" + servletClass
                    + ", initParams=" + initParams + "]";
        }
    }

    public static class InitParam {

        public String paramName;

        public String paramValue;

        @Override
        public String toString() {
            return "InitParam [paramName=" + paramName + ", paramValue=" + paramValue + "]";
        }
    }

    public static class ServletMapping {

        public String servletName;

        public String urlPattern;

        @Override
        public String toString() {
            return "ServletMapping [servletName=" + servletName + ", urlPattern=" + urlPattern
                    + "]";
        }
    }

    public static class Filter {

        public String filterName;

        public String filterClass;

        @Override
        public String toString() {
            return "Filter [filterName=" + filterName + ", filterClass=" + filterClass + "]";
        }
    }

    public static class FilterMapping {

        public String filterName;

        public String urlPattern;

        @Override
        public String toString() {
            return "FilterMapping [filterName=" + filterName + ", urlPattern=" + urlPattern + "]";
        }
    }

    public static class Listener {

        public String listenerClass;

        @Override
        public String toString() {
            return "Listener [listenerClass=" + listenerClass + "]";
        }
    }

    public static class SecurityConstraint {

        public WebResourceCollection webResourceCollection;

        public AuthConstraint authConstraint;

        @Override
        public String toString() {
            return "SecurityConstraint [webResourceCollection=" + webResourceCollection
                    + ", authConstraint=" + authConstraint + "]";
        }
    }

    public static class WebResourceCollection {

        public String webResourceName;

        public String urlPattern;

        @Override
        public String toString() {
            return "WebResourceCollection [webResourceName=" + webResourceName + ", urlPattern="
                    + urlPattern + "]";
        }
    }

    public static class AuthConstraint {

        public String roleName;

        @Override
        public String toString() {
            return "AuthConstraint [roleName=" + roleName + "]";
        }
    }

    public static class LoginConfig {

        public String authMethod;

        public String formLoginPage;

        public String formErrorPage;

        @Override
        public String toString() {
            return "LoginConfig [authMethod=" + authMethod + ", formLoginPage=" + formLoginPage
                    + ", formErrorPage=" + formErrorPage + "]";
        }
    }

    public static class EnvEntry {

        public String entryName;

        public String entryType;

        public String entryValue;

        @Override
        public String toString() {
            return "EnvEntry [entryName=" + entryName + ", entryType=" + entryType
                    + ", entryValue=" + entryValue + "]";
        }
    }

    public static class MimeMapping {

        public String extension;

        public String mimeType;

        @Override
        public String toString() {
            return "MimeMapping [extension=" + extension + ", mimeType=" + mimeType + "]";
        }
    }

    public static class ErrorPage {

        public String exceptionType;

        public String location;

        @Override
        public String toString() {
            return "ErrorPage [exceptionType=" + exceptionType + ", location=" + location + "]";
        }
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
