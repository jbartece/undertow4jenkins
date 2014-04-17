package undertow4jenkins;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.xml.sax.SAXException;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

public class AbstractTest extends Assert {

    protected Launcher containerInstance;

    protected WebConversation wc = new WebConversation();

    @After
    public void shutdown() {
        if (containerInstance != null)
            containerInstance.shutdownApplication();
    }

    public String makeRequest(String url) throws IOException, SAXException {
        WebRequest wreq = new GetMethodWebRequest(url);
        WebResponse wresp = wc.getResponse(wreq);
        InputStream content = wresp.getInputStream();
        assertTrue("Loading CountRequestsServlet", content.available() > 0);
        String s = IOUtils.toString(content);
        content.close();
        return s;
    }

    protected void assertConnectionRefused(String host, int port) throws UnknownHostException, IOException {
        try {
            new Socket(host, port);
            fail("Port or host should be unavailable. Port "
                    + port + ", host: " + host);
        } catch (ConnectException e) {
            // OK
        }
    }
}
