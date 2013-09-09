package org.jamon.codegen;

import java.io.ByteArrayOutputStream;

import org.jamon.node.LocationImpl;

import junit.framework.TestCase;

public class LiteralStatementTest extends TestCase {

  public void testLiteralStatement() {
    assertEquals("foo", new LiteralStatement("foo", null, null).getText());
  }

  public void testAppendText() {
    LiteralStatement statement = new LiteralStatement("foo", null, null);
    statement.appendText("bar");
    assertEquals("foobar", statement.getText());
  }

  public void testEscaping() throws Exception {
    String nl = System.getProperty("line.separator");
    LiteralStatement statement =  new LiteralStatement(
      "\n \r \t \" \\ \u1234",
      new LocationImpl(null, 1, 1),
      null);
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    CodeWriter codeWriter = new CodeWriter(bos, "US-ASCII");
    statement.generateSource(codeWriter, null);
    codeWriter.finish();
    assertEquals(
      "// 1, 1" + nl + "jamonWriter.write(\"\\n \\r \\t \\\" \\\\ \\u1234\");" + nl,
      bos.toString());
  }
}
