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
 * The Initial Developer of the Original Code is Ian Robertson.  Portions
 * created by Ian Robertson are Copyright (C) 2005 Ian Robertson.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */

package org.jamon.parser;

import java.io.IOException;
import java.io.Reader;

import org.jamon.api.TemplateLocation;
import org.jamon.codegen.AnnotationType;
import org.jamon.compiler.ParserErrorImpl;
import org.jamon.compiler.ParserErrorsImpl;
import org.jamon.node.AbsMethodNode;
import org.jamon.node.AbstractPathNode;
import org.jamon.node.AliasDefNode;
import org.jamon.node.AliasesNode;
import org.jamon.node.AnnotationNode;
import org.jamon.node.ClassNode;
import org.jamon.node.EscapeDirectiveNode;
import org.jamon.node.ExtendsNode;
import org.jamon.node.ImplementNode;
import org.jamon.node.ImplementsNode;
import org.jamon.node.ReplacesNode;
import org.jamon.node.ImportsNode;
import org.jamon.node.LocationImpl;
import org.jamon.node.ParentMarkerNode;
import org.jamon.node.TopNode;

public class TopLevelParser extends AbstractBodyParser<TopNode>
{
    public static final String BAD_ABSMETH_CONTENT =
        "<%absmeth> sections can only contain <%args> and <%frag> blocks";
    public static final String EXPECTING_SEMI = "Expecting ';'";
    public static final String EXPECTING_ARROW = "Expecting '=' or '=>'";
    public static final String MALFORMED_EXTENDS_TAG_ERROR =
        "Malformed <%extends ...> tag";
    public static final String MALFORMED_REPLACES_TAG_ERROR =
        "Malformed <%replaces ...> tag";
    public static final String MALFORMED_ANNOTATE_TAG_ERROR =
        "Malformed <%annotate...> tag";
    public static final String UNRECOGNIZED_ANNOTATION_TYPE_ERROR =
        "Unrecognized annotation type";
    private static final String BAD_ALIASES_CLOSE_TAG =
        "Malformed </%alias> tag";
    private static final String BAD_ABS_METHOD_CLOSE_TAG =
        "Malformed </%absmeth> tag";
    public static final String EXPECTING_IMPLEMENTS_CLOSE =
        "Expecting class name or </%implements>";
    public static final String EXPECTING_IMPORTS_CLOSE =
        "Expecting import or </%import>";

    public TopLevelParser(TemplateLocation p_location, Reader p_reader)
    {
        super(new TopNode(new LocationImpl(p_location, 1, 1)),
              new PositionalPushbackReader(p_location, p_reader, 2),
              new ParserErrorsImpl());
    }

    @Override public AbstractBodyParser<TopNode> parse() throws IOException
    {
        super.parse();
        if (m_errors.hasErrors())
        {
            throw m_errors;
        }
        return this;
    }

    @Override
    protected void handleMethodTag(org.jamon.api.Location p_tagLocation) throws IOException
    {
        if (soakWhitespace())
        {
            String name = readIdentifier(true);
            if (checkForTagClosure(p_tagLocation))
            {
                m_root.addSubNode(
                    new MethodParser(name, p_tagLocation, m_reader, m_errors)
                        .parse()
                        .getRootNode());
            }
        }
        else
        {
            addError(p_tagLocation, "malformed <%method methodName> tag");
        }
    }

    @Override
    protected void handleOverrideTag(org.jamon.api.Location p_tagLocation) throws IOException
    {
        if (soakWhitespace())
        {
            String name = readIdentifier(true);
            if (checkForTagClosure(p_tagLocation))
            {
                m_root.addSubNode(
                    new OverrideParser(name, p_tagLocation, m_reader, m_errors)
                        .parse()
                        .getRootNode());
            }
        }
        else
        {
            addError(p_tagLocation, "malformed <%override methodName> tag");
        }
    }

    @Override
    protected void handleDefTag(org.jamon.api.Location p_tagLocation) throws IOException
    {
        if (soakWhitespace())
        {
            String name = readIdentifier(true);
            if (checkForTagClosure(p_tagLocation))
            {
                m_root.addSubNode(
                    new DefParser(name, p_tagLocation, m_reader, m_errors)
                        .parse()
                        .getRootNode());
            }
        }
        else
        {
            addError(p_tagLocation, "malformed <%def defName> tag");
        }
    }

    @Override protected void handleClassTag(org.jamon.api.Location p_tagLocation)
        throws IOException
    {
        if (checkForTagClosure(p_tagLocation))
        {
            m_root.addSubNode(
                new ClassNode(
                    p_tagLocation,
                    readUntil("</%class>", p_tagLocation)));
            soakWhitespace();
        }
    }

    @Override protected void handleExtendsTag(org.jamon.api.Location p_tagLocation)
        throws IOException
    {
        if(soakWhitespace())
        {
            m_root.addSubNode(
                new ExtendsNode(p_tagLocation, parsePath()));
            soakWhitespace();
            checkForTagClosure(m_reader.getLocation());
            soakWhitespace();
        }
        else
        {
            addError(p_tagLocation, MALFORMED_EXTENDS_TAG_ERROR);
        }
    }

    @Override protected void handleReplacesTag(org.jamon.api.Location p_tagLocation)
    throws IOException
    {
      if(soakWhitespace())
      {
        m_root.addSubNode(
          new ReplacesNode(p_tagLocation, parsePath()));
        soakWhitespace();
        checkForTagClosure(m_reader.getLocation());
        soakWhitespace();
      }
      else
      {
        addError(p_tagLocation, MALFORMED_REPLACES_TAG_ERROR);
      }
    }

    @Override protected void handleImplementsTag(org.jamon.api.Location p_tagLocation)
        throws IOException
    {
        if (checkForTagClosure(p_tagLocation))
        {
            ImplementsNode implementsNode = new ImplementsNode(p_tagLocation);
            m_root.addSubNode(implementsNode);
            while(true)
            {
                soakWhitespace();
                org.jamon.api.Location location = m_reader.getNextLocation();
                if (readChar('<'))
                {
                    if (!checkToken("/%implements>"))
                    {
                        addError(location, EXPECTING_IMPLEMENTS_CLOSE);
                    }
                    soakWhitespace();
                    return;
                }
                String className =
                    readClassName(m_reader.getCurrentNodeLocation());
                if (className.length() == 0)
                {
                    addError(location, EXPECTING_IMPLEMENTS_CLOSE);
                    return;
                }
                if (!readChar(';'))
                {
                    addError(m_reader.getNextLocation(), EXPECTING_SEMI);
                }
                implementsNode.addImplement(
                    new ImplementNode(location, className));
            }
        }
    }

    @Override protected void handleImportTag(org.jamon.api.Location p_tagLocation)
        throws IOException
    {
        if (checkForTagClosure(p_tagLocation))
        {
            ImportsNode importsNode = new ImportsNode(p_tagLocation);
            m_root.addSubNode(importsNode);
            while(true)
            {
                soakWhitespace();
                org.jamon.api.Location location = m_reader.getNextLocation();
                if (readChar('<'))
                {
                    if (!checkToken("/%import>"))
                    {
                        addError(location, EXPECTING_IMPORTS_CLOSE);
                    }
                    soakWhitespace();
                    return;
                }
                try
                {
                    importsNode.addImport(
                        new ImportParser(m_reader, m_errors).parse().getNode());
                }
                catch (ParserErrorImpl e)
                {
                    addError(e);
                    addError(m_reader.getLocation(), EXPECTING_IMPORTS_CLOSE);
                    return;
                }
                soakWhitespace();
                if (!readChar(';'))
                {
                    addError(m_reader.getNextLocation(), EXPECTING_SEMI);
                }

            }
        }
    }


    @Override protected void handleAliasesTag(org.jamon.api.Location p_tagLocation)
        throws IOException
    {
        checkForTagClosure(p_tagLocation);
        AliasesNode aliases = new AliasesNode(p_tagLocation);
        m_root.addSubNode(aliases);
        while(true)
        {
            soakWhitespace();
            m_reader.markNodeEnd();
            if (readChar('<'))
            {
                if (!checkToken("/%alias>"))
                {
                    addError(m_reader.getLocation(), BAD_ALIASES_CLOSE_TAG);
                }
                soakWhitespace();
                return;
            }
            String name = readChar('/') ? "/" : readIdentifier(false);
            if (name.length() == 0)
            {
                addError(m_reader.getCurrentNodeLocation(),
                         "Alias name expected");
                return;
            }
            soakWhitespace();
            if (readChar('='))
            {
                readChar('>'); // support old-style syntax
                soakWhitespace();
                AbstractPathNode path = parsePath();
                if (path.getPathElements().isEmpty())
                {
                    return;
                }
                aliases.addAlias(new AliasDefNode(
                    m_reader.getCurrentNodeLocation(),
                    name,
                    path));
                if (!readChar(';'))
                {
                    addError(m_reader.getLocation(), EXPECTING_SEMI);
                }
            }
            else
            {
                addError(m_reader.getLocation(), EXPECTING_ARROW);
            }
        }
    }


    @Override protected void handleAbsMethodTag(org.jamon.api.Location p_tagLocation)
        throws IOException
    {
        if (soakWhitespace())
        {
            String name = readIdentifier(true);
            checkForTagClosure(p_tagLocation);
            AbsMethodNode absMethodNode =
                new AbsMethodNode(p_tagLocation, name);
            m_root.addSubNode(absMethodNode);
            while(true)
            {
                soakWhitespace();
                m_reader.markNodeEnd();
                if (readChar('<'))
                {
                    if (readChar('%'))
                    {
                        String tagName = readTagName();
                        if ("args".equals(tagName))
                        {
                            try
                            {
                                absMethodNode.addArgsBlock(
                                    new ArgsParser(
                                        m_reader, m_errors,
                                        m_reader.getCurrentNodeLocation())
                                        .getArgsNode());
                            }
                            catch (ParserErrorImpl e)
                            {
                                addError(e);
                            }
                        }
                        else if ("frag".equals(tagName))
                        {
                            try
                            {
                                absMethodNode.addArgsBlock(
                                    new FragmentArgsParser(
                                        m_reader, m_errors,
                                        m_reader.getCurrentNodeLocation())
                                        .getFragmentArgsNode());
                            }
                            catch (ParserErrorImpl e)
                            {
                                addError(e);
                            }
                        }
                        else
                        {
                            addError(m_reader.getLocation(),
                                     BAD_ABSMETH_CONTENT);
                            return;
                        }
                    }
                    else
                    {
                        if (!checkToken("/%absmeth>"))
                        {
                            addError(m_reader.getLocation(),
                                     BAD_ABS_METHOD_CLOSE_TAG);
                        }
                        soakWhitespace();
                        return;
                    }
                }
                else
                {
                    addError(m_reader.getLocation(), BAD_ABSMETH_CONTENT);
                    return;
                }
            }
        }
        else
        {
            addError(m_reader.getLocation(),
                     "malformed <%absmeth methodName> tag");
        }
    }



    @Override protected void handleParentArgsNode(org.jamon.api.Location p_tagLocation)
            throws IOException
    {
        m_root.addSubNode(
            new ParentArgsParser(m_reader, m_errors, p_tagLocation)
                .getParentArgsNode());
    }



    @Override protected void handleParentMarkerTag(org.jamon.api.Location p_tagLocation)
        throws IOException
    {
        if (checkForTagClosure(p_tagLocation))
        {
            m_root.addSubNode(new ParentMarkerNode(p_tagLocation));
            soakWhitespace();
        }
    }

    @Override protected void handleEof()
    {
        // end of file is a fine thing at the top level
    }

    @Override protected void handleEscapeTag(org.jamon.api.Location p_tagLocation)
        throws IOException
    {
        soakWhitespace();
        if (!readChar('#'))
        {
            addError(m_reader.getNextLocation(), "Expecting '#'");
        }
        else
        {
            soakWhitespace();
            int c = m_reader.read();
            if (Character.isLetter((char) c))
            {
                m_root.addSubNode(
                    new EscapeDirectiveNode(p_tagLocation,
                                            new String(new char[] {(char) c})));
            }
            else
            {
                addError(m_reader.getLocation(), "Expecting a letter");
            }
            soakWhitespace();
            checkForTagClosure(p_tagLocation);
        }
        soakWhitespace();
    }

    @Override protected void handleGenericTag(org.jamon.api.Location p_tagLocation)
        throws IOException
    {
        m_root.addSubNode(new GenericsParser(m_reader, m_errors, p_tagLocation)
            .getGenericsNode());
    }

    @Override protected void handleAnnotationTag(org.jamon.api.Location p_tagLocation) throws IOException
    {
        if(soakWhitespace())
        {
            try
            {
                HashEndDetector detector = new HashEndDetector();
                String annotations = readJava(p_tagLocation, detector);
                AnnotationType annotationType;
                if (detector.endedWithHash())
                {
                    annotationType = readAnnotationType();
                    soakWhitespace();
                    if (!(readChar('%') && readChar('>')))
                    {
                        throw new ParserErrorImpl(p_tagLocation, MALFORMED_ANNOTATE_TAG_ERROR);
                    }
                }
                else
                {
                    annotationType = AnnotationType.BOTH;
                }
                m_root.addSubNode(new AnnotationNode(p_tagLocation, annotations, annotationType));

            }
            catch (ParserErrorImpl e)
            {
                addError(e);
            }
            soakWhitespace();
        }
        else
        {
            addError(p_tagLocation, MALFORMED_ANNOTATE_TAG_ERROR);
        }
    }

    private AnnotationType readAnnotationType() throws IOException, ParserErrorImpl
    {
        org.jamon.api.Location location = m_reader.getLocation();
        if (readChar('p'))
        {
            if (checkToken("roxy"))
            {
                return AnnotationType.PROXY;
            }
        }
        else if (readChar('i'))
        {
            if (checkToken("mpl"))
            {
                return AnnotationType.IMPL;
            }
        }
        throw new ParserErrorImpl(location, UNRECOGNIZED_ANNOTATION_TYPE_ERROR);
    }

    @Override protected boolean isTopLevel()
    {
        return true;
    }
}

