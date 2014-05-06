package undertow4jenkins.listener;

import org.junit.Test;

import undertow4jenkins.AbstractTest;
import undertow4jenkins.Launcher;
import undertow4jenkins.option.Options;


public class Ajp13ConnectionTest extends AbstractTest {
    
    /**
     * This tests expects prepared Apache instance
     */
    @Test
    public void simpleTestWithHttp() throws Exception {
        Options opts = new Options();
        opts.warfile = "target/test-classes/test.war";
        opts.httpPort = 8090;
        opts.prefix = "jenkins";
        opts.ajp13Port = 8010;
        
        containerInstance = new Launcher(opts);
        containerInstance.run();
        
        makeRequest("http://localhost/jenkins/CountRequestsServlet");
    }
}
