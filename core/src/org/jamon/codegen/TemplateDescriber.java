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
 * Contributor(s):
 */

package org.jamon.codegen;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.PushbackReader;

import org.jamon.JamonException;
import org.jamon.JamonParseException;
import org.jamon.util.StringUtils;
import org.jamon.node.Start;
import org.jamon.parser.Parser;
import org.jamon.parser.ParserException;
import org.jamon.lexer.Lexer;
import org.jamon.lexer.LexerException;

public class TemplateDescriber
{
    public TemplateDescriber(File p_templateSourceDir)
    {
        m_templateSourceDir = p_templateSourceDir;
    }

    private final File m_templateSourceDir;

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
                Map args = (Map)
                    Class.forName(StringUtils.templatePathToClassName(p_path))
                    .getField("FARGINFO_"+p_fargName)
                    .get(null);
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
            return new BaseAnalyzer(parseTemplate(p_path))
                .getUnitInfo().getFargNames();
        }
        catch (FileNotFoundException fnfe)
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
        catch (IOException e)
        {

            throw new JamonException(e);
        }
    }

    public File getTemplateFile(String p_path)
    {
        return new File(m_templateSourceDir,
                        templatePathToFilePath(p_path));
    }

    private String templatePathToFilePath(String p_path)
    {
        StringTokenizer tokenizer = new StringTokenizer(p_path, "/");
        StringBuffer path = new StringBuffer(p_path.length());
        while (tokenizer.hasMoreTokens())
        {
            path.append(tokenizer.nextToken());
            if (tokenizer.hasMoreTokens())
            {
                path.append(File.separator);
            }
        }
        return path.toString();
    }

    public List getRequiredArgNames(final String p_path)
        throws JamonException
    {
        try
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
        catch (FileNotFoundException fnfe)
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
        catch (IOException e)
        {
            throw new JamonException(e);
        }
    }

    public Start parseTemplate(String p_path)
        throws IOException
    {
        File file = getTemplateFile(p_path);
        FileReader reader = new FileReader(file);
        try
        {
            return new Parser(new Lexer
                              (new PushbackReader
                               (reader, 1024)))
                .parse();
        }
        catch (ParserException e)
        {
            throw new JamonParseException(file,e);
        }
        catch (LexerException e)
        {
            throw new JamonParseException(file,e);
        }
        finally
        {
            reader.close();
        }
    }
}
