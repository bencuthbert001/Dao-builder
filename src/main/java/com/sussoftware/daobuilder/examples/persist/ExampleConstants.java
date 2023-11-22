package com.sussoftware.daobuilder.examples.persist;
/**
 * Auto generated dao class by DAO-BUILDER: Wed Nov 22 13:41:41 GMT 2023
 */
public class ExampleConstants {
	public static final String TABLE_NAME = "EXAMPLE_TABLE";
	public static final String INSERT_STATEMENT = "INSERT INTO EXAMPLE_TABLE(ID,NAME,SURNAME,CREATED) VALUES(:ID,:NAME,:SURNAME,:CREATED)";
	public static final String UPDATE_STATEMENT = "UPDATE EXAMPLE_TABLE SET NAME = :NAME,SURNAME = :SURNAME,CREATED = :CREATED  WHERE ID =  :ID";
	public static final String SELECT_ALL_SQL = "SELECT * FROM EXAMPLE_TABLE";
	public static final String DELETE_ID_SQL = "DELETE FROM EXAMPLE_TABLE WHERE ID = :ID";
	public static final String SELECT_BY_CODE = "SELECT * FROM EXAMPLE_TABLE WHERE NAME = :NAME";
	public static final String SELECT_BY_ID = "SELECT * FROM EXAMPLE_TABLE WHERE ID = :ID";
	public static final String ID = "ID";
	public static final String NAME = "NAME";
	public static final String SURNAME = "SURNAME";
	public static final String CREATED = "CREATED";
}