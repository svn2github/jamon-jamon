/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.jamon.compiler;

import org.jamon.api.TemplateLocation;

/**
 * Location of a template stored on the file system.
 */

public class TemplateFileLocation implements TemplateLocation
{
    public TemplateFileLocation(String location)
    {
      this.location = location;
    }

    @Override public String toString()
    {
        return location;
    }

    @Override public boolean equals(Object p_obj)
    {
        return p_obj instanceof TemplateFileLocation
            && location.equals(((TemplateFileLocation) p_obj).location);
    }

    @Override public int hashCode()
    {
        return location.hashCode();
    }

    private final String location;
}
