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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.jamon.JamonException;
import org.jamon.util.StringUtils;

public class TemplateUnit extends AbstractUnit
{
    public TemplateUnit(String p_path)
    {
        super(p_path, null);
    }

    public String getAbsolutePath(String p_path)
    {
        if (p_path.charAt(0) == '/')
        {
            return p_path;
        }
        else
        {
            int i = getName().lastIndexOf('/');
            if (i <= 0)
            {
                return "/" + p_path;
            }
            else
            {
                return getName().substring(0,i) + "/" + p_path;
            }
        }
    }

    public TemplateUnit processParent(TemplateDescriber p_describer)
        throws IOException
    {
        return processParent(p_describer, new HashSet());
    }

    //FIXME - should get fargs working too.
    public TemplateUnit processParent(TemplateDescriber p_describer,
                                      Set p_children)
        throws IOException
    {
        if (hasParentPath())
        {
            if (p_children.contains(getParentPath()))
            {
                throw new JamonException(getParentPath() + " extends itself");
            }
            p_children.add(getParentPath());
            setParentDescription
                (p_describer.getTemplateDescription(getParentPath(),
                                                    p_children));
        }
        return this;
    }

    public void setParentDescription(TemplateDescription p_parent)
    {
        m_parentRequiredArgs.addAll(p_parent.getRequiredArgs());
        m_parentOptionalArgs.addAll(p_parent.getOptionalArgs());
        m_parentFragmentArgs.addAll(p_parent.getFragmentInterfaces());
        //FIXME - join them later.
        m_fragmentArgs.addAll(p_parent.getFragmentInterfaces());

        for(Iterator i = new SequentialIterator
            (p_parent.getRequiredArgs().iterator(),
             p_parent.getOptionalArgs().iterator());
            i.hasNext(); )
        {
            checkArgName(((AbstractArgument) i.next()));
        }

        m_parentSignature = p_parent.getSignature();
    }

    public void addParentArg(String p_name, String p_default)
    {
        for(Iterator i = m_parentRequiredArgs.iterator(); i.hasNext(); )
        {
            RequiredArgument arg = (RequiredArgument) i.next();
            if(arg.getName().equals(p_name))
            {
                if(p_default != null)
                {
                    //FIXME - unit test this.
                    throw new TunnelingException
                        (getName()
                         + " gives a default value to inherited required argument "
                         + p_name);
                }
                m_visibleArgs.add(arg);
                return;
            }
        }

        for(Iterator i = m_parentOptionalArgs.iterator(); i.hasNext(); )
        {
            OptionalArgument arg = (OptionalArgument) i.next();
            if(arg.getName().equals(p_name))
            {
                arg.setDefault(p_default);
                m_visibleArgs.add(arg);
                return;
            }
        }

        for (Iterator i = getFragmentArgs(); i.hasNext(); )
        {
            FragmentArgument arg = (FragmentArgument) i.next();
            if (arg.getName().equals(p_name))
            {
                if(p_default != null)
                {
                    //FIXME - unit test this.
                    throw new TunnelingException
                        (getName()
                         + " gives a default value to inherited fragment argument "
                         + p_name);
                }
                m_visibleArgs.add(arg);
                return;
            }
        }

        throw new TunnelingException(getName() + " mistakenly thinks that "
                                     + getParentPath()
                                     + " has an arg named "  + p_name);
    }

    public Iterator getFragmentArgs()
    {
        return m_fragmentArgs.iterator();
    }

    public List getFragmentArgsList()
    {
        return m_fragmentArgs;
    }

    public void addFragmentArg(FragmentArgument p_arg)
    {
        checkArgName(p_arg);
        m_fragmentArgs.add(p_arg);
        m_declaredFragmentArgs.add(p_arg);
        m_visibleArgs.add(p_arg);
    }

    public Iterator getDeclaredFragmentArgs()
    {
        return m_declaredFragmentArgs.iterator();
    }

    public void addRequiredArg(RequiredArgument p_arg)
    {
        checkArgName(p_arg);
        m_declaredRequiredArgs.add(p_arg);
        m_visibleArgs.add(p_arg);
    }

    public void addOptionalArg(OptionalArgument p_arg)
    {
        checkArgName(p_arg);
        m_declaredOptionalArgs.add(p_arg);
        m_visibleArgs.add(p_arg);
    }

    public Iterator getSignatureRequiredArgs()
    {
        return new SequentialIterator(m_parentRequiredArgs.iterator(),
                                      m_declaredRequiredArgs.iterator());
    }

    public boolean hasSignatureRequiredArgs()
    {
        return ! m_parentRequiredArgs.isEmpty()
            || ! m_declaredRequiredArgs.isEmpty()
            || ! m_fragmentArgs.isEmpty();
    }

    public Iterator getSignatureOptionalArgs()
    {
        return new SequentialIterator(m_parentOptionalArgs.iterator(),
                                      m_declaredOptionalArgs.iterator());
    }

    public Iterator getVisibleArgs()
    {
        return m_visibleArgs.iterator();
    }

    public Iterator getDeclaredRequiredArgs()
    {
        return m_declaredRequiredArgs.iterator();
    }

    public Iterator getDeclaredOptionalArgs()
    {
        return m_declaredOptionalArgs.iterator();
    }

    public Collection getTemplateDependencies()
    {
        Set calls = new HashSet();
        calls.addAll(m_callPaths);
        calls.removeAll(getDefNames());
        List absCalls = new ArrayList(calls.size());
        for (Iterator i = calls.iterator(); i.hasNext(); /* */)
        {
            absCalls.add(getAbsolutePath((String) i.next()));
        }
        if (hasParentPath())
        {
            absCalls.add(getParentPath());
        }
        return absCalls;
    }

    public void addDefUnit(DefUnit p_unit)
    {
        m_defs.put(p_unit.getName(), p_unit);
    }

    public Iterator getDefUnits()
    {
        return m_defs.values().iterator();
    }

    public DefUnit getDefUnit(String p_name)
    {
        return (DefUnit) m_defs.get(p_name);
    }

    public Set getDefNames()
    {
        return m_defs.keySet();
    }

    public boolean doesDefExist(String p_name)
    {
        return m_defs.containsKey(p_name);
    }

    public Iterator getImports()
    {
        return m_imports.iterator();
    }

    public void addImport(String p_import)
    {
        m_imports.add(p_import);
    }

    public void setParentPath(String p_parentPath)
    {
        if(hasParentPath())
        {
            throw new TunnelingException
                ("a template cannot extend multiple templates");
        }
        m_parentPath = getAbsolutePath(p_parentPath);
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

    public String getClassContent()
    {
        return m_classContent.toString();
    }

    public void addClassContent(String p_content)
    {
        m_classContent.append(p_content);
    }

    public void addCallPath(String p_callPath)
    {
        m_callPaths.add(p_callPath);
    }

    private Set m_visibleArgs = new HashSet();
    private List m_parentRequiredArgs = new LinkedList();
    private List m_declaredRequiredArgs = new LinkedList();
    private final List m_fragmentArgs = new LinkedList();
    private Set m_parentOptionalArgs = new HashSet();
    private Set m_declaredOptionalArgs = new HashSet();
    private Set m_declaredFragmentArgs = new HashSet();
    private List m_parentFragmentArgs = new LinkedList();

    private Map m_defs = new HashMap();
    private String m_parentSignature = null;
    private final List m_imports = new LinkedList();
    private String m_parentPath = null;
    private boolean m_isParent = false;
    private StringBuffer m_classContent = new StringBuffer();
    private Set m_callPaths = new HashSet();

    public boolean hasRequiredParentArgs()
    {
        return m_parentRequiredArgs.size() + m_parentFragmentArgs.size() > 0;
    }

    public void printParentRequiredArgs(IndentingWriter p_writer)
    {
        printArgs(p_writer,
                  new SequentialIterator
                      (m_parentRequiredArgs.iterator(),
                       m_parentFragmentArgs.iterator()));
    }

    protected void generateInterfaceSummary(StringBuffer p_buf)
    {
        super.generateInterfaceSummary(p_buf);
        if(m_parentSignature != null)
        {
            p_buf.append("Parent sig: ");
            p_buf.append(m_parentSignature);
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
        for(Iterator i = getImports(); i.hasNext(); )
        {
            p_buf.append("import: ");
            p_buf.append((String) i.next());
            p_buf.append("\n");
        }
    }

    public String getSignature()
        throws JamonException
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
            throw new JamonException(e);
        }
    }
}
