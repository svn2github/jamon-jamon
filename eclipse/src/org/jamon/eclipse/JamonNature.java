package org.jamon.eclipse;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.osgi.service.prefs.BackingStoreException;

public class JamonNature implements IProjectNature {

	static String natureId() {
		return JamonPlugin.getDefault().pluginId() + ".jamonnature";
	}
	
	public static boolean projectHasNature(IProject p_project) throws CoreException {
		return p_project.getProject().hasNature(natureId());
	}
	
	private static ArrayList naturesList(IProjectDescription p_desc) {
		return new ArrayList(Arrays.asList(p_desc.getNatureIds()));
	}
	
	private static void setNatures(IProjectDescription p_description, ArrayList p_natures) {
		p_description.setNatureIds((String[]) p_natures.toArray(new String[p_natures.size()]));
	}
	
	public static void addToProject(IProject p_project, String p_templateSourceDir) throws CoreException {
		IProjectDescription description = p_project.getDescription();
		ArrayList natures = naturesList(description);
		if (! natures.contains(natureId())) {
			natures.add(natureId());
			setNatures(description, natures);
			p_project.setDescription(description, null);
		}
		IEclipsePreferences projectNode = preferences(p_project);
        if (projectNode != null) {
        	projectNode.put(TEMPLATE_SOURCE_DIR_PROPERTY, p_templateSourceDir);
        	try {
        		projectNode.flush();
        	}
        	catch (BackingStoreException e) {
        		e.printStackTrace();
        	}
        }
        else {
        	System.err.println("Couldn't find preferences node");
        }
	}
	
	private static IEclipsePreferences preferences(IProject p_project) {
		IScopeContext projectScope = new ProjectScope(p_project);
		return projectScope.getNode(JAMON_PREFERENCES_NODE);
	}

	private static final String TEMPLATE_SOURCE_DIR_PROPERTY = "templateSourceDir";
	private static final String JAMON_PREFERENCES_NODE = "org.jamon.eclipse";
	
	public static void removeFromProject(IProject p_project) throws CoreException {
		IProjectDescription description = p_project.getDescription();
		ArrayList natures = naturesList(description);
		if (natures.contains(natureId())) {
			natures.remove(natureId());
			setNatures(description, natures);
			p_project.setDescription(description, null);
		}
	}


	public IFolder getTemplateOutputFolder() {
		// TODO: don't hardcode the generated template directory
		return getProject().getFolder(new Path("tsrc"));
	}
	
	static String templateSourceFolder(IProject p_project) {
		return preferences(p_project).get(TEMPLATE_SOURCE_DIR_PROPERTY, DEFAULT_TEMPLATE_SOURCE);
	}
	
	public IFolder getTemplateSourceFolder() {
		IProject project = getProject();
		return project.getFolder(new Path(templateSourceFolder(project)));
	}

	private void unsetReadOnly(IContainer p_container) throws CoreException {
		IResource[] members = p_container.members();
		for (int i = 0; i < members.length; ++i) {
			members[i].setReadOnly(false);
			if (members[i] instanceof IContainer) {
				unsetReadOnly((IContainer) members[i]);
			}
		}
	}
	
	private void removeTsrc() throws CoreException {
		IFolder tsrc = getTemplateOutputFolder();
		IJavaProject jp = getJavaProject();
		ArrayList e = new ArrayList(Arrays.asList(jp.getRawClasspath()));
		e.remove(JavaCore.newSourceEntry(tsrc.getFullPath()));
		jp.setRawClasspath((IClasspathEntry[]) e.toArray(new IClasspathEntry[e.size()]), null);
		if (tsrc.exists()) {
			unsetReadOnly(tsrc);
			tsrc.delete(IResource.DEPTH_INFINITE, null);
		}
	}
	
	public void configure() throws CoreException {

		JamonProjectBuilder.addToProject(getProject());
		MarkerUpdaterBuilder.addToProject(getProject());
		removeTsrc();
		IFolder tsrc = getTemplateOutputFolder();
		tsrc.create(true, true, null);
		tsrc.setDerived(true);
			
		IJavaProject jp = getJavaProject();
		ArrayList e = new ArrayList(Arrays.asList(jp.getRawClasspath()));
		e.add(JavaCore.newSourceEntry(tsrc.getFullPath()));
		jp.setRawClasspath((IClasspathEntry[]) e.toArray(new IClasspathEntry[e.size()]), null);
	}
	
	private IJavaProject getJavaProject() throws CoreException {
		return (IJavaProject) (getProject().getNature(JavaCore.NATURE_ID));
	}

	public void deconfigure() throws CoreException {
		removeTsrc();
	}

	public IProject getProject() {
		return m_project;
	}

	public void setProject(IProject project) {
		m_project = project;
	}
	
	private IProject m_project;
	static final String JAMON_EXTENSION = "jamon";
	static final String DEFAULT_TEMPLATE_SOURCE = "templates";

}
