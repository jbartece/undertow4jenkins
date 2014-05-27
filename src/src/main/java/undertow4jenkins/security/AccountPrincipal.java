package undertow4jenkins.security;

import java.io.Serializable;
import java.security.Principal;

/**
 * This class represents principal of account used in IdentityManager instances
 * Principal contains only account name.
 * 
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */
public class AccountPrincipal implements Principal, Serializable {

    private static final long serialVersionUID = 1L;

    private final String name;

    public AccountPrincipal(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof AccountPrincipal))
            return false;
        AccountPrincipal other = (AccountPrincipal) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "AccountPrincipal [" + (name != null ? "name=" + name : "") + "]";
    }
}
