package org.jamon.eclipse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class TemplateDependencies {
	
	private final Map<String, Collection<String>> m_calledBy = 
        new HashMap<String, Collection<String>>();
	private final Map<String, Set<String>> m_dependenciesOf = 
        new HashMap<String, Set<String>>();
	
	public TemplateDependencies() {
		
	}
	
	public void setCalledBy(String p_caller, Collection<String> p_callees) 
    {
		Collection<String> oldDeps = m_calledBy.get(p_caller);
		if (oldDeps != null) 
        {
			for (Iterator<String> i = oldDeps.iterator(); i.hasNext(); ) 
            {
				Set<String> deps = m_dependenciesOf.get(i.next());
				if (deps != null) {
					deps.remove(p_caller);
				}
			}
		}
		m_calledBy.put(p_caller, p_callees);
        for (String callee : p_callees)
        {
			Set<String> deps = m_dependenciesOf.get(callee);
			if (deps == null)
            {
				deps = new HashSet<String>();
				m_dependenciesOf.put(callee, deps);
			}
			deps.add(p_caller);
		}
	}
	
	public Collection<String> getDependenciesOf(String p_callee) {
		Collection<String> deps = m_dependenciesOf.get(p_callee);
		return deps == null ? Collections.EMPTY_SET : deps;
	}
	
	public TemplateDependencies(InputStream p_in) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(p_in));
		String s;
		while ((s = reader.readLine()) != null)
        {
			Set<String> deps = new HashSet<String>();
			String t;
			while ((t = reader.readLine()).length() != 0) 
            {
				deps.add(t);
			}
			setCalledBy(s, deps);
		}
	}
	
	public void save(OutputStream p_out)
    {
		PrintWriter writer = new PrintWriter(p_out);
        for (Map.Entry<String, Collection<String>> entry : m_calledBy.entrySet())
        {
            writer.println(entry.getKey().toString());
            for (String caller : entry.getValue())
            {
                writer.println(caller);
			}
			writer.println();
		}
		writer.flush();
	}

}
