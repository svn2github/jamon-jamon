package org.jamon.integration;

import java.io.IOException;
import java.io.Writer;

public interface SomeInterface {
    // FIXME: we want covariant return types

//     SomeInterface setX(int x)
//         throws IOException;

//     SomeInterface writeTo(Writer writer)
//         throws IOException;

    void render(String s)
        throws IOException;
}
