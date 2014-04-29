package undertow4jenkins.loader;

import io.undertow.servlet.api.LoginConfig;
import io.undertow.servlet.api.SecurityConstraint;
import io.undertow.servlet.api.WebResourceCollection;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecurityLoader {

    private static final Logger log = LoggerFactory.getLogger("undertow4jenkins.loader.SecurityLoader");
    
    public static List<String> createSecurityRoles(List<String> securityRoles) {
        return securityRoles;
    }

    public static List<SecurityConstraint> createSecurityConstraints(
            List<undertow4jenkins.parser.WebXmlContent.SecurityConstraint> constraintsDataCol) {
        List<SecurityConstraint> securityConstraints = new ArrayList<SecurityConstraint>(3);

        for (undertow4jenkins.parser.WebXmlContent.SecurityConstraint constraintData : constraintsDataCol) {
            SecurityConstraint constraint = new SecurityConstraint();
            WebResourceCollection webResourceCollection = new WebResourceCollection();

            log.trace("UrlPattern: " + constraintData.webResourceCollection.urlPattern);
            webResourceCollection.addUrlPattern(constraintData.webResourceCollection.urlPattern);
            if (constraintData.rolesAllowed != null) {
                constraint.addRolesAllowed(constraintData.rolesAllowed);
                log.trace("Allowed roles: " + constraintData.rolesAllowed);
            }
            
            constraint.addWebResourceCollection(webResourceCollection);
            securityConstraints.add(constraint);
        }

        return securityConstraints;
    }

    public static LoginConfig createLoginConfig(
            undertow4jenkins.parser.WebXmlContent.LoginConfig configData, String realmName){
        if(configData.authMethod != null && configData.formErrorPage != null && configData.formLoginPage != null){
            log.trace("LoginConfig: Method: " + configData.authMethod + ", realm: " + realmName + ", errorPage: " + configData.formErrorPage
                    +  ", loginPage: " + configData.formLoginPage);
            return new LoginConfig(configData.authMethod, realmName, configData.formLoginPage, configData.formErrorPage);
        }

        if(configData.authMethod != null )
            return new LoginConfig(configData.authMethod, realmName);
        
         throw new RuntimeException("Not properly set login-config in web.xml!");    
    }

}
