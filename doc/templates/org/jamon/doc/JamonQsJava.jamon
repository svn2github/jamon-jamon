import java.io.OutputStreamWriter;
import java.util.Date;
import org.jamon.StandardTemplateManager;

public class JamonQs {
  public static void main(String[] argv) {
    try {
      StandardTemplateManager mgr = new StandardTemplateManager();
      QsTemp template = new QsTemp(mgr);
      template.render(new OutputStreamWriter(System.out), new Date(0),argv);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}
