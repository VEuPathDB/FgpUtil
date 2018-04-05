package org.gusdb.fgputil.db.cache;

import java.sql.ResultSet;

import javax.sql.DataSource;

import org.gusdb.fgputil.Wrapper;
import org.gusdb.fgputil.cache.ItemCache;
import org.gusdb.fgputil.cache.NoUpdateItemFetcher;
import org.gusdb.fgputil.cache.UnfetchableItemException;
import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.fgputil.functional.FunctionalInterfaces.Function;

public class SqlResultCache<T> extends ItemCache<String,T> {

  private final DataSource _ds;
  private final Function<ResultSet, T> _resultParser;

  public SqlResultCache(DataSource ds, Function<ResultSet,T> resultParser) {
    _ds = ds;
    _resultParser = resultParser;
  }

  public T getItem(String sql, String sqlName) throws UnfetchableItemException {
    return getItem(sql, getFetcher(sql, sqlName));
  }

  public NoUpdateItemFetcher<String,T> getFetcher(String sql, String sqlName) {
    return sql2 /* unused; same as sql */ -> {
      Wrapper<T> wrapper = new Wrapper<T>();
      new SQLRunner(_ds, sql, sqlName).executeQuery(rs -> {
        wrapper.set(_resultParser.apply(rs));
      });
      return wrapper.get();
    };
  }
}
