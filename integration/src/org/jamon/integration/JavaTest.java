package org.jamon.integration;

import java.io.Writer;
import java.io.StringWriter;
import java.io.IOException;

import junit.framework.TestCase;

import org.jamon.StandardTemplateManager;
import org.jamon.StringUtils;
import org.jamon.JttException;

import foo.bar.test.jamon.JavaEscape;

/************************************************************************************
 *
 * Test Jamon's java escapes.  See "Jamon User's Guide", section 2.
 *
 ************************************************************************************/

public class JavaTest
    extends TestCase
{

    public void testExercise()
        throws IOException, 
               JttException
    {
        Writer w = new StringWriter();
        StandardTemplateManager m =
            new StandardTemplateManager("templates",
                                        "build/work");
        m.setPackagePrefix("foo.bar.");
        JavaEscape.Factory f = new JavaEscape.Factory(m);
        JavaEscape t = f.getInstance(w);
        t.render();
        w.flush();
        assertEquals("0\n1\n2\n", w.toString());
    }

}
