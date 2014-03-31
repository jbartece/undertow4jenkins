package undertow4jenkins.loader;

import io.undertow.servlet.api.ErrorPage;

public class ErrorPageLoader {

    public static ErrorPage createErrorPage() throws ClassNotFoundException {
        String location = "/oops";
        String exceptionType = "java.lang.Throwable";

        Class<? extends Throwable> errorPageClass = Class.forName(exceptionType).asSubclass(
                Throwable.class);

        return new ErrorPage(location, errorPageClass);
    }

}
