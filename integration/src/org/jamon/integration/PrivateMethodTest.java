package org.jamon.integration;

import java.io.Writer;
import java.io.StringWriter;
import java.io.IOException;

import junit.framework.TestCase;

import org.jamon.StandardTemplateManager;
import org.jamon.StringUtils;
import org.jamon.JttException;

import test.jamon.PrivateMethods;

/************************************************************************************
 *
 * Test Jamon's private methods.  See "Jamon User's Guide", section 6.
 *
 ************************************************************************************/

public class PrivateMethodTest
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
        PrivateMethods.Factory f = new PrivateMethods.Factory(m);
        PrivateMethods t = f.getInstance(w);
        t.render();
        w.flush();
        assertEquals("7=1111111", w.toString());
    }

}
