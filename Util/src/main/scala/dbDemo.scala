package org.gusdb.fgputil

import scala.collection.JavaConversions._

import org.gusdb.fgputil.db.platform.SupportedPlatform;
import org.gusdb.fgputil.db.pool.{ SimpleDbConfig, DatabaseInstance }
import org.gusdb.fgputil.db.runner.{ SQLRunner, BasicResultSetHandler }

object DumpWdkUsers {

  val dbConfig = SimpleDbConfig.create(
      SupportedPlatform.ORACLE,
      "jdbc:oracle:thin:@//localhost:5034/apicommr.upenn.edu",
      "uga_fed",
      "********" // <- uga_fed's password
  )

  val userSql = "select * from userlogins5.users where first_name = 'Ryan'"

  def main(args: Array[String]) {
    run()
  }

  def run() {
    println("Establishing connection")
    val db = new DatabaseInstance(dbConfig)
    try {
      val handler = new BasicResultSetHandler()
      println("Running SQL")
      new SQLRunner(db.getDataSource(), userSql).executeQuery(handler)
      println("Printing results")
      printResults(handler)
    }
    catch {
      case e: Exception => println("Error" + e.toString())
    }
    finally {
      db.close()
    }
  }

  def printResults(resultSetHandler: BasicResultSetHandler) {
    val colNames = resultSetHandler.getColumnNames()
    colNames.foreach { col => print(col + "\t") }
    println()
    resultSetHandler.getResults.foreach { row =>
      colNames.foreach { col => print(row.get(col) + "\t") }
      println()
    }
  }
}
