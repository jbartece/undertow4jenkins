package undertow4jenkins;

import java.io.IOException;
import java.net.UnknownHostException;

import org.junit.Test;
import org.xml.sax.SAXException;

import undertow4jenkins.option.Options;

public class HttpConnectionTest extends AbstractTest {

    @Test
    public void testListening() throws UnknownHostException, IOException, SAXException,
            InterruptedException {
        Options opts = new Options();
        opts.warfile = "target/test-classes/test.war";
        opts.httpPort = 11005;
        opts.httpListenAdress = "127.0.0.2";

        containerInstance = new Launcher(opts);
        containerInstance.run();

        assertConnectionRefused("127.0.0.1", 11005);

        makeRequest("http://127.0.0.2:11005/CountRequestsServlet");
    }
}
