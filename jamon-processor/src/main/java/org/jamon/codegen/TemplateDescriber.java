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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import org.jamon.api.Location;
import org.jamon.api.TemplateLocation;
import org.jamon.api.TemplateSource;
import org.jamon.compiler.ParserErrorImpl;
import org.jamon.compiler.ParserErrorsImpl;
import org.jamon.emit.EmitMode;
import org.jamon.node.LocationImpl;
import org.jamon.node.TopNode;
import org.jamon.parser.TopLevelParser;
import org.jamon.util.StringUtils;

public class TemplateDescriber {
  private static final String JAMON_CONTEXT_TYPE_KEY = "org.jamon.contextType";

  private static final String EMIT_MODE_KEY = "org.jamon.emitMode";

  private static final String ESCAPING_KEY = "org.jamon.escape";

  public TemplateDescriber(TemplateSource templateSource, ClassLoader classLoader) {
    this.templateSource = templateSource;
    this.classLoader = classLoader;
  }

  private final Map<String, TemplateDescription> m_descriptionCache =
    new HashMap<String, TemplateDescription>();

  private final TemplateSource templateSource;

  private final ClassLoader classLoader;

  public TemplateDescription getTemplateDescription(String path, Location location)
  throws IOException, ParserErrorImpl {
    return getTemplateDescription(path, location, new HashSet<String>());
  }

  public TemplateDescription getTemplateDescription(
    final String path, final Location location, final Set<String> children) throws IOException,
    ParserErrorImpl {
    if (m_descriptionCache.containsKey(path)) {
      return m_descriptionCache.get(path);
    }
    else {
      TemplateDescription desc = computeTemplateDescription(path, location, children);
      m_descriptionCache.put(path, desc);
      return desc;
    }
  }

  private TemplateDescription computeTemplateDescription(
    final String path, final Location location, final Set<String> children)
  throws IOException, ParserErrorImpl {
    if (templateSource.available(path)) {
      return new TemplateDescription(new Analyzer(path, this, children).analyze());
    }
    else {
      try {
        return new TemplateDescription(
          classLoader.loadClass(StringUtils.templatePathToClassName(path)));
      }
      catch (ClassNotFoundException e) {
        throw new ParserErrorImpl(location, "Unable to find template or class for " + path);
      }
      catch (NoSuchFieldException e) {
        throw new ParserErrorImpl(location, "Malformed class for " + path);
      }
    }
  }

  public TopNode parseTemplate(String path) throws IOException {
    InputStream stream = templateSource.getStreamFor(path);
    try {
      EncodingReader reader = new EncodingReader(stream);
      return new TopLevelParser(templateSource.getTemplateLocation(path), reader, reader
          .getEncoding()).parse().getRootNode();
    }
    catch (EncodingReader.Exception e) {
      throw new ParserErrorsImpl(
        new ParserErrorImpl(new LocationImpl(
          templateSource.getTemplateLocation(path), 1, e.getPos()),
        e.getMessage()));
    }
    catch (ParserErrorsImpl e) {
      throw e;
    }
    finally {
      stream.close();
    }
  }

  public TemplateLocation getTemplateLocation(String path) {
    return templateSource.getTemplateLocation(path);
  }

  public String getExternalIdentifier(String path) {
    return templateSource.getExternalIdentifier(path);
  }

  private Properties getProperties(String path) throws IOException {
    StringTokenizer tokenizer = new StringTokenizer(path, "/");
    StringBuffer partialPath = new StringBuffer("/");
    Properties properties = new Properties();
    while (tokenizer.hasMoreTokens()) {
      templateSource.loadProperties(partialPath.toString(), properties);
      String nextComponent = tokenizer.nextToken();
      if (tokenizer.hasMoreTokens()) // still talking directories
      {
        partialPath.append(nextComponent);
        partialPath.append("/");
      }
    }
    return properties;
  }

  private static final String ALIAS_PROPERTY_PREFIX = "org.jamon.alias.";

  public Map<String, String> getAliases(String path) throws IOException {
    Map<String, String> result = new HashMap<String, String>();
    Properties props = getProperties(path);
    for (Object okey : props.keySet()) {
      String key = (String) okey;
      if (key.startsWith(ALIAS_PROPERTY_PREFIX)) {
        String aliasName = key.substring(ALIAS_PROPERTY_PREFIX.length());
        String alias = props.getProperty(key);
        if (alias == null || alias.trim().length() == 0) {
          result.remove(aliasName);
        }
        else {
          result.put(aliasName, props.getProperty(key));
        }
      }
    }
    return result;
  }

  public String getJamonContextType(String path) throws IOException {
    String contextType = getProperties(path).getProperty(JAMON_CONTEXT_TYPE_KEY, "").trim();
    return contextType.length() > 0
        ? contextType
        : null;
  }

  public EmitMode getEmitMode(String path) throws IOException {
    String emitModeName = getProperties(path).getProperty(EMIT_MODE_KEY);
    if (emitModeName != null) {
      EmitMode emitMode = EmitMode.fromString(emitModeName);
      if (emitMode == null) {
        throw new RuntimeException("Unknown emit mode: " + emitModeName);
      }
      return emitMode;
    }
    else {
      return EmitMode.STANDARD;
    }
  }

  /**
   * Get The {@code EscapingDirective} specified by jamon.properties for a path. Returns null if no
   * directive is specified.
   *
   * @param path the path
   * @return the {@code EscapingDirective} specified by jamon.properties.
   * @throws IOException
   */
  public EscapingDirective getEscaping(String path) throws IOException {
    return EscapingDirective.get(getProperties(path).getProperty(ESCAPING_KEY));
  }
}
