# Dao-builder
<h2>Builds JAVA JDBC SQL Database components from POJO</h2>

This component will allow you to define a Java POJO object like ABCBO.java and have it auto generate the SQL structure for some basic queries like insert,update,delete and fine.
Reason for this component was DAOs are always hard and time consuming to build this just helps automate the process a bit.
When using this library it does require some dependencies
<br><br>
dependencies {<br>
   implementation 'org.slf4j:slf4j-log4j12:2.0.10'<br>
   implementation 'org.springframework:spring-jdbc:5.3.16'<br>
}<br>
<br>
For working example please see ExampleBO.java inside the examples.persist directory
<br>
<h2>Annotations:</h2>

@DatabaseField(name = "ID", isPrimaryKey = true) - This should be placed on the primary key field to generate a primary key statement
<br><br>
@DatabaseField(name = "NAME", isSearchField = true, searchFieldSqlName = "SELECT_BY_SURNAME")  - This should be placed on your secondary search field
When using isSearchField = true, you can specify the searchFieldSqlName select statement name to help build more support for multiple field search
<br><br>
@DatabaseField(name = "SURNAME")  - This annotates a field as a regular field and is included in create,update,delete,insert
<br>
<br>
@DatabaseField(name = "CREATED", isTimestampField = true)  - isTimestampField will convert the time from date to long on the row mapper
<br>
<br>
@DatabaseField(name = "status", isEnumField = true)  - isEnumField will convert the Enum to a string insert it and decode it back
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
Gradle: implementation 'io.github.susspectsoftware-dev:dao-builder:1.16.0'
<br>