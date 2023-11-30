# Dao-builder
<b>Builds JAVA JDBC SQL Database components from Bus</b><br>


This component will allow you to define a Java POJO object like ABCBO.java and have it auto generate the SQL structure for some basic queries like insert,update,delete and fine.<b>
Reason for this component was DAOs are always hard and time consuming to build this just helps automate the process a bit.<b>
<br>
When using this library it does require some dependencies
<br><br>
<pre>
dependencies {<br>
    implementation "mysql:mysql-connector-java:8.0.33"<br>
    implementation "org.springframework.boot:spring-boot-starter-jdbc:2.7.3"<br>
    implementation 'commons-dbcp:commons-dbcp:1.4'<br>
}
</pre>
<br>
Replace spring-boot with whatever JDBC library you require.
<br>
For working example please see ExampleBO.java inside the examples.persist directory
<br><br>
<b>Annotations:</b><br>
<br>
@DatabaseField(name = "ID", isPrimaryKey = true) - This should be placed on the primary key field to generate a primary key statement
<br><br>
@DatabaseField(name = "NAME", isSearchField = true, searchFieldSqlName = "SELECT_BY_SURNAME")  - This should be placed on your secondary search field
<br>
When using isSearchField = true, you can specify the searchFieldSqlName select statement name to help build more support for multiple field search
<br><br>
@DatabaseField(name = "SURNAME")  - This annotates a field as a regular field and is included in create,update,delete,insert
<br>
<br>
To the build create a List of objects on a return statement simply specify isSearchFieldMultiple = true
<br><br>
@DatabaseField(name = "NAME", isSearchField = true, isSearchFieldMultiple = true, searchFieldSqlName = "SELECT_BY_NAME")
private final String name;
