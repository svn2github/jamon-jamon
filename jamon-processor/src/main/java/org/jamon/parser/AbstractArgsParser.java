package org.jamon.parser;

import java.io.IOException;

import org.jamon.compiler.ParserErrorImpl;
import org.jamon.compiler.ParserErrorsImpl;
import org.jamon.node.AbstractArgsNode;
import org.jamon.node.ArgNameNode;
import org.jamon.node.ArgNode;
import org.jamon.node.ArgTypeNode;

public abstract class AbstractArgsParser extends AbstractParser
{
    public AbstractArgsParser(PositionalPushbackReader p_reader,
                              ParserErrorsImpl p_errors,
                              org.jamon.api.Location p_tagLocation)
        throws IOException, ParserErrorImpl
    {
        super(p_reader, p_errors);

        if (finishOpenTag(p_tagLocation))
        {
            AbstractArgsNode argsNode = makeArgsNode(p_tagLocation);
            while (true)
            {
                soakWhitespace();
                if (readChar('<'))
                {
                    checkArgsTagEnd();
                    soakWhitespace();
                    return;
                }
                ArgTypeNode argType = readArgType();
                soakWhitespace();
                m_reader.markNodeEnd();

                ArgNameNode argName =
                    new ArgNameNode(
                        m_reader.getCurrentNodeLocation(),
                        readIdentifier(true));
                soakWhitespace();
                if (readChar(';'))
                {
                    argsNode.addArg(
                        new ArgNode(argType.getLocation(), argType, argName));
                }
                else if (!handleDefaultValue(argsNode, argType, argName))
                {
                    throw new ParserErrorImpl(
                        m_reader.getNextLocation(), postArgNameTokenError());
                }
            }
        }
    }

    private ArgTypeNode readArgType() throws IOException
    {
        final org.jamon.api.Location location = m_reader.getNextLocation();
        return new ArgTypeNode(location, readType(location));
    }

    /**
     * Finish processing the opening tag.
     * @return true if there is more to process
     */
    protected abstract boolean finishOpenTag(org.jamon.api.Location p_tagLocation) throws IOException;

    /**
     * Handle a default value for an arg; returns true if there is one. 
     * @param argsNode The parent node for the argument
     * @param argType The argument type
     * @param argName The argument name
     * @return true if there was a default value
     * @throws IOException
     * @throws ParserErrorImpl
     */
    protected abstract boolean handleDefaultValue(AbstractArgsNode argsNode,
                                                  ArgTypeNode argType,
                                                  ArgNameNode argName)
        throws IOException, ParserErrorImpl;

    protected abstract void checkArgsTagEnd() throws IOException;

    protected abstract String postArgNameTokenError();

    protected abstract AbstractArgsNode makeArgsNode(org.jamon.api.Location p_tagLocation);}
