package com.earac.api.check;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declarative metadata for a check. The engine reads this to build the
 * configuration node, default thresholds and documentation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CheckInfo {

    String id();

    String name();

    CheckCategory category();

    String description() default "";

    /**
     * Minimum Minecraft version key (minor) where this check is meaningful.
     */
    int since() default 0;

    /**
     * Maximum Minecraft version key (minor) where this check applies (0 = unlimited).
     */
    int until() default 0;
}
