package org.jamon.parser;

import java.io.IOException;

import org.jamon.api.Location;
import org.jamon.compiler.ParserErrorImpl;
import org.jamon.compiler.ParserErrorsImpl;
import org.jamon.node.AbstractArgsNode;
import org.jamon.node.ArgNameNode;
import org.jamon.node.ArgTypeNode;
import org.jamon.node.ArgValueNode;
import org.jamon.node.ArgsNode;
import org.jamon.node.OptionalArgNode;

public class ArgsParser extends AbstractArgsParser {
  public ArgsParser(
    PositionalPushbackReader reader, ParserErrorsImpl errors, Location tagLocation)
  throws IOException, ParserErrorImpl {
    super(reader, errors, tagLocation);
  }

  public ArgsNode getArgsNode() {
    return argsNode;
  }

  @Override
  protected AbstractArgsNode makeArgsNode(Location tagLocation) {
    return argsNode = new ArgsNode(tagLocation);
  }

  @Override
  protected String postArgNameTokenError() {
    return OptionalValueTagEndDetector.NEED_SEMI_OR_ARROW;
  }

  @Override
  protected void checkArgsTagEnd() throws IOException {
    if (!checkToken("/%args>")) {
      addError(reader.getLocation(), BAD_ARGS_CLOSE_TAG);
    }
  }

  @Override
  protected boolean handleDefaultValue(
    AbstractArgsNode argsNode, ArgTypeNode argType, ArgNameNode argName)
  throws IOException, ParserErrorImpl {
    if (readChar('=')) {
      readChar('>'); // support old-style syntax
      soakWhitespace();
      Location valueLocation = reader.getNextLocation();
      argsNode.addArg(new OptionalArgNode(argType.getLocation(), argType, argName,
          new ArgValueNode(valueLocation,
              readJava(valueLocation, new OptionalValueTagEndDetector()))));
      return true;
    }
    else
      return false;
  }

  @Override
  protected boolean finishOpenTag(Location tagLocation) throws IOException {
    return checkForTagClosure(tagLocation);
  }

  private ArgsNode argsNode;

  public static final String EOF_LOOKING_FOR_SEMI = "Reached end of file while looking for ';'";
}
