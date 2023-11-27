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

    public DAOBuilder() {

    }

    public void start(String args[]) {
        String className = args[0];
        String directory = args[1];
        String javaPackage = args[2];
        try {
            Class c = Class.forName(className);
            this.directory = directory;
            this.PACKAGE_NAME = javaPackage;
            buildDAO(c);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String args[]) {
        if(args.length != 3) {
            System.out.println("Invalid arguments, className,directory,javaPackage");
            System.exit(-1);
        }
       new DAOBuilder().start(args);
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
        String selectAllStatement = String.format(SELECT_ALL, "\"+ExampleConstants.TABLE_NAME+\"");
        String secondaryKeySearch = null;
        String primaryKeyFieldName = null;
        String deletePrimaryKeyStatement = null;
        String findWherePrimaryKeyStatement = null;
        String secondarySearchFieldName = null;
        final Method[] declaredMethods = object.getDeclaredMethods();
        final Field[] declaredFields = object.getDeclaredFields();
        
        List<Field> databaseFields = new ArrayList<>();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(DatabaseField.class)) {
                DatabaseField dbField = field.getAnnotation(DatabaseField.class);
                databaseFields.add(field);
                Boolean primaryKey = dbField.isPrimaryKey();
                Boolean searchFieldSingle = dbField.isSearchFieldSingle();
                if (primaryKey) {
                    deletePrimaryKeyStatement = String.format(DELETE_WHERE_ID, "\"+ExampleConstants.TABLE_NAME+\"", dbField.name(), dbField.name());
                    findWherePrimaryKeyStatement = String.format(FIND_WHERE_ID, "\"+ExampleConstants.TABLE_NAME+\"", dbField.name(), dbField.name());
                    primaryKeyFieldName = field.getName();
                }

                if (searchFieldSingle) {
                    secondaryKeySearch = String.format(SELECT_SECONDARY, "\"+ExampleConstants.TABLE_NAME+\"", dbField.name(), dbField.name());
                    secondarySearchFieldName = field.getName();
                }
            }
        }

        String columnNames = getColumnNamesFromFields(databaseFields);
        String columnValues = getColumnValuesFromFields(databaseFields);
        String columnsForUpdate = getColumnsForUpdate(databaseFields);
        String updateWhereStatement = getUpdateWhereStatement(databaseFields);
        String insertStatement = String.format(INSERT_INTO, "\"+ExampleConstants.TABLE_NAME+\"", columnNames, columnValues);
        String updateStatement = String.format(UPDATE, "\"+ExampleConstants.TABLE_NAME+\"", columnsForUpdate, updateWhereStatement);

        final String implClass = buildJavaClass(newDaoName, newDaoInterfaceName, boName, databaseFields, constantsName, secondarySearchFieldName, primaryKeyFieldName, declaredMethods, object);
        final String interfaceClass = builderInterfaceClass(newDaoInterfaceName, boName, primaryKeyFieldName, secondarySearchFieldName, object);
        final String constantsClass = buildMemberConstantsClass(constantsName, tableName, databaseFields, insertStatement, selectAllStatement, deletePrimaryKeyStatement, secondaryKeySearch, updateStatement, findWherePrimaryKeyStatement);

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

    private String buildJavaClass(String newDaoName, String implName, String boName, List<Field> databaseFields, String constantsName, String secondarySearchFieldName, String primaryKeyFieldName, Method[] declaredMethods, Class object) {
        StringBuilder builder = new StringBuilder();

        String s1 = primaryKeyFieldName.substring(0, 1).toUpperCase();
        String primaryKeyFieldInCaps = s1 + primaryKeyFieldName.substring(1, primaryKeyFieldName.length());
        String s2 = secondarySearchFieldName.substring(0, 1).toUpperCase();
        String secondaryFieldINCaps = s2 + secondarySearchFieldName.substring(1, secondarySearchFieldName.length());

        builder.append("package "+PACKAGE_NAME+";");
        builder.append("\n");
        builder.append("/**");
        builder.append("\n");
        builder.append("* Auto generated dao implementation class by DAO-Builder : "+new Date());
        builder.append("\n");
        builder.append("*/");
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
        builder.append("public class " + newDaoName + " implements " + implName + " {");
        builder.append("\n");
        builder.append("\n");
        builder.append("\tprivate static final Logger logger = LoggerFactory.getLogger(" + newDaoName + ".class);");
        builder.append("\n");
        builder.append("\n");
        builder.append("\tprivate final NamedParameterJdbcTemplate jdbcTemplate;");
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
        builder.append("\t\t logger.debug(\"Creating row {}\",data);\n");
        builder.append("\t\t final long l = data.getCreated();");
        builder.append("\n");
        builder.append("\t\t final Timestamp from = Timestamp.from(Instant.ofEpochMilli(l));");
        builder.append("\n");
        builder.append("\t\t Map<String, Object> parameters = new HashMap<>();");
        builder.append("\n");

        for (Field field : databaseFields) {
            DatabaseField dbField = field.getAnnotation(DatabaseField.class);
            Method method = findMethodForField(field, declaredMethods);
            String dbNameFieldAnnotation = dbField.name();
            final String databaseFieldName = dbField.name();
            if(dbNameFieldAnnotation.contains("CREATED")) {
                builder.append("\t\t parameters.put(" + constantsName + "." + databaseFieldName + ", from);");
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
        builder.append("\t return true;\n");
        builder.append("\t}\n");
        // Updated method
        builder.append("\t@Override");
        builder.append("\n");
        builder.append("\t public void update(" + boName + " data) throws SQLException {\n");
        builder.append("\t\t logger.debug(\"Updating row {}\",data);\n");
        builder.append("\t\t final long l = data.getCreated();");
        builder.append("\n");
        builder.append("\t\t final Timestamp from = Timestamp.from(Instant.ofEpochMilli(l));");
        builder.append("\n");
        builder.append("\t\t Map<String, Object> parameters = new HashMap<>();");
        builder.append("\n");

        for (Field field : databaseFields) {
            DatabaseField dbField = field.getAnnotation(DatabaseField.class);
            String dbNameFieldAnnotation = dbField.name();
            Method method = findMethodForField(field, declaredMethods);
            final String databaseFieldName = dbField.name();

            if(dbNameFieldAnnotation.contains("CREATED")) {
                builder.append("\t\t parameters.put(" + constantsName + "." + databaseFieldName + ", from);");
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
        builder.append("\t\t logger.debug(\"Attempting to delete {}\",id);\n");
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
        builder.append("\t\t logger.debug(\"Attempting to findBy {}\",id);\n");
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
        // Secondary value search
        builder.append("\t@Override");
        builder.append("\n");
        builder.append("\t public " + boName + " findBy" + secondaryFieldINCaps + "(String key) {\n");
        builder.append("\t\t Map<String, Object> parameters = new HashMap<>();");
        builder.append("\n");
        DatabaseField secondarySearchField = findByFieldFromName(secondarySearchFieldName, databaseFields);
        builder.append("\t\t parameters.put(" + constantsName + "." + secondarySearchField.name() + ", key);");
        builder.append("\n");
        builder.append("\t\t final List<" + boName + "> query = this.jdbcTemplate.query(" + constantsName + ".SELECT_BY_CODE, parameters, this.dataRowMapper);");
        builder.append("\n");
        builder.append("\t\t final " + boName + " data = (!query.isEmpty()) ? query.get(0) : null;");
        builder.append("\n");
        builder.append("\t\t return data;\n");
        builder.append("\t}\n");
        builder.append("\n");
        builder.append("\t private class DataRowMapper implements RowMapper<" + boName + "> {");
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
            builder.append("\t\t\t" + className + " " + name + " = rs.get" + capitaliseFirstChar + "(" + constantsName + "." + databaseFieldName + ");");
            builder.append("\n");
        }
        String constructureValueWithComma = constructorBuilder.toString();
        // Take out the last ,
        String constructureValue = constructureValueWithComma.substring(0, constructureValueWithComma.length() -1);
        builder.append("\t\t\t return new " + boName + "("+constructureValue+");");
        builder.append("\n\t\t}");
        builder.append("\n");
        builder.append("\t}");
        builder.append("\n");
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

    private String buildMemberConstantsClass(String constantClassName, String tableName, List<Field> databaseFields, String insertStatement, String selectAllStatement, String deletePrimaryKeyStatement, String secondaryKeySearch, String updateStatement, String findWherePrimaryKeyStatement) {
        StringBuilder builder = new StringBuilder();
        builder.append("package "+PACKAGE_NAME+";");
        builder.append("\n");
        builder.append("/**");
        builder.append("\n");
        builder.append("* Auto generated dao class by DAO-BUILDER: "+new Date());
        builder.append("\n");
        builder.append("*/");
        builder.append("\n");
        builder.append("public class " + constantClassName + " {");
        builder.append("\n");
        builder.append("\tpublic static final String TABLE_NAME = \"" + tableName + "\";");
        builder.append("\n");
        builder.append("\tpublic static final String INSERT_STATEMENT = \"" + insertStatement + "\";");
        builder.append("\n");
        builder.append("\tpublic static final String UPDATE_STATEMENT = \"" + updateStatement + "\";");
        builder.append("\n");
        builder.append("\tpublic static final String SELECT_ALL_SQL = \"" + selectAllStatement + "\";");
        builder.append("\n");
        builder.append("\tpublic static final String DELETE_ID_SQL = \"" + deletePrimaryKeyStatement + "\";");
        builder.append("\n");
        builder.append("\tpublic static final String SELECT_BY_CODE = \"" + secondaryKeySearch + "\";");
        builder.append("\n");
        builder.append("\tpublic static final String SELECT_BY_ID = \"" + findWherePrimaryKeyStatement + "\";");
        builder.append("\n");
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

    private String builderInterfaceClass(String constantClassName, String boName, String primaryKeyFieldName, String secondarySearchFieldName, Class object) {
        StringBuilder builder = new StringBuilder();
        builder.append("package "+PACKAGE_NAME+";");
        builder.append("\n");
        builder.append("/**");
        builder.append("\n");
        builder.append("* Auto generated dao class by DAO-Builder: "+new Date());
        builder.append("\n");
        builder.append("*/");
        builder.append("\n");
        builder.append("import "+object.getName()+";\n");
        builder.append("import java.sql.SQLException;\n");
        builder.append("import java.util.List;\n");
        builder.append("\n");
        builder.append("public interface " + constantClassName + " {");
        builder.append("\n");
        builder.append("\n");
        builder.append("\tpublic boolean create(" + boName + " data) throws SQLException;");
        builder.append("\n");
        builder.append("\n");
        builder.append("\tpublic List<" + boName + "> findAll();");
        builder.append("\n");
        builder.append("\n");
        builder.append("\tpublic void delete(long id);");
        builder.append("\n");
        builder.append("\n");
        builder.append("\tpublic void update("+boName+" data) throws SQLException;");
        builder.append("\n");
        builder.append("\n");
        String s1 = secondarySearchFieldName.substring(0, 1).toUpperCase();
        String capitaliseFirstChar = s1 + secondarySearchFieldName.substring(1, secondarySearchFieldName.length());
        builder.append("\t" + boName + " findBy" + capitaliseFirstChar + "(String " + secondarySearchFieldName + ");");
        builder.append("\n");
        builder.append("\n");
        String s2 = primaryKeyFieldName.substring(0, 1).toUpperCase();
        String capitalisePrimaryChar = s2 + primaryKeyFieldName.substring(1, primaryKeyFieldName.length());
        builder.append("\t" + boName + " findBy" + capitalisePrimaryChar + "(long id);");
        builder.append("\n");
        builder.append("\n");
        builder.append("}");
        return builder.toString();
    }
}
