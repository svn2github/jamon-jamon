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
	
	public long lastModified(String arg0) throws IOException {
		return m_templateFolder.findMember(new Path(arg0).addFileExtension("jamon")).getLocalTimeStamp();
	}

	public boolean available(String arg0) throws IOException {
		// TODO Auto-generated method stub
		return m_templateFolder.findMember(new Path(arg0).addFileExtension("jamon")) != null;
	}

	public InputStream getStreamFor(String arg0) throws IOException {
		IFile file = (IFile) m_templateFolder.findMember(new Path(arg0).addFileExtension("jamon"));
		try {
			return file.getContents(true);
		}
		catch (CoreException e) {
			throw new IOException(e.getMessage());
		}
	}

	public String getExternalIdentifier(String arg0) {
		return arg0;
	}

}
