package com.sussoftware.daobuilder.examples.persist;
import com.sussoftware.daobuilder.examples.ExampleBO;
import java.sql.SQLException;
import java.util.List;

/**
* Auto generated dao implementation class by DAO-Builder : Thu Nov 30 08:38:53 GMT 2023
*/
public interface ExampleDao {

	public boolean create(ExampleBO data) throws SQLException;

	public List<ExampleBO> findAll();

	public void delete(long id);

	public void update(ExampleBO data) throws SQLException;

	public ExampleBO findById(long id);

	public List<ExampleBO> findByName(String name);

	public ExampleBO findBySurname(String surname);

}