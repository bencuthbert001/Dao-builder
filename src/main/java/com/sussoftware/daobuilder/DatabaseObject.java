package com.sussoftware.daobuilder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotation to help describe database
 * objects, so that we can generate automatic DAO code
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DatabaseObject {
    /**
     * Name of the database table
     * @return
     */
    String tableName();
}
