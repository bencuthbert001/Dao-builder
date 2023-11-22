package com.sussoftware.daobuilder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotation to help describe database
 * objects, so that we can generate automatic DAO code
 */
@Target(ElementType.FIELD)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface DatabaseField {
    String name();
    boolean isPrimaryKey() default false;
    boolean isSearchFieldSingle()default false;
    boolean isTimestampField()default false;
}
