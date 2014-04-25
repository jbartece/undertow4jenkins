package undertow4jenkins.security;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import undertow4jenkins.AbstractTest;
import undertow4jenkins.Launcher;
import undertow4jenkins.option.Options;

import com.meterware.httpunit.AuthorizationRequiredException;

public class ArgumentsRealmTest extends AbstractTest {

     @Test
    public void basicTest() throws Exception {
        Options opts = new Options();
        opts.warfile = "target/test-classes/test.war";
        opts.httpPort = 13000;
        
        Map<String, String> passwdMap = new HashMap<String, String>();
        passwdMap.put("joe", "passJoe");
        opts.argumentsRealmPasswd = passwdMap;
        
        Map<String, String[]> rolesMap = new HashMap<String, String[]>();
        rolesMap.put("joe", new String[]{"user"});
        opts.argumentsRealmRoles = rolesMap;

        containerInstance = new Launcher(opts);
        containerInstance.run();

        try {
            makeRequest("http://localhost:13000/secure/secret.txt");
            fail("Autentication should be required!");
        } catch (AuthorizationRequiredException e) {
            // OK
        }

        wc.setAuthorization("joe", "passJoe");
        assertEquals("diamond", makeRequest("http://localhost:13000/secure/secret.txt"));
    }
}
