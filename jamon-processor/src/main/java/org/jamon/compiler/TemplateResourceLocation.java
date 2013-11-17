/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.jamon.compiler;

import org.jamon.api.TemplateLocation;

public class TemplateResourceLocation implements TemplateLocation
{
    public TemplateResourceLocation(String location)
    {
      this.location = location;
    }

    @Override public String toString()
    {
        return location;
    }

    @Override public boolean equals(Object obj)
    {
        return obj instanceof TemplateResourceLocation
            && location.equals(((TemplateResourceLocation) obj).location);
    }

    @Override public int hashCode()
    {
        return location.hashCode();
    }

    private final String location;

}
