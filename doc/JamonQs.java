import java.io.OutputStreamWriter;
import org.jamon.StandardTemplateManager;

public class JamonQs {
  public static void main(String[] argv) {
    try {
      StandardTemplateManager mgr = new StandardTemplateManager();
      QsTemp template = new QsTemp(mgr);
      template
          .writeTo(new OutputStreamWriter(System.out))
          .render(argv);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}
