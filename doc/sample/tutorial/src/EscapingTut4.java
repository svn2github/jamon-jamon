import java.io.OutputStreamWriter;
import org.jamon.StandardTemplateManager;
import org.jamon.TemplateManager;
import org.jamon.TemplateManagerSource;

public class EscapingTut4 {
  public static void main(String[] argv) {
    try {
       TemplateManager manager = new StandardTemplateManager();
       TemplateManagerSource.setTemplateManager(manager);
       new EscapingTemplate()
           .setName("Duke & Co.")
           .render(new OutputStreamWriter(System.out), "Hello");
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}
