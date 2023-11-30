/**
 * ***************************************************************************** This source code is
 * subject to copyright. No part of this work may be reproduced without the consent of Celer
 * Technologies and all rights are reserved, whether the whole or part of this material is
 * concerned, including the rights of translation, reprinting, reproduction, electronic and other
 * dissemination and reuse of illustrations, diagrams, formulae and other content.
 *
 * <p>Copyright Celer Technologies 2018.
 * *****************************************************************************
 */
package com.sussoftware.daobuilder;

import java.util.StringTokenizer;

public class SecondarySearchFieldBO {

    private final String sqlStatement;
    private final String fieldName;
    private final String sqlStatementName;
    private final String type;
    private final boolean shouldReturnMultiple;

    public SecondarySearchFieldBO(String sqlStatement, String fieldName, String sqlStatementName,
                                  Class<?> type,
                                  boolean shouldReturnMultiple
    ) {
        this.sqlStatement = sqlStatement;
        this.fieldName = fieldName;
        this.sqlStatementName = sqlStatementName;
        this.type = initType(type);
        this.shouldReturnMultiple = shouldReturnMultiple;
    }

    private String initType(Class<?> type) {
        final String name = type.getName();
        StringTokenizer stringTokenizer = new StringTokenizer(name, ".");
        final int i = stringTokenizer.countTokens();
        for (int n = 1; n <= i; n++) {
            final String s = stringTokenizer.nextToken();
            if(n == i) {
                return s;
            }
        }
        return type.getName();
    }

    public String getSqlStatement() {
        return sqlStatement;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getSqlStatementName() {
        return sqlStatementName;
    }

    public String getType() {
        return type;
    }

    public boolean isShouldReturnMultiple() {
        return shouldReturnMultiple;
    }

    @Override
    public String toString() {
        return "SecondarySearchFieldBO{" +
                "sqlStatement='" + sqlStatement + '\'' +
                ", fieldName='" + fieldName + '\'' +
                ", sqlStatementName='" + sqlStatementName + '\'' +
                ", type='" + type + '\'' +
                ", shouldReturnMultiple=" + shouldReturnMultiple +
                '}';
    }
}
