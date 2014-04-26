package undertow4jenkins.loader;

import io.undertow.servlet.api.LoginConfig;
import io.undertow.servlet.api.SecurityConstraint;
import io.undertow.servlet.api.WebResourceCollection;

import java.util.ArrayList;
import java.util.List;

public class SecurityLoader {

    public static List<String> createSecurityRoles(List<String> securityRoles) {
        return securityRoles;
    }

    public static List<SecurityConstraint> createSecurityConstraints(
            List<undertow4jenkins.parser.WebXmlContent.SecurityConstraint> constraintsDataCol) {
        List<SecurityConstraint> securityConstraints = new ArrayList<SecurityConstraint>(3);

        for (undertow4jenkins.parser.WebXmlContent.SecurityConstraint constraintData : constraintsDataCol) {
            SecurityConstraint constraint = new SecurityConstraint();
            WebResourceCollection webResourceCollection = new WebResourceCollection();

            webResourceCollection.addUrlPattern(constraintData.webResourceCollection.urlPattern);
            if (constraintData.rolesAllowed != null)
                constraint.addRolesAllowed(constraintData.rolesAllowed);
            
            constraint.addWebResourceCollection(webResourceCollection);
        }

        return securityConstraints;
    }

    public static LoginConfig createLoginConfig(
            undertow4jenkins.parser.WebXmlContent.LoginConfig configData, String realmName){
        if(configData.authMethod != null && configData.formErrorPage != null && configData.formLoginPage != null)
            return new LoginConfig(configData.authMethod, realmName, configData.formLoginPage, configData.formErrorPage);

        if(configData.authMethod != null )
            return new LoginConfig(configData.authMethod, realmName);
        
         throw new RuntimeException("Not properly set login-config in web.xml!");    
    }

}
