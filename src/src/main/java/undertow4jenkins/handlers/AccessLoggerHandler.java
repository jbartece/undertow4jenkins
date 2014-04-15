package undertow4jenkins.handlers;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;


public interface AccessLoggerHandler extends HttpHandler {

    void log(HttpServerExchange exchange);

}
