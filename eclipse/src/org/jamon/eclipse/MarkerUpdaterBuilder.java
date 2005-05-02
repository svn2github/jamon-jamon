package org.jamon.eclipse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class MarkerUpdaterBuilder extends IncrementalProjectBuilder {

	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
        throws CoreException 
    {
        //FIXME - there has to be a better way.  We don't want really want a 
        // builder here, because it's not when the impl and proxies change, it's
        // when they are marked that we need to handle things.  A resource 
        // change listener is what we need.  But a builder seems to be the only
        // way to instantiate the listener.  Moreover, there needs to be a way
        // to remove the listener if the project is closed, or if the jamon
        // nature is removed.
        
        if (m_javaMarkerListener == null)
        {
            m_javaMarkerListener = new JavaMarkerListener(
                JamonNature.templateSourceFolder(getProject()),
                JamonNature.templateOutputFolder(getProject()));
        }
        org.eclipse.core.resources.ResourcesPlugin.getWorkspace()
            .addResourceChangeListener(
                m_javaMarkerListener, IResourceChangeEvent.POST_BUILD);
        return null;
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

    private IResourceChangeListener m_javaMarkerListener;
}
