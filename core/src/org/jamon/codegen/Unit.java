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
 * The Original Code is Jamon code, released February, 2003.
 *
 * The Initial Developer of the Original Code is Jay Sachs.  Portions
 * created by Jay Sachs are Copyright (C) 2003 Jay Sachs.  All Rights
 * Reserved.
 *
 * Contributor(s): Ian Robertson
 */

package org.jamon.codegen;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;


public interface Unit
{
    String getName();
    Unit getParent();
    Iterator getFragmentArgs();
    FragmentUnit getFragmentUnitIntf(String p_path);
    Iterator getSignatureRequiredArgs();
    Iterator getSignatureOptionalArgs();
    List getFragmentArgsList();

    void addStatement(Statement p_statement);

    void addFragmentArg(FragmentArgument p_arg);
    void addRequiredArg(RequiredArgument p_arg);
    void addOptionalArg(OptionalArgument p_arg);

    void printRenderArgsDecl(IndentingWriter p_writer);
    void generateRenderBody(IndentingWriter p_writer,
                            TemplateDescriber p_describer)
        throws IOException;
}
