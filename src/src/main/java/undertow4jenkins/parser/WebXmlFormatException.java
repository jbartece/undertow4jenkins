package undertow4jenkins.parser;

/**
 * Exception, which indicates error in web.xml format
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */
public class WebXmlFormatException extends Exception {

    public WebXmlFormatException(String msg) {
        super(msg);
    }
}
