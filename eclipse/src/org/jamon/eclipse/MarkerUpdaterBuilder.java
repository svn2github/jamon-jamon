package org.jamon.eclipse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

public class MarkerUpdaterBuilder extends IncrementalProjectBuilder {

	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		moveAllMarkers(getNature().getTemplateOutputFolder());
		return null;
	}
	
	private void moveMarkers(IResource p_srcFile) throws CoreException {
		IMarker[] markers = p_srcFile.findMarkers(null, true, IResource.DEPTH_INFINITE);
		IPath path = p_srcFile.getProjectRelativePath().removeFirstSegments(1).removeFileExtension();
		if (path.lastSegment().endsWith("Impl")) {
			String name = path.lastSegment();
			path = path.removeLastSegments(1);
			path = path.append(name.substring(0, name.length() - "Impl".length()));
		}
		path = path.addFileExtension(JamonNature.JAMON_EXTENSION);
		
		IFile template = getNature().getTemplateSourceFolder().getFile(path);
		for (int j = 0; j < markers.length; ++j) {
			moveMarker(p_srcFile, markers[j], template);
		}
	}
	
	private void moveMarker(IResource p_srcFile, IMarker p_marker, IFile p_template) throws CoreException {
		System.err.println("Moving marker " + p_marker + " for " + p_srcFile + " to " + p_template);
		// IMarker marker = p_template.createMarker(p_marker.getType());
		// TODO: need to translate line and column number!
		// marker.setAttributes(p_marker.getAttributes());
		// p_marker.delete();
	}
	
	private void moveAllMarkers(IContainer p_container) throws CoreException {
		IResource[] members = p_container.members();
		for (int i = 0; i < members.length; ++i) {
			if (members[i] instanceof IContainer) {
				moveAllMarkers((IContainer) members[i]);
			}
			else {
				moveMarkers(members[i]);
			}
		}
	}
	
	private JamonNature getNature() throws CoreException {
		return (JamonNature) getProject().getNature(JamonNature.natureId());
	}

	private static String builderId() {
		return JamonProjectPlugin.getDefault().pluginId() + ".markerUpdater";
	}

	
	public static void addToProject(IProject p_project) throws CoreException {
		IProjectDescription description = p_project.getDescription();
		ArrayList cmds = new ArrayList();
		cmds.addAll(Arrays.asList(description.getBuildSpec()));
		for (Iterator i = cmds.iterator(); i.hasNext();) {
			if (((ICommand) i.next()).getBuilderName().equals(builderId())) {
				return;
			}
		}
		ICommand jamonCmd = description.newCommand();
		jamonCmd.setBuilderName(builderId());
		cmds.add(jamonCmd);
		description.setBuildSpec((ICommand[]) cmds.toArray(new ICommand[cmds
				.size()]));
		p_project.setDescription(description, null);

	}

}
