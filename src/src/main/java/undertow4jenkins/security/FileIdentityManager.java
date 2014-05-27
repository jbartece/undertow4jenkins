package undertow4jenkins.security;

import io.undertow.security.idm.IdentityManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import undertow4jenkins.CustomException;
import undertow4jenkins.option.Options;

/**
 * 
 * Identity manager, which loads users, passwords and roles from
 * XML file.
 * 
 * @author Jakub Bartecek <jbartece@redhat.com>
 * 
 */
public class FileIdentityManager extends GenericIdentityManager implements IdentityManager {

    private final String MSG_FATAL = "No valid file with users for FileIdentityManager! Feature disabled!";

    private final String DEFAULT_FILE_NAME = "users.xml";

    private final String ATT_USERNAME = "username";

    private final String ATT_PASSWORD = "password";

    private final String ATT_ROLES = "roles";

    private final String EL_USER = "user";

    /**
     * Initializes identity manager and set users
     * 
     * @param options Undertow4Jenkins options
     * @throws CustomException Thrown if file with users is malformed or not found
     */
    public FileIdentityManager(Options options) throws CustomException {
        File configFile;
        if (options.fileRealm_configFile != null)
            configFile = new File(options.fileRealm_configFile);
        else
            configFile = new File(DEFAULT_FILE_NAME);

        if (!configFile.exists())
            throw new CustomException(MSG_FATAL);

        try {
            loadUsersFromFile(configFile);
        } catch (Throwable e) {
            throw new CustomException(MSG_FATAL + " Reason: " + e.getMessage());
        }
    }

    /**
     * Loads data about users from file
     * 
     * @param configFile File with users
     * 
     * @throws Exception Thrown if loading of users fails
     */
    private void loadUsersFromFile(File configFile) throws Exception {
        Document document = createDOMFromXml(configFile);

        UserData data = new UserData();
        Element rootEl = document.getDocumentElement();
        int childrenCount = rootEl.getChildNodes().getLength();
        for (int i = 0; i < childrenCount; i++) {
            Node child = rootEl.getChildNodes().item(i);

            if (child.getNodeType() == Node.ELEMENT_NODE && EL_USER.equals(child.getNodeName())) {
                data.clearData();
                getDataFromAttributes(child, data);

                if (data.username == null || data.password == null
                        || data.roles.isEmpty()) {
                    log.debug("Incomplete data! Skipping user " + data.username);
                }
                else {
                    if (log.isDebugEnabled())
                        log.debug("Created user with username: " + data.username
                                + ", password: " + data.password + ", roles: " + data.roles);

                    this.users.put(data.username,
                            new UserAccount(data.username, data.roles.toArray(new String[0])));
                    this.passwords.put(data.username, data.password.toCharArray());

                }
            }
        }

    }

    /**
     * Load data from attributes in XML node
     * 
     * @param node Current node in document
     * @param data Container for loaded data
     */
    private void getDataFromAttributes(Node node, UserData data) {
        int attLen = node.getAttributes().getLength();
        for (int i = 0; i < attLen; i++) {
            Node attr = node.getAttributes().item(i);
            if (attr.getNodeName().equals(ATT_USERNAME)) {
                data.username = attr.getNodeValue();
                continue;
            }

            if (attr.getNodeName().equals(ATT_PASSWORD)) {
                data.password = attr.getNodeValue();
                continue;
            }

            if (attr.getNodeName().equals(ATT_ROLES)) {
                String rolesString = attr.getNodeValue();
                for (String role : rolesString.split(","))
                    data.roles.add(role.trim());
                continue;
            }
        }
    }

    /**
     * DOM document creator
     * 
     * @param configFile File with users
     * @return Created DOM document object
     * @throws Exception Thrown if loading of file fails
     */
    private Document createDOMFromXml(File configFile) throws Exception {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setValidating(false);
        builderFactory.setNamespaceAware(false);
        builderFactory.setExpandEntityReferences(false);
        builderFactory.setCoalescing(true);
        builderFactory.setIgnoringComments(true);
        builderFactory.setIgnoringElementContentWhitespace(true);
        return builderFactory.newDocumentBuilder().parse(configFile);
    }

    /**
     * Container for user data
     * 
     * @author Jakub Bartecek <jbartece@redhat.com>
     * 
     */
    private class UserData {

        String username = null;

        String password = null;

        List<String> roles = new ArrayList<String>(3);

        void clearData() {
            username = null;
            password = null;
            roles.clear();
        }
    }

}
