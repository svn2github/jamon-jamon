package org.jamon.doc;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServlet;

import org.jamon.Invoker;
import org.jamon.StandardTemplateManager;
import org.jamon.TemplateManager;

public class StaticServlet
    extends HttpServlet
{
    public void doGet(HttpServletRequest p_request,
                      HttpServletResponse p_response)
        throws IOException, ServletException
    {
        doPost(p_request, p_response);
    }

    public void doPost(HttpServletRequest p_request,
                       HttpServletResponse p_response)
        throws IOException, ServletException
    {
        Writer writer = p_response.getWriter();
        expireResponse(p_response);
        p_response.setContentType("text/html");
        String pathInfo = p_request.getServletPath();
        new Invoker(m_manager,
                    "/org/jamon/doc"
                    + pathInfo.substring(0,pathInfo.length()-5))
            .render(writer, p_request.getParameterMap());
        writer.close();
    }

    public void init(ServletConfig p_config)
        throws ServletException
    {
        try
        {
            m_manager = new StandardTemplateManager(
                new StandardTemplateManager.Data()
                .setWorkDir("build/work")
                .setSourceDir("templates"));
        }
        catch (IOException e)
        {
            throw new ServletException(e);
        }
    }

    private void expireResponse(HttpServletResponse p_response)
        throws IOException,
               ServletException
    {
        long now = System.currentTimeMillis();
        p_response.setDateHeader("Expires", now);
        p_response.setDateHeader("Date", now);
        p_response.setHeader("Pragma", "no-cache");
    }

    private TemplateManager m_manager;
}
