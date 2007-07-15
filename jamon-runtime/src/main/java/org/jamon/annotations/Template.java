package org.jamon.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Template
{
    Argument[] requiredArguments() default {};
    Argument[] optionalArguments() default {};
    Fragment[] fragmentArguments() default {};
    Method[] methods() default {};
    String[] abstractMethodNames() default {};
    String signature();
    int genericsCount() default 0;
    int inheritanceDepth() default 0;
    String jamonContextType() default "";
}
