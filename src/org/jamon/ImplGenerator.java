package org.modusponens.jtt;

import java.io.Writer;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import org.modusponens.jtt.node.*;
import org.modusponens.jtt.analysis.*;

public class ImplGenerator extends BaseGenerator
{
    private final Map m_unitStatements = new HashMap();
    private StringBuffer m_current = new StringBuffer();
    private final Set m_calls = new HashSet();

    private final static String FRAGMENT_CLASS =
        Fragment.class.getName();

    private final static String IOEXCEPTION_CLASS =
        IOException.class.getName();

    private final static String WRITER_CLASS =
        Writer.class.getName();

    public ImplGenerator(Writer p_writer,
                         TemplateDescriber p_describer,
                         String p_templatePath)
    {
        super(p_writer,p_describer,p_templatePath);
        m_unitStatements.put(MAIN_UNIT_NAME,new ArrayList());
    }

    public Collection getCalledTemplateNames()
    {
        Set calls = new HashSet();
        calls.addAll(m_calls);
        calls.removeAll(getDefNames());
        List absCalls = new ArrayList(calls.size());
        for (Iterator i = calls.iterator(); i.hasNext(); /* */)
        {
            absCalls.add(getAbsolutePath((String) i.next()));
        }
        return absCalls;
    }

    public void generateClassSource()
        throws IOException
    {
        generatePrologue();
        generateImports();
        generateDeclaration();
        generateConstructor();
        generateRender();
        generateOptionalArgs();
        generateDefs();
        generateEpilogue();
    }

    public void caseABodyBaseComponent(ABodyBaseComponent node)
    {
        m_current.append(node.getText().getText());
    }

    public void caseANewlineBaseComponent(ANewlineBaseComponent node)
    {
        m_current.append(node.getNewline().getText());
    }

    public void caseADefComponent(ADefComponent node)
    {
        handleBody();
        super.caseADefComponent(node);
    }

    public void caseAJavaBaseComponent(AJavaBaseComponent node)
    {
        handleBody();
        AJava java = (AJava) node.getJava();
        addStatement(new RawStatement(java.getJavaStmts().getText()));
    }

    public void caseAJlineBaseComponent(AJlineBaseComponent node)
    {
        handleBody();
        AJline jline = (AJline) node.getJline();
        addStatement(new RawStatement(jline.getExpr().getText()));
    }

    private void handleBody()
    {
        if (m_current.length() > 0)
        {
            addStatement(new WriteStatement
                         ("\""
                          + javaEscape(newlineEscape(m_current.toString()))
                          + "\"",
                          Encoding.NONE));
            m_current = new StringBuffer();
        }
    }

    public void caseAEmitBaseComponent(AEmitBaseComponent node)
    {
        handleBody();
        AEmit emit = (AEmit) node.getEmit();
        TEscape escape = emit.getEscape();
        Encoding encoding = Encoding.DEFAULT;
        if (escape != null)
        {
            char c = escape.getText().trim().charAt(1);
            switch (c)
            {
              case 'h': encoding = Encoding.HTML; break;
              case 'u': encoding = Encoding.URL; break;
              case 'n': encoding = Encoding.NONE; break;
              case 'x': encoding = Encoding.XML; break;
              default:
                throw new RuntimeException("Unknown escape " + c);
            }
        }
        addStatement(new WriteStatement(emit.getEmitExpr().getText(),
                                        encoding));
    }

    public void caseAContentBaseComponent(AContentBaseComponent node)
    {
        handleBody();
        AContent c = (AContent) node.getContent();
        addStatement(new RawStatement(c.getIdentifier().getText()
                                      + ".render();"));
    }

    public void caseACallBaseComponent(ACallBaseComponent node)
    {
        handleBody();
        ACall call = (ACall) node.getCall();
        String path = asText(call.getPath());
        m_calls.add(path);
        addStatement(new CallStatement(path,call.getParam()));
    }

    private int fragments = 0;

    public void caseAFragmentCallBaseComponent(AFragmentCallBaseComponent node)
    {
        handleBody();
        pushUnitName("#" + (fragments++));
        m_unitStatements.put(getUnitName(),new ArrayList());
        AFragmentCall call = (AFragmentCall) node.getFragmentCall();
        String path = asText(call.getPath());
        m_calls.add(path);
        for (Iterator i = call.getBaseComponent().iterator(); i.hasNext(); /* */)
        {
            ((Node) i.next()).apply(this);
        }

        handleBody();
        Statement s = new FragmentCallStatement(path,
                                                call.getParam(),
                                                getStatements(getUnitName()));
        popUnitName();
        addStatement(s);

    }

    private String asText(PPath node)
    {
        if (node instanceof ASimplePath)
        {
            ASimplePath path = (ASimplePath) node;
            if (path.getPathsep() != null)
            {
                return "/" + path.getIdentifier().getText();
            }
            else
            {
                return path.getIdentifier().getText();
            }
        }
        else
        {
            AQualifiedPath path = (AQualifiedPath) node;
            return asText(path.getPath())
                + "/"
                + path.getIdentifier().getText();
        }
    }

    public void caseEOF(EOF node)
    {
        handleBody();
    }

    public void caseTDefEnd(TDefEnd node)
    {
        handleBody();
    }

    public void caseTDefStart(TDefStart node)
    {
        handleBody();
        m_unitStatements.put(getUnitName(),new ArrayList());
    }

    private static String newlineEscape(String p_string)
    {
        // assert p_string != null
        if (p_string.length() < 2)
        {
            return p_string;
        }
        StringBuffer s = new StringBuffer();
        int j = 0;
        int i = p_string.indexOf("\\\n");
        while (i >= 0)
        {
            s.append(p_string.substring(j,i));
            j = i+2;
            i = p_string.indexOf("\\\n",j);
        }
        s.append(p_string.substring(j));
        return s.toString();
    }

    private static String javaEscape(String p_string)
    {
        // assert p_string != null
        StringBuffer s = new StringBuffer();
        for (int i = 0; i < p_string.length(); ++i)
        {
            char c = p_string.charAt(i);
            switch(c)
            {
              case '\n': s.append("\\n"); break;
              case '\t': s.append("\\t"); break;
              case '\"': s.append("\\\""); break;
              default: s.append(c);
            }
        }
        return s.toString();
    }

    private String getInterfaceClassName()
    {
        return getTemplateDescriber().getIntfClassName(getPath());
    }

    private String getClassName()
    {
        return getTemplateDescriber().getImplClassName(getPath());
    }

    private void generateDeclaration()
        throws IOException
    {
        print  ("public class ");
        println(              getClassName());
        print  ("  extends ");
        println(           BASE_TEMPLATE);
        print  ("  implements ");
        String pkgName = getPackageName();
        if (pkgName.length() > 0)
        {
            print(pkgName);
            print(".");
        }
        println(              getInterfaceClassName());
        println("{");
    }

    private void generateConstructor()
        throws IOException
    {
        print("  public ");
        print(getClassName());
        print("(");
        print(WRITER_CLASS);
        println(" p_writer,");
        print  ("        ");
        print  (         TEMPLATE_MANAGER);
        println(                        " p_templateManager)");
        println("  {");
        println("    super(p_writer, p_templateManager);");
        println("  }");
        println();

    }

    private String getPackageName()
    {
        return getTemplateDescriber().getImplPackageName(getPath());
    }

    private void generatePrologue()
        throws IOException
    {
        if (getPackageName().length() > 0)
        {
            print("package ");
            print(getPackageName());
            println(";");
            println();
        }
    }


    private void generateDefs()
        throws IOException
    {
        for (Iterator d = getDefNames().iterator(); d.hasNext(); /* */)
        {
            String name = (String) d.next();
            println();
            print("  private void $def$");
            print(name);
            print("(");
            int argNum = 0;
            for (Iterator a = getRequiredArgNames(name);
                 a.hasNext();
                 /* */)
            {
                if (argNum++ > 0)
                {
                    print(",");
                }
                String arg = (String) a.next();
                print("final ");
                print(getArgType(name,arg));
                print(" ");
                print(arg);
            }
            for (Iterator a = getOptionalArgNames(name);
                 a.hasNext();
                 /* */)
            {
                if (argNum++ > 0)
                {
                    print(",");
                }
                String arg = (String) a.next();
                print(getArgType(name,arg));
                print(" ");
                print(arg);
            }
            println(")");
            print  ("    throws ");
            println(IOEXCEPTION_CLASS);
            println("  {");
            for (Iterator i = getStatements(name).iterator();
                 i.hasNext();
                 /* */)
            {
                print("    ");
                println(((Statement)i.next()).asString());
            }
            println("  }");
            println();
        }
    }


    private int m_lastVar = 0;

    private String newVarName()
    {
        return "j$" + (m_lastVar++);
    }

    private static final String TEMPLATE_MANAGER =
        TemplateManager.class.getName();

    private static final String BASE_TEMPLATE =
        AbstractTemplate.class.getName();

    private void generateRender()
        throws IOException
    {
        print("  public void render(");
        for (Iterator i = getRequiredArgNames(MAIN_UNIT_NAME);
             i.hasNext();
             /* */)
        {
            String name = (String) i.next();
            print("final ");
            print(getArgType(MAIN_UNIT_NAME,name));
            print(" ");
            print(name);
            if (i.hasNext())
            {
                print(", ");
            }
        }
        println(")");

        print  ("    throws ");
        println(IOEXCEPTION_CLASS);
        println("  {");
        for (Iterator i = getStatements(MAIN_UNIT_NAME).iterator();
             i.hasNext();
             /* */)
        {
            print("    ");
            println(((Statement)i.next()).asString());
        }
        println("  }");
    }

    private void generateOptionalArgs()
        throws IOException
    {
        for (Iterator i = getOptionalArgNames(MAIN_UNIT_NAME);
             i.hasNext();
             /* */)
        {
            println();
            String name = (String) i.next();
            print("  public ");
            print(getInterfaceClassName());
            print(" set");
            print(capitalize(name));
            print("(");
            String type = getArgType(MAIN_UNIT_NAME,name);
            print(type);
            print(" p_");
            print(name);
            println(")");
            println("  {");
            print("    ");
            print(name);
            print(" = p_");
            print(name);
            println(";");
            println("    return this;");
            println("  }");
            println();
            print("  private ");
            print(type);
            print(" ");
            print(name);
            print(" = ");
            print(getDefault(MAIN_UNIT_NAME,name));
            println(";");
        }
    }

    private void generateEpilogue()
        throws IOException
    {
        println();
        println("}");
    }

    private List getStatements(String p_unitName)
    {
        return (List) m_unitStatements.get(p_unitName);
    }

    private void addStatement(Statement p_statement)
    {
        getStatements(getUnitName()).add(p_statement);
    }

    private static class Encoding
    {
        private Encoding(String p_name)
        {
            m_name = p_name;
        }
        public String toString()
        {
            return m_name;
        }
        public boolean equals(Object p_obj)
        {
            return p_obj == this;
        }

        private final String m_name;

        final static Encoding DEFAULT = new Encoding("");
        final static Encoding NONE = new Encoding("Un");
        final static Encoding HTML = new Encoding("Html");
        final static Encoding XML = new Encoding("Xml");
        final static Encoding URL = new Encoding("Url");
    }

    private static interface Statement
    {
        String asString()
            throws JttException;
    }

    private static class WriteStatement
        implements Statement
    {
        WriteStatement(String p_expr, Encoding p_encoding)
        {
            m_expr = p_expr;
            m_encoding = p_encoding;
        }
        private final String m_expr;
        private final Encoding m_encoding;
        public String asString()
        {
            return "write" + m_encoding + "Escaped(String.valueOf(" + m_expr + "));";
        }
    }

    private static class RawStatement
        implements Statement
    {
        RawStatement(String p_code)
        {
            m_code = p_code;
        }
        private final String m_code;
        public String asString()
        {
            return m_code;
        }
    }

    private class FragmentCallStatement
        extends CallStatement
    {
        FragmentCallStatement(String p_path, List p_params, List p_fragment)
        {
            super(p_path, p_params);
            m_fragment = p_fragment;
        }

        private final List m_fragment;

        private Object getFirstArgName()
            throws JttException
        {
            return isDefCall()
                ? getRequiredArgNames(m_path).next()
                : getRequiredArgs().get(0);
        }

        public String asString()
            throws JttException
        {
            StringBuffer s = new StringBuffer();
            String fragVar = newVarName();
            s.append("{\n");
            s.append("    final ");
            s.append(FRAGMENT_CLASS);
            s.append(" ");
            s.append(fragVar);
            s.append(" =\n");
            s.append("      new ");
            s.append(FRAGMENT_CLASS);
            s.append(" () {\n");
            s.append("       public void render() throws ");
            s.append(IOEXCEPTION_CLASS);
            s.append(" {\n");
            for (Iterator i = m_fragment.iterator(); i.hasNext(); /* */)
            {
                s.append(((Statement)i.next()).asString());
                s.append("\n");
            }
            s.append("    }\n");
            s.append("  };\n");
            m_params.put(getFirstArgName(),fragVar);
            s.append(super.asString());
            s.append("}\n");
            return s.toString();
        }
    }

    protected String getAbsolutePath(String p_path)
    {
        // FIXME: should use properties ...
        if (p_path.charAt(0) == '/')
        {
            return p_path;
        }
        else
        {
            String path = getPath();
            int i = path.lastIndexOf('/');
            if (i <= 0)
            {
                return "/" + p_path;
            }
            else
            {
                return path.substring(0,i) + "/" + p_path;
            }
        }

    }

    private class CallStatement
        implements Statement
    {
        CallStatement(String p_path,List p_params)
        {
            m_path = p_path;
            m_params = new HashMap();
            for (Iterator p = p_params.iterator(); p.hasNext(); /* */)
            {
                AParam param = (AParam) p.next();
                m_params.put(param.getIdentifier().getText(),
                             param.getParamExpr().getText().trim());
            }
        }
        protected final String m_path;
        protected final Map m_params;

        protected boolean isDefCall()
        {
            return getDefNames().contains(m_path);
        }

        private String getInterfaceClassName()
        {
            String pkg = getTemplateDescriber()
                .getIntfPackageName(getAbsolutePath(m_path));
            String clsName = getTemplateDescriber()
                .getIntfClassName(getAbsolutePath(m_path));
            return "".equals(pkg) ? clsName : (pkg + "." + clsName);
        }

        public String asString()
            throws JttException
        {
            return isDefCall() ? asDefCall() : asComponentCall();
        }

        protected String asDefCall()
            throws JttException
        {
            StringBuffer s = new StringBuffer();
            s.append("$def$");
            s.append(m_path);
            s.append("(");
            int argNum = 0;
            for (Iterator r = getRequiredArgNames(m_path); r.hasNext(); )
            {
                if (argNum++ > 0)
                {
                    s.append(",");
                }
                String name = (String) r.next();
                String expr = (String) m_params.get(name);
                if (expr == null)
                {
                    throw new JttException("No value supplied for required argument " + name);
                }
                s.append("(");
                s.append(expr);
                s.append(")");
            }
            for (Iterator o = getOptionalArgNames(m_path); o.hasNext(); )
            {
                if (argNum++ > 0)
                {
                    s.append(",");
                }
                String name = (String) o.next();
                s.append("(");
                String expr = (String) m_params.get(name);
                if (expr == null)
                {
                    s.append(getDefault(m_path,name));
                }
                else
                {
                    s.append(expr);
                }
                s.append(")");
            }
            s.append(");\n");
            return s.toString();
        }

        protected List getRequiredArgs()
            throws JttException
        {
            return getTemplateDescriber()
                .getRequiredArgNames(getAbsolutePath(m_path));
        }

        protected String asComponentCall()
            throws JttException
        {
            StringBuffer s = new StringBuffer();
            String tVar = newVarName();
            s.append("{\n      final ");
            s.append(getInterfaceClassName());
            s.append(" ");
            s.append(tVar);
            s.append(" = (");
            s.append(getInterfaceClassName());
            s.append(") getTemplateManager().getInstance(\"");
            s.append(getAbsolutePath(m_path));
            s.append("\", getWriter());\n");

            List requiredArgs = getRequiredArgs();

            for (Iterator i = m_params.keySet().iterator();
                 i.hasNext();
                 /* */ )
            {
                String name = (String) i.next();
                if (! requiredArgs.contains(name) )
                {
                    s.append("      ");
                    s.append(tVar);
                    s.append(".set");
                    s.append(capitalize(name));
                    s.append("(");
                    s.append(m_params.get(name));
                    s.append(");\n");
                }
            }
            s.append("      ");
            s.append(tVar);
            s.append(".render(");
            for (Iterator i = getRequiredArgs().iterator(); i.hasNext(); /* */)
            {
                String name = (String) i.next();
                String expr = (String) m_params.get(name);
                if (expr == null)
                {
                    throw new JttException("No parameter supplied for argument " + name + " in call to " + m_path);
                }
                s.append(expr);
                if (i.hasNext())
                {
                    s.append(",");
                }
            }
            s.append(");\n");
            s.append("    }\n");
            return s.toString();
        }
    }
}
