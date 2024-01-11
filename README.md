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
<h2>Annotations:</h2>
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
To have the interface return a List of objects on a return statement simply specify isSearchFieldMultiple = true
<br><br>
@DatabaseField(name = "NAME", isSearchField = true, isSearchFieldMultiple = true, searchFieldSqlName = "SELECT_BY_NAME")
private final String name;
<br>
<br>
Running this from IntelliJ or Eclipse you need the following setup:
<br>
main class: com.sussoftware.daobuilder.DAOBuilder
<br>
<h2>Example JVM parameters:</h2>
<br>
-DclassName="com.sussoftware.daobuilder.examples.ExampleBO"
<br>
-Ddirectory="src/main/java/com/sussoftware/daobuilder/examples/persist"
<br>
-DpackageName="com.sussoftware.daobuilder.examples.persist"
<br>
-Djournal=true
<br>
Note: -Djournal=true is optional if set, then a new method to create a journal of the table will be added
<br>
<h2>Gradle</h2>
Gradle: implementation 'io.github.susspectsoftware-dev:dao-builder:1.11.0'
<br>