import java.io.OutputStreamWriter;

public class EscapingTut4b {
  public static void main(String[] argv) throws Exception {
    new EscapingTemplateB()
      .setName("Duke & Co.")
      .render(new OutputStreamWriter(System.out), "Hello");
  }
}
