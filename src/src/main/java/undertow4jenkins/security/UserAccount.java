package undertow4jenkins.security;

import io.undertow.security.idm.Account;
import io.undertow.security.idm.Credential;

import java.io.Serializable;
import java.security.Principal;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * This class represents user account used in IdentityManager instances
 * 
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */
public class UserAccount implements Account, Serializable {

    private static final long serialVersionUID = 1L;

    private final Set<String> roles = new CopyOnWriteArraySet<String>();

    private final Principal principal;
    
    private final Credential credential;

    public UserAccount(Principal principal) {
        this.principal = principal;
        this.credential = null;
    }

    public UserAccount(String name, String ... roles) {
        this.principal = new AccountPrincipal(name);
        this.credential = null;
        for(String role : roles)
            this.roles.add(role);
    }

    public UserAccount(Principal principal, Set<String> roles, Credential credential) {
        this.principal = principal;
        this.roles.addAll(roles);
        this.credential = credential;
    }

    @Override
    public Principal getPrincipal() {
        return principal;
    }

    @Override
    public Set<String> getRoles() {
        return Collections.unmodifiableSet(roles);
    }

    public Credential getCredential() {
        return credential;
    }

}
