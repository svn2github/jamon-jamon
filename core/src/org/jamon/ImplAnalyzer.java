package org.modusponens.jtt;

import java.io.Writer;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import org.modusponens.jtt.node.*;

public class ImplAnalyzer extends BaseAnalyzer
{
    private final Map m_unitStatements = new HashMap();
    private StringBuffer m_current = new StringBuffer();
    private final Set m_calls = new HashSet();

    private final String m_path;

    public ImplAnalyzer(String p_templatePath, Start p_start)
    {
        m_unitStatements.put(MAIN_UNIT_NAME,new ArrayList());
        m_path = p_templatePath;
        p_start.apply(new Adapter());
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

    // FIXME: does this belong here?
    private int m_lastVar = 0;
    public String newVarName()
    {
        return "j$" + (m_lastVar++);
    }


    public String getPath()
    {
        return m_path;
    }


    public String getAbsolutePath(String p_path)
    {
        // FIXME: should use properties ...
        if (p_path.charAt(0) == '/')
        {
            return p_path;
        }
        else
        {
            int i = getPath().lastIndexOf('/');
            if (i <= 0)
            {
                return "/" + p_path;
            }
            else
            {
                return getPath().substring(0,i) + "/" + p_path;
            }
        }

    }

    public FargInfo getFargInfo(String p_unitName, String p_fargName)
    {
        return new FargInfo(p_fargName);
    }

    public List getStatements(String p_unitName)
    {
        return (List) m_unitStatements.get(p_unitName);
    }

    public List getStatements()
    {
        return getStatements(MAIN_UNIT_NAME);
    }



    protected class Adapter extends BaseAnalyzer.Adapter
    {
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
            addStatement(new CallStatement(path,makeParamMap(call.getParam())));
        }

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
                                                    makeParamMap(call.getParam()),
                                                    getStatements(getUnitName()));
            popUnitName();
            addStatement(s);

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

    }

    private int fragments = 0;

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


    private Map makeParamMap(List p_paramList)
    {
        Map paramMap = new HashMap();
        for (Iterator p = p_paramList.iterator(); p.hasNext(); /* */)
        {
            AParam param = (AParam) p.next();
            paramMap.put(param.getIdentifier().getText(),
                         param.getParamExpr().getText().trim());
        }
        return paramMap;
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

    private void addStatement(Statement p_statement)
    {
        getStatements(getUnitName()).add(p_statement);
    }

}
