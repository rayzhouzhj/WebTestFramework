package com.scmp.framework.annotations.testrail;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface TestRailTestCase {
    int id() default -1;
    String description() default "";
    String testRailUrl() default "";
}
