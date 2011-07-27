package org.jamon.compiler;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.jamon.api.Location;
import org.jamon.api.ParserError;
import org.jamon.api.ParserErrors;

/**
 * @author ian
 **/
public final class ParserErrorsImpl extends IOException implements ParserErrors {
  public ParserErrorsImpl() {}

  public ParserErrorsImpl(ParserError p_error) {
    errors.add(p_error);
  }

  @Override
  public boolean hasErrors() {
    return !errors.isEmpty();
  }

  @Override
  public List<ParserError> getErrors() {
    return errors;
  }

  public void addError(ParserError p_error) {
    errors.add(p_error);
  }

  public void addError(String message, Location location) {
    addError(new ParserErrorImpl(location, message));
  }

  public void addErrors(ParserErrorsImpl p_errors) {
    errors.addAll(p_errors.errors);
  }

  private final List<ParserError> errors = new ArrayList<ParserError>();

  @Override
  public String getMessage() {
    StringBuilder buffer = new StringBuilder("Errors occured during parsing:");
    for (ParserError error : getErrors()) {
      buffer.append("\n   ");
      buffer.append(error.toString());
    }
    return buffer.toString();
  }

  public void printErrors(PrintStream stream) {
    for (ParserError error : getErrors()) {
      stream.println(error);
    }
  }

  private static final long serialVersionUID = 2006091701L;
}
