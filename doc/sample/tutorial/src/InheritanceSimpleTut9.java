import java.io.OutputStreamWriter;

public class InheritanceSimpleTut9
{
  public static void main(String[] args) throws Exception {
    new InheritanceChild().render(new OutputStreamWriter(System.out));
  }
}
