package org.jamon.integration;

import java.io.Writer;
import java.io.StringWriter;
import java.io.IOException;

import junit.framework.TestCase;

import org.jamon.StandardTemplateManager;
import org.jamon.StringUtils;
import org.jamon.JttException;

import test.jamon.ParametrizedFragment;

/************************************************************************************
 *
 * Test Jamon's parametrized template fragments.  See "Jamon User's Guide",
 * section 9.
 *
 ************************************************************************************/

public class ParametrizedFragmentTest
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
        ParametrizedFragment.Factory f = new ParametrizedFragment.Factory(m);
        ParametrizedFragment t = f.getInstance(w);
        t.render(new int[] { -2, 0, 15 });
        w.flush();
        assertEquals("-0+", w.toString());
    }

}
