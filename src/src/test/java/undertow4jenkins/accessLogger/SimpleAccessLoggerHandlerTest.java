package undertow4jenkins.accessLogger;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import undertow4jenkins.AbstractTest;
import undertow4jenkins.Launcher;
import undertow4jenkins.handlers.SimpleAccessLogger;
import undertow4jenkins.option.Options;

public class SimpleAccessLoggerHandlerTest extends AbstractTest {

    @Test
    public void testLoggerConnection() throws Exception {
        File logFile = new File("target/test.log");
        logFile.delete();

        // Start container
        Options options = new Options();
        options.warfile = "target/test-classes/test.war";
        options.prefix = "/prefix";
        options.httpPort = 5555;
        options.accessLoggerClassName = SimpleAccessLogger.class.getName();
        options.simpleAccessLogger_file = logFile.getAbsolutePath();
        options.simpleAccessLogger_format = "###ip### - ###user### ###uriLine### ###status###";
        containerInstance = new Launcher(options);
        containerInstance.run();
        
        makeRequest("http://127.0.0.1:5555/prefix/CountRequestsServlet");

        // Check content of log file
        String text = FileUtils.readFileToString(logFile);
        assertEquals("127.0.0.1 - - GET /prefix/CountRequestsServlet HTTP/1.1 200\n", text);

    }
}
