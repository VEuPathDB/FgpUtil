package org.gusdb.fgputil.db.stream;

import javax.sql.DataSource;

import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.fgputil.db.stream.ResultSetIterator.RowConverter;

/**
 * This is the preferred way to create streams of records from the results of
 * database calls.
 *
 * @author rdoherty
 */
public class ResultSets {

  public static <T> ResultSetStream<T> openStream(
      DataSource ds, String sql, RowConverter<T> converter) {
    return openStream(ds, sql, SQLRunner.generateName(sql), converter);
  }

  public static <T> ResultSetStream<T> openStream(
      DataSource ds, String sql, String sqlName, RowConverter<T> converter) {
    return openStream(ds, sql, sqlName, new Object[]{}, new Integer[]{}, converter);
  }

  public static <T> ResultSetStream<T> openStream(
      DataSource ds, String sql, Object[] argValues, Integer[] argTypes, RowConverter<T> converter) {
    return openStream(ds, sql, SQLRunner.generateName(sql), argValues, argTypes, converter);
  }

  public static <T> ResultSetStream<T> openStream(
      DataSource ds, String sql, String sqlName, Object[] argValues, Integer[] argTypes, RowConverter<T> converter) {
    return new SQLRunner(ds, sql, sqlName)
        .setNotResponsibleForClosing()
        .executeQuery(argValues, argTypes,
            rs -> new ResultSetStream<T>(rs, converter));
  }

  public static <T> ResultSetIterator<T> openIterator(
      DataSource ds, String sql, RowConverter<T> converter) {
    return openIterator(ds, sql, SQLRunner.generateName(sql), converter);
  }

  public static <T> ResultSetIterator<T> openIterator(
      DataSource ds, String sql, String sqlName, RowConverter<T> converter) {
    return openIterator(ds, sql, sqlName, new Object[]{}, new Integer[]{}, converter);
  }

  public static <T> ResultSetIterator<T> openIterator(
      DataSource ds, String sql, Object[] argValues, Integer[] argTypes, RowConverter<T> converter) {
    return openIterator(ds, sql, SQLRunner.generateName(sql), argValues, argTypes, converter);
  }

  public static <T> ResultSetIterator<T> openIterator(
      DataSource ds, String sql, String sqlName, Object[] argValues, Integer[] argTypes, RowConverter<T> converter) {
    return new SQLRunner(ds, sql, sqlName)
        .setNotResponsibleForClosing()
        .executeQuery(argValues, argTypes,
            rs -> new ResultSetIterator<T>(rs, converter));
  }

}
