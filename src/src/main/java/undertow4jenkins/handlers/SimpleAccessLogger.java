package undertow4jenkins.handlers;

import io.undertow.security.idm.Account;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SimpleAccessLogger implements AccessLoggerHandler {

    private static final DateFormat DF = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z");

    private static final String COMMON = "###ip### - ###user### ###time### \"###uriLine###\" ###status### ###size###";

    private static final String COMBINED = COMMON + " \"###referer###\" \"###userAgent###\"";

    private static final String RESIN = COMMON + " \"###userAgent###\"";

    private final HttpHandler next;

    private String pattern;

    private String fileName;

    private FileOutputStream outputStream;

    private PrintWriter outputWriter;

    public SimpleAccessLogger(HttpHandler next, String appName,
            String loggerFilePattern, String loggerFormat) throws Exception {

        if (loggerFilePattern == null || loggerFormat == null)
            throw new Exception("Output file for access logger and logger format has to be set!");

        this.next = next;

        if (loggerFormat.equalsIgnoreCase("combined")) {
            this.pattern = COMBINED;
        } else if (loggerFormat.equalsIgnoreCase("common")) {
            this.pattern = COMMON;
        } else if (loggerFormat.equalsIgnoreCase("resin")) {
            this.pattern = RESIN;
        } else {
            this.pattern = loggerFormat;
        }

        this.fileName = patternReplace(loggerFilePattern, new String[][] { { "###webapp###",
                appName } });

        File file = new File(this.fileName);

        file.getAbsoluteFile().getParentFile().mkdirs();
        this.outputStream = new FileOutputStream(file, true);
        this.outputWriter = new PrintWriter(this.outputStream, true);
    }

    private String patternReplace(String pattern, String[][] fields) {
        if (fields == null)
            return pattern;
        else {
            StringBuffer sb = new StringBuffer(pattern);
            for (String[] singleField : fields) {
                patternReplace0(sb, singleField[0], singleField[1]);
            }
            return sb.toString();
        }
    }

    private void patternReplace0(StringBuffer pattern, String fromStr, String toStr) {
        if (pattern == null || fromStr == null)
            return;

        if (toStr == null)
            toStr = "(null)";

        int index = 0;
        int foundAt = pattern.indexOf(fromStr, index);
        while (foundAt != -1) {
            pattern.replace(foundAt, foundAt + fromStr.length(), toStr);
            index = foundAt + toStr.length();
            foundAt = toStr.indexOf(fromStr, index);
        }

    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        log(exchange);
        next.handleRequest(exchange);
    }

    @Override
    public void log(HttpServerExchange exchange) {
        String uriLine = exchange.getRequestMethod() + " " +
                exchange.getRequestURI() + " " + exchange.getProtocol();
        int status = exchange.getResponseCode();
        long size = exchange.getResponseContentLength();

        String date;
        synchronized (DF) {
            date = DF.format(new Date());
        }

        String logLine = patternReplace(pattern,
                new String[][] {
                        { "###ip###", exchange.getHostName() }, // TODO translate to IP?
                        { "###user###", "-" },
                        // toString(exchange.getSecurityContext()
                        // .getAuthenticatedAccount()) }, // TODO
                        { "###time###", "[" + date + "]" },
                        { "###uriLine###", uriLine },
                        { "###status###", "" + status },
                        { "###size###", "" + size },
                        { "###referer###",
                                hyphenIfNull(exchange.getRequestHeaders().getFirst("Referer")) },
                        { "###userAgent###",
                                hyphenIfNull(exchange.getRequestHeaders().getFirst("User-Agent")) }
                });

        outputWriter.println(logLine);

    }

    private String hyphenIfNull(String str) {
        return str != null ? str : "-";
    }

    private String toString(Account acc) {
        return acc != null ? acc.getPrincipal().getName() : "-";
    }

    // TODO add Closeable interface and close it when app ends
    public void close() throws IOException {
        if (outputWriter != null) {
            outputWriter.flush();
            outputWriter.close();
            outputWriter = null;
        }

        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException e) {
            }
            outputStream = null;
        }

        fileName = null;
    }

}
