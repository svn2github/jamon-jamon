import java.io.OutputStreamWriter;

public class InheritanceSimple
{
  public static void main(String[] args) throws Exception {
    new InheritanceChild().makeParentRenderer()
      .render(new OutputStreamWriter(System.out));
  }
}
