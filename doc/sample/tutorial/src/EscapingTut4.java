import java.io.OutputStreamWriter;
import org.jamon.StandardTemplateManager;
import org.jamon.TemplateManager;
import org.jamon.TemplateManagerSource;

public class EscapingTut4 {
  public static void main(String[] argv) throws Exception {
    TemplateManagerSource.setTemplateManager(new StandardTemplateManager());
    new EscapingTemplate()
      .setName("Duke & Co.")
      .render(new OutputStreamWriter(System.out), "Hello");
  }
}
