package org.jamon.integration;

import java.util.Iterator;

import org.jamon.JamonRuntimeException;
import org.jamon.ParserError;
import org.jamon.ParserErrors;
import org.jamon.TemplateCompilationException;
import test.jamon.context.ContextCaller;

public class ContextTest extends TestBase
{
    public void testContext() throws Exception
    {
        new ContextCaller()
            .setJamonContext(new TestJamonContext(3))
            .render(getWriter());
        checkOutput("Caller: 3\n" +
                    "Parent: 3\nCallee: 3\nCalleeFragment: 3\n" +
                    "Def: 3\n");
    }

    public void testCallContextFromContextless() throws Exception
    {
        try
        {
            getRecompilingTemplateManager()
                .constructProxy("/test/jamon/broken/CallContextFromContextless");
            fail();
        }
        catch (JamonRuntimeException e)
        {
            assertEquals(ParserErrors.class, e.getCause().getClass());
            ParserErrors cause = (ParserErrors) e.getCause();
            Iterator<ParserError> errors = cause.getErrors();
            assertTrue(errors.hasNext());
            ParserError error = errors.next();
            assertEquals(
               "Calling component does not have a jamonContext, but called" +
               " component /test/jamon/context/ContextCallee expects one of" +
               " type org.jamon.integration.TestJamonContext",
               error.getMessage());
            assertFalse(errors.hasNext());
        }
    }
}
