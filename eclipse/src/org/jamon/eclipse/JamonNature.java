package org.jamon.eclipse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
		return JamonProjectPlugin.getDefault().pluginId() + ".jamonnature";
	}
	
	public static boolean projectHasNature(IProject p_project) throws CoreException {
		return p_project.getProject().hasNature(natureId());
	}
	
	private static List<String> naturesList(IProjectDescription p_desc) {
		return new ArrayList<String>(Arrays.asList(p_desc.getNatureIds()));
	}
	
	private static void setNatures(
        IProjectDescription p_description, List<String> p_natures)
    {
		p_description.setNatureIds(p_natures.toArray(new String[p_natures.size()]));
	}
	
	public static void addToProject(IProject p_project, String p_templateSourceDir) throws CoreException {
		IProjectDescription description = p_project.getDescription();
		List<String> natures = naturesList(description);
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
	private static final String JAMON_PREFERENCES_NODE = "org.jamon";
	
	public static void removeFromProject(IProject p_project) throws CoreException 
    {
		IProjectDescription description = p_project.getDescription();
		List<String> natures = naturesList(description);
		if (natures.contains(natureId())) {
			natures.remove(natureId());
			setNatures(description, natures);
			p_project.setDescription(description, null);
		}
	}

	static IFolder templateOutputFolder(IProject p_project)
    {
        // TODO: don't hardcode the generated template directory
        return p_project.getFolder(new Path("tsrc"));
    }

    public IFolder getTemplateOutputFolder() {
        return templateOutputFolder(getProject());
    }
    
    static String templateSourceFolderName(IProject p_project) {
		return preferences(p_project)
            .get(TEMPLATE_SOURCE_DIR_PROPERTY, DEFAULT_TEMPLATE_SOURCE);
	}
    
    static IFolder templateSourceFolder(IProject p_project)
    {
        return p_project.getFolder(
            new Path(templateSourceFolderName(p_project)));
    }
	
	public IFolder getTemplateSourceFolder() {
		IProject project = getProject();
		return project.getFolder(new Path(templateSourceFolderName(project)));
	}

	private void unsetReadOnly(IContainer p_container) throws CoreException {
		IResource[] members = p_container.members();
		for (IResource resource : members)
        {
            EclipseUtils.unsetReadOnly(resource);
			if (resource instanceof IContainer) {
				unsetReadOnly((IContainer) resource);
			}
		}
	}

    private void removeTsrc() throws CoreException {
		IFolder tsrc = getTemplateOutputFolder();
		IJavaProject jp = getJavaProject();
		List<IClasspathEntry> e = 
            new ArrayList<IClasspathEntry>(Arrays.asList(jp.getRawClasspath()));
		e.remove(JavaCore.newSourceEntry(tsrc.getFullPath()));
		jp.setRawClasspath(e.toArray(new IClasspathEntry[e.size()]), null);
		if (tsrc.exists()) {
			unsetReadOnly(tsrc);
			tsrc.delete(IResource.DEPTH_INFINITE, null);
		}
	}
	
	public void configure() throws CoreException 
    {
		TemplateBuilder.addToProject(getProject());
        MarkerUpdaterBuilder.addToProject(getProject());
		removeTsrc();
		IFolder tsrc = getTemplateOutputFolder();
		tsrc.create(true, true, null);
		tsrc.setDerived(true);
			
		IJavaProject jp = getJavaProject();
		List<IClasspathEntry> e = 
            new ArrayList<IClasspathEntry>(Arrays.asList(jp.getRawClasspath()));
		e.add(JavaCore.newSourceEntry(tsrc.getFullPath()));
		jp.setRawClasspath(e.toArray(new IClasspathEntry[e.size()]), null);
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
