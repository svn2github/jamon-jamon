package org.jamon.eclipse;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

public class TemplateResources
{
    public TemplateResources(
        IFile p_templateFile, IFolder p_outFolder, IFolder p_templateFolder)
    {
        m_template = p_templateFile;
        m_path = m_template
            .getFullPath()
            .removeFirstSegments(
                p_templateFolder.getFullPath().segmentCount())
            .removeFileExtension();
        EclipseUtils.logInfo("translating Jamon template /" + m_path);
        m_proxy = p_outFolder.getFile(m_path.addFileExtension("java"));
        m_impl = p_outFolder.getFile(
            m_path.removeLastSegments(1)
                .append(m_path.lastSegment() + "Impl")
                .addFileExtension("java"));
    }

    public void clearGeneratedResources() throws CoreException
    {
        EclipseUtils.delete(m_impl);
        EclipseUtils.delete(m_proxy);
    }

    public IFile getImpl()
    {
        return m_impl;
    }

    public IFile getProxy()
    {
        return m_proxy;
    }

    public IFile getTemplate()
    {
        return m_template;
    }

    public IPath getPath()
    {
        return m_path;
    }

    final IPath m_path;
    final IFile m_impl;
    final IFile m_template;
    final IFile m_proxy;
}