# Dao-builder
Builds JAVA JDBC SQL Database components from Bus
This component will allow you to define a Java POJO object like ABCBO.java and have it auto generate the SQL structure for some basic queries like insert,update,delete and fine.
Reason for this component was DAOs are always hard and time consuming to build this just helps automate the process a bit.

When using this library it does require some dependencies

dependencies {
    implementation "mysql:mysql-connector-java:8.0.33"
    implementation "org.springframework.boot:spring-boot-starter-jdbc:2.7.3"
    implementation 'commons-dbcp:commons-dbcp:1.4'
    implementation project(':utils')
}

Replace spring-boot with whatever JDBC library you require.
