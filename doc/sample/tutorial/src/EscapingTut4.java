import java.io.OutputStreamWriter;
import org.jamon.StandardTemplateManager;

public class EscapingTut4 {
  public static void main(String[] argv) throws Exception {
    new EscapingTemplate()
      .setName("Duke & Co.")
      .render(new OutputStreamWriter(System.out), "Hello");
  }
}
