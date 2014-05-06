package undertow4jenkins.parser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import undertow4jenkins.parser.WebXmlContent.EnvEntry;
import undertow4jenkins.parser.WebXmlContent.ErrorPage;
import undertow4jenkins.parser.WebXmlContent.Filter;
import undertow4jenkins.parser.WebXmlContent.FilterMapping;
import undertow4jenkins.parser.WebXmlContent.InitParam;
import undertow4jenkins.parser.WebXmlContent.Listener;
import undertow4jenkins.parser.WebXmlContent.LoginConfig;
import undertow4jenkins.parser.WebXmlContent.MimeMapping;
import undertow4jenkins.parser.WebXmlContent.SecurityConstraint;
import undertow4jenkins.parser.WebXmlContent.Servlet;
import undertow4jenkins.parser.WebXmlContent.ServletMapping;
import undertow4jenkins.parser.WebXmlContent.WebResourceCollection;

public class WebXmlParser {

    public WebXmlContent parse(String pathToFile) throws FileNotFoundException, XMLStreamException,
            WebXmlFormatException {
        WebXmlContent result = new WebXmlContent();

        XMLInputFactory xmlInFactory = XMLInputFactory.newFactory();
        XMLStreamReader xmlReader = xmlInFactory.createXMLStreamReader(
                new FileInputStream(pathToFile));

        for (; xmlReader.hasNext(); xmlReader.next()) {
            if (!xmlReader.isStartElement())
                continue;

            String tagName = xmlReader.getLocalName();

            // Each IF statement corresponds to one typo of entry in Jenkins CI web.xml
            // Order is sorted to have best performance (from types with the a lot of entries)
            if (tagName.equals("mime-mapping")) {
                result.mimeMappings.add(loadMimeMapping(xmlReader));
                continue;
            }

            if (tagName.equals("filter")) {
                result.filters.add(loadFilter(xmlReader));
                continue;
            }

            if (tagName.equals("filter-mapping")) {
                result.filterMappings.add(loadFilterMapping(xmlReader));
                continue;
            }

            if (tagName.equals("security-role")) {
                result.securityRoles.add(loadSecurityRole(xmlReader));
                continue;
            }

            if (tagName.equals("servlet")) {
                result.servlets.add(loadServlet(xmlReader));
                continue;
            }

            if (tagName.equals("servlet-mapping")) {
                result.servletsMapping.add(loadServletMapping(xmlReader));
                continue;
            }

            if (tagName.equals("web-app")) {
                result.webAppVersion = getWebAppVersion(xmlReader);
                continue;
            }

            if (tagName.equals("listener")) {
                result.listeners.add(loadListener(xmlReader));
                continue;
            }

            if (tagName.equals("security-constraint")) {
                result.securityConstraints.add(loadSecurityConstraint(xmlReader));
                continue;
            }

            if (tagName.equals("login-config")) {
                result.loginConfig = loadLoginConfig(xmlReader);
                continue;
            }

            if (tagName.equals("env-entry")) {
                result.envEntries.add(loadEnvEntry(xmlReader));
                continue;
            }

            if (tagName.equals("error-page")) {
                result.errorPages.add(loadErrorPage(xmlReader));
                continue;
            }

        }
        xmlReader.close();

        return result;
    }

    // <error-page>
    // <exception-type>java.lang.Throwable</exception-type>
    // <location>/oops</location>
    // </error-page>
    private ErrorPage loadErrorPage(XMLStreamReader xmlReader) throws WebXmlFormatException,
            XMLStreamException {
        String tagContent = null;
        String tagName;

        ErrorPage errorPage = new ErrorPage();

        while (xmlReader.hasNext()) {
            switch (xmlReader.next()) {

                case XMLStreamConstants.CHARACTERS:
                    tagContent = xmlReader.getText().trim();
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    tagName = xmlReader.getLocalName();

                    if (tagName.equals("exception-type")) {
                        errorPage.exceptionType = tagContent;
                        continue;
                    }

                    if (tagName.equals("location")) {
                        errorPage.location = tagContent;
                        continue;
                    }

                    if (tagName.equals("error-page")) {
                        if (errorPage.exceptionType == null || errorPage.location == null)
                            throwMalformedWebXml("Error-page");
                        else
                            return errorPage;
                    }

                default:
                    break;
            }
        }

        throwMalformedWebXml("Error-page");
        return null; // Never happens. Method above always throws exception
    }

    // <mime-mapping>
    // <extension>webm</extension>
    // <mime-type>video/webm</mime-type>
    // </mime-mapping>
    private MimeMapping loadMimeMapping(XMLStreamReader xmlReader) throws WebXmlFormatException,
            XMLStreamException {
        String tagContent = null;
        String tagName;

        MimeMapping mimeMapping = new MimeMapping();

        while (xmlReader.hasNext()) {
            switch (xmlReader.next()) {

                case XMLStreamConstants.CHARACTERS:
                    tagContent = xmlReader.getText().trim();
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    tagName = xmlReader.getLocalName();

                    if (tagName.equals("extension")) {
                        mimeMapping.extension = tagContent;
                        continue;
                    }

                    if (tagName.equals("mime-type")) {
                        mimeMapping.mimeType = tagContent;
                        continue;
                    }

                    if (tagName.equals("mime-mapping")) {
                        if (mimeMapping.extension == null || mimeMapping.mimeType == null)
                            throwMalformedWebXml("Mime-mapping");
                        else
                            return mimeMapping;
                    }

                default:
                    break;
            }
        }

        throwMalformedWebXml("Mime-mapping");
        return null; // Never happens. Method above always throws exception
    }

    // <env-entry>
    // <env-entry-name>HUDSON_HOME</env-entry-name>
    // <env-entry-type>java.lang.String</env-entry-type>
    // <env-entry-value></env-entry-value>
    // </env-entry>
    private EnvEntry loadEnvEntry(XMLStreamReader xmlReader) throws WebXmlFormatException,
            XMLStreamException {
        String tagContent = null;
        String tagName;

        EnvEntry envEntry = new EnvEntry();

        while (xmlReader.hasNext()) {
            switch (xmlReader.next()) {

                case XMLStreamConstants.CHARACTERS:
                    tagContent = xmlReader.getText().trim();
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    tagName = xmlReader.getLocalName();

                    if (tagName.equals("env-entry-name")) {
                        envEntry.entryName = tagContent;
                        continue;
                    }

                    if (tagName.equals("env-entry-type")) {
                        envEntry.entryType = tagContent;
                        continue;
                    }

                    if (tagName.equals("env-entry-value")) {
                        envEntry.entryValue = tagContent;
                        continue;
                    }

                    if (tagName.equals("env-entry")) {
                        if (envEntry.entryName == null || envEntry.entryValue == null
                                || envEntry.entryType == null)
                            throwMalformedWebXml("Env-entry");
                        else
                            return envEntry;
                    }

                default:
                    break;
            }
        }

        throwMalformedWebXml("Env-entry");
        return null; // Never happens. Method above always throws exception
    }

    // <login-config>
    // <auth-method>FORM</auth-method>
    // <form-login-config>
    // <form-login-page>/login</form-login-page>
    // <form-error-page>/loginError</form-error-page>
    // </form-login-config>
    // </login-config>
    private LoginConfig loadLoginConfig(XMLStreamReader xmlReader)
            throws WebXmlFormatException, XMLStreamException {
        String tagContent = null;
        String tagName;

        LoginConfig loginConfig = new LoginConfig();

        while (xmlReader.hasNext()) {
            switch (xmlReader.next()) {

                case XMLStreamConstants.CHARACTERS:
                    tagContent = xmlReader.getText().trim();
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    tagName = xmlReader.getLocalName();

                    if (tagName.equals("auth-method")) {
                        loginConfig.authMethod = tagContent;
                        continue;
                    }

                    if (tagName.equals("form-login-page")) {
                        loginConfig.formLoginPage = tagContent;
                        continue;
                    }

                    if (tagName.equals("form-error-page")) {
                        loginConfig.formErrorPage = tagContent;
                        continue;
                    }

                    if (tagName.equals("form-login-config")) {
                        if (loginConfig.formLoginPage == null || loginConfig.formErrorPage == null)
                            throwMalformedWebXml("Login-config");
                    }

                    if (tagName.equals("login-config")) {
                        return loginConfig;
                    }

                default:
                    break;
            }
        }

        throwMalformedWebXml("Login-config");
        return null; // Never happens. Method above always throws exception
    }

    // <security-constraint>
    // // //<web-resource-collection>
    // // // //<web-resource-name>Hudson</web-resource-name>
    // // // //<url-pattern>/loginEntry</url-pattern>
    // // //</web-resource-collection>
    // // //<auth-constraint>
    // // // //<role-name>*</role-name>
    // // //</auth-constraint>
    // </security-constraint>
    private SecurityConstraint loadSecurityConstraint(XMLStreamReader xmlReader)
            throws WebXmlFormatException, XMLStreamException {
        String tagContent = null;
        String tagName;

        SecurityConstraint securityConstraint = new SecurityConstraint();
        WebResourceCollection currentWebResourceCollection = null;
        List<String> authConstraint = null;

        while (xmlReader.hasNext()) {
            switch (xmlReader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    if (xmlReader.getLocalName().equals("web-resource-collection"))
                        currentWebResourceCollection = new WebResourceCollection();

                    if (xmlReader.getLocalName().equals("auth-constraint"))
                        authConstraint = new ArrayList<String>(3);
                    break;

                case XMLStreamConstants.CHARACTERS:
                    tagContent = xmlReader.getText().trim();
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    tagName = xmlReader.getLocalName();

                    if (tagName.equals("web-resource-name")) {
                        if (currentWebResourceCollection != null)
                            currentWebResourceCollection.webResourceName = tagContent;
                        else
                            throwMalformedWebXml("security-constraint");
                        continue;
                    }

                    if (tagName.equals("url-pattern")) {
                        if (currentWebResourceCollection != null)
                            currentWebResourceCollection.urlPatterns.add(tagContent);
                        else
                            throwMalformedWebXml("security-constraint");
                        continue;
                    }

                    if (tagName.equals("web-resource-collection")) {
                        if (currentWebResourceCollection == null
                                || currentWebResourceCollection.webResourceName == null)
                            throwMalformedWebXml("security-constraint");
                        else
                            securityConstraint.webResourceCollections.add(currentWebResourceCollection);
                        continue;
                    }

                    if (tagName.equals("auth-constraint")) {
                        if (authConstraint != null)
                            securityConstraint.rolesAllowed = authConstraint;
                        continue;
                    }

                    if (tagName.equals("role-name")) {
                        if (authConstraint != null)
                            authConstraint.add(tagContent);
                        else
                            throwMalformedWebXml("security-constraint");
                        continue;
                    }

                    if (tagName.equals("security-constraint")) {
                        if (securityConstraint == null
                                || securityConstraint.webResourceCollections == null)
                            throwMalformedWebXml("security-constraint");
                        else
                            return securityConstraint;
                    }
                    break;

                default:
                    break;
            }
        }

        throwMalformedWebXml("security-constraint");
        return null; // Never happens. Method above always throws exception
    }

    // <security-role>
    // <role-name>admin</role-name>
    // </security-role>
    private String loadSecurityRole(XMLStreamReader xmlReader) throws WebXmlFormatException,
            XMLStreamException {
        String tagContent = null;
        String tagName;

        String roleName = null;
        
        while (xmlReader.hasNext()) {
            switch (xmlReader.next()) {
                case XMLStreamConstants.CHARACTERS:
                    tagContent = xmlReader.getText().trim();
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    tagName = xmlReader.getLocalName();

                    if (tagName.equals("role-name")) {
                        roleName = tagContent;
                        continue;
                    }

                    if (tagName.equals("security-role")) {
                        if (roleName == null)
                            throwExceptionNotSpecifiedParameter("Security-role", "role-name");
                        else
                            return roleName;
                    }
                    break;

                default:
                    break;
            }
        }

        throwMalformedWebXml("security-role");
        return null; // Never happens. Method above always throws exception
    }

    // <listener>
    // <listener-class>hudson.WebAppMain</listener-class>
    // </listener>
    private Listener loadListener(XMLStreamReader xmlReader) throws XMLStreamException,
            WebXmlFormatException {
        String tagContent = null;
        String tagName;

        Listener listener = new Listener();

        while (xmlReader.hasNext()) {
            switch (xmlReader.next()) {
                case XMLStreamConstants.CHARACTERS:
                    tagContent = xmlReader.getText().trim();
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    tagName = xmlReader.getLocalName();

                    if (tagName.equals("listener-class")) {
                        listener.listenerClass = tagContent;
                        continue;
                    }

                    if (tagName.equals("listener")) {
                        if (listener.listenerClass == null)
                            throwExceptionNotSpecifiedParameter("Listener", "listener-class");
                        else
                            return listener;
                    }
                    break;

                default:
                    break;
            }
        }

        throwMalformedWebXml("listener");
        return null; // Never happens. Method above always throws exception
    }

    // <filter-mapping>
    // <filter-name>encoding-filter</filter-name>
    // <url-pattern>/*</url-pattern>
    // </filter-mapping>
    private FilterMapping loadFilterMapping(XMLStreamReader xmlReader)
            throws WebXmlFormatException, XMLStreamException {
        String tagContent = null;
        String tagName;

        FilterMapping filterMapping = new FilterMapping();

        while (xmlReader.hasNext()) {
            switch (xmlReader.next()) {
                case XMLStreamConstants.CHARACTERS:
                    tagContent = xmlReader.getText().trim();
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    tagName = xmlReader.getLocalName();

                    if (tagName.equals("filter-name")) {
                        filterMapping.filterName = tagContent;
                        continue;
                    }

                    if (tagName.equals("url-pattern")) {
                        filterMapping.urlPattern = tagContent;
                        continue;
                    }

                    if (tagName.equals("filter-mapping")) {
                        if (filterMapping.filterName == null || filterMapping.urlPattern == null)
                            throwExceptionNotSpecifiedParameter("Filter-mapping", "filter-name",
                                    "url-pattern");
                        else
                            return filterMapping;
                    }
                    break;

                default:
                    break;
            }
        }

        throwMalformedWebXml("filter-mapping");
        return null; // Never happens. Method above always throws exception
    }

    // <filter>
    // <filter-name>encoding-filter</filter-name>
    // <filter-class>hudson.util.CharacterEncodingFilter</filter-class>
    // </filter>
    private Filter loadFilter(XMLStreamReader xmlReader) throws WebXmlFormatException,
            XMLStreamException {
        String tagContent = null;
        String tagName;

        Filter filter = new Filter();

        while (xmlReader.hasNext()) {
            switch (xmlReader.next()) {
                case XMLStreamConstants.CHARACTERS:
                    tagContent = xmlReader.getText().trim();
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    tagName = xmlReader.getLocalName();

                    if (tagName.equals("filter-name")) {
                        filter.filterName = tagContent;
                        continue;
                    }

                    if (tagName.equals("filter-class")) {
                        filter.filterClass = tagContent;
                        continue;
                    }

                    if (tagName.equals("filter")) {
                        if (filter.filterName == null || filter.filterClass == null)
                            throwExceptionNotSpecifiedParameter("Filter", "filter-name",
                                    "filter-class");
                        else
                            return filter;
                    }
                    break;

                default:
                    break;
            }
        }

        throwMalformedWebXml("filter");
        return null; // Never happens. Method above always throws exception
    }

    // <servlet-mapping>
    // <servlet-name>Stapler</servlet-name>
    // <url-pattern>/*</url-pattern>
    // </servlet-mapping>
    private ServletMapping loadServletMapping(XMLStreamReader xmlReader)
            throws WebXmlFormatException, XMLStreamException {
        String tagContent = null;
        String tagName;

        ServletMapping servletMapping = new ServletMapping();

        while (xmlReader.hasNext()) {
            switch (xmlReader.next()) {
                case XMLStreamConstants.CHARACTERS:
                    tagContent = xmlReader.getText().trim();
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    tagName = xmlReader.getLocalName();

                    if (tagName.equals("servlet-name")) {
                        servletMapping.servletName = tagContent;
                        continue;
                    }

                    if (tagName.equals("url-pattern")) {
                        servletMapping.urlPattern = tagContent;
                        continue;
                    }

                    if (tagName.equals("servlet-mapping")) {
                        if (servletMapping.servletName == null || servletMapping.urlPattern == null)
                            throwExceptionNotSpecifiedParameter("Servlet-mapping", "servlet-name",
                                    "url-pattern");
                        else
                            return servletMapping;
                    }
                    break;

                default:
                    break;
            }
        }

        throwMalformedWebXml("servlet-mapping");
        return null; // Never happens. Method above always throws exception
    }

    // <servlet>
    // <servlet-name>Stapler</servlet-name>
    // <servlet-class>org.kohsuke.stapler.Stapler</servlet-class>
    // <init-param>
    // <param-name>default-encodings</param-name>
    // <param-value>text/html=UTF-8</param-value>
    // </init-param>
    // </servlet>
    private Servlet loadServlet(XMLStreamReader xmlReader) throws WebXmlFormatException,
            XMLStreamException {
        String tagContent = null;
        String tagName;

        Servlet servlet = new Servlet();
        InitParam currentInitParam = null;

        while (xmlReader.hasNext()) {
            switch (xmlReader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    if (xmlReader.getLocalName().equals("init-param"))
                        currentInitParam = new InitParam();
                    break;

                case XMLStreamConstants.CHARACTERS:
                    tagContent = xmlReader.getText().trim();
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    tagName = xmlReader.getLocalName();

                    if (tagName.equals("servlet-name")) {
                        servlet.servletName = tagContent;
                        continue;
                    }

                    if (tagName.equals("servlet-class")) {
                        servlet.servletClass = tagContent;
                        continue;
                    }

                    if (tagName.equals("init-param")) {
                        if (currentInitParam == null || currentInitParam.paramName == null
                                || currentInitParam.paramValue == null) {
                            throwMalformedWebXml("servlet");
                        }
                        else {
                            servlet.initParams.add(currentInitParam);
                        }
                        continue;
                    }

                    if (tagName.equals("param-value")) {
                        if (currentInitParam != null)
                            currentInitParam.paramValue = tagContent;
                        else
                            throwMalformedWebXml("servlet");
                        continue;
                    }

                    if (tagName.equals("param-name")) {
                        if (currentInitParam != null)
                            currentInitParam.paramName = tagContent;
                        else
                            throwMalformedWebXml("servlet");
                        continue;
                    }

                    if (tagName.equals("servlet")) {
                        if (servlet.servletClass == null || servlet.servletName == null)
                            throwExceptionNotSpecifiedParameter("Servlet", "servlet-name",
                                    "servlet-class");
                        else
                            return servlet;
                    }
                    break;

                default:
                    break;
            }
        }

        throwMalformedWebXml("servlet");
        return null; // Never happens. Method above always throws exception
    }

    private String getWebAppVersion(XMLStreamReader xmlReader) throws WebXmlFormatException {
        try {
            return xmlReader.getAttributeValue(null, "version");
        } catch (IllegalStateException e) {
            throwMalformedWebXml("web-app");
        }
        return null;
    }

    private void throwMalformedWebXml(String entryType) throws WebXmlFormatException {
        throw new WebXmlFormatException(String.format(
                "Malformed web.xml file. Not properly set entry %s.", entryType));
    }

    private void throwExceptionNotSpecifiedParameter(String entryType, String... params)
            throws WebXmlFormatException {
        StringBuffer buffer = new StringBuffer();
        buffer.append(entryType);
        buffer.append(" has to have specified ");
        for (String param : params) {
            buffer.append(param);
            buffer.append(", ");
        }
        throw new WebXmlFormatException(buffer.toString());
    }
}
