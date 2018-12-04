package org.gusdb.fgputil.db.cache;

import java.sql.ResultSet;
import java.util.function.Function;

import javax.sql.DataSource;

import org.gusdb.fgputil.Wrapper;
import org.gusdb.fgputil.cache.InMemoryCache;
import org.gusdb.fgputil.cache.ValueFactory;
import org.gusdb.fgputil.cache.ValueProductionException;
import org.gusdb.fgputil.db.runner.SQLRunner;

public class SqlResultCache<T> extends InMemoryCache<String,T> {

  private final DataSource _ds;
  private final Function<ResultSet, T> _resultParser;

  public SqlResultCache(DataSource ds, Function<ResultSet,T> resultParser) {
    _ds = ds;
    _resultParser = resultParser;
  }

  public T getItem(String sql, String sqlName) throws ValueProductionException {
    return getValue(sql, getFetcher(sql, sqlName));
  }

  public ValueFactory<String,T> getFetcher(String sql, String sqlName) {
    return sql2 /* unused; same as sql */ -> {
      Wrapper<T> wrapper = new Wrapper<T>();
      new SQLRunner(_ds, sql, sqlName).executeQuery(rs -> {
        wrapper.set(_resultParser.apply(rs));
      });
      return wrapper.get();
    };
  }
}
