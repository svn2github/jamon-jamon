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

import java.util.Map;

public class MapSelect
    extends SingleSelect
{
    public MapSelect(String p_name, final Map p_options)
    {
        this(p_name, p_options,null);
    }

    public MapSelect(String p_name, final Map p_options, Object p_default)
    {
        super(p_name,
              p_default == null ? null : p_default.toString(),
              p_options.keySet().iterator(),
              new ItemMaker() {
                  public Select.Item makeItem(final Object p_data)
                  {
                      return new SingleSelect.Item()
                          {
                              @Override public Object getRenderable()
                                  { return p_options.get(p_data); }
                              @Override public String getValue()
                                  { return p_data.toString(); }
                          };
                  }
              } );
    }
}
