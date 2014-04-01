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

    public WebXmlContent parse(String pathToFile) throws FileNotFoundException, XMLStreamException {
        WebXmlContent result = new WebXmlContent();

        XMLInputFactory xmlInFactory = XMLInputFactory.newFactory();
        XMLStreamReader xmlReader = xmlInFactory.createXMLStreamReader(
                new FileInputStream(pathToFile));

        for (; xmlReader.hasNext(); xmlReader.next()) {
            if (!xmlReader.isStartElement())
                continue;

            String tagName = xmlReader.getLocalName();

            // each IF statement corresponds to one typo of entry in Jenkins CI web.xml
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
    private SecurityRole loadSecurityRole(XMLStreamReader xmlReader) {
        // TODO Auto-generated method stub
        return null;
    }

    // <listener>
    // <listener-class>hudson.WebAppMain</listener-class>
    // </listener>
    private Listener loadListener(XMLStreamReader xmlReader) {
        // TODO Auto-generated method stub
        return null;
    }

    // <filter-mapping>
    // <filter-name>encoding-filter</filter-name>
    // <url-pattern>/*</url-pattern>
    // </filter-mapping>
    private FilterMapping loadFilterMapping(XMLStreamReader xmlReader) {
        // TODO Auto-generated method stub
        return null;
    }

    // <filter>
    // <filter-name>encoding-filter</filter-name>
    // <filter-class>hudson.util.CharacterEncodingFilter</filter-class>
    // </filter>
    private Filter loadFilter(XMLStreamReader xmlReader) {
        // TODO Auto-generated method stub
        return null;
    }

    // <servlet-mapping>
    // <servlet-name>Stapler</servlet-name>
    // <url-pattern>/*</url-pattern>
    // </servlet-mapping>
    private ServletMapping loadServletMapping(XMLStreamReader xmlReader) {
        // TODO Auto-generated method stub
        return null;
    }

    private Servlet loadServlet(XMLStreamReader xmlReader) throws XMLStreamException {
        String tagContent = null;
        String tagName;

        Servlet servlet = new Servlet();
        InitParam currentInitParam = null;

        while (xmlReader.hasNext()) {
            int event = xmlReader.next();
            switch (event) {
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
                            throw new XMLStreamException(
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
                            throw new XMLStreamException(
                                    "Servlet has to have specified servlet-name and servlet-class");
                        else
                            return servlet;
                    }

                    break;

                default:
                    break;
            }
        }

        throw new XMLStreamException(
                "Malformed web.xml file. Not properly set servlet.");
    }

    private String getWebAppVersion(XMLStreamReader xmlReader) {
        return xmlReader.getAttributeValue(null, "version");
    }
}
