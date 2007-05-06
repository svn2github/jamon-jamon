package org.jamon.parser;

import java.io.IOException;

import org.jamon.compiler.ParserErrorImpl;
import org.jamon.compiler.ParserErrorsImpl;
import org.jamon.node.AbstractArgsNode;
import org.jamon.node.ArgNameNode;
import org.jamon.node.ArgTypeNode;
import org.jamon.node.ArgValueNode;
import org.jamon.node.ArgsNode;
import org.jamon.node.OptionalArgNode;

public class ArgsParser extends AbstractArgsParser
{
    public ArgsParser(PositionalPushbackReader p_reader,
            ParserErrorsImpl p_errors,
            org.jamon.api.Location p_tagLocation) throws IOException, ParserErrorImpl
    {
        super(p_reader, p_errors, p_tagLocation);
    }

    public ArgsNode getArgsNode() { return m_argsNode; }

    @Override protected AbstractArgsNode makeArgsNode(org.jamon.api.Location p_tagLocation)
    {
        return m_argsNode = new ArgsNode(p_tagLocation);
    }

    @Override protected String postArgNameTokenError()
    {
        return OptionalValueTagEndDetector.NEED_SEMI_OR_ARROW;
    }

    @Override protected void checkArgsTagEnd() throws IOException
    {
        if (!checkToken("/%args>"))
        {
            addError(m_reader.getLocation(), BAD_ARGS_CLOSE_TAG);
        }
    }

    @Override
    protected boolean handleDefaultValue(
        AbstractArgsNode argsNode, ArgTypeNode argType, ArgNameNode argName)
        throws IOException, ParserErrorImpl
    {
        if (readChar('='))
        {
            readChar('>'); // support old-style syntax
            soakWhitespace();
            org.jamon.api.Location valueLocation = m_reader.getNextLocation();
            argsNode.addArg(new OptionalArgNode(
                argType.getLocation(),
                argType,
                argName,
                new ArgValueNode(valueLocation,
                    readJava(
                        valueLocation,
                        new OptionalValueTagEndDetector()))));
            return true;
        }
        else return false;
    }

    @Override
    protected boolean finishOpenTag(org.jamon.api.Location p_tagLocation) throws IOException
    {
        return checkForTagClosure(p_tagLocation);
    }

    private ArgsNode m_argsNode;
    public static final String EOF_LOOKING_FOR_SEMI =
        "Reached end of file while looking for ';'";
}
