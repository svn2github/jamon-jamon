package org.modusponens.jtt;

import java.util.List;

public interface TemplateDescriber
{
    String getIntfClassName(String p_path);
    String getImplClassName(String p_path);
    String getIntfPackageName(String p_path);
    String getImplPackageName(String p_path);
    List getRequiredArgNames(String p_path)
        throws JttException;
}
