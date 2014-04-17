package undertow4jenkins.testApplication.servlets;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CountRequestsServlet extends HttpServlet {

    private int numberOfRequests = 0;

    @Override
    public void init() {
        String offset = getServletConfig().getInitParameter("offset");
        numberOfRequests = offset == null ? 0 : Integer.parseInt(offset);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        numberOfRequests++;
        ServletOutputStream outputStream = response.getOutputStream();
        outputStream.println("<html><body>This servlet has been accessed via GET "
                + numberOfRequests + " times</body></html>");
        outputStream.flush();
    }

}
