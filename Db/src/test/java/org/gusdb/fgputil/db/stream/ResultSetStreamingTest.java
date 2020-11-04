package org.gusdb.fgputil.db.stream;

import static org.gusdb.fgputil.FormatUtil.NL;
import static org.gusdb.fgputil.functional.Functions.cSwallow;
import static org.gusdb.fgputil.functional.Functions.wrapException;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.gusdb.fgputil.Tuples.ThreeTuple;
import org.gusdb.fgputil.db.SqlScriptRunner;
import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.fgputil.db.stream.ResultSetIterator.RowConverter;
import org.gusdb.fgputil.functional.FunctionalInterfaces.FunctionWithException;
import org.gusdb.fgputil.iterator.GroupingIterator;
import org.gusdb.fgputil.iterator.GroupingStream;
import org.gusdb.fgputil.iterator.IteratorUtil;
import org.gusdb.fgputil.test.TestUtil;
import org.junit.Assert;
import org.junit.Test;

public class ResultSetStreamingTest {

  private static final String DB_SETUP_SCRIPT = "org/gusdb/fgputil/db/runner/streamingTestSetup.sql";

  private static final String[] EXPECTED_OUTPUT = {
      "infra: [ ryan, ellie, steve ]",
      "ui: [ dave, jamie, cristina ]",
      "dd: [ johnb, bindu, haiming ]",
      "outreach: [ brian, omar, suzanne ]"
  };

  // class will represent a row from the "records" table in our DB
  private static class Person extends ThreeTuple<Integer, String, String> {
    public Person(int id, String group, String name) {
      super(id, group, name);
    }
    public String getName() { return getThird(); }
    public String getGroup() { return getSecond(); }

    public static RowConverter<Person> fromResultSet = row ->
        Optional.of(new Person(row.getInt(1), row.getString(2), row.getString(3)));
  }

  // formatter to convert a group of people to a single String
  private static FunctionWithException<List<Person>,String> FORMATTER = people -> people.get(0).getGroup() +
    ": [ " + people.stream().map(Person::getName).collect(Collectors.joining(", ")) + " ]";

  private static DataSource _ds;

  private static DataSource getDb() throws SQLException, IOException {
    if (_ds == null) {
      _ds = TestUtil.getTestDataSource("ResultSetStreamingTest");
      SqlScriptRunner.runSqlScript(_ds, DB_SETUP_SCRIPT);
    }
    return _ds;
  }

  @Test
  public void iteratorTestWithWrite() throws Exception {
    testWritingAggregatedOutput((rs, writer) -> {

      // construct an iterator over objects constructed by the rows returned by the query
      Iterator<Person> people = new ResultSetIterator<>(rs, Person.fromResultSet);

      // group the objects by a common value
      Iterator<List<Person>> groups = new GroupingIterator<Person>(people,
          (p1, p2) -> p1.getGroup().equals(p2.getGroup()));

      // iterate through groups and format into strings to be written to stream
      for (List<Person> group : IteratorUtil.toIterable(groups)) {
        wrapException(() -> { writer.write(FORMATTER.apply(group) + NL); return null; });
      }
    });
  }

  @Test
  public void streamTestWithWrite() throws Exception {
    testWritingAggregatedOutput((rs, writer) -> {

      // construct an iterator over objects constructed by the rows returned by the query
      Stream<Person> people = new ResultSetStream<>(rs, Person.fromResultSet);

      // group the objects by a common value
      Stream<List<Person>> groups = GroupingStream.create(people,
          (p1, p2) -> p1.getGroup().equals(p2.getGroup()));

      // iterate through groups and format into strings to be written to stream
      groups.forEach(cSwallow(group -> writer.write(FORMATTER.apply(group) + NL)));

    });
  }

  private void testWritingAggregatedOutput(BiConsumer<ResultSet,Writer> writer) throws Exception {
    // create an output stream to capture the output from our streamer
    ByteArrayOutputStream capturedOutput = new ByteArrayOutputStream();

    // stream DB rows into an aggregator, format, and write to an output stream
    writeAggregatedData(getDb(), capturedOutput, writer);

    // read the output from the stream for analysis
    String[] outputRows = capturedOutput.toString().split("\\n");

    // same number of rows as expected?
    Assert.assertEquals(EXPECTED_OUTPUT.length, outputRows.length);

    // row values same as expected?
    for (int i = 0; i < EXPECTED_OUTPUT.length; i++) {
      System.out.println(outputRows[i]);
      Assert.assertEquals(EXPECTED_OUTPUT[i], outputRows[i]);
    }
  }

  private static void writeAggregatedData(DataSource ds, OutputStream out, BiConsumer<ResultSet,Writer> appender) {

    // SQL to get rows from our test DB
    String sql = "select * from records";

    // run SQL, process output, format and write aggregated records to output stream
    new SQLRunner(ds, sql).executeQuery(rs -> {
      try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out))) {
        appender.accept(rs, writer);
        writer.flush();
        return null;
      }
      catch (IOException e) {
        throw new RuntimeException("Unable to write to output stream", e);
      }
    });
  }

  @Test
  public void emptyIteratorTest() throws SQLException, IOException {

    // SQL to get rows from our test DB
    String sql = "select * from records where 1 = 0";

    try (ResultSetIterator<Person> people = ResultSets.openIterator(
        getDb(), sql, Person.fromResultSet)) {

      // count people
      int count = 0;
      while (people.hasNext()) {
        people.next();
        count++;
      }
      Assert.assertEquals(0, count);
    }
  }

  @Test
  public void streamTest() throws SQLException, IOException {

    // SQL to get rows from our test DB
    String sql = "select * from records";

    // run SQL, process output, format and write aggregated records to output stream
    try (Stream<Person> people = ResultSets.openStream(getDb(), sql, Person.fromResultSet)) {
      List<Person> list = people.collect(Collectors.toList());
      Assert.assertEquals(12, list.size());
    }
  }

  @Test
  public void streamCountTest() throws SQLException, IOException {

    // SQL to get rows from our test DB
    String sql = "select * from records";

    // run SQL, process output, format and write aggregated records to output stream
    try (Stream<Person> people = ResultSets.openStream(getDb(), sql, Person.fromResultSet)) {
      Assert.assertEquals(12, people.count());
    }
  }

}
