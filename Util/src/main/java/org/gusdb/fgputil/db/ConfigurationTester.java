package org.gusdb.fgputil.db;

import org.gusdb.fgputil.db.platform.SupportedPlatform;
import org.gusdb.fgputil.db.pool.ConnectionPoolConfig;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.fgputil.db.pool.SimpleDbConfig;
import org.gusdb.fgputil.db.runner.SQLRunner;

public class ConfigurationTester {

  public static void main(String[] args) throws Exception {
    if (args.length != 4) {
      System.err.println("USAGE: fgpJava " + ConfigurationTester.class.getName() +
          " [" + SupportedPlatform.getSupportedPlatformsString() + "] <connectionUrl> <login> <password>");
      System.exit(1);
    }
    SupportedPlatform platform = SupportedPlatform.toPlatform(args[0]);
    ConnectionPoolConfig dbConfig = SimpleDbConfig.create(platform, args[1], args[2], args[3], (short)1);
    try (DatabaseInstance db = new DatabaseInstance(dbConfig)) {
      new SQLRunner(db.getDataSource(), db.getPlatform().getValidationQuery()).executeStatement();
      System.out.println("Successfully connected and executed validation query.");
    }
  }
}
