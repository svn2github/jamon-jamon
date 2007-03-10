package org.jamon.codegen;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.jamon.TemplateLocation;
import org.jamon.TemplateSource;
import org.jamon.emit.EmitMode;

import junit.framework.TestCase;

public class TemplateDescriberTest extends TestCase
{
    private MockTemplateSource m_mockTemplateSource;
    private TemplateDescriber m_templateDescriber;

    private static class MockTemplateSource implements TemplateSource
    {
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

        public void setProperties(String p_path, Properties p_properties)
        {
            m_properties.put(p_path, p_properties);
        }

        public void loadProperties(String p_path, Properties p_properties)
        {
            Properties properties = m_properties.get(p_path);
            if (properties != null)
            {
                p_properties.putAll(properties);
            }
        }

        private Map<String, Properties> m_properties =
            new HashMap<String, Properties>();
    }

    @Override protected void setUp() throws Exception
    {
        super.setUp();
        m_mockTemplateSource = new MockTemplateSource();
        m_templateDescriber = new TemplateDescriber(m_mockTemplateSource, null);
    }

    public void testGetAliases() throws Exception
    {
        assertEquals(0, m_templateDescriber.getAliases("/foo").size());
        Properties properties = new Properties();
        properties.put("org.jamon.alias.bar", "/a/b");
        m_mockTemplateSource.setProperties("/", properties);
        assertEquals(1, m_templateDescriber.getAliases("/foo").size());
        assertEquals("/a/b", m_templateDescriber.getAliases("/foo").get("bar"));
        Properties subProperties = new Properties();
        subProperties.put("org.jamon.alias.bar", "/b/c");
        m_mockTemplateSource.setProperties("/foo/", subProperties);
        subProperties.put("org.jamon.alias.bar2", "/c/d");
        assertEquals("/b/c", m_templateDescriber.getAliases("/foo/a").get("bar"));
        assertEquals("/c/d", m_templateDescriber.getAliases("/foo/a").get("bar2"));
        subProperties.put("org.jamon.alias.bar", "");
        assertFalse(m_templateDescriber.getAliases("/foo/a").containsKey("bar"));
    }

    public void testGetJamonContextType() throws Exception
    {
        assertNull(m_templateDescriber.getJamonContextType("/foo"));

        Properties properties = new Properties();
        properties.put("org.jamon.contextType", "foo.bar");
        m_mockTemplateSource.setProperties("/", properties);
        assertEquals(
            "foo.bar",
            m_templateDescriber.getJamonContextType("/foo"));

        Properties subProperties = new Properties();
        subProperties.put("org.jamon.contextType", "foo.baz");
        m_mockTemplateSource.setProperties("/foo/", subProperties);
        assertEquals(
            "foo.baz",
            m_templateDescriber.getJamonContextType("/foo/bat"));

        Properties subSubProperties = new Properties();
        subProperties.put("org.jamon.contextType", "");
        m_mockTemplateSource.setProperties("/foo/bar/", subSubProperties);
        assertNull(m_templateDescriber.getJamonContextType("/foo/bar/baz"));
    }

    public void testGetEmitMode() throws Exception
    {
        Properties properties = new Properties();
        setUp();
        assertEquals(
            EmitMode.STANDARD, m_templateDescriber.getEmitMode("/foo"));
        properties.put("org.jamon.emitMode", "strict");
        m_mockTemplateSource.setProperties("/", properties);
        assertEquals(
            EmitMode.STRICT, m_templateDescriber.getEmitMode("/foo"));
    }
}
