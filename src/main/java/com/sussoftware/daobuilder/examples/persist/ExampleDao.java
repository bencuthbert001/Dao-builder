package com.sussoftware.daobuilder.examples.persist;

import com.sussoftware.daobuilder.examples.ExampleBO;
import java.sql.SQLException;
import java.util.List;

/**
* Auto generated dao class by DAO-Builder
*/
public interface ExampleDao {

	public boolean create(ExampleBO data) throws SQLException;

	public List<ExampleBO> findAll();

	public void delete(long id);

	ExampleBO findByName(String name);

}