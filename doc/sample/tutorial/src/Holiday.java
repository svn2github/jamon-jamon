import java.io.OutputStreamWriter;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import org.jamon.StandardTemplateManager;
import org.jamon.TemplateManagerSource;

public class Holiday {
  private Date date;
  private String name;

  static private DateFormat format = new SimpleDateFormat("yyyy-MM-dd");

  public Holiday (String p_name, Date p_date)
  {
    name = p_name;
    date = p_date;
  }

  public String getName() { return  name; }
  public Date getDate() { return  date; }

  public static void main(String[] argv) {
    try {
      List holidays = new ArrayList();
      holidays.add(new Holiday("New Year's", format.parse("2003-1-1")));
      holidays.add(new Holiday("July 4", format.parse("2003-7-4")));
      holidays.add(
          new Holiday("Thanks Giving", format.parse("2003-11-17")));
      holidays.add(new Holiday("Christmas", format.parse("2003-12-25")));
      // set the template manager once for all time ...
      TemplateManagerSource
          .setTemplateManager(new StandardTemplateManager());
      // now create templates without specifying a template manager
      new FragmentExampleTemplate()
          .render(new OutputStreamWriter(System.out), holidays);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}
