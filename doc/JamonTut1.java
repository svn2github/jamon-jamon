import java.io.OutputStreamWriter;
import org.jamon.StandardTemplateManager;

public class JamonTut1 {
  public static void main(String[] args) {
    try {
      StandardTemplateManager mgr = new StandardTemplateManager();
      TutTemp1 template = new TutTemp1(mgr);
      template
          .writeTo(new OutputStreamWriter(System.out))
          .render();
      template.render();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}
