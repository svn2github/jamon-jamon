import java.io.OutputStreamWriter;
import org.jamon.StandardTemplateManager;
import org.jamon.TemplateManagerSource;

public class InheritanceSimple
{
  public static void main(String[] args)
  {
    try
    {
      TemplateManagerSource.setTemplateManager(new StandardTemplateManager());
      new InheritanceChild()
          .makeParentRenderer().render(new OutputStreamWriter(System.out));
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}
