/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Jamon code, released February, 2003.
 *
 * The Initial Developer of the Original Code is Jay Sachs.  Portions
 * created by Jay Sachs are Copyright (C) 2003 Jay Sachs.  All Rights
 * Reserved.
 *
 * Contributor(s): Ian Robertson
 */

package org.jamon.codegen;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

import org.jamon.node.*;
import org.jamon.JamonException;

public class ImplAnalyzer extends BaseAnalyzer
{
    public ImplAnalyzer(String p_templatePath,
                        TemplateDescriber p_describer)
    {
        super(p_templatePath, p_describer);
    }

    private StringBuffer m_current = new StringBuffer();

    protected void mainAnalyze(Start p_start)
        throws IOException
    {
        p_start.apply(new Adapter());
    }

    private List getStatements()
    {
        return getCurrentUnit().getStatements();
    }

    protected class Adapter extends BaseAnalyzer.Adapter
    {
        public void caseAClassComponent(AClassComponent node)
        {
            handleBody();
            for(Iterator i=((AClassTag)node.getClassTag()).getClassContent()
                    .iterator();
                i.hasNext();)
            {
                getTemplateUnit().addClassContent
                    (((TClassContent)i.next()).getText());
            }
        }

        public void caseALiteralBaseComponent(ALiteralBaseComponent node)
        {
            handleBody();
            addStatement(new LiteralStatement(node.getLiteralText().getText(),
                                              false));
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

        public void caseAPartialJline(APartialJline node)
        {
            handleBody();
            addStatement(new RawStatement(node.getExpr().getText()));
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
            AEscape escape = (AEscape) emit.getEscape();
            EscapingDirective directive;
            if (escape == null)
            {
                directive = EscapingDirective.DEFAULT;
            }
            else
            {
                directive = EscapingDirective.get(escape.getEscapeCode().getText());
            }
            addStatement(new WriteStatement(emit.getEmitExpr().getText(),
                                            directive));
        }

        public void caseACallBaseComponent(ACallBaseComponent node)
        {
            handleBody();
            node.getCall().apply(new PCallAdapter());
        }

        public void caseAMultiFragmentCallBaseComponent(AMultiFragmentCallBaseComponent node)
        {
            handleBody();
            AMultiFragmentCall call =
                (AMultiFragmentCall) node.getMultiFragmentCall();
            String path = NodeUtils.asText(call.getPath());
            AbstractCallStatement s = makeCallStatement(path, call.getParam());
            for (Iterator f = call.getNamedFarg().iterator(); f.hasNext(); )
            {
                ANamedFarg farg = (ANamedFarg) f.next();
                pushFragmentUnitImpl(farg.getIdentifier().getText());
                for (Iterator i = farg.getBaseComponent().iterator();
                     i.hasNext(); )
                {
                    ((Node) i.next()).apply(this);
                }
                handleBody();
                s.addFragmentImpl((FragmentUnit) getCurrentUnit());
                popUnit();
            }
            addStatement(s);
        }

        public void caseAFragmentCallBaseComponent(AFragmentCallBaseComponent node)
        {
            handleBody();
            AFragmentCall call = (AFragmentCall) node.getFragmentCall();
            String path = NodeUtils.asText(call.getPath());
            AbstractCallStatement s = makeCallStatement(path, call.getParam());
            pushFragmentUnitImpl(null);
            for (Iterator i = call.getBaseComponent().iterator();
                 i.hasNext(); )
            {
                ((Node) i.next()).apply(this);
            }
            handleBody();
            s.addFragmentImpl((FragmentUnit) getCurrentUnit());
            popUnit();

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
        }

    }

    protected class PCallAdapter extends BaseAnalyzer.PCallAdapter
    {
        public void caseACall(ACall p_call)
        {
            String path = NodeUtils.asText(p_call.getPath());
            FragmentUnit fragmentUnit = getCurrentUnit()
                .getFragmentUnitIntf(path);
            if (fragmentUnit != null)
            {
                addStatement(new FargCallStatement
                    (path,
                     makeParamMap(p_call.getParam()),
                     fragmentUnit));
            }
            else
            {
                addStatement(makeCallStatement(path, p_call.getParam()));
            }
        }

        public void caseAChildCall(AChildCall p_childCall)
        {
            super.caseAChildCall(p_childCall);
            addStatement(new Statement() {
                    public void generateSource(IndentingWriter p_writer,
                                               TemplateResolver p_resolver,
                                               TemplateDescriber p_describer)
                    {
                        p_writer.println(ImplGenerator.CHILD_FARG_NAME
                                         + ".writeTo(this.getWriter());");
                        p_writer.println(ImplGenerator.CHILD_FARG_NAME
                                         + ".escaping(this.getEscaping());");
                        p_writer.println(ImplGenerator.CHILD_FARG_NAME
                                         + ".render();");
                    }
                });
        }
    }

    private void handleBody()
    {
        if (m_current.length() > 0)
        {
            addStatement(new LiteralStatement(m_current.toString(),true));
            m_current = new StringBuffer();
        }
    }


    private AbstractCallStatement makeCallStatement(String p_path,
                                                    List p_calls)
    {
        DefUnit defUnit = getTemplateUnit().getDefUnit(p_path);
        if (defUnit != null)
        {
            return new DefCallStatement
                (p_path, makeParamMap(p_calls), defUnit);
        }
        else
        {
            getTemplateUnit().addCallPath(p_path);
            return new ComponentCallStatement
                (getTemplateUnit().getAbsolutePath(p_path),
                 makeParamMap(p_calls));
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

    private void addStatement(Statement p_statement)
    {
        getCurrentUnit().addStatement(p_statement);
    }

}
