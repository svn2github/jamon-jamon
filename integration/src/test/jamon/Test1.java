package test.jamon;

import gnu.regexp.RE;

import java.io.Writer;
import java.io.StringWriter;
import java.math.BigDecimal;

import org.jamon.StandardTemplateManager;
import org.jamon.StringUtils;

import test.jamon.TestTemplate;

public class Test1
{

    public static void main(String [] args)
        throws Exception
    {
        Writer w = new StringWriter();
        StandardTemplateManager m =
            new StandardTemplateManager();
        m.setSourceDir("templates");
        m.setWorkDir("build/work");
        TestTemplate t = new TestTemplate(m);
        t.writeTo(w);
        t.setX(57);
        t.render(new BigDecimal("34.5324"));
        w.flush();

        // Now perform some tests
        RE re = new RE(".*An external template with a "
                       + "parameterized fragment parameter \\(farg\\)"
                       + "\\s*"
                       + "i is 3 and s is yes."
                       + "\\s*"
                       + "i is 7 and s is no.*",
                       RE.REG_DOT_NEWLINE);
        if (!re.isMatch(w))
        {
            throw new Exception("Failed to match " + re);
        }
    }

}
