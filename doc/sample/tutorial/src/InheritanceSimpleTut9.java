import java.io.OutputStreamWriter;
import org.jamon.StandardTemplateManager;
import org.jamon.TemplateManagerSource;

public class InheritanceSimpleTut9
{
  public static void main(String[] args) throws Exception {
    TemplateManagerSource.setTemplateManager(new StandardTemplateManager());
    new InheritanceChild().render(new OutputStreamWriter(System.out));
  }
}
