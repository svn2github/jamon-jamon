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
	
	private final Map m_calledBy = new HashMap();
	private final Map m_dependenciesOf = new HashMap();
	
	public TemplateDependencies() {
		
	}
	
	public void setCalledBy(String p_caller, Collection p_callees) {
		Collection oldDeps = (Collection) m_calledBy.get(p_caller);
		if (oldDeps != null) {
			for (Iterator i = oldDeps.iterator(); i.hasNext(); ) {
				Set deps = (Set) m_dependenciesOf.get(i.next());
				if (deps != null) {
					deps.remove(p_caller);
				}
			}
		}
		m_calledBy.put(p_caller, p_callees);
		for (Iterator i = p_callees.iterator(); i.hasNext(); ) {
			Object callee = i.next();
			Set deps = (Set) m_dependenciesOf.get(callee);
			if (deps == null) {
				deps = new HashSet();
				m_dependenciesOf.put(callee, deps);
			}
			deps.add(p_caller);
		}
	}
	
	public Collection getDependenciesOf(String p_callee) {
		Collection deps = (Collection) m_dependenciesOf.get(p_callee);
		return deps == null ? Collections.EMPTY_SET : deps;
	}
	
	public TemplateDependencies(InputStream p_in) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(p_in));
		String s;
		while ((s = reader.readLine()) != null) {
			Set deps = new HashSet();
			String t;
			while ((t = reader.readLine()).length() != 0) {
				deps.add(t);
			}
			setCalledBy(s, deps);
		}
	}
	
	public void save(OutputStream p_out) {
		PrintWriter writer = new PrintWriter(p_out);
		for (Iterator i = m_calledBy.entrySet().iterator(); i.hasNext(); ) {
			Map.Entry entry = (Map.Entry) i.next();
			writer.println(entry.getKey().toString());
			Set deps = (Set) entry.getValue();
			for (Iterator j = deps.iterator(); j.hasNext(); ) {
				writer.println(j.next());
			}
			writer.println();
		}
		writer.flush();
	}

}
