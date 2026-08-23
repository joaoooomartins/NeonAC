package com.neonac.api.check;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CheckInfo {

    String id();

    String name();

    CheckCategory category();

    String description() default "";
    int since() default 0;
    int until() default 0;
}
