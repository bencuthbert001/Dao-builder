package com.sussoftware.daobuilder.examples;

import com.sussoftware.daobuilder.DatabaseField;
import com.sussoftware.daobuilder.DatabaseObject;

/**
 * Example BO class that will show how generation works
 */
@DatabaseObject(tableName = "EXAMPLE_TABLE")
public class ExampleBO {

    @DatabaseField(name = "ID", isPrimaryKey = true)
    private final long id;
    @DatabaseField(name = "NAME", isSearchField = true, isSearchFieldMultiple = true, searchFieldSqlName = "SELECT_BY_NAME")
    private final String name;
    @DatabaseField(name = "SURNAME", isSearchField = true, searchFieldSqlName = "SELECT_BY_SURNAME")
    private final String surname;
    @DatabaseField(name = "CREATED")
    private final long created;

    public ExampleBO(long id, String name, String surname, long created) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.created = created;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public long getCreated() {
        return created;
    }

    @Override
    public String toString() {
        return "ExampleBO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", surname=" + surname +
                ", created=" + created +
                '}';
    }
}
