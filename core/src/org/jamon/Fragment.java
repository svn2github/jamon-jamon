package org.jamon;

import java.io.IOException;

public interface Fragment
{
    void render()
        throws IOException;

    public static final Fragment NULL =
        new Fragment() {
            public void render() { /* */ }
        };
}
