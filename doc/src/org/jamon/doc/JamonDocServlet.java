package org.jamon.doc;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;
import java.util.HashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServlet;

import org.jamon.Invoker;
import org.jamon.StandardTemplateManager;
import org.jamon.TemplateManager;
import org.jamon.JamonTemplateException;

public class JamonDocServlet
    extends HttpServlet
{
    public void doGet(HttpServletRequest p_request,
                      HttpServletResponse p_response)
        throws IOException, ServletException
    {
        Writer writer = p_response.getWriter();
        expireResponse(p_response);
        p_response.setContentType("text/html");
        String templatePath = p_request.getServletPath();
        templatePath = templatePath.substring(0,templatePath.length()-5);
        try
        {
            new Invoker(m_manager, "/org/jamon/doc" + templatePath)
                .render(writer, m_parameters, true);
        }
        catch (JamonTemplateException e)
        {
            writer.write("In " + e.getFileName()
                                + ", line " + e.getLine()
                                + ", column " + e.getColumn()
                                + "\n<pre>\n");
            e.printStackTrace(new PrintWriter(writer));
            writer.write("</pre>");
        }
        writer.close();
    }

    public void init(ServletConfig p_config)
        throws ServletException
    {
        m_parameters.put("srcTarball", "jamon-src.tgz");
        m_parameters.put("srcZip", "jamon-src.zip");
        m_parameters.put("binTarball", "jamon.tgz");
        m_parameters.put("binZip", "jamon.zip");
        m_parameters.put("version", "3.14");
        m_parameters.put("output", "whatever");

        m_manager = new StandardTemplateManager
            (new StandardTemplateManager.Data()
                .setWorkDir("build/work")
                .setSourceDir("templates")
                .setJavaCompiler("jikes"));
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

    private final Map m_parameters = new HashMap();
    private TemplateManager m_manager;
}
