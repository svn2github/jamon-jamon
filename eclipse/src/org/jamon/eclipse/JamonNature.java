package org.jamon.eclipse;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

public class JamonNature implements IProjectNature {

	private static String natureId() {
		return JamonPlugin.getDefault().getBundle().getSymbolicName() + ".jamonnature";
	}
	
	public static boolean projectHasNature(IProject p_project) throws CoreException {
		return p_project.getProject().hasNature(natureId());
	}
	
	private static ArrayList naturesList(IProjectDescription p_desc) throws CoreException {
		return new ArrayList(Arrays.asList(p_desc.getNatureIds()));
	}
	
	private static void setNatures(IProjectDescription p_description, ArrayList p_natures) {
		p_description.setNatureIds((String[]) p_natures.toArray(new String[p_natures.size()]));
	}
	
	public static void addToProject(IProject p_project) throws CoreException {
		IProjectDescription description = p_project.getDescription();
		ArrayList natures = naturesList(description);
		if (natures.contains(natureId())) {
			return;
		}
		natures.add(natureId());
		setNatures(description, natures);
		p_project.setDescription(description, null);
	}

	public static void removeFromProject(IProject p_project) throws CoreException {
		IProjectDescription description = p_project.getDescription();
		ArrayList natures = naturesList(description);
		if (natures.contains(natureId())) {
			natures.remove(natureId());
			setNatures(description, natures);
			p_project.setDescription(description, null);
		}
	}


	private IFolder getTemplateOutputFolder() {
		// TODO: don't hardcode the generated template directory
		return getProject().getFolder(new Path("tsrc"));
	}
	
	public void configure() throws CoreException {

		JamonProjectBuilder.addToProject(getProject());
		
		IFolder tsrc = getTemplateOutputFolder();
		if (tsrc.exists()) {
			tsrc.delete(IResource.DEPTH_INFINITE, null);
		}
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
	}

	public IProject getProject() {
		return m_project;
	}

	public void setProject(IProject project) {
		m_project = project;
	}
	
	private IProject m_project;

}
