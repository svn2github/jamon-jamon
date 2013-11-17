/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

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
