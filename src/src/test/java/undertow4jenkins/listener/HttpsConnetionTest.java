package undertow4jenkins.listener;

import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import undertow4jenkins.AbstractTest;
import undertow4jenkins.Launcher;
import undertow4jenkins.option.Options;

public class HttpsConnetionTest extends AbstractTest {

    @Test
    public void basicListenerTest() throws Exception {
        Options opts = new Options();
        opts.warfile = "target/test-classes/test.war";
        opts.httpPort = -1;
        opts.httpsPort = 12000;
        opts.httpsPrivateKey = "src/ssl/server.key";
        opts.httpsCertificate = "src/ssl/server.crt";
        opts.httpsListenAddress = "localhost";

        containerInstance = new Launcher(opts);
        containerInstance.startApplication();
        
        assertConnectionRefused("127.0.0.2", 12000);

        request(new TrustManagerImpl());
    }

    private void request(X509TrustManager trustManager) throws Exception {
        HttpsURLConnection connection = (HttpsURLConnection)
                new URL("https://localhost:12000/CountRequestsServlet").openConnection();
        connection.setHostnameVerifier(new HostnameVerifier() {

            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });

        SSLContext ssl = SSLContext.getInstance("SSL");
        ssl.init(null, new X509TrustManager[] { trustManager }, null);
        connection.setSSLSocketFactory(ssl.getSocketFactory());
        IOUtils.toString(connection.getInputStream());
    }
    
    @Test
    public void testSelfSignedCert() throws Exception {
        Options opts = new Options();
        opts.warfile = "target/test-classes/test.war";
        opts.httpPort = -1;
        opts.httpsPort = 12000;

        containerInstance = new Launcher(opts);
        containerInstance.startApplication();
        
        try {
            request(new TrustManagerImpl());
            fail("Unique key should be generated!");
        } catch (SSLHandshakeException e) {
            //OK
        }
        
        request(new TrustEveryoneManager());
    }

}
