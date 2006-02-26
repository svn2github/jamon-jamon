package org.jamon.codegen;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.jamon.TemplateLocation;
import org.jamon.TemplateSource;
import org.jamon.emit.EmitMode;

import junit.framework.TestCase;

public class TemplateDescriberTest extends TestCase
{
    private static class MockTemplateSource implements TemplateSource
    {
        public MockTemplateSource(Properties p_properties)
        {
            m_properties = p_properties;
        }

        public long lastModified(String p_templatePath)
        {
            return 0;
        }

        public boolean available(String p_templatePath)
        {
            return false;
        }

        public InputStream getStreamFor(String p_templatePath)
        {
            return null;
        }

        public String getExternalIdentifier(String p_templatePath)
        {
            return null;
        }

        public TemplateLocation getTemplateLocation(String p_templatePath)
        {
            return null;
        }

        public Properties getProperties()
        {
            return m_properties;
        }

        private Properties m_properties;
    }

    private TemplateDescriber makeTemplateDescriber(Properties properties) throws IOException
    {
        return new TemplateDescriber(
            new MockTemplateSource(properties), null, EmitMode.STANDARD);
    }

   public void testGetJamonContextType() throws Exception
    {
        Properties properties = new Properties();
        assertEquals(
            "java.lang.Object",
            makeTemplateDescriber(properties).getJamonContextType());
        properties.put("org.jamon.contextType", "foo.bar");
        assertEquals(
            "foo.bar",
            makeTemplateDescriber(properties).getJamonContextType());
    }

    public void testGetEmitMode() throws Exception
    {
        Properties properties = new Properties();
        assertEquals(
            EmitMode.STANDARD, makeTemplateDescriber(properties).getEmitMode());
        properties.put("org.jamon.emitMode", "strict");
        assertEquals(
            EmitMode.STRICT, makeTemplateDescriber(properties).getEmitMode());
    }
}
