package org.jamon.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Method 
{
    String name();
    Argument[] requiredArguments() default {};
    Argument[] optionalArguments() default {};
    Fragment[] fragmentArguments() default {};
}
