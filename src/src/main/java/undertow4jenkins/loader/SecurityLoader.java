package undertow4jenkins.loader;

import io.undertow.servlet.api.LoginConfig;
import io.undertow.servlet.api.SecurityConstraint;
import io.undertow.servlet.api.WebResourceCollection;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to create security entities from web.xml data
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */
public class SecurityLoader {

    private static final Logger log = LoggerFactory
            .getLogger("undertow4jenkins.loader.SecurityLoader");

    /**
     * Creates security roles
     * @param securityRoles List of security roles
     * @return List of security roles
     */
    public static List<String> createSecurityRoles(List<String> securityRoles) {
        return securityRoles;
    }

    /**
     * Creates security constraints from web.xml data
     * @param constraintsDataCol Data from web.xml
     * @return List of created security constraints
     */
    public static List<SecurityConstraint> createSecurityConstraints(
            List<undertow4jenkins.parser.WebXmlContent.SecurityConstraint> constraintsDataCol) {
        List<SecurityConstraint> securityConstraints = new ArrayList<SecurityConstraint>(3);

        for (undertow4jenkins.parser.WebXmlContent.SecurityConstraint constraintData : constraintsDataCol) {
            for (undertow4jenkins.parser.WebXmlContent.WebResourceCollection webResourceCol : constraintData.webResourceCollections) {
                SecurityConstraint constraint = new SecurityConstraint();
                WebResourceCollection webResourceCollection = new WebResourceCollection();

                for (String urlPattern : webResourceCol.urlPatterns)
                    webResourceCollection.addUrlPattern(urlPattern);

                if (constraintData.rolesAllowed != null)
                    constraint.addRolesAllowed(constraintData.rolesAllowed);

                constraint.addWebResourceCollection(webResourceCollection);
                securityConstraints.add(constraint);
            }
        }

        return securityConstraints;
    }

    /**
     * creates login config object from web.xml data
     * @param configData Data from web.xml
     * @param realmName Name of realm
     * @return Created login config
     */
    public static LoginConfig createLoginConfig(
            undertow4jenkins.parser.WebXmlContent.LoginConfig configData, String realmName) {
        if (configData.authMethod != null && configData.formErrorPage != null
                && configData.formLoginPage != null) {
            log.trace("LoginConfig: Method: " + configData.authMethod + ", realm: " + realmName
                    + ", errorPage: " + configData.formErrorPage
                    + ", loginPage: " + configData.formLoginPage);
            return new LoginConfig(configData.authMethod, realmName, configData.formLoginPage,
                    configData.formErrorPage);
        }

        if (configData.authMethod != null)
            return new LoginConfig(configData.authMethod, realmName);

        throw new RuntimeException("Not properly set login-config in web.xml!");
    }

}
