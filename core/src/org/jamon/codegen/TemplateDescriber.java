/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Jamon code, released February, 2003.
 *
 * The Initial Developer of the Original Code is Jay Sachs.  Portions
 * created by Jay Sachs are Copyright (C) 2003 Jay Sachs.  All Rights
 * Reserved.
 *
 * Contributor(s): Ian Robertson
 */

package org.jamon.codegen;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.io.Reader;
import java.io.IOException;
import java.io.PushbackReader;

import org.jamon.JamonException;
import org.jamon.TemplateSource;
import org.jamon.JamonParseException;
import org.jamon.util.StringUtils;
import org.jamon.node.Start;
import org.jamon.parser.Parser;
import org.jamon.parser.ParserException;
import org.jamon.lexer.Lexer;
import org.jamon.lexer.LexerException;

public class TemplateDescriber
{
    public TemplateDescriber(TemplateSource p_templateSource,
                             ClassLoader p_classLoader)
    {
        m_templateSource = p_templateSource;
        m_classLoader = p_classLoader;
    }

    private final TemplateSource m_templateSource;
    private final ClassLoader m_classLoader;

    public TemplateDescription getTemplateDescription(final String p_path)
         throws IOException
    {
        return getTemplateDescription(p_path, new HashSet());
    }

    public TemplateDescription getTemplateDescription(final String p_path,
                                                      final Set p_children)
         throws IOException
     {
         if (m_templateSource.available(p_path))
         {
             return new TemplateDescription
                 (new Analyzer(p_path, this, p_children).analyze());
         }
         else
         {
             try
             {
                 return new TemplateDescription
                     (m_classLoader.loadClass
                      (StringUtils.templatePathToClassName(p_path)));
             }
             catch(ClassNotFoundException e)
             {
                 throw new JamonException
                     ("Unable to find template or class for " + p_path);
             }
         }
    }

    public Start parseTemplate(String p_path)
        throws IOException
    {
        Reader reader = m_templateSource.getReaderFor(p_path);
        try
        {
            return new Parser(new Lexer(new PushbackReader(reader, 1024)))
                .parse();
        }
        catch (ParserException e)
        {
            throw new JamonParseException(getExternalIdentifier(p_path),e);
        }
        catch (LexerException e)
        {
            throw new JamonParseException(getExternalIdentifier(p_path),e);
        }
        finally
        {
            reader.close();
        }
    }

    public String getExternalIdentifier(String p_path)
    {
        return m_templateSource.getExternalIdentifier(p_path);
    }
}
