/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Jamon code, released ??.
 *
 * The Initial Developer of the Original Code is Jay Sachs.  Portions
 * created by Jay Sachs are Copyright (C) 2002 Jay Sachs.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */

package org.jamon;

import java.io.PrintWriter;
import java.io.IOException;


/**
 * A <code>Statement</code> represents a generatable java statement
 * as translated from a template.
 */
public interface Statement
{
    /**
     * Generate the java source corresponding to this statement,
     * emitting the code to the specified writer.
     *
     * @param p_writer where to emit the java source
     * @param p_resolver the <code>TemplateResolver</code> to use
     * @param p_describer the <code>TemplateDescriber</code> to use
     * @param p_analyzer the <code>ImplAnalyzer</code> to use
     *
     * @exception IOException if something goes wrong
     */
    void generateSource(PrintWriter p_writer,
                        TemplateResolver p_resolver,
                        TemplateDescriber p_describer,
                        ImplAnalyzer p_analyzer)
        throws IOException;
}
