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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.io.InputStream;
import java.io.IOException;

import org.jamon.ParserError;
import org.jamon.ParserErrors;
import org.jamon.TemplateSource;
import org.jamon.node.Location;
import org.jamon.node.TopNode;
import org.jamon.parser.TopLevelParser;
import org.jamon.util.StringUtils;

public class TemplateDescriber
{
    public TemplateDescriber(TemplateSource p_templateSource,
                             ClassLoader p_classLoader)
    {
        m_templateSource = p_templateSource;
        m_classLoader = p_classLoader;
    }

    private final Map<String, TemplateDescription> m_descriptionCache =
        new HashMap<String, TemplateDescription>();
    private final TemplateSource m_templateSource;
    private final ClassLoader m_classLoader;

    public TemplateDescription getTemplateDescription(
        String p_path, Location p_location, String p_templateIdentifier)
        throws IOException, ParserError
    {
        return getTemplateDescription(p_path,
                                      p_location,
                                      p_templateIdentifier,
                                      new HashSet<String>());
    }

    public TemplateDescription getTemplateDescription(
        final String p_path,
        final Location p_location,
        final String p_templateIdentifier,
        final Set<String> p_children)
         throws IOException, ParserError
    {
        if (m_descriptionCache.containsKey(p_path))
        {
            return m_descriptionCache.get(p_path);
        }
        else
        {
            TemplateDescription desc = computeTemplateDescription(
                p_path, p_location, p_templateIdentifier, p_children);
            m_descriptionCache.put(p_path, desc);
            return desc;
        }
    }

    private TemplateDescription computeTemplateDescription(
        final String p_path,
        final Location p_location,
        final String p_templateIdentifier,
        final Set<String> p_children)
        throws IOException, ParserError
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
                 throw new ParserError(
                     p_location,
                     "Unable to find template or class for " + p_path);
             }
             catch (NoSuchFieldException e)
             {
                 throw new ParserError(p_location,
                                       "Malformed class for " + p_path);
             }
             catch (IllegalAccessException e)
             {
                 throw new ParserError(p_location,
                                       "Malformed class for " + p_path);
             }
         }
     }

    public TopNode parseTemplate(String p_path)
        throws IOException
    {
        InputStream stream = m_templateSource.getStreamFor(p_path);
        try
        {
            return
                new TopLevelParser(
                    m_templateSource.getTemplateLocation(p_path),
                    new EncodingReader(stream))
                    .parse()
                    .getRootNode();
        }
        catch (EncodingReader.Exception e)
        {
            throw new ParserErrors(
                new ParserError(
                    new Location(
                        m_templateSource.getTemplateLocation(p_path),
                        1,
                        e.getPos()),
                    e.getMessage()));
        }
        catch (ParserErrors e)
        {
            throw e;
        }
        finally
        {
            stream.close();
        }
    }

    public String getExternalIdentifier(String p_path)
    {
        return m_templateSource.getExternalIdentifier(p_path);
    }
}
