package org.jamon;

import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.HashMap;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PushbackReader;

import org.jamon.node.Start;
import org.jamon.parser.Parser;
import org.jamon.parser.ParserException;
import org.jamon.lexer.Lexer;
import org.jamon.lexer.LexerException;

public class TemplateDescriber
{
    public TemplateDescriber(String p_templateSourceDir)
    {
        m_templateSourceDir = p_templateSourceDir;
    }

    private String getTemplateFileName(String p_path)
    {
        return m_templateSourceDir + p_path;
    }

    private final String m_templateSourceDir;

    public FargInfo getFargInfo(String p_path, String p_fargName)
    {
        return new FargInfo(p_fargName,
                            new HashMap());
    }

    public Iterator getFargNames(String p_path)
        throws JttException
    {
        try
        {
            return new BaseAnalyzer(parseTemplate(p_path)).getFargNames();
        }
        catch (IOException e)
        {
            throw new JttException(e);
        }
    }

    public File getTemplateFile(String p_path)
    {
        return new File(getTemplateFileName(p_path));
    }

    public List getRequiredArgNames(final String p_path)
        throws JttException
    {
        try
        {
            LinkedList list = new LinkedList();
            BaseAnalyzer g = new BaseAnalyzer(parseTemplate(p_path));
            for (Iterator i = g.getRequiredArgNames(); i.hasNext(); /* */)
            {
                list.add(i.next());
            }
            return list;
        }
        catch (IOException e)
        {
            throw new JttException(e);
        }
    }

    public Start parseTemplate(String p_path)
        throws IOException
    {
        try
        {
            return new Parser(new Lexer
                              (new PushbackReader
                               (new FileReader(getTemplateFileName(p_path)),
                                1024)))
                .parse();
        }
        catch (ParserException e)
        {
            throw new JttException(e);
        }
        catch (LexerException e)
        {
            throw new JttException(e);
        }
    }
}
