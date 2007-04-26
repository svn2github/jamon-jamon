package org.jamon.integration;

import java.util.List;

import org.jamon.ParserErrorsImpl;
import org.jamon.api.ParserError;
import org.jamon.api.ParserErrors;

import test.jamon.context.Child;
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

    public void testParentRenderer() throws Exception
    {
        new Child().setJamonContext(new TestJamonContext(3)).makeParentRenderer()
            .render(getWriter());
        checkOutput("3");
    }

    public void testSetContextViaParentRenderer() throws Exception
    {
        new Child().makeParentRenderer()
            .setJamonContext(new TestJamonContext(3))
            .render(getWriter());
        checkOutput("3");
    }

    public void testCallContextFromContextless() throws Exception
    {
        try
        {
            getRecompilingTemplateManager()
                .constructProxy("/test/jamon/broken/CallContextFromContextless");
            fail();
        }
        catch (RuntimeException e)
        {
            assertEquals(ParserErrorsImpl.class, e.getCause().getClass());
            ParserErrors cause = (ParserErrors) e.getCause();
            List<ParserError> errors = cause.getErrors();
            assertEquals(1, errors.size());
            assertEquals(
               "Calling component does not have a jamonContext, but called" +
               " component /test/jamon/context/ContextCallee expects one of" +
               " type org.jamon.integration.TestJamonContext",
               errors.get(0).getMessage());
        }
    }
}
