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
 * Contributor(s):
 */

package org.jamon.integration;

import org.jamon.Invoker;
import java.util.List;
import java.util.Iterator;

public class InvokerTest
    extends TestBase
{
    public void testArgumentReflection()
        throws Exception
    {
        Invoker invoker = new Invoker("/test/jamon/Grandchild");
        List args = invoker.getRequiredArgumentNames();
        assertEquals(5, args.size());
        assertEquals("i", args.get(0));
        assertEquals("j", args.get(1));
        assertEquals("a", args.get(2));
        assertEquals("b", args.get(3));
        assertEquals("x", args.get(4));
        assertEquals(Integer.TYPE, invoker.getArgumentType("i"));
        assertEquals(Integer.class, invoker.getArgumentType("j"));
        assertEquals(String.class, invoker.getArgumentType("a"));
        assertEquals(Boolean.TYPE, invoker.getArgumentType("b"));
        assertEquals(Boolean.class, invoker.getArgumentType("x"));

        args = invoker.getOptionalArgumentNames();
        assertEquals(10, args.size());
        for (int i = 1; i <= 10; ++i)
        {
            String name = "opt" + i;
            assertTrue(args.contains(name));
            assertEquals(String.class, invoker.getArgumentType(name));
        }
    }
}
