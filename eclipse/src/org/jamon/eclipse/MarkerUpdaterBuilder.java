/*
 * Created on Jun 30, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.jamon.eclipse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author jay
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MarkerUpdaterBuilder extends IncrementalProjectBuilder {

	/* (non-Javadoc)
	 * @see org.eclipse.core.internal.events.InternalBuilder#build(int, java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {
		// TODO Auto-generated method stub
		System.err.println("Moving markers from generated source to templates; nature is " + getNature());
		return null;
	}
	
	private JamonNature getNature() throws CoreException {
		return (JamonNature) getProject().getNature(JamonNature.natureId());
	}

	private static String builderId() {
		return JamonPlugin.getDefault().pluginId() + ".markerUpdater";
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
