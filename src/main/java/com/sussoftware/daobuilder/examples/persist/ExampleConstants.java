package com.sussoftware.daobuilder.examples.persist;

/**
* Auto generated dao implementation class by DAO-Builder : Mon Jan 08 10:52:08 GMT 2024
*/

public class ExampleConstants {
	public static final String TABLE_NAME = "EXAMPLE_TABLE";
	public static final String TABLE_NAME_JOURNAL = "EXAMPLE_TABLE_JOURNAL";
	public static final String INSERT_STATEMENT = "INSERT INTO "+ExampleConstants.TABLE_NAME+"(ID,NAME,SURNAME,CREATED) VALUES(:ID,:NAME,:SURNAME,:CREATED)";

	public static final String INSERT_STATEMENT_JOURNAL = "INSERT INTO "+ExampleConstants.TABLE_NAME_JOURNAL+"(ID,NAME,SURNAME,CREATED) VALUES(:ID,:NAME,:SURNAME,:CREATED)";	public static final String UPDATE_STATEMENT = "UPDATE "+ExampleConstants.TABLE_NAME+" SET NAME = :NAME,SURNAME = :SURNAME,CREATED = :CREATED  WHERE ID =  :ID";
	public static final String SELECT_ALL_SQL = "SELECT * FROM "+ExampleConstants.TABLE_NAME+"";
	public static final String DELETE_ID_SQL = "DELETE FROM "+ExampleConstants.TABLE_NAME+" WHERE ID = :ID";
	public static final String SELECT_BY_ID = "SELECT * FROM "+ExampleConstants.TABLE_NAME+" WHERE ID = :ID";
	public static final String SELECT_BY_NAME = "SELECT * FROM "+ExampleConstants.TABLE_NAME+" WHERE NAME = :NAME";
	public static final String SELECT_BY_SURNAME = "SELECT * FROM "+ExampleConstants.TABLE_NAME+" WHERE SURNAME = :SURNAME";
	public static final String ID = "ID";
	public static final String NAME = "NAME";
	public static final String SURNAME = "SURNAME";
	public static final String CREATED = "CREATED";
}