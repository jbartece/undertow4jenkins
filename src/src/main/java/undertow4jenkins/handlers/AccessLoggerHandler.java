package undertow4jenkins.handlers;

import io.undertow.server.HttpHandler;

import java.io.Closeable;

/**
 * Interface for access logger, which can be added to undertow4jenkins
 * as external or internal class
 * 
 * @author Jakub Bartecek <jbartece@redhat.com>
 * 
 */
public interface AccessLoggerHandler extends HttpHandler, Closeable {

}
