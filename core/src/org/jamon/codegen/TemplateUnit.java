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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jamon.ParserErrors;
import org.jamon.node.ClassNode;
import org.jamon.node.ImportNode;
import org.jamon.node.Location;
import org.jamon.node.ParentArgNode;
import org.jamon.util.StringUtils;

public class TemplateUnit
    extends AbstractUnit
    implements InheritedUnit
{
    public TemplateUnit(String p_path, ParserErrors p_errors)
    {
        super(p_path, null, p_errors);
    }

    public int getInheritanceDepth()
    {
        return m_parentDescription == null
            ? 0
            : 1 + m_parentDescription.getInheritanceDepth();
    }


    /**
     * public for unit testing purposes
     **/

    public void setParentDescription(TemplateDescription p_parent)
    {
        m_parentDescription = p_parent;
        //FIXME - join them later.
        m_fragmentArgs.addAll(p_parent.getFragmentInterfaces());

        m_inheritedArgs = new InheritedArgs(getName(),
                                            getParentPath(),
                                            p_parent.getRequiredArgs(),
                                            p_parent.getOptionalArgs(),
                                            p_parent.getFragmentInterfaces(),
                                            getErrors());

        for (Iterator i = new SequentialIterator(
                p_parent.getRequiredArgs().iterator(),
                p_parent.getOptionalArgs().iterator(),
                p_parent.getFragmentInterfaces().iterator());
             i.hasNext(); )
        {
            addArgName(((AbstractArgument) i.next()));
        }

        m_callNames.addAll(p_parent.getMethodUnits().keySet());
        m_abstractMethodNames.addAll(p_parent.getAbstractMethodNames());
    }

    public void addParentArg(ParentArgNode p_arg)
    {
        m_inheritedArgs.addParentArg(p_arg);
    }

    @Override
    public Iterator getFragmentArgs()
    {
        return m_fragmentArgs.iterator();
    }

    @Override
    public List getFragmentArgsList()
    {
        return m_fragmentArgs;
    }

    @Override
    public void addFragmentArg(FragmentArgument p_arg, Location p_location)
    {
        m_fragmentArgs.add(p_arg);
        m_declaredFragmentArgs.add(p_arg);
    }

    public Iterator getDeclaredFragmentArgs()
    {
        return m_declaredFragmentArgs.iterator();
    }

    @Override
    public void addRequiredArg(RequiredArgument p_arg)
    {
        m_declaredRequiredArgs.add(p_arg);
    }

    @Override
    public void addOptionalArg(OptionalArgument p_arg)
    {
        m_declaredOptionalArgs.add(p_arg);
    }

    @Override
    public Iterator getSignatureRequiredArgs()
    {
        return new SequentialIterator
            (m_parentDescription.getRequiredArgs().iterator(),
             m_declaredRequiredArgs.iterator());
    }

    @Override
    public Iterator getSignatureOptionalArgs()
    {
        return new SequentialIterator
            (m_parentDescription.getOptionalArgs().iterator(),
             m_declaredOptionalArgs.iterator());
    }

    public String getOptionalArgDefault(OptionalArgument p_arg)
    {
        return m_declaredOptionalArgs.contains(p_arg)
            ? p_arg.getDefault()
            : m_inheritedArgs.getDefaultValue(p_arg);
    }
    
    @Override
    public Iterator getVisibleArgs()
    {
        return m_inheritedArgs == null
            ? new SequentialIterator(getDeclaredRenderArgs(),
                                     getDeclaredOptionalArgs())
            : new SequentialIterator(m_inheritedArgs.getVisibleArgs(),
                                     getDeclaredRenderArgs(),
                                     getDeclaredOptionalArgs());
    }

    public Iterator getDeclaredOptionalArgs()
    {
        return m_declaredOptionalArgs.iterator();
    }

    public Collection getTemplateDependencies()
    {
        return m_dependencies;
    }

    private void checkCallName(String p_name, Location p_location)
    {
        if (! m_callNames.add(p_name))
        {
            getErrors().addError(
                "multiple defs and/or methods named " + p_name, p_location);
        }
    }

    public void makeDefUnit(String p_defName, Location p_location)
    {
        checkCallName(p_defName, p_location);
        m_defs.put(p_defName, new DefUnit(p_defName, this, getErrors()));
    }

    public Iterator getDefUnits()
    {
        return m_defs.values().iterator();
    }

    public DefUnit getDefUnit(String p_name)
    {
        return (DefUnit) m_defs.get(p_name);
    }

    public void makeMethodUnit(
            String p_methodName, Location p_location, boolean p_isAbstract)
    {
        checkCallName(p_methodName, p_location);
        m_methods.put(p_methodName,
                      new DeclaredMethodUnit(
                          p_methodName, this, getErrors(), p_isAbstract));
        if(p_isAbstract)
        {
            m_abstractMethodNames.add(p_methodName);
        }
    }

    public OverriddenMethodUnit makeOverridenMethodUnit(
            String p_name, Location p_location)
    {
        DeclaredMethodUnit methodUnit = (DeclaredMethodUnit)
            m_parentDescription.getMethodUnits().get(p_name);
        if(methodUnit == null)
        {
            getErrors().addError(
                "There is no such method " + p_name + " to override",
                p_location);
            // Provide a dummy parent, to allow us to catch errors inside the
            // override
            methodUnit = new DeclaredMethodUnit(p_name, this, getErrors());
        }

        m_abstractMethodNames.remove(p_name);
        OverriddenMethodUnit override =
            new OverriddenMethodUnit(methodUnit, this, getErrors());
        m_overrides.add(override);
        return override;
    }

    public MethodUnit getMethodUnit(String p_name)
    {
        return
            (MethodUnit) (m_methods.containsKey(p_name)
                          ? m_methods.get(p_name)
                          : m_parentDescription.getMethodUnits().get(p_name));
    }

    public Iterator getSignatureMethodUnits()
    {
        return new SequentialIterator
            (getDeclaredMethodUnits(),
             m_parentDescription.getMethodUnits().values().iterator());
    }

    public Iterator getDeclaredMethodUnits()
    {
        return m_methods.values().iterator();
    }

    public Iterator getImplementedMethodUnits()
    {
        return new SequentialIterator(getDeclaredMethodUnits(),
                                      m_overrides.iterator());
    }

    public Collection getAbstractMethodNames()
    {
        return m_abstractMethodNames;
    }

    private Iterator getImports()
    {
        return m_imports.iterator();
    }

    public void addImport(ImportNode p_node)
    {
        m_imports.add(p_node);
    }

    public void addInterface(String p_interface)
    {
        m_interfaces.add(p_interface);
    }

    public void setParentPath(String p_parentPath)
    {
        m_parentPath = p_parentPath;
        m_dependencies.add(m_parentPath);
    }

    public String getParentPath()
    {
        return m_parentPath;
    }

    public boolean hasParentPath()
    {
        return m_parentPath != null;
    }

    public boolean isParent()
    {
        return m_isParent;
    }

    public void setIsParent()
    {
        m_isParent = true;
    }

    public void printClassContent(CodeWriter p_writer)
    {
        for (Iterator i = m_classContent.iterator(); i.hasNext(); )
        {
            ClassNode node = (ClassNode) i.next();
            p_writer.printLocation(node.getLocation());
            p_writer.println(node.getContent());
        }
    }

    public void addClassContent(ClassNode p_node)
    {
        m_classContent.add(p_node);
    }

    public void addCallPath(String p_callPath)
    {
        m_dependencies.add(p_callPath);
    }

    private InheritedArgs m_inheritedArgs;
    private TemplateDescription m_parentDescription =
        TemplateDescription.EMPTY;
    private final List m_declaredRequiredArgs = new LinkedList();
    private final List m_fragmentArgs = new LinkedList();
    private final Set m_declaredOptionalArgs = new HashSet();
    private final Set m_declaredFragmentArgs = new HashSet();

    private final Map m_defs = new HashMap();
    private final Map m_methods = new HashMap();
    private final List m_overrides = new LinkedList();
    private final List m_imports = new LinkedList();
    private final List m_interfaces = new LinkedList();
    private String m_parentPath;
    private boolean m_isParent = false;
    private final List m_classContent = new LinkedList();
    private final Set m_dependencies = new HashSet();
    private final Set m_callNames = new HashSet();
    private final Collection m_abstractMethodNames = new HashSet();

    public Iterator getParentRenderArgs()
    {
        return new SequentialIterator
            (m_parentDescription.getRequiredArgs().iterator(),
             m_parentDescription.getFragmentInterfaces().iterator());
    }

    public void printImports(CodeWriter p_writer)
    {
        for (Iterator i = getImports(); i.hasNext(); )
        {
            ImportNode node = (ImportNode) i.next();
            p_writer.printLocation(node.getLocation());
            p_writer.println("import " + node.getName() + ";");
        }
        p_writer.println();
    }

    public void printParentRenderArgs(CodeWriter p_writer)
    {
        printArgs(p_writer, getParentRenderArgs());
    }

    public void printParentRenderArgsDecl(CodeWriter p_writer)
    {
        printArgsDecl(p_writer, getParentRenderArgs());
    }

    public Iterator getDeclaredRenderArgs()
    {
        return new SequentialIterator(m_declaredRequiredArgs.iterator(),
                                      m_declaredFragmentArgs.iterator());
    }

    public Iterator getDeclaredArgs()
    {
        return new SequentialIterator(getDeclaredRenderArgs(),
                                      m_declaredOptionalArgs.iterator());
    }

    public void printDeclaredRenderArgs(CodeWriter p_writer)
    {
        printArgs(p_writer, getDeclaredRenderArgs());
    }

    public void printDeclaredRenderArgsDecl(CodeWriter p_writer)
    {
        printArgsDecl(p_writer, getDeclaredRenderArgs());
    }

    public void printInterfaces(CodeWriter p_writer)
    {
        if (m_interfaces.size() > 0)
        {
            p_writer.print("  implements ");
            for (Iterator i = m_interfaces.iterator(); i.hasNext(); )
            {
                p_writer.print(i.next());
                if (i.hasNext())
                {
                    p_writer.print(", ");
                }
            }
        }
    }

    @Override
    protected void generateInterfaceSummary(StringBuffer p_buf)
    {
        super.generateInterfaceSummary(p_buf);
        if(m_parentDescription != null)
        {
            p_buf.append("Parent sig: ");
            p_buf.append(m_parentDescription.getSignature());
            p_buf.append("\n");
        }
        for(Iterator i = getFragmentArgs(); i.hasNext(); )
        {
            FragmentArgument arg = (FragmentArgument) i.next();
            p_buf.append("Fragment: ");
            p_buf.append(arg.getName());
            p_buf.append("\n");
            arg.getFragmentUnit().generateInterfaceSummary(p_buf);
        }
    }

    public String getSignature()
    {
        try
        {
            StringBuffer buf = new StringBuffer();
            generateInterfaceSummary(buf);
            return StringUtils.byteArrayToHexString
                (MessageDigest.getInstance("MD5").digest
                     (buf.toString().getBytes()));
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException("Unable to get md5 instance");
        }
    }
}
