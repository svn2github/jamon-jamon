package org.jamon.tests.templates.escape;

import java.io.IOException;
import java.io.Writer;

import org.jamon.Renderer;
import org.jamon.AbstractTemplateImpl;
import org.jamon.escaping.Escaping;
import org.jamon.escape.Html;


public class HtmlTest
    extends TestBase
{
    private class Fragment
        extends AbstractTemplateImpl
        implements Html.Fragment_content
    {
        Fragment(String p_body)
        {
            super(HtmlTest.this.getTemplateManager(), Escaping.NONE);
            m_body = p_body;
        }
        private final String m_body;
        public void render()
            throws IOException
        {
            writeEscaped(m_body, Escaping.NONE);
        }

        public Renderer makeRenderer()
        {
            return new Renderer()
                {
                    public void renderTo(Writer p_writer)
                        throws IOException
                    {
                        writeTo(p_writer);
                        render();
                    }
                };
        }
    }

    public void testEmpty()
        throws Exception
    {
        new Html(getTemplateManager())
            .render(getWriter(), new Fragment(""));
        checkOutput("");
    }

    public void testSimple()
        throws Exception
    {
        new Html(getTemplateManager())
            .render(getWriter(), new Fragment("hello"));
        checkOutput("hello");
    }

    public void testEscaping()
        throws Exception
    {
        new Html(getTemplateManager())
            .render(getWriter(), new Fragment("<& &gt; &>!"));
        checkOutput("&lt;&amp; &amp;gt; &amp;&gt;!");
    }

}
