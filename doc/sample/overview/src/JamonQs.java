import java.io.OutputStreamWriter;
import java.io.IOException;
import java.util.Date;

public class JamonQs {
  public static void main(String[] argv) throws IOException {
    QsTemp template = new QsTemp();
    template.render(new OutputStreamWriter(System.out), new Date(0),argv);
  }
}
