package undertow4jenkins.loader;

import static io.undertow.servlet.Servlets.listener;
import io.undertow.servlet.api.ListenerInfo;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import undertow4jenkins.parser.WebXmlContent.Listener;

public class ListenerLoader {

    public static List<ListenerInfo> createListener(List<Listener> listenersData,
            ClassLoader jenkinsWarClassLoader) throws ClassNotFoundException {
        List<ListenerInfo> listenerInfos = new ArrayList<ListenerInfo>(3);
        
        for(Listener list : listenersData) {
            Class<? extends EventListener> clazz = Class.forName(list.listenerClass, true,
                    jenkinsWarClassLoader).asSubclass(EventListener.class);
            listenerInfos.add(listener(clazz));
        }
                
        return listenerInfos;
    }
}
