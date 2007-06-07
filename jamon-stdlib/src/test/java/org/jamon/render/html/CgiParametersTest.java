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

package org.jamon.render.html;

import junit.framework.TestCase;

@Deprecated public class CgiParametersTest
    extends TestCase
{
    public void testEmpty()
    {
        CgiParameters parameters = new CgiParameters();
        assertEquals( 0, parameters.getInputs().length );
    }

    public void testAddSome()
    {
        CgiParameters parameters = new CgiParameters();
        parameters.add( "A", "B" );
        parameters.add( "C", "D" );
        parameters.add( "E", "F" );
        Input[] inputs = parameters.getInputs();
        assertEquals( 3, inputs.length );
        assertEquals( "A", inputs[0].getName() );
        assertEquals( "C", inputs[1].getName() );
        assertEquals( "E", inputs[2].getName() );
        assertEquals( "B", inputs[0].getValue() );
        assertEquals( "D", inputs[1].getValue() );
        assertEquals( "F", inputs[2].getValue() );
    }


    public void testAddDups()
    {
        CgiParameters parameters = new CgiParameters();
        parameters.add( "A", "B" );
        parameters.add( "A", "C" );
        parameters.add( "A", "D" );
        parameters.add( "E", "F" );
        Input[] inputs = parameters.getInputs();
        assertEquals( 4, inputs.length );
        assertEquals( "A", inputs[0].getName() );
        assertEquals( "A", inputs[1].getName() );
        assertEquals( "A", inputs[2].getName() );
        assertEquals( "E", inputs[3].getName() );
        assertEquals( "B", inputs[0].getValue() );
        assertEquals( "C", inputs[1].getValue() );
        assertEquals( "D", inputs[2].getValue() );
        assertEquals( "F", inputs[3].getValue() );
    }
}
