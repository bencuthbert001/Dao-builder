package com.sussoftware.daobuilder.examples.persist;
/**
 * Auto generated dao class by DAO-Builder: Wed Nov 22 13:41:41 GMT 2023
 */
import com.sussoftware.daobuilder.examples.ExampleBO;
import java.sql.SQLException;
import java.util.List;

public interface ExampleDao {

	public boolean create(ExampleBO data) throws SQLException;

	public List<ExampleBO> findAll();

	public void delete(long id);

	public void update(ExampleBO data) throws SQLException;

	ExampleBO findByName(String name);

	ExampleBO findById(long id);

}