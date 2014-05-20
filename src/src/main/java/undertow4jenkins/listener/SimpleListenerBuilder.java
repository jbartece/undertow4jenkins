package undertow4jenkins.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import undertow4jenkins.option.Options;
import io.undertow.Undertow.Builder;

public class SimpleListenerBuilder {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private Options options;

    /**
     * IP address, which should be set to bind listener in Undertow to listen on all interfaces
     */
    private static final String hostAllInterfacesString = "0.0.0.0";

    private static final int MAX_PORT = 65535;

    public SimpleListenerBuilder(Options options) {
        this.options = options;
    }

    // "   --httpKeepAliveTimeout   = how long idle HTTP keep-alive connections are kept around (in ms; default 5000)?\n" +
    /**
     * Creates HTTP listener based on values from options.httpPort, options.httpListenAdress.
     * 
     * @param serverBuilder Prepared Undertow instance to which listener will be added
     */
    public void setHttpListener(Builder serverBuilder) {
        if (options.httpPort == -1) {
            log.info("HTTP listener is disabled.");
            return;
        }
        if (options.httpPort < -1 || options.httpPort > MAX_PORT) {
            log.warn("Unallowed httpPort value. HTTP listener is disabled!");
            return;
        }

        if (options.httpListenAddress != null)
            serverBuilder.addHttpListener(options.httpPort, options.httpListenAddress);
        else {
            // Listen on all interfaces
            serverBuilder.addHttpListener(options.httpPort, hostAllInterfacesString);
        }

        log.info("HTTP listener created");
    }

    /**
     * Creates AJP listener based on values from options.ajpPort, options.ajpListenAdress.
     * 
     * @param serverBuilder Prepared Undertow instance to which listener will be added
     */
    public void setAjpListener(Builder serverBuilder) {
        if (options.ajp13Port == -1)
            return;
        if (options.ajp13Port < -1 || options.ajp13Port > MAX_PORT) {
            log.warn("Unallowed ajp13Port value. AJP listener is disabled!");
            return;
        }

        if (options.ajp13ListenAddress != null)
            serverBuilder.addAjpListener(options.ajp13Port, options.ajp13ListenAddress);
        else {
            // Listen on all interfaces
            serverBuilder.addAjpListener(options.ajp13Port, hostAllInterfacesString);
        }

        log.info("AJP listener created");
    }

}
