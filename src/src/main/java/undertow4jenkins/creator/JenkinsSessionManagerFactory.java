package undertow4jenkins.creator;

import io.undertow.server.session.InMemorySessionManager;
import io.undertow.server.session.SessionManager;
import io.undertow.servlet.api.Deployment;
import io.undertow.servlet.api.SessionManagerFactory;

/**
 * Session manager factory , which provides option to set session 
 * timeout value for each session manager
 * 
 * @author Jakub Bartecek <jbartece@redhat.com>
 * 
 */
public class JenkinsSessionManagerFactory implements SessionManagerFactory {

    private final int maxSessions;
    
    private final int defaultSessionTimeout;

    /**
     * @param defaultSessionTimeout Session timeout value in seconds
     */
    public JenkinsSessionManagerFactory(int defaultSessionTimeout) {
        this.maxSessions = -1;
        this.defaultSessionTimeout = defaultSessionTimeout;
    }

    @Override
    public SessionManager createSessionManager(Deployment deployment) {
        InMemorySessionManager sessionManager = new InMemorySessionManager(
                deployment.getDeploymentInfo().getDeploymentName(), maxSessions);
        sessionManager.setDefaultSessionTimeout(defaultSessionTimeout);
        return sessionManager;
    }

}
