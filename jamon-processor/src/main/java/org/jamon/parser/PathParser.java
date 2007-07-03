package org.jamon.parser;

import java.io.IOException;

import org.jamon.compiler.ParserErrorsImpl;
import org.jamon.node.AbsolutePathNode;
import org.jamon.node.AbstractPathNode;
import org.jamon.node.NamedAliasPathNode;
import org.jamon.node.PathElementNode;
import org.jamon.node.RelativePathNode;
import org.jamon.node.RootAliasPathNode;
import org.jamon.node.UpdirNode;

/**
 * @author ian
 **/
public class PathParser extends AbstractParser
{
    public static final String GENERIC_PATH_ERROR = "Malformed path";

    public PathParser(PositionalPushbackReader p_reader, ParserErrorsImpl p_errors)
        throws IOException
    {
        super(p_reader, p_errors);
        m_path = parse();
    }

    public AbstractPathNode getPathNode()
    {
        return m_path;
    }

    private AbstractPathNode parse() throws IOException
    {
        AbstractPathNode path;
        org.jamon.api.Location location = m_reader.getNextLocation();
        int c = m_reader.read();
        switch (c)
        {
            case '/' :
                path =
                    readChar('/')
                        ? (AbstractPathNode) new RootAliasPathNode(location)
                        : (AbstractPathNode) new AbsolutePathNode(location);
                addToPath(path, false);
                break;
            case '.' :
                m_reader.unread(c);
                path = new RelativePathNode(location);
                addToPath(path, true);
                break;
            default :
                if (Character.isJavaIdentifierStart((char) c))
                {
                    m_reader.unread(c);
                    String identifier = readIdentifier(true);
                    if ((c = m_reader.read()) == '/')
                    {
                        if ((c = m_reader.read()) == '/')
                        {
                            path = new NamedAliasPathNode(location, identifier);
                            addToPath(path, false);
                        }
                        else
                        {
                            m_reader.unread(c);
                            path =
                                new RelativePathNode(location).addPathElement(
                                    new PathElementNode(location, identifier));
                            addToPath(path, false);
                        }
                    }
                    else
                    {
                        m_reader.unread(c);
                        path =
                            new RelativePathNode(location).addPathElement(
                                new PathElementNode(location, identifier));
                    }
                }
                else
                {
                    addError(location, GENERIC_PATH_ERROR);
                    path = new RelativePathNode(location);
                }
                break;
        }
        return path;
    }

    private void addToPath(AbstractPathNode p_path, boolean p_updirsAllowed)
        throws IOException
    {
        int c;
        StringBuilder identifier = new StringBuilder();
        boolean identStart = true;
        boolean updirsAllowed = p_updirsAllowed;
        org.jamon.api.Location location = m_reader.getNextLocation();
        while ((c = m_reader.read()) >= 0)
        {
            if (c == '/')
            {
                if (identStart)
                {
                    addError(location, GENERIC_PATH_ERROR);
                    return;
                }
                else
                {
                    p_path.addPathElement(
                        new PathElementNode(location, identifier.toString()));
                    identifier = new StringBuilder();
                    identStart = true;
                    updirsAllowed = false;
                    location = m_reader.getNextLocation();
                }
            }
            else if (c == '.')
            {
                if (updirsAllowed)
                {
                    if (m_reader.read() == '.')
                    {
                        p_path.addPathElement(new UpdirNode(location));
                        identifier = new StringBuilder();
                        identStart = true;
                        if ((c = m_reader.read()) == '/')
                        {
                            location = m_reader.getNextLocation();
                        }
                        else
                        {
                            if (Character.isJavaIdentifierPart((char) c))
                            {
                                addError(location, GENERIC_PATH_ERROR);
                                return;
                            }
                        }
                    }
                    else
                    {
                        addError(location, GENERIC_PATH_ERROR);
                        return;
                    }
                }
                else
                {
                    addError(location, GENERIC_PATH_ERROR);
                    return;
                }
            }
            else if (
                identStart
                    ? Character.isJavaIdentifierStart((char) c)
                    : Character.isJavaIdentifierPart((char) c))
            {
                identStart = false;
                identifier.append((char) c);
            }
            else
            {
                m_reader.unread(c);
                break;
            }
        }
        if (!identStart)
        {
            p_path.addPathElement(
                new PathElementNode(location, identifier.toString()));
        }
        else
        {
            addError(m_reader.getCurrentNodeLocation(), GENERIC_PATH_ERROR);
        }
    }

    private final AbstractPathNode m_path;
}
