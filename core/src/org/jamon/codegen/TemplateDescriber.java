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
 * The Original Code is Jamon code, released October, 2002.
 *
 * The Initial Developer of the Original Code is Jay Sachs.  Portions
 * created by Jay Sachs are Copyright (C) 2002 Jay Sachs.  All Rights
 * Reserved.
 *
 * Contributor(s): Ian Robertson
 */

package org.jamon.codegen;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
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
    public TemplateDescriber(TemplateSource p_templateSource)
    {
        m_templateSource = p_templateSource;
    }

    private final TemplateSource m_templateSource;

    public FargInfo getFargInfo(String p_path, String p_fargName)
        throws IOException
    {
        if (m_templateSource.available(p_path))
        {
            return new BaseAnalyzer(parseTemplate(p_path)).getFargInfo(p_fargName);
        }
        else
        {
            FargInfo fargInfo = new FargInfo(p_fargName);
            try
            {
                 String[] argNames = (String[])
                     Class.forName(StringUtils.templatePathToClassName(p_path))
                     .getField("FARGINFO_" + p_fargName + "_ARG_NAMES")
                     .get(null);
                 String[] argTypes = (String[])
                     Class.forName(StringUtils.templatePathToClassName(p_path))
                     .getField("FARGINFO_" + p_fargName + "_ARG_TYPES")
                     .get(null);
                 for(int i = 0; i < argNames.length; i++)
                 {
                     fargInfo.addRequiredArg(argNames[i], argTypes[i]);
                 }
                 return fargInfo;
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
        throws IOException
    {
        if (m_templateSource.available(p_path))
        {
            return new BaseAnalyzer(parseTemplate(p_path))
                .getUnitInfo().getFargNames();
        }
        else
        {
            try
            {
                return Arrays.asList
                    ( (String [])
                      Class.forName(StringUtils.templatePathToClassName(p_path))
                          .getField("FARGNAMES")
                          .get(null) )
                    .iterator();
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

    public List getRequiredArgNames(final String p_path)
        throws IOException
    {
        if (m_templateSource.available(p_path))
        {
            LinkedList list = new LinkedList();
            BaseAnalyzer g = new BaseAnalyzer(parseTemplate(p_path));
            for (Iterator i = g.getUnitInfo().getRequiredArgs();
                 i.hasNext();
                 /* */)
            {
                list.add(((Argument)i.next()).getName());
            }
            return list;
        }
        else
        {
            try
            {
                return Arrays.asList
                    ( (String [])
                      Class.forName(StringUtils.templatePathToClassName(p_path))
                          .getField("REQUIRED_ARGS")
                          .get(null) );
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
            throw new JamonParseException
                (m_templateSource.getExternalIdentifier(p_path),e);
        }
        catch (LexerException e)
        {
            throw new JamonParseException
                (m_templateSource.getExternalIdentifier(p_path),e);
        }
        finally
        {
            reader.close();
        }
    }
}
