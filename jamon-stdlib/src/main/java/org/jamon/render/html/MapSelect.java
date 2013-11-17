/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.render.html;

import java.util.Map;

@Deprecated public class MapSelect<Renderable>
    extends SingleSelect<Renderable>
{
    public <DataType> MapSelect(
        String p_name,
        final Map<? extends DataType, ? extends Renderable> p_options)
    {
        this(p_name, p_options,null);
    }

    public <DataType> MapSelect(
        String p_name,
        final Map<? extends DataType, ? extends Renderable> p_options,
        Object p_default)
    {
        super(p_name,
              p_default == null ? null : p_default.toString(),
              p_options.keySet().iterator(),
              new ItemMaker<DataType, Renderable>() {
                  public Select.Item<Renderable> makeItem(final DataType p_data)
                  {
                      return new SingleSelect.Item<Renderable>()
                      {
                         public Renderable getRenderable()
                             { return p_options.get(p_data); }
                         public String getValue()
                             { return p_data.toString(); }
                          };
                  }
              } );
    }
}
