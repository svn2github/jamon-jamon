package foo.bar;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jamon.RecompilingTemplateManager;
import org.jamon.TemplateManagerSource;

public final class HelloServlet
    extends HttpServlet {

    public void init() {
        RecompilingTemplateManager.Data data = new RecompilingTemplateManager.Data();
        data.setSourceDir("templates");
        data.setWorkDir("build/work");
        TemplateManagerSource.setTemplateManager(
            new RecompilingTemplateManager(data));
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        response.setContentType("text/html");
        new HelloTemplate().render(response.getWriter());
    }
}
