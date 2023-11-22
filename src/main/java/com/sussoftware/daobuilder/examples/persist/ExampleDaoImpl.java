package com.sussoftware.daobuilder.examples.persist;

import com.sussoftware.daobuilder.examples.ExampleBO;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * Auto generated dao implementation class by DAO-Builder
 */
public class ExampleDaoImpl implements ExampleDao {

	private static final Logger logger = LoggerFactory.getLogger(ExampleDaoImpl.class);

	private final NamedParameterJdbcTemplate jdbcTemplate;
	private final DataRowMapper dataRowMapper;

	public ExampleDaoImpl(NamedParameterJdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
		this.dataRowMapper = new DataRowMapper();
	}
	@Override
	public boolean create(ExampleBO data) throws SQLException {
		final long l = data.getCreated();
		final Timestamp from = Timestamp.from(Instant.ofEpochMilli(l));
		Map<String, Object> parameters = new HashMap<>();
		parameters.put(ExampleConstants.ID, data.getId());
		parameters.put(ExampleConstants.NAME, data.getName());
		parameters.put(ExampleConstants.SURNAME, data.getSurname());
		parameters.put(ExampleConstants.CREATED, data.getCreated());

		this.jdbcTemplate.update(ExampleConstants.INSERT_STATEMENT, parameters);
		return true;
	}
	@Override
	public List<ExampleBO> findAll() {
		final List<ExampleBO> query = this.jdbcTemplate.query(ExampleConstants.SELECT_ALL_SQL, this.dataRowMapper);
		return query;

	}
	@Override
	public void delete(long id) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put(ExampleConstants.ID, id);
		this.jdbcTemplate.update(ExampleConstants.DELETE_ID_SQL, parameters);
	}
	@Override
	public ExampleBO findByName(String key) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put(ExampleConstants.NAME, key);
		final List<ExampleBO> query = this.jdbcTemplate.query(ExampleConstants.SELECT_BY_CODE, parameters, this.dataRowMapper);
		final ExampleBO data = query.get(0);
		return data;
	}

	private class DataRowMapper implements RowMapper<ExampleBO> {
		public ExampleBO mapRow(ResultSet rs, int rowNum) throws SQLException {
			long id = rs.getLong(ExampleConstants.ID);
			String name = rs.getString(ExampleConstants.NAME);
			long surname = rs.getLong(ExampleConstants.SURNAME);
			long created = rs.getLong(ExampleConstants.CREATED);
			return new ExampleBO(id, name, surname, created);
		}
	}
}