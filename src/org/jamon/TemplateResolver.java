package org.modusponens.jtt;

public class TemplateResolver
{
    private static final String FS = System.getProperty("file.separator");

    public TemplateResolver(String p_packagePrefix)
    {
        m_packagePrefix = p_packagePrefix;
    }

    private final String m_packagePrefix;

    public String getIntfClassName(final String p_path)
    {
        int i = p_path.lastIndexOf(FS);
        return i < 0 ? p_path : p_path.substring(i+1);
    }

    public String getImplClassName(final String p_path)
    {
        return getIntfClassName(p_path) + "Impl";
    }

    public String getIntfPackageName(final String p_path)
    {
        StringBuffer pkg = new StringBuffer();
        if (! "".equals(m_packagePrefix))
        {
            pkg.append(m_packagePrefix);
        }
        int i = p_path.lastIndexOf(FS);
        if (i > 0)
        {
            pkg.append(PathUtils.pathToClassName(p_path.substring(0,i)));
        }
        else
        {
            pkg.deleteCharAt(pkg.length()-1);
        }
        return pkg.toString();
    }

    public String getImplPackageName(final String p_path)
    {
        return getIntfPackageName(p_path);
    }
}
