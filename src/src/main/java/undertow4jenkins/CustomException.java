package undertow4jenkins;

/**
 * Internal Exception of this application.
 * 
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */
public class CustomException extends Exception {
    public CustomException(String msg) {
        super(msg);
    }

}
