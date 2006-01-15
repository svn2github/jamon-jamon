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

import org.jamon.ParserError;
import org.jamon.ParserErrors;
import org.jamon.TemplateLocation;
import org.jamon.node.AbsMethodNode;
import org.jamon.node.AliasDefNode;
import org.jamon.node.AliasesNode;
import org.jamon.node.ClassNode;
import org.jamon.node.EscapeDirectiveNode;
import org.jamon.node.ExtendsNode;
import org.jamon.node.ImplementNode;
import org.jamon.node.ImplementsNode;
import org.jamon.node.ImportNode;
import org.jamon.node.ImportsNode;
import org.jamon.node.Location;
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
    private static final String BAD_ALIASES_CLOSE_TAG =
        "Malformed </%alias> tag";
    private static final String BAD_ABS_METHOD_CLOSE_TAG =
        "Malformed </%absmeth> tag";
    public static final String EXPECTING_IMPLEMENTS_CLOSE =
        "Expecting class name or </%implements>";
    public static final String EXPECTING_IMPORTS_CLOSE =
        "Expecting import or </%import>";

    public TopLevelParser(TemplateLocation p_location, Reader p_reader)
        throws IOException
    {
        super(new TopNode(new Location(p_location, 1, 1)),
              new PositionalPushbackReader(p_location, p_reader, 2),
              new ParserErrors());
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
    protected void handleMethodTag(Location p_tagLocation) throws IOException
    {
        if (soakWhitespace())
        {
            String name = readIdentifier();
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
    protected void handleOverrideTag(Location p_tagLocation) throws IOException
    {
        if (soakWhitespace())
        {
            String name = readIdentifier();
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
    protected void handleDefTag(Location p_tagLocation) throws IOException
    {
        if (soakWhitespace())
        {
            String name = readIdentifier();
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

    @Override protected void handleClassTag(Location p_tagLocation)
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

    @Override protected void handleExtendsTag(Location p_tagLocation)
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

    @Override protected void handleImplementsTag(Location p_tagLocation)
        throws IOException
    {
        if (checkForTagClosure(p_tagLocation))
        {
            ImplementsNode implementsNode = new ImplementsNode(p_tagLocation);
            m_root.addSubNode(implementsNode);
            while(true)
            {
                soakWhitespace();
                Location location = m_reader.getNextLocation();
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

    @Override protected void handleImportTag(Location p_tagLocation)
        throws IOException
    {
        if (checkForTagClosure(p_tagLocation))
        {
            ImportsNode importsNode = new ImportsNode(p_tagLocation);
            m_root.addSubNode(importsNode);
            while(true)
            {
                soakWhitespace();
                Location location = m_reader.getNextLocation();
                if (readChar('<'))
                {
                    if (!checkToken("/%import>"))
                    {
                        addError(location, EXPECTING_IMPORTS_CLOSE);
                    }
                    soakWhitespace();
                    return;
                }
                String className =
                    readImport(m_reader.getCurrentNodeLocation());
                if (className.length() == 0)
                {
                    addError(location, EXPECTING_IMPORTS_CLOSE);
                    return;
                }
                soakWhitespace();
                if (!readChar(';'))
                {
                    addError(m_reader.getNextLocation(), EXPECTING_SEMI);
                }
                importsNode.addImport(new ImportNode(location, className));
            }
        }
    }


    @Override protected void handleAliasesTag(Location p_tagLocation)
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
            String name = readChar('/') ? "/" : readIdentifier();
            if (name.length() == 0)
            {
                addError(m_reader.getCurrentNodeLocation(),
                         "Identifier expected");
            }
            soakWhitespace();
            if (readChar('='))
            {
                readChar('>'); // support old-style syntax
                soakWhitespace();
                aliases.addAlias(new AliasDefNode(
                    m_reader.getCurrentNodeLocation(),
                    name,
                    parsePath()));
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


    @Override protected void handleAbsMethodTag(Location p_tagLocation)
        throws IOException
    {
        if (soakWhitespace())
        {
            String name = readIdentifier();
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
                            catch (ParserError e)
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
                            catch (ParserError e)
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



    @Override protected void handleParentArgsNode(Location p_tagLocation)
            throws IOException
    {
        m_root.addSubNode(
            new ParentArgsParser(m_reader, m_errors, p_tagLocation)
                .getParentArgsNode());
    }



    @Override protected void handleParentMarkerTag(Location p_tagLocation)
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

    @Override protected void handleEscapeTag(Location p_tagLocation)
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

    @Override protected void handleGenericTag(Location p_tagLocation)
        throws IOException
    {
        m_root.addSubNode(new GenericsParser(m_reader, m_errors, p_tagLocation)
            .getGenericsNode());
    }

    @Override protected boolean isTopLevel()
    {
        return true;
    }
}
