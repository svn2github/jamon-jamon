package org.modusponens.jtt;

import java.io.PrintWriter;
import java.io.IOException;
import java.util.List;
import java.util.Iterator;

public class FragmentCallStatement
    extends CallStatement
{
    FragmentCallStatement(String p_path, List p_params, List p_fragment)
    {
        super(p_path, p_params);
        m_fragment = p_fragment;
    }

    private final List m_fragment;

    private final static String FRAGMENT_CLASS =
        Fragment.class.getName();

    private final static String IOEXCEPTION_CLASS =
        IOException.class.getName();

    private Object getFirstArgName(ImplAnalyzer p_analyzer,TemplateDescriber p_describer)
        throws JttException
    {
        return isDefCall(p_analyzer)
            ? p_analyzer.getRequiredArgNames(getPath()).next()
            : p_describer.getRequiredArgNames(p_analyzer.getAbsolutePath(getPath())).get(0);
    }


    public void generateSource(PrintWriter p_writer,
                               TemplateResolver p_resolver,
                               TemplateDescriber p_describer,
                               ImplAnalyzer p_analyzer)
        throws IOException
    {
        String fragVar = p_analyzer.newVarName();
        p_writer.println("{");
        p_writer.print("    final ");
        p_writer.print(FRAGMENT_CLASS);
        p_writer.print(" ");
        p_writer.print(fragVar);
        p_writer.println(" =");
        p_writer.print("      new ");
        p_writer.print(FRAGMENT_CLASS);
        p_writer.println(" () {");
        p_writer.print("       public void render() throws ");
        p_writer.print(IOEXCEPTION_CLASS);
        p_writer.println(" {");
        for (Iterator i = m_fragment.iterator(); i.hasNext(); /* */)
        {
            ((Statement)i.next()).generateSource(p_writer,
                                                 p_resolver,
                                                 p_describer,
                                                 p_analyzer);
            p_writer.println();
        }
        p_writer.println("    }");
        p_writer.println("  };");

        m_params.put(getFirstArgName(p_analyzer,p_describer),fragVar);

        super.generateSource(p_writer,p_resolver,p_describer,p_analyzer);

        p_writer.println("}");
    }
}
