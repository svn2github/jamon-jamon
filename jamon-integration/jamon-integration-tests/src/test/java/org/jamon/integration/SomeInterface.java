package org.jamon.integration;

import java.io.IOException;
import java.io.Writer;

public interface SomeInterface {
  // FIXME: we want covariant return types

  // SomeInterface setX(int x)
  // throws IOException;

  void render(Writer w, String s) throws IOException;
}
