package org.jamon.eclipse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

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
import org.jamon.JamonRuntimeException;
import org.jamon.JamonTemplateException;
import org.jamon.codegen.Analyzer;
import org.jamon.codegen.ImplGenerator;
import org.jamon.codegen.ProxyGenerator;
import org.jamon.codegen.TemplateDescriber;
import org.jamon.codegen.TemplateUnit;
import org.jamon.emit.EmitMode;
import org.jamon.util.StringUtils;

public class JamonProjectBuilder extends IncrementalProjectBuilder {

	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {
		if (kind == IncrementalProjectBuilder.FULL_BUILD) {
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
		return null;
	}

	private void fullBuild(IProgressMonitor monitor) throws CoreException {
		try {
			getProject().accept(new BuildVisitor());
		}
		catch (CoreException e) {
		}
	}
	
	private static final String JAMON_EXTENSION = "jamon";
	
	private void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) throws CoreException {
		delta.accept(new BuildVisitor());
	}
	
	private class BuildVisitor implements IResourceVisitor, IResourceDeltaVisitor {
		BuildVisitor() {
			m_templateDir = getProject().getFolder(new Path("templates"));
			m_source = new ResourceTemplateSource(m_templateDir);
			// TODO: this is certainly not the right class loader to use ...
			// Instead, get the resolved IClassPath[] from the JavaProject, filter out
			//  source entries, and then create a UrlClassLoader out of them (?)
			m_describer = new TemplateDescriber(m_source, getClass().getClassLoader());
			m_outFolder = getProject().getFolder(new Path("tsrc"));
		}
		
		private final ResourceTemplateSource m_source;
		private final TemplateDescriber m_describer;
		private final IFolder m_templateDir;
		private final IFolder m_outFolder;
		
		private void createParents(IContainer p_container) throws CoreException {
			if (! p_container.exists()) {
				createParents(p_container.getParent());
				((IFolder) p_container).create(true, true, null);
			}
		}
		
		private void delete(IFile p_file) throws CoreException {
			if (p_file.exists()) {
				p_file.setReadOnly(false);
				p_file.delete(true, null);
			}
		}
		
		private CoreException createCoreException(Exception e) {
			return new CoreException(new Status(IStatus.ERROR,JamonPlugin.getDefault().getBundle().getSymbolicName(), 0, e.getMessage(), e));
		}
		
		private void markFile(IFile file, JamonTemplateException e) throws CoreException {
			IMarker marker = file.createMarker(IMarker.PROBLEM);
			marker.setAttribute(IMarker.LINE_NUMBER, e.getLine());
			marker.setAttribute(IMarker.MESSAGE, e.getMessage());
			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
		}
		
		private TemplateUnit analyze(IPath path, IFile file) throws CoreException {
			file.deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
			try {
				return new Analyzer("/" + StringUtils.filePathToTemplatePath(path.toString()), m_describer).analyze();
			}
			catch (IOException e) {
				throw createCoreException(e);
			}
			catch (JamonTemplateException e) {
				markFile(file, e);
				return null;
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
			catch (IOException e) {
				throw createCoreException(e);
			}
			catch (JamonTemplateException e) {
				markFile(file, e);
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
			catch (IOException e) {
				throw createCoreException(e);
			}
			catch (JamonTemplateException e) {
				markFile(file, e);
			}
			catch (JamonRuntimeException e) {
				throw createCoreException(e);
			}
			return baos.toByteArray();
		}
		
		private void createSourceFile(byte[] contents, IFile p_file) throws CoreException {
			createParents(p_file.getParent());
			p_file.create(new ByteArrayInputStream(contents), true, null);
			p_file.setReadOnly(true);
		}
		
		public boolean visit(IResource resource) throws CoreException {
			if (resource.getType() == IResource.FILE) {
				IFile file = (IFile) resource;
				if (JAMON_EXTENSION.equals(file.getFileExtension())) {
					if (m_templateDir.getFullPath().isPrefixOf(file.getFullPath())) {
						IPath path = file.getFullPath().removeFirstSegments(m_templateDir.getFullPath().segmentCount()).removeFileExtension();
						System.err.println("translating Jamon template " + path);
						IFile pFile = m_outFolder.getFile(path.addFileExtension("java"));
						delete(pFile);
						IFile iFile = m_outFolder.getFile(path.removeLastSegments(1).append(path.lastSegment() + "Impl").addFileExtension("java"));
						delete(iFile);
						TemplateUnit templateUnit = analyze(path, file);
						if (templateUnit != null) {
							createSourceFile(generateProxy(templateUnit, file), pFile);
							createSourceFile(generateImpl(templateUnit, file), iFile);
						}
					}
				}
			}
			return true;
		}

		public boolean visit(IResourceDelta delta) throws CoreException {
			return visit(delta.getResource());
		}
		
	}

	private static String builderId() {
		return JamonPlugin.getDefault().getBundle().getSymbolicName()
				+ ".templateBuilder";
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