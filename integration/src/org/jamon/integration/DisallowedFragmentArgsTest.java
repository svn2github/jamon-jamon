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
 * The Initial Developer of the Original Code is Ian Robertson.  Portions
 * created by Ian Robertson are Copyright (C) 2003 Ian Robertson.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */

package org.jamon.integration;

import org.jamon.JamonException;

public class DisallowedFragmentArgsTest
    extends TestBase
{
    public void testFragmentInFragment()
        throws Exception
    {
        expectTemplateException("FragmentInFragment",
                                "Fragments cannot have fragment arguments",
                                5, 10);
    }

    public void testOptionalArgInFragment()
        throws Exception
    {
        expectTemplateException("OptionalArgInFragment",
                                "Fragments cannot have optional arguments",
                                5, 16);
    }

    public void testUnusedArgumentToFragment()
        throws Exception
    {
        expectTemplateException("UnusedArgumentToFragment",
                                "fragment f doesn't expect args i",
                                2, 1);
    }
}
