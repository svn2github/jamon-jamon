package org.jamon.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Fragment
{
    String name();
    Argument[] requiredArguments() default {};
}
