package org.modusponens.jtt;

import java.io.Writer;
import java.io.IOException;
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
    private final String m_packagePrefix;

    private final static String FRAGMENT_CLASS =
        Fragment.class.getName();

    private final static String IOEXCEPTION_CLASS =
        IOException.class.getName();

    private final static String WRITER_CLASS =
        Writer.class.getName();

    public ImplGenerator(Writer p_writer,
                         String p_packagePrefix,
                         String p_packageName,
                         String p_className)
    {
        super(p_writer,p_packageName,p_className);
        m_packagePrefix = p_packagePrefix;
        m_unitStatements.put(MAIN_UNIT_NAME,new ArrayList());
    }

    public Iterator getCalledTemplateNames()
    {
        return m_calls.iterator();
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
        StringBuffer buf = new StringBuffer();
        AJava java = (AJava) node.getJava();
        for (Iterator i = java.getAny().iterator(); i.hasNext(); /* */)
        {
            buf.append(((TAny)i.next()).getText());
        }
        addStatement(new RawStatement(buf.toString()));
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
        StringBuffer expr = new StringBuffer();
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
        for (Iterator i = emit.getAny().iterator(); i.hasNext(); /* */)
        {
            expr.append(((TAny)i.next()).getText());
        }
        addStatement(new WriteStatement(expr.toString(), encoding));
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
            if (path.getSlash() != null)
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
        String pkgName = getPackageName();
        if (pkgName.length() > 0)
        {
            return m_packagePrefix + pkgName + "." + getClassName();
        }
        else
        {
            return m_packagePrefix + getClassName();
        }
    }

    private void generateDeclaration()
        throws IOException
    {
        print  ("public class ");
        print  (              getClassName());
        println(                           "Impl");
        print  ("  extends ");
        println(           BASE_TEMPLATE);
        print  ("  implements ");
        println(              getInterfaceClassName());
        println("{");
    }

    private void generateConstructor()
        throws IOException
    {
        print("  public ");
        print(getClassName());
        print("Impl(");
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
            s.append("{\n");
            s.append("    ");
            s.append(FRAGMENT_CLASS);
            s.append(" f =\n");
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
            m_params.put(getFirstArgName(),"f");
            s.append(super.asString());
            s.append("}\n");
            return s.toString();
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

        protected String getAbsolutePath()
        {
            if (m_path.charAt(0) == '/')
            {
                return m_path;
            }
            else
            {
                String pkgName = getPackageName();
                if (pkgName.length() > 0)
                {
                    return "/" + pkgName.replace('.','/') + '/' + m_path;
                }
                else
                {
                    return "/" + m_path;
                }
            }

        }

        private String getClassName()
        {
            return PathUtils.pathToClassName(getAbsolutePath());
        }

        private String getInterfaceClassName()
        {
            return m_packagePrefix + getClassName();
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
            List requiredArgs = new ArrayList();
            try
            {
                Class c = Class.forName(getInterfaceClassName());
                requiredArgs.addAll
                    (Arrays.asList
                     ((String []) c.getField("RENDER_ARGS").get(null)));
                return requiredArgs;
            }
            catch (ClassNotFoundException e)
            {
                throw new JttException("Class " + e.getMessage() + " not found",e);
            }
            catch (IllegalAccessException e)
            {
                throw new JttException(e);
            }
            catch (NoSuchFieldException e)
            {
                throw new JttException(e);
            }
        }

        protected String asComponentCall()
            throws JttException
        {
            StringBuffer s = new StringBuffer();
            s.append("{\n      ");
            s.append(getInterfaceClassName());
            s.append(" c = (");
            s.append(getInterfaceClassName());
            s.append(") getTemplateManager().getInstance(\"");
            s.append(getAbsolutePath());
            s.append("\", getWriter());\n");

            List requiredArgs = getRequiredArgs();

            for (Iterator i = m_params.keySet().iterator();
                 i.hasNext();
                 /* */ )
            {
                String name = (String) i.next();
                if (! requiredArgs.contains(name) )
                {
                    s.append("      c.set");
                    s.append(capitalize(name));
                    s.append("(");
                    s.append(m_params.get(name));
                    s.append(");\n");
                }
            }
            s.append("      c.render(");
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
