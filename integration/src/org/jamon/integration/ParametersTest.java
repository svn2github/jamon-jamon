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
        checkForFailure
            ("UnusedAnonDefFragment",
             "Call to foo provides a fragment, but none are expected");
    }

    public void testUnusedAnonTemplateFragment()
        throws Exception
    {
        checkForFailure("UnusedAnonTemplateFragment",
                        "Call to /test/jamon/Arguments provides a fragment, but none are expected");
    }

    public void testUnusedNamedTemplateFragment()
        throws Exception
    {
        checkForFailure("UnusedNamedTemplateFragment",
                        "Call to /test/jamon/Arguments provides unused fragments content");
    }

    public void testUnusedNamedDefFragment()
        throws Exception
    {
        checkForFailure("UnusedNamedDefFragment",
                        "Call to foo provides unused fragments content");
    }

    public void testSingleFragmentCallToMultiFragmentUnit()
        throws Exception
    {
        checkForFailure("MultiFarg",
                        "Call to foo must provide multiple fragments");
    }

    public void testUnusedDefArgument()
        throws Exception
    {
        checkForFailure("UnusedDefArgument",
                        "Call to foo provides unused arguments x");
    }

    public void testUnusedTemplateArgument()
        throws Exception
    {
        checkForFailure
            ("UnusedTemplateArgument",
             "Call to /test/jamon/Arguments provides unused arguments x");
    }

    public void testMissingDefFragment()
        throws Exception
    {
        checkForFailure
            ("MissingDefFragment",
             "Call to foo is missing fragment content");
    }

    public void testMissingTemplateFragment()
        throws Exception
    {
        checkForFailure
            ("MissingTemplateFragment",
             "Call to /test/jamon/SubZ is missing fragment f");
    }

    public void testMissingRequiredArgumentForDef()
        throws Exception
    {
        checkForFailure(
            "MissingRequiredArgument",
            "No value supplied for required argument x in call to foo");
    }

    public void testMissingRequiredArgumentForTemplate()
        throws Exception
    {
        checkForFailure("MissingTemplateRequiredArgument",
                        "No value supplied for required argument i in call to /test/jamon/Arguments");
    }

    public void testFictionalParentArgument()
        throws Exception
    {
        expectTemplateException(
            "test/jamon/broken/FictionalParentArgument",
            "/test/jamon/Parent does not have an arg named nosucharg",
            3,
            3);
    }

    public void testSettingDefaultForInheritedRequiredArg()
        throws Exception
    {
        expectTemplateException(
            "test/jamon/broken/DefaultForInheritedRequiredArg",
            "i is an inherited required argument, and may not be given a default value",
            3,
            5);
    }

    public void testSettingDefaultForInheritedFragmentArg()
        throws Exception
    {
        expectTemplateException(
            "test/jamon/broken/DefaultForInheritedFragmentArg",
            "f is an inherited fragment argument, and may not be given a default value",
            3,
            5);
    }

    public void testDuplicateArgument()
        throws Exception
    {
        checkForFailure("DuplicateArgument",
                        "/test/jamon/broken/DuplicateArgument has multiple arguments named opt1");
    }

    public void testDuplicateFragmentArgument()
        throws Exception
    {
        checkForFailure("DuplicateFragmentArgument",
                        "/test/jamon/broken/DuplicateFragmentArgument has multiple arguments named f");
    }

    public void testXargsWithoutExtends()
        throws Exception
    {
        expectTemplateException(
            "test/jamon/broken/XargsWithoutExtends",
            "xargs may not be declared without extending another template",
            1,
            1);
    }
}
