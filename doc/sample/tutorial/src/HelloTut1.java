import java.io.OutputStreamWriter;
import org.jamon.StandardTemplateManager;

public class HelloTut1 {
  public static void main(String[] argv) throws Exception {
    StandardTemplateManager mgr = new StandardTemplateManager();
    new HelloTemplate(mgr).render(new OutputStreamWriter(System.out));
  }
}
