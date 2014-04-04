package undertow4jenkins.loader;

import undertow4jenkins.parser.WebXmlContent.ErrorPage;

import java.util.ArrayList;
import java.util.List;

public class ErrorPageLoader {

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
