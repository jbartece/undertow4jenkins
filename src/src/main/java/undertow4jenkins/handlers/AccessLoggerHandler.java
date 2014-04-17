package undertow4jenkins.handlers;

import java.io.Closeable;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;


public interface AccessLoggerHandler extends HttpHandler, Closeable {

    void log(HttpServerExchange exchange);

}
