package org.jamon.integration;

import java.io.Writer;
import java.io.StringWriter;
import java.io.IOException;

import junit.framework.TestCase;

import org.jamon.StandardTemplateManager;
import org.jamon.StringUtils;
import org.jamon.JttException;

import foo.bar.test.jamon.Arguments;
import foo.bar.test.jamon.OptionalArguments;

/************************************************************************************
 *
 * Test Jamon's parameterized templates.  See "Jamon User's Guide", section 5.
 *
 ************************************************************************************/

public class ArgumentTest
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
        Arguments.Factory f = new Arguments.Factory(m);
        Arguments t = f.getInstance(w);
        t.render(INT, BOOLEAN, STRING);
        w.flush();
        assertEquals("" + INT + BOOLEAN + STRING, w.toString());
    }


    public void testOptional()
        throws IOException, 
               JttException
    {
        Writer w = new StringWriter();
        StandardTemplateManager m =
            new StandardTemplateManager("templates",
                                        "build/work");
        m.setPackagePrefix("foo.bar.");
        OptionalArguments.Factory f = new OptionalArguments.Factory(m);
        OptionalArguments t = f.getInstance(w);
        t.render(BOOLEAN, STRING);
        t.setI(INT);
        t.render(BOOLEAN, STRING);
        w.flush();
        assertEquals("" + 0 + BOOLEAN + STRING +
                     INT + BOOLEAN + STRING,
                     w.toString());
    }

    private static final int INT = 3;
    private static final boolean BOOLEAN = true;
    private static final String STRING = "foobar";

}

