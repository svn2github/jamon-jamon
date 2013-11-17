/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.render.html;

@Deprecated public interface Select<Renderable>
{
    @SuppressWarnings("hiding")
    interface Item<Renderable>
    {
        Renderable getRenderable();
        String getValue();
        boolean isSelected();
    }

    String getName();

    Item<? extends Renderable>[] getItems();
}
