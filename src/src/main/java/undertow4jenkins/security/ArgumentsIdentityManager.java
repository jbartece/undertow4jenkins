package undertow4jenkins.security;

import io.undertow.security.idm.Account;
import io.undertow.security.idm.Credential;
import io.undertow.security.idm.IdentityManager;
import io.undertow.security.idm.PasswordCredential;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArgumentsIdentityManager implements IdentityManager {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Map<String, UserAccount> users = new HashMap<String, UserAccount>();

    private final Map<String, char[]> passwords = new HashMap<String, char[]>();

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

    @Override
    public Account verify(Account account) {
        log.debug("Verify: Account: " + account.toString());
        if (account instanceof UserAccount) {
            return verifyCredential(account, ((UserAccount) account).getCredential());
        }
        else {
            log.debug("Account is not instance of UserAccount");
            return null;
        }
    }

    @Override
    public Account verify(String id, Credential credential) {
        log.debug("Verify: Id " + id + ", " + credential.toString());
        UserAccount account = users.get(id);
        if (account != null) {
            return verifyCredential(new UserAccount(account.getPrincipal(), account.getRoles(),
                    credential), credential);
        }
        else
            return null;
    }

    @Override
    public Account verify(Credential credential) {
        log.warn("Only authentization with username and password is supported.");
        return null;
    }

    private Account verifyCredential(Account account, Credential credential) {
        if (credential instanceof PasswordCredential) {
            String username = account.getPrincipal().getName();
            char[] password = passwords.get(username);
            if (password == null)
                return null; // No password for this account

            char[] credentialPassword = ((PasswordCredential) credential).getPassword();

            if (Arrays.equals(password, credentialPassword))
                return account;
            else
                return null;
        }
        else {
            log.warn("Only authentization with username and password is supported.");
            return null;
        }

    }

}
