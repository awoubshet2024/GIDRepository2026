package com.alem.GIA.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)

public @interface Auditable {

    String action();

    String entity() default "";

    String idField() default "";

    boolean captureBefore() default false;
    boolean captureAfter() default false;
}

