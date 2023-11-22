# Dao-builder
<b>Builds JAVA JDBC SQL Database components from Bus<b>
This component will allow you to define a Java POJO object like ABCBO.java and have it auto generate the SQL structure for some basic queries like insert,update,delete and fine.
Reason for this component was DAOs are always hard and time consuming to build this just helps automate the process a bit.

When using this library it does require some dependencies
\n
dependencies {\n
    implementation "mysql:mysql-connector-java:8.0.33"\n
    implementation "org.springframework.boot:spring-boot-starter-jdbc:2.7.3"\n
    implementation 'commons-dbcp:commons-dbcp:1.4'\n
}\n
\n
Replace spring-boot with whatever JDBC library you require.
