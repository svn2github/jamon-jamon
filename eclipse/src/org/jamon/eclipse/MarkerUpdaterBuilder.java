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
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class MarkerUpdaterBuilder extends IncrementalProjectBuilder {

	@Override
    protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
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
        ResourcesPlugin.getWorkspace()
            .addResourceChangeListener(
                m_javaMarkerListener, IResourceChangeEvent.POST_BUILD);
        return null;
	}

	private static String builderId() {
		return JamonProjectPlugin.getDefault().pluginId() + ".markerUpdater";
	}


	public static void addToProject(IProject p_project) throws CoreException {
		IProjectDescription description = p_project.getDescription();
		ArrayList<ICommand> cmds = new ArrayList<ICommand>();
		cmds.addAll(Arrays.asList(description.getBuildSpec()));
		for (Iterator<ICommand> i = cmds.iterator(); i.hasNext();) {
			if (i.next().getBuilderName().equals(builderId())) {
				return;
			}
		}
		ICommand jamonCmd = description.newCommand();
		jamonCmd.setBuilderName(builderId());
		cmds.add(jamonCmd);
		description.setBuildSpec(cmds.toArray(new ICommand[cmds.size()]));
		p_project.setDescription(description, null);
	}

    private IResourceChangeListener m_javaMarkerListener;
}
