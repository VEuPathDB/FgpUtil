package org.gusdb.fgputil.server;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.EncryptionUtil;
import org.gusdb.fgputil.IoUtil;
import org.gusdb.fgputil.db.platform.SupportedPlatform;
import org.gusdb.fgputil.db.pool.ConnectionPoolConfig;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.fgputil.db.pool.SimpleDbConfig;
import org.json.JSONArray;
import org.json.JSONObject;

public class UserAwareContext extends BasicApplicationContext {

  private static final Logger LOG = Logger.getLogger(UserAwareContext.class);

  // account DB config
  private static final String ACCOUNT_DB_CONNECTION_URL = "accountDbConnectionUrl";
  private static final String ACCOUNT_DB_USERNAME = "accountDbUsername";
  private static final String ACCOUNT_DB_PASSWORD = "accountDbPassword";
  private static final String ACCOUNT_DB_POOL_SIZE = "accountDbPoolSize";
  private static final String ACCOUNT_DB_PLATFORM = "accountDbPlatform";

  // account DB defaults
  private static final String DEFAULT_PLATFORM = SupportedPlatform.ORACLE.toString();
  private static final int DEFAULT_POOL_SIZE = 20;

  // key to access account DB instance object
  private static final String ACCOUNT_DB = "account_db";

  // secret key file key (in JSON and map)
  private static final String SECRET_KEY_FILE = "secretKeyFile";


  public UserAwareContext(JSONObject config) {
    try {
      put(ACCOUNT_DB, createAccountDb(config));
      put(SECRET_KEY_FILE, config.getString(SECRET_KEY_FILE));
      // test that secret key can be read on load
      getSecretKey();
    }
    catch (Exception e) {
      handleInitException(e);
    }
  }

  private void handleInitException(Exception e) {
    closeAccountDb();
    throw new RuntimeException("Unable to initialize application.  " +
        "Config file JSON must have the following properties: " +
        getConfigFileHelp().toString(2), e);
  }

  private void closeAccountDb() {
    if (getAccountDb() != null) {
      try {
        getAccountDb().close();
      }
      catch (Exception e) {
        // log but do not throw; this is a quiet failure
        LOG.error("Unable to close account DB instance", e);
      }
    }
  }

  private DatabaseInstance createAccountDb(JSONObject config) {
    ConnectionPoolConfig configObj = SimpleDbConfig.create(
        SupportedPlatform.toPlatform(config.optString(ACCOUNT_DB_PLATFORM, DEFAULT_PLATFORM)),
        config.getString(ACCOUNT_DB_CONNECTION_URL),
        config.getString(ACCOUNT_DB_USERNAME),
        config.getString(ACCOUNT_DB_PASSWORD),
        config.optInt(ACCOUNT_DB_POOL_SIZE, DEFAULT_POOL_SIZE));
    return new DatabaseInstance(configObj);
  }

  public DatabaseInstance getAccountDb() {
    return (DatabaseInstance)get(ACCOUNT_DB);
  }

  public String getSecretKey() {
    String fileName = (String)get(SECRET_KEY_FILE);
    try (Reader in = new FileReader(fileName)) {
      return EncryptionUtil.md5(IoUtil.readAllChars(in).strip());
    }
    catch (IOException e) {
      throw new RuntimeException("Could not read secret key from file: " + fileName, e);
    }
  }

  @Override
  public void close() {
    closeAccountDb();
  }

  public JSONArray getConfigFileHelp() {
    return new JSONArray()
      .put(new JSONObject()
        .put("name", ACCOUNT_DB_CONNECTION_URL)
        .put("type", "string")
        .put("description", "Connection URL to account DB")
        .put("isRequired", true))
      .put(new JSONObject()
        .put("name", ACCOUNT_DB_USERNAME)
        .put("type", "string")
        .put("description", "Username used to access account DB")
        .put("isRequired", true))
      .put(new JSONObject()
        .put("name", ACCOUNT_DB_PASSWORD)
        .put("type", "string")
        .put("description", "Password used to access account DB")
        .put("isRequired", true))
      .put(new JSONObject()
        .put("name", ACCOUNT_DB_POOL_SIZE)
        .put("type", "positive integer")
        .put("description", "Size of account DB connection pool")
        .put("isRequired", false)
        .put("defaultIfOmitted", DEFAULT_POOL_SIZE))
      .put(new JSONObject()
        .put("name", ACCOUNT_DB_PLATFORM)
        .put("type", "string")
        .put("description", "Platform of account DB")
        .put("isRequired", false)
        .put("defaultIfOmitted", DEFAULT_PLATFORM))
      .put(new JSONObject()
        .put("name", SECRET_KEY_FILE)
        .put("type", "string")
        .put("description", "Path to secret key file used to decode user auth keys")
        .put("isRequired", true));
  }
}
