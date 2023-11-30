package com.sussoftware.daobuilder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to help describe database
 * objects, so that we can generate automatic DAO code
 */
@Target(ElementType.FIELD)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface DatabaseField {
    /**
     * Name of the field
     * @return String
     */
    String name();

    /**
     * If set to true, then this will be used in the primary key lookup
     * @return boolean
     */
    boolean isPrimaryKey() default false;

    /**
     * If true, then an additional search statement will be created to allow additional select by
     * @return boolean
     */
    boolean isSearchField() default false;

    /**
     * If set to true, then a statement with a return of List of POJOs
     * @return boolean
     */
    boolean isSearchFieldMultiple() default false;

    /**
     * This will create the correct constants file SQL statement name
     * @return String
     */
    String searchFieldSqlName() default "BY_CODE";

    /**
     * Time stamp helps with formatting using Timestamp values in the update and create statements.
     * @return boolean
     */
    boolean isTimestampField() default false;
}
