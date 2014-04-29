package undertow4jenkins.security;

import io.undertow.security.idm.IdentityManager;

import java.util.Map;

public class ArgumentsIdentityManager extends GenericIdentityManager implements IdentityManager {

    /**
     * Initializes identity manager and set users
     * 
     * @param argumentsRealmPasswd Map with entries username,password
     * @param argumentsRealmRoles Map with entries username,roles
     */
    public ArgumentsIdentityManager(Map<String, String> argumentsRealmPasswd,
            Map<String, String[]> argumentsRealmRoles) {
        for (Map.Entry<String, String> user : argumentsRealmPasswd.entrySet()) {
            String username = user.getKey();

            String[] roles = argumentsRealmRoles.get(username);
            if (roles == null)
                log.warn("No roles were specified for user " + username);

            this.users.put(username, new UserAccount(username, roles));
            this.passwords.put(username, user.getValue().toCharArray());
        }
    }

}
