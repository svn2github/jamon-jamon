package org.modusponens.jtt;

import java.util.List;

public interface TemplateDescriber
{
    List getRequiredArgNames(String p_path)
        throws JttException;
}
