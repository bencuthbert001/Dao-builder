package com.sussoftware.daobuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This is a simple DAO class builder, that will create a java class
 * for a given POJO that is annotated with the @{DatabaseObject and @DatabaseField}
 */
public class DAOBuilder {

    private String directory;
    private String PACKAGE_NAME;
    private final static String SELECT_ALL = "SELECT * FROM %s";
    private final static String SELECT_SECONDARY = "SELECT * FROM %s WHERE %s = :%s";
    private final static String DELETE_WHERE_ID = "DELETE FROM %s WHERE %s = :%s";
    private final static String FIND_WHERE_ID = "SELECT * FROM %s WHERE %s = :%s";
    private final static String INSERT_INTO = "INSERT INTO %s(%s) VALUES(%s)";
    private final static String UPDATE = "UPDATE %s SET %s WHERE %s";
    private boolean shouldCreateArchive;

    public DAOBuilder() {

    }

    public void start(String className, String directory, String packageName, String journal) {
        this.shouldCreateArchive = false;
        if(journal != null) {
            shouldCreateArchive = true;
        }
        try {
            Class c = Class.forName(className);
            this.directory = directory;
            this.PACKAGE_NAME = packageName;
            buildDAO(c);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String args[]) {
        final String className = System.getProperty("className");
        if(className == null) {
            System.out.println("invalid className property, please specify");
            System.exit(-1);
        }

        final String directory = System.getProperty("directory");
        if(directory == null) {
            System.out.println("invalid directory property, please specify");
            System.exit(-1);
        }

        final String packageName = System.getProperty("packageName");
        if(packageName == null) {
            System.out.println("invalid packageName property, please specify");
            System.exit(-1);
        }

        final String journal = System.getProperty("journal");
        if(journal == null) {
            System.out.println("invalid journal property, please specify");
            System.exit(-1);
        }

       new DAOBuilder().start(className, directory, packageName, journal);
    }

    public final void buildDAO(Class object) {
        String name = object.getSimpleName();
        String boName = object.getSimpleName();
        if (name.contains("BO")) {
            name = name.replaceAll("BO", "");
        }
        String newDaoName = name + "DaoImpl";
        String newDaoInterfaceName = name + "Dao";
        String constantsName = name + "Constants";
        final DatabaseObject annotation = (DatabaseObject) object.getDeclaredAnnotation(DatabaseObject.class);
        final String tableName = annotation.tableName();
        if(tableName == null) {
            System.out.println("Please specify the table property on the object "+boName);
            System.exit(-1);
        }
        String selectAllStatement = String.format(SELECT_ALL, "\"+"+constantsName+".TABLE_NAME+\"");
        String primaryKeyFieldName = null;
        String deletePrimaryKeyStatement = null;
        String findWherePrimaryKeyStatement = null;
        final Method[] declaredMethods = object.getDeclaredMethods();
        final Field[] declaredFields = object.getDeclaredFields();

        List<SecondarySearchFieldBO> secondarySearchFieldBOList = new ArrayList<>();
        
        List<Field> databaseFields = new ArrayList<>();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(DatabaseField.class)) {
                DatabaseField dbField = field.getAnnotation(DatabaseField.class);
                databaseFields.add(field);
                boolean primaryKey = dbField.isPrimaryKey();
                boolean isSearchField = dbField.isSearchField();
                if (primaryKey) {
                    deletePrimaryKeyStatement = String.format(DELETE_WHERE_ID, "\"+"+constantsName+".TABLE_NAME+\"", dbField.name(), dbField.name());
                    findWherePrimaryKeyStatement = String.format(FIND_WHERE_ID, "\"+"+constantsName+".TABLE_NAME+\"", dbField.name(), dbField.name());
                    primaryKeyFieldName = field.getName();
                }

                if (isSearchField) {
                    boolean shouldReturnMultiple = dbField.isSearchFieldMultiple();
                    String secondaryKeySearch = String.format(SELECT_SECONDARY, "\"+"+constantsName+".TABLE_NAME+\"", dbField.name(), dbField.name());
                    String secondarySearchFieldName = field.getName();
                    String sqlStatementName =  dbField.searchFieldSqlName();
                    final Class<?> type = field.getType();
                    secondarySearchFieldBOList.add(new SecondarySearchFieldBO(secondaryKeySearch, secondarySearchFieldName, sqlStatementName, type, shouldReturnMultiple));
                }
            }
        }

        String columnNames = getColumnNamesFromFields(databaseFields);
        String columnValues = getColumnValuesFromFields(databaseFields);
        String columnsForUpdate = getColumnsForUpdate(databaseFields);
        String updateWhereStatement = getUpdateWhereStatement(databaseFields);
        String insertStatement = String.format(INSERT_INTO, "\"+"+constantsName+".TABLE_NAME+\"", columnNames, columnValues);
        String updateStatement = String.format(UPDATE, "\"+"+constantsName+".TABLE_NAME+\"", columnsForUpdate, updateWhereStatement);

        String insertJournal = null;
        if(this.shouldCreateArchive) {
            insertJournal = String.format(INSERT_INTO, "\"+"+constantsName+".TABLE_NAME_JOURNAL+\"", columnNames, columnValues);
        }

        final String implClass = buildJavaClass(newDaoName, newDaoInterfaceName, boName, databaseFields, constantsName, secondarySearchFieldBOList, primaryKeyFieldName, declaredMethods, object);
        final String interfaceClass = builderInterfaceClass(newDaoInterfaceName, boName, primaryKeyFieldName, secondarySearchFieldBOList, object);
        final String constantsClass = buildMemberConstantsClass(this.shouldCreateArchive, constantsName, tableName, databaseFields, insertStatement, selectAllStatement, deletePrimaryKeyStatement, secondarySearchFieldBOList, updateStatement, findWherePrimaryKeyStatement, insertJournal);

        final File baseDir = new File(directory);
        if(!baseDir.exists()) {
            baseDir.mkdir();
        }

        String implFileName = directory + "/" + newDaoName + ".java";
        String constantsFileName = directory + "/" + constantsName + ".java";
        String interfaceName = directory + "/" + newDaoInterfaceName + ".java";
        try {
            final File file = new File(implFileName);
            if(!file.exists()) {
                file.createNewFile();
            }else {
                file.delete();
                file.createNewFile();
            }
            final FileWriter filterWriter = new FileWriter(file);
            filterWriter.write(implClass);
            filterWriter.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            final File interfaceFile = new File(interfaceName);
            if(!interfaceFile.exists()) {
                interfaceFile.createNewFile();
            }else {
                interfaceFile.delete();
                interfaceFile.createNewFile();
            }
            final FileWriter fileWriter = new FileWriter(interfaceFile);
            fileWriter.write(interfaceClass);
            fileWriter.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            final File constantsFile = new File(constantsFileName);
            if(!constantsFile.exists()) {
                constantsFile.createNewFile();
            }else {
                constantsFile.delete();
                constantsFile.createNewFile();
            }
            final FileWriter fileWriter = new FileWriter(constantsFile);
            fileWriter.write(constantsClass);
            fileWriter.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getUpdateWhereStatement(List<Field> databaseFields) {
        StringBuilder builder = new StringBuilder();
        for (Field field : databaseFields
        ) {
            DatabaseField dbField = field.getAnnotation(DatabaseField.class);
            final String name = dbField.name();
            boolean isPrimaryKey = dbField.isPrimaryKey();
            if(isPrimaryKey) {
                builder.append(name);
                builder.append(" = ");
                builder.append(" :"+name);
            }
        }

        return builder.toString();
    }

    private String getColumnsForUpdate(List<Field> databaseFields) {

        StringBuilder builder = new StringBuilder();
        int counter = 0;
        for (Field field : databaseFields
        ) {
            counter++;
            DatabaseField dbField = field.getAnnotation(DatabaseField.class);
            final String name = dbField.name();
            if(dbField.isPrimaryKey()) {
                continue;
            }
            // make sure we remove the primary key
            if (counter == databaseFields.size()) {
                builder.append(name+" = :"+name+" ");
            } else {
                builder.append(name+" = :"+name+",");
            }
        }

        return builder.toString();
    }

    private String getColumnValuesFromFields(List<Field> databaseFields) {
        StringBuilder builder = new StringBuilder();
        int counter = 0;
        for (Field field : databaseFields
        ) {
            counter++;
            DatabaseField dbField = field.getAnnotation(DatabaseField.class);
            final String name = dbField.name();
            if (counter == databaseFields.size()) {
                builder.append(":" + name);
            } else {
                builder.append(":" + name + ",");
            }
        }

        return builder.toString();
    }

    private String getColumnNamesFromFields(List<Field> databaseFields) {
        StringBuilder builder = new StringBuilder();
        int counter = 0;
        for (Field field : databaseFields
        ) {
            counter++;
            DatabaseField dbField = field.getAnnotation(DatabaseField.class);
            final String name = dbField.name();
            if (counter == databaseFields.size()) {
                builder.append(name);
            } else {
                builder.append(name + ",");
            }
        }

        return builder.toString();
    }

    private String buildJavaClass(String newDaoName, String implName, String boName, List<Field> databaseFields, String constantsName, List<SecondarySearchFieldBO> secondaryFieldList, String primaryKeyFieldName, Method[] declaredMethods, Class object) {
        StringBuilder builder = new StringBuilder();

        String s1 = primaryKeyFieldName.substring(0, 1).toUpperCase();
        String primaryKeyFieldInCaps = s1 + primaryKeyFieldName.substring(1, primaryKeyFieldName.length());

        builder.append("package "+PACKAGE_NAME+";");
        builder.append("\n");
        builder.append("\n");
        builder.append("import "+object.getName()+";\n");
        builder.append("import java.sql.ResultSet;\n");
        builder.append("import java.sql.SQLException;\n");
        builder.append("import java.sql.Timestamp;\n");
        builder.append("import java.time.Instant;\n");
        builder.append("import java.util.HashMap;\n");
        builder.append("import java.util.List;\n");
        builder.append("import java.util.Map;\n");
        builder.append("import org.slf4j.Logger;\n");
        builder.append("import org.slf4j.LoggerFactory;\n");
        builder.append("import org.springframework.jdbc.core.RowMapper;\n");
        builder.append("import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;\n");
        builder.append("\n");
        builder.append("/**");
        builder.append("\n");
        builder.append("* Auto generated dao implementation class by DAO-Builder : "+new Date());
        builder.append("\n");
        builder.append("*/");
        builder.append("\n");
        builder.append("\n");
        builder.append("public class " + newDaoName + " implements " + implName + " {");
        builder.append("\n");
        builder.append("\n");
        builder.append("\tprivate static final Logger logger = LoggerFactory.getLogger(" + newDaoName + ".class);");
        builder.append("\n");
        builder.append("\n");
        builder.append("\tprivate final NamedParameterJdbcTemplate jdbcTemplate;");
        builder.append("\n");
        builder.append("\n");
        builder.append("\tprivate final DataRowMapper dataRowMapper;");
        builder.append("\n");
        builder.append("\n");
        builder.append("\tpublic " + newDaoName + "(NamedParameterJdbcTemplate jdbcTemplate) {\n");
        builder.append("\t\tthis.jdbcTemplate = jdbcTemplate;\n");
        builder.append("\t\tthis.dataRowMapper = new DataRowMapper();\n");
        builder.append("\t}\n");
        // Build the methods
        builder.append("\t@Override");
        builder.append("\n");
        builder.append("\t public boolean create(" + boName + " data) throws SQLException {\n");
        builder.append("\t\t logger.trace(\"Creating row {}\",data);\n");
        builder.append("\n");
        builder.append("\t\t Map<String, Object> parameters = new HashMap<>();");
        builder.append("\n");

        for (Field field : databaseFields) {
            DatabaseField dbField = field.getAnnotation(DatabaseField.class);
            Method method = findMethodForField(field, declaredMethods);
            final String databaseFieldName = dbField.name();
            boolean isTimestamp = dbField.isTimestampField();
            if(isTimestamp) {
                String fieldNameWithTimestamp = method.getName()+"AsTimestamp";
                builder.append("\t\t final Timestamp "+fieldNameWithTimestamp+" = Timestamp.from(Instant.ofEpochMilli(data." + method.getName()+"()));\n");
                builder.append("\t\t parameters.put(" + constantsName + "." + databaseFieldName + ", "+fieldNameWithTimestamp+");");
                builder.append("\n");
                continue;
            }
            if(dbField.isEnumField()) {
                builder.append("\t\t parameters.put(" + constantsName + "." + databaseFieldName + ", data." + method.getName() + "().name());");
                builder.append("\n");
                continue;
            }
            if (method != null) {
                builder.append("\t\t parameters.put(" + constantsName + "." + databaseFieldName + ", data." + method.getName() + "());");
                builder.append("\n");
            } else {
                builder.append("\t\t parameters.put(" + constantsName + "." + databaseFieldName + ", data.getValue());");
                builder.append("\n");

            }
        }
        builder.append("\n");
        builder.append("\t\t this.jdbcTemplate.update(" + constantsName + ".INSERT_STATEMENT, parameters);");
        builder.append("\n");
        builder.append("\t\t return true;\n");
        builder.append("\t}\n");
        builder.append("\n");
        if(this.shouldCreateArchive) {
            builder.append("\t public boolean journal(" + boName + " data) throws SQLException {\n");
            builder.append("\t\t logger.trace(\"Creating row {}\",data);\n");
            builder.append("\n");
            builder.append("\t\t Map<String, Object> parameters = new HashMap<>();");
            builder.append("\n");

            for (Field field : databaseFields) {
                DatabaseField dbField = field.getAnnotation(DatabaseField.class);
                Method method = findMethodForField(field, declaredMethods);
                final String databaseFieldName = dbField.name();
                if(dbField.isTimestampField()) {
                    String fieldNameWithTimestamp = method.getName()+"AsTimestamp";
                    builder.append("\t\t final Timestamp "+fieldNameWithTimestamp+" = Timestamp.from(Instant.ofEpochMilli(data." + method.getName()+"()));\n");
                    builder.append("\t\t parameters.put(" + constantsName + "." + databaseFieldName + ", "+fieldNameWithTimestamp+");");
                    builder.append("\n");
                    continue;
                }
                if(dbField.isEnumField()) {
                    builder.append("\t\t parameters.put(" + constantsName + "." + databaseFieldName + ", data." + method.getName() + "().name());");
                    builder.append("\n");
                    continue;
                }
                if (method != null) {
                    builder.append("\t\t parameters.put(" + constantsName + "." + databaseFieldName + ", data." + method.getName() + "());");
                    builder.append("\n");
                } else {
                    builder.append("\t\t parameters.put(" + constantsName + "." + databaseFieldName + ", data.getValue());");
                    builder.append("\n");

                }
            }
        }
        builder.append("\n");
        builder.append("\t\t this.jdbcTemplate.update(" + constantsName + ".INSERT_STATEMENT_JOURNAL, parameters);");
        builder.append("\n");
        builder.append("\t\t return true;\n");
        builder.append("\t}\n");
        // Updated method
        builder.append("\t@Override");
        builder.append("\n");
        builder.append("\t public void update(" + boName + " data) throws SQLException {\n");
        builder.append("\t\t logger.trace(\"Updating row {}\",data);\n");
        builder.append("\n");
        builder.append("\t\t Map<String, Object> parameters = new HashMap<>();");
        builder.append("\n");

        for (Field field : databaseFields) {
            DatabaseField dbField = field.getAnnotation(DatabaseField.class);
            Method method = findMethodForField(field, declaredMethods);
            final String databaseFieldName = dbField.name();

            if(dbField.isTimestampField()) {
                String fieldNameWithTimestamp = method.getName()+"AsTimestamp";
                builder.append("\t\t final Timestamp "+fieldNameWithTimestamp+" = Timestamp.from(Instant.ofEpochMilli(data." + method.getName()+"()));\n");
                builder.append("\t\t parameters.put(" + constantsName + "." + databaseFieldName + ", "+fieldNameWithTimestamp+");");
                builder.append("\n");
                continue;
            }
            if(dbField.isEnumField()) {
                builder.append("\t\t parameters.put(" + constantsName + "." + databaseFieldName + ", data." + method.getName() + "().name());");
                builder.append("\n");
                continue;
            }
            if (method != null) {
                builder.append("\t\t parameters.put(" + constantsName + "." + databaseFieldName + ", data." + method.getName() + "());");
                builder.append("\n");
            } else {
                builder.append("\t\t parameters.put(" + constantsName + "." + databaseFieldName + ", data.getValue());");
                builder.append("\n");
            }
        }
        builder.append("\n");
        builder.append("\t\t this.jdbcTemplate.update(" + constantsName + ".UPDATE_STATEMENT, parameters);");
        builder.append("\n");
        builder.append("\t}\n");

        builder.append("\t@Override");
        builder.append("\n");
        builder.append("\t public List<" + boName + "> findAll() {\n");
        builder.append("\t\t final List<" + boName + "> query = this.jdbcTemplate.query(" + constantsName + ".SELECT_ALL_SQL, this.dataRowMapper);");
        builder.append("\n");
        builder.append("\t return query;\n");
        builder.append("\n");
        builder.append("\t}\n");
        builder.append("\t@Override");
        builder.append("\n");
        DatabaseField primarySearchField = findByFieldFromName(primaryKeyFieldName, databaseFields);
        builder.append("\t public void delete(long id) {\n");
        builder.append("\t\t logger.trace(\"Attempting to delete {}\",id);\n");
        builder.append("\t\t Map<String, Object> parameters = new HashMap<>();");
        builder.append("\n");
        builder.append("\t\t parameters.put(" + constantsName + "." + primarySearchField.name() + ", id);");
        builder.append("\n");
        builder.append("\t\t this.jdbcTemplate.update(" + constantsName + ".DELETE_ID_SQL, parameters);\n");
        builder.append("\t}\n");
        // Primary key search
        builder.append("\t@Override");
        builder.append("\n");
        builder.append("\t public " + boName + " findBy" + primaryKeyFieldInCaps + "(long id) {\n");
        builder.append("\t\t logger.trace(\"Attempting to findBy {}\",id);\n");
        builder.append("\t\t Map<String, Object> parameters = new HashMap<>();");
        builder.append("\n");
        builder.append("\t\t parameters.put(" + constantsName + "." + primarySearchField.name() + ", id);");
        builder.append("\n");
        builder.append("\t\t final List<" + boName + "> query = this.jdbcTemplate.query(" + constantsName + ".SELECT_BY_ID, parameters, this.dataRowMapper);");
        builder.append("\n");
        builder.append("\t\t final " + boName + " data = (!query.isEmpty()) ? query.get(0) : null;");
        builder.append("\n");
        builder.append("\t\t return data;\n");
        builder.append("\t}\n");
        /**
         * Secondary field search, we loop over the lists and create the correct statements
         */
        for (SecondarySearchFieldBO secondarySearchFieldBO: secondaryFieldList
             ) {

            String type = secondarySearchFieldBO.getType();
            String sqlStatementName = secondarySearchFieldBO.getSqlStatementName();
            String secondarySearchFieldName = secondarySearchFieldBO.getFieldName();
            String s2 = secondarySearchFieldName.substring(0, 1).toUpperCase();
            String secondaryFieldINCaps = s2 + secondarySearchFieldName.substring(1, secondarySearchFieldName.length());
            boolean shouldReturnMultiple = secondarySearchFieldBO.isShouldReturnMultiple();

            builder.append("\t@Override");
            builder.append("\n");
            if(!shouldReturnMultiple) {
                builder.append("\t public " + boName + " findBy" + secondaryFieldINCaps + "("+type+" key) {\n");
                builder.append("\t\t Map<String, Object> parameters = new HashMap<>();");
                builder.append("\n");
                DatabaseField secondarySearchField = findByFieldFromName(secondarySearchFieldName, databaseFields);
                builder.append("\t\t parameters.put(" + constantsName + "." + secondarySearchField.name() + ", key);");
                builder.append("\n");
                builder.append("\t\t final List<" + boName + "> query = this.jdbcTemplate.query(" + constantsName + "."+sqlStatementName+", parameters, this.dataRowMapper);");
                builder.append("\n");
                builder.append("\t\t final " + boName + " data = (!query.isEmpty()) ? query.get(0) : null;");
                builder.append("\n");
                builder.append("\t\t return data;\n");
                builder.append("\t}\n");
            } else {
                builder.append("\t public List<" + boName + "> findBy" + secondaryFieldINCaps + "("+type+" key) {\n");
                builder.append("\t\t Map<String, Object> parameters = new HashMap<>();");
                builder.append("\n");
                DatabaseField secondarySearchField = findByFieldFromName(secondarySearchFieldName, databaseFields);
                builder.append("\t\t parameters.put(" + constantsName + "." + secondarySearchField.name() + ", key);");
                builder.append("\n");
                builder.append("\t\t final List<" + boName + "> query = this.jdbcTemplate.query(" + constantsName + "."+sqlStatementName+", parameters, this.dataRowMapper);");
                builder.append("\n");
                builder.append("\t\t return query;\n");
                builder.append("\t}\n");
            }
        }
        builder.append("\n");
        builder.append("\t private class DataRowMapper implements RowMapper<" + boName + "> {");
        builder.append("\n");
        builder.append("\n");
        builder.append("\t\tpublic " + boName + " mapRow(ResultSet rs, int rowNum) throws SQLException {");
        builder.append("\n");
        StringBuilder constructorBuilder = new StringBuilder();
        for (Field field : databaseFields
        ) {
            DatabaseField dbField = field.getAnnotation(DatabaseField.class);
            final String databaseFieldName = dbField.name();
            String typeAsString = field.getType().getName();
            // we need to split out the java packages, so take the last .
            String reType = typeAsString.replace(".", "-");
            String[] splits = reType.split("-");
            String className;
            if (splits.length == 0 || splits.length == 1) {
                className = typeAsString;
            } else {
                className = splits[splits.length - 1];
            }
            String s3 = className.substring(0, 1).toUpperCase();
            String capitaliseFirstChar = s3 + className.substring(1, className.length());
            final String name = field.getName();
            constructorBuilder.append(name+",");

            if(dbField.isTimestampField()) {
                builder.append("\t\t\t" + className + " " + name + " = rs.getTimestamp(" + constantsName + "." + databaseFieldName + ").getTime();");
            }else if(dbField.isEnumField()) {
                builder.append("\t\t\tString " + name + "AsString = rs.getString(" + constantsName + "." + databaseFieldName + ");");
                builder.append("\n");
                final Class<?> type = field.getType();
                final String enumTypeName = type.getName();
                builder.append("\t\t\t"+enumTypeName+" " + name + " = "+enumTypeName+".valueOf("+name+"AsString);");
            }else {
                builder.append("\t\t\t" + className + " " + name + " = rs.get" + capitaliseFirstChar + "(" + constantsName + "." + databaseFieldName + ");");
            }
            builder.append("\n");
        }
        String constructureValueWithComma = constructorBuilder.toString();
        // Take out the last ,
        String constructureValue = constructureValueWithComma.substring(0, constructureValueWithComma.length() -1);
        builder.append("\t\t\treturn new " + boName + "("+constructureValue+");");
        builder.append("\n\t\t}");
        builder.append("\n");
        builder.append("\t}");
        builder.append("\n");
        builder.append("}");
        return builder.toString();
    }

    private String buildMemberConstantsClass(boolean shouldCreateArchive, String constantClassName, String tableName, List<Field> databaseFields, String insertStatement, String selectAllStatement, String deletePrimaryKeyStatement, List<SecondarySearchFieldBO> secondaryKeySearch, String updateStatement, String findWherePrimaryKeyStatement, String insertJournal) {
        StringBuilder builder = new StringBuilder();
        builder.append("package "+PACKAGE_NAME+";");
        builder.append("\n");
        builder.append("\n");
        builder.append("/**");
        builder.append("\n");
        builder.append("* Auto generated dao implementation class by DAO-Builder : "+new Date());
        builder.append("\n");
        builder.append("*/");
        builder.append("\n");
        builder.append("\n");
        builder.append("public class " + constantClassName + " {");
        builder.append("\n");
        builder.append("\tpublic static final String TABLE_NAME = \"" + tableName + "\";");
        builder.append("\n");
        if(shouldCreateArchive) {
            builder.append("\tpublic static final String TABLE_NAME_JOURNAL = \"" + tableName + "_JOURNAL\";");
        }
        builder.append("\n");
        builder.append("\tpublic static final String INSERT_STATEMENT = \"" + insertStatement + "\";");
        builder.append("\n");
        if(this.shouldCreateArchive) {
            builder.append("\n");
            builder.append("\tpublic static final String INSERT_STATEMENT_JOURNAL = \"" + insertJournal + "\";");
        }
        builder.append("\tpublic static final String UPDATE_STATEMENT = \"" + updateStatement + "\";");
        builder.append("\n");
        builder.append("\tpublic static final String SELECT_ALL_SQL = \"" + selectAllStatement + "\";");
        builder.append("\n");
        builder.append("\tpublic static final String DELETE_ID_SQL = \"" + deletePrimaryKeyStatement + "\";");
        builder.append("\n");
        builder.append("\tpublic static final String SELECT_BY_ID = \"" + findWherePrimaryKeyStatement + "\";");
        builder.append("\n");
        for (SecondarySearchFieldBO secondarySearchFieldBO: secondaryKeySearch
             ) {
            String sqlStatementName = secondarySearchFieldBO.getSqlStatementName();
            String sqlStatement = secondarySearchFieldBO.getSqlStatement();
            builder.append("\tpublic static final String "+sqlStatementName+" = \"" + sqlStatement + "\";");
            builder.append("\n");
        }
        for (Field field : databaseFields
        ) {
            DatabaseField dbField = field.getAnnotation(DatabaseField.class);
            final String name = dbField.name();
            builder.append("\tpublic static final String " + name + " = \"" + name + "\";");
            builder.append("\n");
        }
        builder.append("}");
        return builder.toString();
    }

    private String builderInterfaceClass(String constantClassName, String boName, String primaryKeyFieldName, List<SecondarySearchFieldBO> secondarySearchList, Class object) {
        StringBuilder builder = new StringBuilder();
        builder.append("package "+PACKAGE_NAME+";");
        builder.append("\n");
        builder.append("import "+object.getName()+";\n");
        builder.append("import java.sql.SQLException;\n");
        builder.append("import java.util.List;\n");
        builder.append("\n");
        builder.append("/**");
        builder.append("\n");
        builder.append("* Auto generated dao implementation class by DAO-Builder : "+new Date());
        builder.append("\n");
        builder.append("*/");
        builder.append("\n");
        builder.append("public interface " + constantClassName + " {");
        builder.append("\n");
        builder.append("\n");
        builder.append("\tpublic boolean create(" + boName + " data) throws SQLException;");
        builder.append("\n");
        builder.append("\n");
        if(this.shouldCreateArchive) {
            builder.append("\tpublic boolean journal(" + boName + " data) throws SQLException;");
            builder.append("\n");
            builder.append("\n");
        }
        builder.append("\tpublic List<" + boName + "> findAll();");
        builder.append("\n");
        builder.append("\n");
        builder.append("\tpublic void delete(long id);");
        builder.append("\n");
        builder.append("\n");
        builder.append("\tpublic void update("+boName+" data) throws SQLException;");
        builder.append("\n");
        builder.append("\n");
        String s2 = primaryKeyFieldName.substring(0, 1).toUpperCase();
        String capitalisePrimaryChar = s2 + primaryKeyFieldName.substring(1, primaryKeyFieldName.length());
        builder.append("\tpublic " + boName + " findBy" + capitalisePrimaryChar + "(long id);");
        builder.append("\n");
        builder.append("\n");

        for (SecondarySearchFieldBO secondarySearchFieldBO: secondarySearchList
             ) {
            final String type = secondarySearchFieldBO.getType();
            String secondarySearchFieldName = secondarySearchFieldBO.getFieldName();
            String s1 = secondarySearchFieldName.substring(0, 1).toUpperCase();
            String capitaliseFirstChar = s1 + secondarySearchFieldName.substring(1, secondarySearchFieldName.length());
            boolean shouldReturnMultiple = secondarySearchFieldBO.isShouldReturnMultiple();
            if(!shouldReturnMultiple) {
                builder.append("\tpublic " + boName + " findBy" + capitaliseFirstChar + "("+type+" " + secondarySearchFieldName + ");");
            }else {
                builder.append("\tpublic List<" + boName + "> findBy" + capitaliseFirstChar + "("+type+" " + secondarySearchFieldName + ");");
            }
            builder.append("\n");
            builder.append("\n");
        }
        builder.append("}");
        return builder.toString();
    }


    private Method findMethodForField(Field field, Method[] declaredMethods) {
        for (int i = 0; i < declaredMethods.length; i++) {
            final Method declaredMethod = declaredMethods[i];
            final String methodNameLowerCase = declaredMethod.getName().toLowerCase();
            final String fieldNameLowerCase = field.getName().toLowerCase();
            if (methodNameLowerCase.contains(fieldNameLowerCase)) {
                return declaredMethod;
            }
        }

        return null;
    }

    private DatabaseField findByFieldFromName(String name, List<Field> databaseFields) {
        for (Field dbField : databaseFields
        ) {
            if (dbField.getName().equals(name)) {
                return dbField.getAnnotation(DatabaseField.class);
            }
        }

        return null;
    }
}
