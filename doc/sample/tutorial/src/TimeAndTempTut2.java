import java.io.OutputStreamWriter;

public class TimeAndTempTut2 {
  public static void main(String[] argv) throws Exception {
    new TimeAndTempTemplate().render(new OutputStreamWriter(System.out));
  }
}
