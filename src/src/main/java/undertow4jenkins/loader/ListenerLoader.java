package undertow4jenkins.loader;

import static io.undertow.servlet.Servlets.listener;
import io.undertow.servlet.api.ListenerInfo;

import java.util.EventListener;

public class ListenerLoader {

    // <listener>
    // <listener-class>hudson.WebAppMain</listener-class>
    // </listener>
    public static ListenerInfo createListener(String listenerClassName,
            ClassLoader jenkinsWarClassLoader) throws ClassNotFoundException {

        Class<? extends EventListener> clazz = Class.forName(listenerClassName, true,
                jenkinsWarClassLoader).asSubclass(EventListener.class);
        return listener(clazz);
    }
}
