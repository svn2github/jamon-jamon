package org.jamon;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Field;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileNotFoundException;
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
        throws IOException
    {
        try
        {
            return new BaseAnalyzer(parseTemplate(p_path)).getFargInfo(p_fargName);
        }
        catch (FileNotFoundException fnfe)
        {
            try
            {
                Class c = Class.forName(StringUtils.pathToClassName(p_path));
                Field f = c.getField("FARGINFO_"+p_fargName);
                Map args = (Map) f.get(null);
                return new FargInfo(p_fargName,
                                    args.keySet().iterator(),
                                    args);
            }
            catch (RuntimeException e)
            {
                throw e;
            }
            catch (Exception e)
            {
                throw new JamonException(e);
            }
        }
    }

    public Iterator getFargNames(String p_path)
        throws JamonException
    {
        try
        {
            return new BaseAnalyzer(parseTemplate(p_path)).getFargNames();
        }
        catch (FileNotFoundException fnfe)
        {
            try
            {
                Class c = Class.forName(StringUtils.pathToClassName(p_path));
                Field f = c.getField("FARGNAMES");
                return Arrays.asList( (String []) f.get(null) ).iterator();
            }
            catch (RuntimeException e)
            {
                throw e;
            }
            catch (Exception e)
            {
                throw new JamonException(e);
            }
        }
        catch (IOException e)
        {

            throw new JamonException(e);
        }
    }

    public File getTemplateFile(String p_path)
    {
        return new File(getTemplateFileName(p_path));
    }

    public List getRequiredArgNames(final String p_path)
        throws JamonException
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
        catch (FileNotFoundException fnfe)
        {
            try
            {
                Class c = Class.forName(StringUtils.pathToClassName(p_path));
                Field f = c.getField("REQUIRED_ARGS");
                return Arrays.asList( (String []) f.get(null) );
            }
            catch (RuntimeException e)
            {
                throw e;
            }
            catch (Exception e)
            {
                throw new JamonException(e);
            }
        }
        catch (IOException e)
        {
            throw new JamonException(e);
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
            throw new JamonException(e);
        }
        catch (LexerException e)
        {
            throw new JamonException(e);
        }
    }
}
