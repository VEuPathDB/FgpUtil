package org.gusdb.fgputil.db.stream;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.gusdb.fgputil.Tuples.ThreeTuple;
import org.gusdb.fgputil.db.SqlScriptRunner;
import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.fgputil.iterator.GroupingIterator;
import org.gusdb.fgputil.iterator.IteratorUtil;
import org.gusdb.fgputil.test.TestUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ResultSetStreamingTest {

  private static final String DB_SETUP_SCRIPT = "org/gusdb/fgputil/db/runner/streamingTestSetup.sql";

  private DataSource _ds;

  @Before
  public void setUpTests() throws Exception {
    _ds = TestUtil.getTestDataSource("mymemdb");
    SqlScriptRunner.runSqlScript(_ds, DB_SETUP_SCRIPT);
  }

  private static class Person extends ThreeTuple<Integer, String, String>{
    public Person(int id, String group, String name) {
      super(id, group, name);
    }
    public String getName() { return getThird(); }
    public String getGroup() { return getSecond(); }
  }

  private static final String[] EXPECTED_OUTPUT = {
      "infra: [ ryan, ellie, steve ]",
      "ui: [ dave, jamie, cristina ]",
      "dd: [ johnb, bindu, haiming ]",
      "outreach: [ brian, omar, suzanne ]"
  };

  @Test
  public void streamingTest() throws IOException {

    // create an output stream to capture the output from our streamer
    ByteArrayOutputStream capturedOutput = new ByteArrayOutputStream();

    writeAggregatedData(capturedOutput);

    String[] aggregatedRows = capturedOutput.toString().split("\\n");

    Assert.assertEquals(EXPECTED_OUTPUT.length, aggregatedRows.length);
    for (int i = 0; i < EXPECTED_OUTPUT.length; i++) {
      System.out.println(aggregatedRows[i]);
      Assert.assertEquals(EXPECTED_OUTPUT[i], aggregatedRows[i]);
    }
  }

  private void writeAggregatedData(OutputStream out) throws IOException {

    // SQL to get rows from our test DB
    String sql = "select * from records";

    // formatter to convert a group of people to a single String
    Function<List<Person>,String> formatter = people -> people.get(0).getGroup() +
      ": [ " + people.stream().map(Person::getName).collect(Collectors.joining(", ")) + " ]";

    try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out))) {
      new SQLRunner(_ds, sql).executeQuery(rs -> {
        try {
          // construct an iterator over objects constructed by the rows returned by the query
          Iterator<Person> people = new ResultSetIterator<>(rs, row ->
            Optional.of(new Person(row.getInt(1), row.getString(2), row.getString(3))));

          // group the objects by a common value
          Iterator<List<Person>> groups = new GroupingIterator<Person>(people,
            (p1, p2) -> p1.getGroup().equals(p2.getGroup()));

          // iterate through groups and format into strings to be written to stream
          for (List<Person> group : IteratorUtil.toIterable(groups)) {
            writer.write(formatter.apply(group));
            writer.newLine();
          }

          writer.flush();
          return null;
        }
        catch (IOException e) {
          throw new RuntimeException("Unable to write to output stream", e);
        }
      });
    }
  }
}
