package org.jamon.integration;

import java.io.Writer;
import java.io.StringWriter;
import java.io.IOException;

import junit.framework.TestCase;

import org.jamon.StandardTemplateManager;
import org.jamon.StringUtils;
import org.jamon.JttException;

import test.jamon.Fragment;

/************************************************************************************
 *
 * Test Jamon's template fragments.  See "Jamon User's Guide", section 8.
 *
 ************************************************************************************/

public class FragmentTest
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
        Fragment.Factory f = new Fragment.Factory(m);
        Fragment t = f.getInstance(w);
        t.render(1);
        w.flush();
        assertEquals("1(2)1", w.toString());
    }

}
