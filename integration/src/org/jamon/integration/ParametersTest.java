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

public class ParametersTest
    extends TestBase
{
    public void testUnusedAnonDefFragment()
        throws Exception
    {
        expectTemplateException(
            "UnusedAnonDefFragment",
            "Call provides a fragment, but none are expected",
            2, 1);
    }

    public void testUnusedAnonTemplateFragment()
        throws Exception
    {
        expectTemplateException(
            "UnusedAnonTemplateFragment",
            "Call provides a fragment, but none are expected",
            1, 1);
    }

    public void testUnusedNamedTemplateFragment()
        throws Exception
    {
        expectTemplateException("UnusedNamedTemplateFragment",
                                "Call provides unused fragments content",
                                1, 1);
    }

    public void testUnusedNamedDefFragment()
        throws Exception
    {
        expectTemplateException("UnusedNamedDefFragment",
                                "Call provides unused fragments content",
                                2, 1);
    }

    public void testSingleFragmentCallToMultiFragmentUnit()
        throws Exception
    {
        expectTemplateException("MultiFarg",
                                "Call must provide multiple fragments",
                                5, 1);
    }

    public void testUnusedDefArgument()
        throws Exception
    {
        expectTemplateException("UnusedDefArgument",
                                "Call provides unused arguments x",
                                2, 1);
    }

    public void testUnusedTemplateArgument()
        throws Exception
    {
        expectTemplateException("UnusedTemplateArgument",
                                "Call provides unused arguments x",
                                1, 1);
    }

    public void testMissingDefFragment()
        throws Exception
    {
        expectTemplateException(
            "MissingDefFragment", "Call is missing fragment content", 4, 1);
    }

    public void testMissingTemplateFragment()
        throws Exception
    {
        expectTemplateException(
            "MissingTemplateFragment", "Call is missing fragment f", 1, 1);
    }

    public void testMissingRequiredArgumentForDef()
        throws Exception
    {
        expectTemplateException("MissingRequiredArgument",
                                "No value supplied for required argument x",
                                6, 1);
    }

    public void testMissingRequiredArgumentForTemplate()
        throws Exception
    {
        expectTemplateException("MissingTemplateRequiredArgument",
                                "No value supplied for required argument i",
                                1, 1);
    }

    public void testMissingRequiredArgumentForFragment()
        throws Exception
    {
        expectTemplateException("MissingFragmentRequiredArgument",
                                "No value supplied for required argument x",
                                5, 1);
    }

    public void testFictionalParentArgument()
        throws Exception
    {
        expectTemplateException(
            "FictionalParentArgument",
            "/test/jamon/Parent does not have an arg named nosucharg",
            3, 3);
    }

    public void testSettingDefaultForInheritedRequiredArg()
        throws Exception
    {
        expectTemplateException("DefaultForInheritedRequiredArg",
                                "i is an inherited required argument, and may not be given a default value",
                                3, 5);
    }

    public void testSettingDefaultForInheritedFragmentArg()
        throws Exception
    {
        expectTemplateException("DefaultForInheritedFragmentArg",
                                "f is an inherited fragment argument, and may not be given a default value",
                                3, 5);
    }

    public void testDuplicateArgument()
        throws Exception
    {
        expectTemplateException("DuplicateArgument",
                                "multiple arguments named opt1",
                                3, 7);
    }

    public void testDuplicateFragmentArgument()
        throws Exception
    {
        expectTemplateException("DuplicateFragmentArgument",
                                "multiple arguments named f",
                                3, 7);
    }

    public void testXargsWithoutExtends()
        throws Exception
    {
        expectTemplateException(
            "XargsWithoutExtends",
            "xargs may not be declared without extending another template",
            1, 1);
    }
}
