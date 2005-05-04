package org.jamon.eclipse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;
import org.jamon.JamonRuntimeException;
import org.jamon.codegen.Analyzer;
import org.jamon.codegen.ImplGenerator;
import org.jamon.codegen.ProxyGenerator;
import org.jamon.codegen.TemplateDescriber;
import org.jamon.codegen.TemplateUnit;
import org.jamon.emit.EmitMode;
import org.jamon.ParserError;
import org.jamon.ParserErrors;
import org.jamon.util.StringUtils;


public class TemplateBuilder extends IncrementalProjectBuilder {

	private TemplateDependencies m_dependencies = null;
	private void logInfo(String p_message) {
		JamonProjectPlugin.getDefault().logInfo(p_message);
	}
	
	private void logError(Throwable p_error) {
		JamonProjectPlugin.getDefault().logError(p_error);
	}
	
	private IPath getWorkDir() {
		return getProject().getWorkingLocation(JamonProjectPlugin.getDefault().pluginId());
	}
	
	private File getDependencyFile() {
		return new File(getWorkDir().toOSString(), "dependencies");
	}
	
	private void loadDependencies() {
		FileInputStream in;
		try {
			in = new FileInputStream(getDependencyFile());
			m_dependencies = new TemplateDependencies(in);
			in.close();
		}
		catch (FileNotFoundException e) {
			return;
		}
		catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}
	
	private void saveDependencies() {
		try {
			FileOutputStream out = new FileOutputStream(getDependencyFile());
			m_dependencies.save(out);
			out.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {
		if (m_dependencies == null) {
			loadDependencies();
		}
		if (kind == IncrementalProjectBuilder.FULL_BUILD || m_dependencies == null) {
			fullBuild(monitor);
		} else {
			IResourceDelta delta = getDelta(getProject());
			if (delta == null) {
				fullBuild(monitor);
			}
			else {
				incrementalBuild(delta, monitor);
			}
		}
		saveDependencies();
		return null;
	}
	
	protected void clean(IProgressMonitor monitor) throws CoreException {
		IFolder tsrc = getNature().getTemplateOutputFolder();
		IResource[] things = tsrc.members();
		for (int i = 0; i < things.length; ++i) {
			things[i].setReadOnly(false);
			things[i].delete(true, monitor);
		}
     }

	private synchronized void fullBuild(IProgressMonitor monitor) throws CoreException {
		m_dependencies = new TemplateDependencies();
		getProject().accept(new BuildVisitor());
	}
	
	private JamonNature getNature() throws CoreException {
		return (JamonNature) getProject().getNature(JamonNature.natureId());
	}

	private synchronized void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) throws CoreException {
		BuildVisitor visitor = new BuildVisitor();
		delta.accept(visitor);
		Set changed = visitor.getChanged();
		logInfo("Changed templates are " + changed);
		IFolder templateDir = getNature().getTemplateSourceFolder();
		for (Iterator i = changed.iterator(); i.hasNext(); ) {
			IPath s = (IPath) i.next();
			Collection c = m_dependencies.getDependenciesOf(s.toString());
			for (Iterator j = c.iterator(); j.hasNext(); ) {
				visitor.visit(templateDir.findMember((new Path((String) j.next())).addFileExtension(JamonNature.JAMON_EXTENSION)));
			}
		}
	}

	private IJavaProject getJavaProject() throws CoreException {
		return (IJavaProject) (getProject().getNature(JavaCore.NATURE_ID));
	}
	
	private class BuildVisitor implements IResourceVisitor, IResourceDeltaVisitor {
		BuildVisitor() throws CoreException  {
			m_templateDir = getNature().getTemplateSourceFolder();
			m_source = new ResourceTemplateSource(m_templateDir);
			m_describer = new TemplateDescriber(m_source, classLoader());
			m_outFolder = getNature().getTemplateOutputFolder();
			m_changed = new HashSet();
		}
		
		Set getChanged() {
			return m_changed;
		}
		
		private List classpathUrlsForProject(IJavaProject p_project) throws CoreException {
			logInfo("Computing classpath for project " + p_project.getProject().getName());
			List urls = new ArrayList();
			String[] entries = JavaRuntime.computeDefaultRuntimeClassPath(p_project);
			for (int i = 0; i < entries.length; ++i) {
				logInfo("Found entry " + entries[i]);
				try {
					urls.add(new File(entries[i]).toURL());
				}
				catch (MalformedURLException e) {
					logError(e);
				}
			}
			IProject[] dependencies = p_project.getProject().getReferencedProjects();
			for (int i = 0; i < dependencies.length; ++i) {
				if (dependencies[i].hasNature(JavaCore.NATURE_ID)) {
					urls.addAll(classpathUrlsForProject((IJavaProject) dependencies[i].getNature(JavaCore.NATURE_ID)));
				}
				else {
					logInfo("Skipping non-java referenced project " + dependencies[i].getName());
				}
			}
			return urls;
		}
		
		private ClassLoader classLoader() throws CoreException {
			List urls = classpathUrlsForProject(getJavaProject());
			logInfo("Classpath URLs are " + urls);
			// TODO: does this have the proper parent?
			return new URLClassLoader((URL[]) urls.toArray(new URL[urls.size()]));
			
		}

		private final ResourceTemplateSource m_source;
		private final TemplateDescriber m_describer;
		private final IFolder m_templateDir;
		private final IFolder m_outFolder;
		private final Set m_changed;
		
		private void createParents(IContainer p_container) throws CoreException {
			if (! p_container.exists()) {
				createParents(p_container.getParent());
				((IFolder) p_container).create(true, true, null);
			}
		}
		
		private CoreException createCoreException(Exception e) {
			return new CoreException(new Status(IStatus.ERROR,JamonProjectPlugin.getDefault().getBundle().getSymbolicName(), 0, e.getMessage(), e));
		}
		
		private void markFile(ParserError e) throws CoreException
        {
            EclipseUtils.populateProblemMarker(
                ((ResourceTemplateLocation) e.getLocation().getTemplateLocation())
                    .getFile().createMarker(IMarker.PROBLEM), 
                e.getLocation().getLine(),
                e.getMessage(), IMarker.SEVERITY_ERROR);
		}
        
        private void addMarkers(ParserErrors p_errors) throws CoreException
        {
            for (Iterator i = p_errors.getErrors(); i.hasNext(); )
            {
                markFile((ParserError) i.next());
            }
        }
		
		private TemplateUnit analyze(IPath path, IFile file) throws CoreException {
			file.deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
			try {
				return new Analyzer(
                    "/" + StringUtils.filePathToTemplatePath(path.toString()), 
                    m_describer)
                    .analyze();
			}
            catch (ParserErrors e) {
                addMarkers(e);
                return null;
            }
			catch (IOException e) {
				throw createCoreException(e);
			}
			catch (JamonRuntimeException e) {
				throw createCoreException(e);
			}
		}
		
		private byte[] generateProxy(TemplateUnit templateUnit, IFile file) throws CoreException {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try {
				new ProxyGenerator(baos, m_describer, templateUnit).generateClassSource();
			}
			catch (ParserErrors e)
            {
			    addMarkers(e);
            }
            catch (IOException e) {
				throw createCoreException(e);
			}
			catch (JamonRuntimeException e) {
				throw createCoreException(e);
			}
			return baos.toByteArray();
		}
		
		private byte[] generateImpl(TemplateUnit templateUnit, IFile file) throws CoreException {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try {
				new ImplGenerator(baos, m_describer, templateUnit, EmitMode.STANDARD).generateSource();			
			}
            catch (ParserErrors e) {
                addMarkers(e);
            }
            catch (IOException e) {
				throw createCoreException(e);
			}
			catch (JamonRuntimeException e) {
				throw createCoreException(e);
			}
			return baos.toByteArray();
		}
		
        private class TemplateResources
        {
            public TemplateResources(IFile p_templateFile)
            {
                m_template = p_templateFile;
                m_path = m_template
                    .getFullPath()
                    .removeFirstSegments(
                        m_templateDir.getFullPath().segmentCount())
                    .removeFileExtension();
                logInfo("translating Jamon template /" + m_path);
                m_proxy = m_outFolder.getFile(m_path.addFileExtension("java"));
                m_impl = m_outFolder.getFile(
                    m_path.removeLastSegments(1)
                        .append(m_path.lastSegment() + "Impl")
                        .addFileExtension("java"));
            }
            
            public void clearGeneratedResources() throws CoreException
            {
                delete(m_impl);
                delete(m_proxy);
            }
            
            public void generateResources() throws CoreException
            {
                delete(m_impl);
                delete(m_proxy);
                TemplateUnit templateUnit = analyze(m_path, m_template);
                if (templateUnit != null)
                {
                    IPath path = m_path.makeAbsolute();
                    m_dependencies.setCalledBy(
                        path.toString(), templateUnit.getTemplateDependencies());
                    m_changed.add(path);
                    createSourceFile(
                        generateProxy(templateUnit, m_template), m_proxy);
                    createSourceFile(
                        generateImpl(templateUnit, m_template), m_impl);
                }
            }
            
            private void createSourceFile(byte[] contents, IFile p_file) throws CoreException {
                createParents(p_file.getParent());
                p_file.create(new ByteArrayInputStream(contents), true, null);
                // p_file.setReadOnly(true);
            }
            
            private void delete(IFile p_file) throws CoreException
            {
                if (p_file.exists()) 
                {
                    p_file.setReadOnly(false);
                    p_file.delete(true, null);
                }
            }

            private final IPath m_path;
            private final IFile m_template, m_proxy, m_impl;
        }
        
        private TemplateResources makeResources(IResource p_resource)
        {
            if (p_resource.getType() == IResource.FILE) 
            {
                IFile file = (IFile) p_resource;
                if (JamonNature.JAMON_EXTENSION.equals(file.getFileExtension())
                    && m_templateDir.getFullPath().isPrefixOf(file.getFullPath()))
                {
                    return new TemplateResources(file);
                }
            }
            return null;
        }
        
		public boolean visit(IResource p_resource) throws CoreException 
        {
            TemplateResources resources = makeResources(p_resource);
            if (resources != null)
            {
                resources.generateResources();
            }
            return true;
		}

		public boolean visit(IResourceDelta p_delta) throws CoreException 
        {
            switch(p_delta.getKind())
            {
                case IResourceDelta.ADDED :
                case IResourceDelta.CHANGED :
                    visit(p_delta.getResource());
                break;
                case IResourceDelta.REMOVED:
                    TemplateResources resources = 
                        makeResources(p_delta.getResource());
                    if (resources != null)
                    {
                        resources.clearGeneratedResources();
                    }
            }
            return true;
        }
	}

	private static String builderId() {
		return JamonProjectPlugin.getDefault().pluginId() + ".templateBuilder";
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
		cmds.add(0, jamonCmd);
		description.setBuildSpec((ICommand[]) cmds.toArray(new ICommand[cmds
				.size()]));
		p_project.setDescription(description, null);
	}

}