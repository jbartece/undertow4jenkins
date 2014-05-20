package undertow4jenkins.security;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.undertow.security.idm.Account;
import io.undertow.security.idm.Credential;
import io.undertow.security.idm.IdentityManager;
import io.undertow.security.idm.PasswordCredential;

public abstract class GenericIdentityManager implements IdentityManager {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected final Map<String, UserAccount> users = new HashMap<String, UserAccount>();

    protected final Map<String, char[]> passwords = new HashMap<String, char[]>();

    @Override
    public Account verify(Account account) {
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

    protected Account verifyCredential(Account account, Credential credential) {
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
