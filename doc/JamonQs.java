import java.io.OutputStreamWriter;
import java.util.Date;
import org.jamon.StandardTemplateManager;

public class JamonQs {
  public static void main(String[] argv) {
    try {
      StandardTemplateManager mgr = new StandardTemplateManager();
      QsTemp template = new QsTemp(mgr);
      template
          .writeTo(new OutputStreamWriter(System.out))
          .render(new Date(),argv);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}
