package org.jamon.codegen;

import java.io.OutputStream;

public interface SourceGenerator
{
    void generateSource(OutputStream p_out) throws java.io.IOException;
}
