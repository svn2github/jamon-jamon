package org.jamon.eclipse;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.jamon.TemplateSource;

public class ResourceTemplateSource implements TemplateSource {

	ResourceTemplateSource(IFolder templateFolder) {
		m_templateFolder = templateFolder;
	}
	
	private final IFolder m_templateFolder;
	
	private IFile resourceFor(String p_templatePath) {
		return (IFile) m_templateFolder.findMember(new Path(p_templatePath).addFileExtension("jamon"));
	}
	
	public long lastModified(String p_templatePath) throws IOException {
		return resourceFor(p_templatePath).getLocalTimeStamp();
	}

	public boolean available(String p_templatePath) throws IOException {
		return resourceFor(p_templatePath) != null;
	}

	public InputStream getStreamFor(String p_templatePath) throws IOException {
		try {
			return resourceFor(p_templatePath).getContents(true);
		}
		catch (CoreException e) {
			throw new IOException(e.getMessage());
		}
	}

	public String getExternalIdentifier(String p_templatePath) {
		return p_templatePath;
	}

}
