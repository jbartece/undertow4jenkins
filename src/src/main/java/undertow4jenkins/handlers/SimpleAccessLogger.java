package undertow4jenkins.handlers;

import io.undertow.security.idm.Account;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SimpleAccessLoggerHandler implements AccessLoggerHandler {

    private static final DateFormat DF = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z");

    private static final String COMMON = "###ip### - ###user### ###time### \"###uriLine###\" ###status### ###size###";

    private static final String COMBINED = COMMON + " \"###referer###\" \"###userAgent###\"";

    private static final String RESIN = COMMON + " \"###userAgent###\"";

    private final HttpHandler next;

    private String pattern;

    private String fileName;

    private FileOutputStream outputStream;

    private PrintWriter outputWriter;

    public SimpleAccessLoggerHandler(HttpHandler next, String appName,
            String loggerFile, String loggerFormat) throws FileNotFoundException {
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

        this.fileName = patternReplace(loggerFile, new String[][] {{"###webapp###", appName}});

        File file = new File(this.fileName);
        file.getParentFile().mkdirs();
        this.outputStream = new FileOutputStream(file, true);
        this.outputWriter = new PrintWriter(this.outputStream, true);
    }


    
    private String patternReplace(String pattern, String[][] fields) {
        // TODO Auto-generated method stub
        return null;
    }



    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        log(exchange);
        next.handleRequest(exchange);
    }

    @Override
    public void log(HttpServerExchange exchange) {
        String uriLine = String.format("%s %s %s", exchange.getRequestMethod(),
                exchange.getRequestURI(), exchange.getProtocol());
        int status = exchange.getResponseCode(); 
        long size = exchange.getResponseContentLength();
        
        String date;
        synchronized (DF) {
            date = DF.format(new Date());
        }
        
        String logLine = patternReplace(pattern, new String[][] {
                {"###ip###", exchange.getHostName()}, 
                {"###user###", hyphenIfNull(exchange.getSecurityContext().getAuthenticatedAccount())}, //TODO
                {"###time###", "[" + date + "]"},
                {"###uriLine###", uriLine},
                {"###status###", "" + status},
                {"###size###", "" + size},
                {"###referer###", hyphenIfNull(exchange.getRequestHeaders().getFirst("Referer"))},
                {"###userAgent###", hyphenIfNull(exchange.getRequestHeaders().getFirst("User-Agent"))}
        });
        
        outputWriter.println(logLine);
        
    }

    private String hyphenIfNull(String str) {
        return str != null ? str : "-" ;
    }
    
    private String hyphenIfNull(Account acc) {
        return acc != null ? acc.getPrincipal().getName() : "-" ;
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
