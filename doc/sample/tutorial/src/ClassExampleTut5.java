import java.io.OutputStreamWriter;
import java.util.Date;
import org.jamon.StandardTemplateManager;
import org.jamon.TemplateManagerSource;

public class ClassExampleTut5 {
  public static void main(String[] argv) throws Exception {
    TemplateManagerSource.setTemplateManager(new StandardTemplateManager());
    ClassExampleTemplate example = new ClassExampleTemplate();
    example.render(new OutputStreamWriter(System.out), new Date(), 3);
  }
}
