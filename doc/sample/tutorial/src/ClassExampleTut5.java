import java.io.OutputStreamWriter;
import java.util.Date;

public class ClassExampleTut5 {
  public static void main(String[] argv) throws Exception {
    new ClassExampleTemplate()
      .render(new OutputStreamWriter(System.out), new Date(), 3);
  }
}
