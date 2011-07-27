package org.jamon.parser;

import java.io.IOException;

import org.jamon.api.Location;
import org.jamon.compiler.ParserErrorImpl;
import org.jamon.compiler.ParserErrorsImpl;
import org.jamon.node.AbstractArgsNode;
import org.jamon.node.ArgNameNode;
import org.jamon.node.ArgNode;
import org.jamon.node.ArgTypeNode;

public abstract class AbstractArgsParser extends AbstractParser {
  public AbstractArgsParser(PositionalPushbackReader reader, ParserErrorsImpl errors,
      Location tagLocation) throws IOException, ParserErrorImpl {
    super(reader, errors);

    if (finishOpenTag(tagLocation)) {
      AbstractArgsNode argsNode = makeArgsNode(tagLocation);
      while (true) {
        soakWhitespace();
        if (readChar('<')) {
          checkArgsTagEnd();
          soakWhitespace();
          return;
        }
        ArgTypeNode argType = readArgType();
        soakWhitespace();
        reader.markNodeEnd();

        ArgNameNode argName = new ArgNameNode(reader.getCurrentNodeLocation(),
            readIdentifier(true));
        soakWhitespace();
        if (readChar(';')) {
          argsNode.addArg(new ArgNode(argType.getLocation(), argType, argName));
        }
        else if (!handleDefaultValue(argsNode, argType, argName)) {
          throw new ParserErrorImpl(reader.getNextLocation(), postArgNameTokenError());
        }
      }
    }
  }

  private ArgTypeNode readArgType() throws IOException {
    final Location location = reader.getNextLocation();
    return new ArgTypeNode(location, readType(location));
  }

  /**
   * Finish processing the opening tag.
   *
   * @return true if there is more to process
   */
  protected abstract boolean finishOpenTag(Location tagLocation) throws IOException;

  /**
   * Handle a default value for an arg; returns true if there is one.
   *
   * @param argsNode The parent node for the argument
   * @param argType The argument type
   * @param argName The argument name
   * @return true if there was a default value
   * @throws IOException
   * @throws ParserErrorImpl
   */
  protected abstract boolean handleDefaultValue(AbstractArgsNode argsNode, ArgTypeNode argType,
    ArgNameNode argName) throws IOException, ParserErrorImpl;

  protected abstract void checkArgsTagEnd() throws IOException;

  protected abstract String postArgNameTokenError();

  protected abstract AbstractArgsNode makeArgsNode(Location tagLocation);
}
