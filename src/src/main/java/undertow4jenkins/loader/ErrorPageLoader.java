package undertow4jenkins.loader;

import undertow4jenkins.parser.WebXmlContent.ErrorPage;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to create error page entity from web.xml data
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */
public class ErrorPageLoader {

    /**
     * Creates error page entity from web.xml data
     * @param data Data from web.xml
     * @return Created entity
     * @throws ClassNotFoundException Thrown if some class 
     *      could not be loaded by current class loader
     */
    public static List<io.undertow.servlet.api.ErrorPage> createErrorPage(
            List<ErrorPage> data) throws ClassNotFoundException {
        List<io.undertow.servlet.api.ErrorPage> errorPages =
                new ArrayList<io.undertow.servlet.api.ErrorPage>(3);

        for (ErrorPage singleData : data) {
            Class<? extends Throwable> errorPageClass =
                    Class.forName(singleData.exceptionType).asSubclass(Throwable.class);
            errorPages.add(
                    new io.undertow.servlet.api.ErrorPage(singleData.location, errorPageClass));
        }

        return errorPages;
    }

}
