import java.io.OutputStreamWriter;
import org.jamon.StandardTemplateManager;

public class HelloTut1 {
  public static void main(String[] argv) {
    try {
      StandardTemplateManager mgr = new StandardTemplateManager();
      new HelloTemplate(mgr).render(new OutputStreamWriter(System.out));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
