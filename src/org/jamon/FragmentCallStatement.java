package org.jamon;

import java.io.PrintWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Iterator;

public class FragmentCallStatement
    extends CallStatement
{
    FragmentCallStatement(String p_path, Map p_params, List p_fragment)
    {
        super(p_path, p_params);
        m_fragment = p_fragment;
    }

    private final List m_fragment;

    private final static String IOEXCEPTION_CLASS =
        IOException.class.getName();

    private Object getFargName(ImplAnalyzer p_analyzer,
                               TemplateDescriber p_describer)
        throws JttException
    {
        if (isDefCall(p_analyzer))
        {
            return p_analyzer.getFargNames(getPath()).next();
        }
        else
        {
            return p_describer
                .getFargNames(p_analyzer.getAbsolutePath(getPath())).next();
        }
    }



    public void generateSource(PrintWriter p_writer,
                               TemplateResolver p_resolver,
                               TemplateDescriber p_describer,
                               ImplAnalyzer p_analyzer)
        throws IOException
    {
        String fragVar = p_analyzer.newVarName();

        String fargName = (String) getFargName(p_analyzer, p_describer);
        FargInfo fargInfo =
            isDefCall(p_analyzer)
            ? p_analyzer.getFargInfo(fargName)
            : p_describer.getFargInfo(p_analyzer.getAbsolutePath(getPath()),fargName);

        p_writer.println("{");
        p_writer.print("    final ");

        String fargIntf = fargInfo.getFargInterfaceName();
        if (! isDefCall(p_analyzer))
        {
            fargIntf =
                p_resolver.getFullyQualifiedIntfClassName
                    (p_analyzer.getAbsolutePath(getPath()))
                + "." + fargIntf;
        }

        p_writer.print(fargIntf);
        p_writer.print(" ");
        p_writer.print(fragVar);
        p_writer.println(" =");
        p_writer.print("      new ");
        p_writer.print(fargIntf);
        p_writer.println(" () {");
        p_writer.print("       public void render(");

        for (Iterator a = fargInfo.getArgumentNames(); a.hasNext(); /* */)
        {
            String arg = (String) a.next();
            p_writer.print(fargInfo.getArgumentType(arg));
            p_writer.print(" ");
            p_writer.print(arg);
            if (a.hasNext())
            {
                p_writer.print(", ");
            }
        }

        p_writer.print(") throws ");
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

        m_params.put(fargName, fragVar);

        super.generateSource(p_writer,p_resolver,p_describer,p_analyzer);

        p_writer.println("}");
    }
}
