package undertow4jenkins.parser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

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
import undertow4jenkins.parser.WebXmlContent.SecurityRole;
import undertow4jenkins.parser.WebXmlContent.Servlet;
import undertow4jenkins.parser.WebXmlContent.ServletMapping;

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
    private ErrorPage loadErrorPage(XMLStreamReader xmlReader) {
        // TODO Auto-generated method stub
        return null;
    }

    // <mime-mapping>
    // <extension>webm</extension>
    // <mime-type>video/webm</mime-type>
    // </mime-mapping>
    private MimeMapping loadMimeMapping(XMLStreamReader xmlReader) {
        // TODO Auto-generated method stub
        return null;
    }

    // <env-entry>
    // <env-entry-name>HUDSON_HOME</env-entry-name>
    // <env-entry-type>java.lang.String</env-entry-type>
    // <env-entry-value></env-entry-value>
    // </env-entry>
    private EnvEntry loadEnvEntry(XMLStreamReader xmlReader) {
        // TODO Auto-generated method stub
        return null;
    }

    // <login-config>
    // <auth-method>FORM</auth-method>
    // <form-login-config>
    // <form-login-page>/login</form-login-page>
    // <form-error-page>/loginError</form-error-page>
    // </form-login-config>
    // </login-config>
    private LoginConfig loadLoginConfig(XMLStreamReader xmlReader) {
        // TODO Auto-generated method stub
        return null;
    }

    // <security-constraint>
    // <web-resource-collection>
    // <web-resource-name>Hudson</web-resource-name>
    // <url-pattern>/loginEntry</url-pattern>
    // <!--http-method>GET</http-method-->
    // </web-resource-collection>
    // <auth-constraint>
    // <role-name>*</role-name>
    // </auth-constraint>
    // </security-constraint>
    private SecurityConstraint loadSecurityConstraint(XMLStreamReader xmlReader) {
        // TODO Auto-generated method stub
        return null;
    }

    // <security-role>
    // <role-name>admin</role-name>
    // </security-role>
    private SecurityRole loadSecurityRole(XMLStreamReader xmlReader) throws WebXmlFormatException,
            XMLStreamException {
        String tagContent = null;
        String tagName;

        SecurityRole securityRole = new SecurityRole();

        while (xmlReader.hasNext()) {
            switch (xmlReader.next()) {
                case XMLStreamConstants.CHARACTERS:
                    tagContent = xmlReader.getText().trim();
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    tagName = xmlReader.getLocalName();

                    if (tagName.equals("role-name")) {
                        securityRole.roleName = tagContent;
                        continue;
                    }

                    if (tagName.equals("listener")) {
                        if (securityRole.roleName == null)
                            throwExceptionNotSpecifiedParameter("SecurityRole", "role-name");
                        else
                            return securityRole;
                    }
                    break;

                default:
                    break;
            }
        }

        throwMalformedWebXml("security-role");
        return null; //Never happens. Method above always throws exception
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
        return null; //Never happens. Method above always throws exception
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
                            throwExceptionNotSpecifiedParameter("FilterMapping", "filter-name",
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
        return null; //Never happens. Method above always throws exception
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
        return null; //Never happens. Method above always throws exception
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
        return null; //Never happens. Method above always throws exception
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
                            throw new WebXmlFormatException(
                                    "Init-param of servlet is not correctly specified!");
                        }
                        else {
                            servlet.initParams.add(currentInitParam);
                        }
                        continue;
                    }

                    if (tagName.equals("param-value")) {
                        currentInitParam.paramValue = tagContent;
                        continue;
                    }

                    if (tagName.equals("param-name")) {
                        currentInitParam.paramName = tagContent;
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
        return null; //Never happens. Method above always throws exception
    }

    private String getWebAppVersion(XMLStreamReader xmlReader) {
        return xmlReader.getAttributeValue(null, "version");
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
