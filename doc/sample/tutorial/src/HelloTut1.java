import java.io.OutputStreamWriter;

public class HelloTut1 {
  public static void main(String[] argv) throws Exception {
    new HelloTemplate().render(new OutputStreamWriter(System.out));
  }
}
