package org.jamon.codegen;

import java.io.IOException;

import org.jamon.api.ParsedTemplate;
import org.jamon.api.TemplateParser;
import org.jamon.api.TemplateSource;

public class TemplateParserImpl implements TemplateParser
{
    private final TemplateDescriber m_templateDescriber;


    public TemplateParserImpl(TemplateSource p_templateSource, ClassLoader p_classLoader) {
        m_templateDescriber = new TemplateDescriber(p_templateSource, p_classLoader);
    }

    @Override
    public ParsedTemplate parseTemplate(String p_templatePath) throws IOException
    {
        return new ParsedTemplateImpl(
            m_templateDescriber,
            new Analyzer(p_templatePath, m_templateDescriber).analyze());
    }

}
