package undertow4jenkins.loader;

import static io.undertow.servlet.Servlets.listener;
import io.undertow.servlet.api.ListenerInfo;

import java.util.EventListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ListenerLoader {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private ClassLoader classLoader;

    public ListenerLoader(ClassLoader cl) {
        this.classLoader = cl;
    }
    
    // <listener>
    // <listener-class>hudson.WebAppMain</listener-class>
    // </listener>
    public ListenerInfo createListener(String listenerClassName) {
        try {
            Class<? extends EventListener> clazz = Class.forName(listenerClassName, true,
                    this.classLoader)
                    .asSubclass(EventListener.class);
            return listener(clazz);
        } catch (ClassNotFoundException e) {
            log.error("Loading of listener class failed!", e);
            return null;
        }
    }
}
