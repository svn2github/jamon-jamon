import java.io.OutputStreamWriter;

public class EscapingTut4a {
  public static void main(String[] argv) throws Exception {
    new EscapingTemplateA()
      .setName("Duke & Co.")
      .render(new OutputStreamWriter(System.out), "Hello");
  }
}
