package org.gusdb.fgputil.accountdb;

import static org.gusdb.fgputil.EncryptionUtil.encryptPassword;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.Wrapper;
import org.gusdb.fgputil.db.DBStateException;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.fgputil.db.runner.SQLRunner.ArgumentBatch;
import org.gusdb.fgputil.db.runner.SQLRunner.ResultSetHandler;
import org.gusdb.fgputil.functional.FunctionalInterfaces.Function;
import org.gusdb.fgputil.functional.FunctionalInterfaces.Predicate;
import org.gusdb.fgputil.functional.FunctionalInterfaces.Procedure;
import org.gusdb.fgputil.functional.Functions;
import org.gusdb.fgputil.iterator.IteratorUtil;

public class AccountManager {

  @SuppressWarnings("unused")
  private static final Logger LOG = Logger.getLogger(AccountManager.class);

  public static final String TABLE_ACCOUNTS = "accounts";
  public static final String TABLE_ACCOUNT_PROPS = "account_properties";

  private static final String COL_USER_ID = "user_id";
  private static final String COL_EMAIL = "email";
  private static final String COL_PASSWORD = "passwd";
  private static final String COL_IS_GUEST = "is_guest";
  private static final String COL_SIGNATURE = "signature";
  private static final String COL_STABLE_ID = "stable_id";
  private static final String COL_REGISTER_TIME = "register_time";
  private static final String COL_LAST_LOGIN = "last_login";

  private static final String COL_PROP_KEY = "key";
  private static final String COL_PROP_VALUE = "value";

  private static final String ACCOUNT_SCHEMA_MACRO = "$$ACCOUNT_SCHEMA$$";
  private static final String DEFINED_PROPERTY_NAMES_MACRO = "$$DEFINED_PROPERTY_NAMES$$";
  private static final String DEFINED_PROPERTY_SELECTION_MACRO = "$$DEFINED_PROPERTIES_MACRO$$";
  private static final String DEFINED_PROPERTY_NAME_MACRO = "$$PROPERTY_NAME$$";

  private static final String INSERT_USER_SQL =
      "insert into " + ACCOUNT_SCHEMA_MACRO + TABLE_ACCOUNTS + " (" + COL_USER_ID +
      "    , " + COL_EMAIL +
      "    , " + COL_PASSWORD +
      "    , " + COL_IS_GUEST +
      "    , " + COL_SIGNATURE +
      "    , " + COL_STABLE_ID +
      "    , " + COL_REGISTER_TIME +
      "    , " + COL_LAST_LOGIN +
      ") values (?, ?, ?, ?, ?, ?, ?, ?)";
  private static final Integer[] INSERT_USER_PARAM_TYPES = {
      Types.BIGINT, Types.VARCHAR, Types.VARCHAR, Types.INTEGER,
      Types.VARCHAR, Types.VARCHAR, Types.TIMESTAMP, Types.TIMESTAMP
  };

  private static final String INSERT_PROPERTY_SQL = 
      "insert into " + ACCOUNT_SCHEMA_MACRO + TABLE_ACCOUNT_PROPS +
      " (" + COL_USER_ID + "," + COL_PROP_KEY + "," + COL_PROP_VALUE + ") values (?, ?, ?)";
  private static final Integer[] INSERT_PROPERTY_PARAM_TYPES = {
      Types.BIGINT, Types.VARCHAR, Types.VARCHAR
  };

  private static final String REMOVE_PROPERTIES_SQL =
      "delete from " + ACCOUNT_SCHEMA_MACRO + TABLE_ACCOUNT_PROPS + " where " + COL_USER_ID + " = ?";
  private static final Integer[] REMOVE_PROPERTIES_PARAM_TYPES = { Types.BIGINT };

  private static final String SELECT_FLAT_USER_SQL =
      "select u." + COL_USER_ID +
      "    , " + COL_EMAIL +
      "    , " + COL_IS_GUEST +
      "    , " + COL_SIGNATURE +
      "    , " + COL_STABLE_ID +
      "    , " + COL_REGISTER_TIME +
      "    , " + COL_LAST_LOGIN + DEFINED_PROPERTY_NAMES_MACRO +
      "  from " + ACCOUNT_SCHEMA_MACRO + TABLE_ACCOUNTS + " u " +
      "  left join (" +
      "    select " + COL_USER_ID + DEFINED_PROPERTY_SELECTION_MACRO +
      "    from " + ACCOUNT_SCHEMA_MACRO + TABLE_ACCOUNT_PROPS +
      "    group by " + COL_USER_ID +
      "  ) p" +
      "  on u." + COL_USER_ID + " = p." + COL_USER_ID;

  private static final String PROPERTY_COLUMN_SELECTION_SQL =
      ", max(case when key = '" + DEFINED_PROPERTY_NAME_MACRO + "' then value end) as " + DEFINED_PROPERTY_NAME_MACRO;

  private static final String UPDATE_PASSWORD_SQL =
      "update " + ACCOUNT_SCHEMA_MACRO + TABLE_ACCOUNTS + " set " + COL_PASSWORD + " = ? where " + COL_USER_ID + " = ?";
  private static final Integer[] UPDATE_PASSWORD_PARAM_TYPES = { Types.VARCHAR, Types.BIGINT };

  private static final String UPDATE_LAST_LOGIN_SQL =
      "update " + ACCOUNT_SCHEMA_MACRO + TABLE_ACCOUNTS + " set "+ COL_LAST_LOGIN + " = ? where " + COL_USER_ID + " = ?";
  private static final Integer[] UPDATE_LAST_LOGIN_PARAM_TYPES = { Types.TIMESTAMP, Types.BIGINT };

  private static final String UPDATE_EMAIL_SQL =
      "update " + ACCOUNT_SCHEMA_MACRO + TABLE_ACCOUNTS + " set "+ COL_EMAIL + " = ? where " + COL_USER_ID + " = ?";
  private static final Integer[] UPDATE_EMAIL_PARAM_TYPES = { Types.VARCHAR, Types.BIGINT };

  private final DatabaseInstance _accountDb;
  private final String _accountSchema;
  private final Map<String, UserPropertyName> _propertyNames = new LinkedHashMap<>();
  private final String _selectSql;

  public AccountManager(DatabaseInstance accountDb, String accountSchema, List<UserPropertyName> propertyNames) {
    _accountDb = accountDb;
    _accountSchema = accountSchema;
    for (UserPropertyName prop : propertyNames) {
      _propertyNames.put(prop.getName(), prop);
    }
    _selectSql = getSelectSql(_accountSchema, propertyNames);
  }

  private static String getSelectSql(String schema, List<UserPropertyName> propertyNames) {
    return SELECT_FLAT_USER_SQL
        .replace(ACCOUNT_SCHEMA_MACRO, schema)
        .replace(DEFINED_PROPERTY_NAMES_MACRO, FormatUtil.join(
            Functions.mapToList(propertyNames, new Function<UserPropertyName, String>() {
              @Override public String apply(UserPropertyName prop) {
                return ", " + prop.getDbKey();
              }}).toArray(), ""))
        .replace(DEFINED_PROPERTY_SELECTION_MACRO, FormatUtil.join(
            Functions.mapToList(propertyNames, new Function<UserPropertyName, String>() {
              @Override public String apply(UserPropertyName prop) {
                return PROPERTY_COLUMN_SELECTION_SQL.replace(DEFINED_PROPERTY_NAME_MACRO, prop.getDbKey());
              }}).toArray(), ""));
  }

  public UserProfile getUserProfile(Long userId) {
    // note: need to qualify user_id column with 'u.' because of join above; not needed for other fields
    return getSingleUserProfile(" where u." + COL_USER_ID + " = ?",
        new Object[] { userId },
        new Integer[] { Types.INTEGER });
  }

  public UserProfile getUserProfile(String email) {
    return getSingleUserProfile(" where " + COL_EMAIL + " = ?",
        new Object[] { email.trim().toLowerCase() },
        new Integer[] { Types.VARCHAR });
  }

  public UserProfile getUserProfile(String email, String password) {
    return getSingleUserProfile(" where " + COL_EMAIL + " = ? and " + COL_PASSWORD + " = ?",
        new Object[] { email.trim().toLowerCase(), encryptPassword(password) },
        new Integer[] { Types.VARCHAR, Types.VARCHAR });
  }

  public UserProfile getUserProfileBySignature(String signature) {
    return getSingleUserProfile(" where " + COL_SIGNATURE + " = ?",
        new Object[] { signature },
        new Integer[] { Types.VARCHAR });
  }

  private UserProfile getSingleUserProfile(final String condition, final Object[] params, final Integer[] types) {
    String sql = new StringBuilder(_selectSql).append(condition).toString();
    final Wrapper<UserProfile> profileWrapper = new Wrapper<>();
    new SQLRunner(_accountDb.getDataSource(), sql).executeQuery(params, types, new ResultSetHandler() {
      @Override public void handleResult(ResultSet rs) throws SQLException {
        if (rs.next()) {
          profileWrapper.set(loadUserProfile(rs, _propertyNames.values()));
          if (rs.next()) {
            throw new IllegalStateException("More than one user found under condition '" +
                condition + "' with values: " + FormatUtil.join(params, ", "));
          }}}});
    return profileWrapper.get();
  }

  private static UserProfile loadUserProfile(ResultSet rs, Collection<UserPropertyName> props) throws SQLException {
    UserProfile profile = new UserProfile();
    profile.setUserId(rs.getLong(COL_USER_ID));
    profile.setEmail(rs.getString(COL_EMAIL));
    profile.setGuest(rs.getBoolean(COL_IS_GUEST));
    profile.setSignature(rs.getString(COL_SIGNATURE));
    profile.setStableId(rs.getString(COL_STABLE_ID));
    profile.setRegisterTime(rs.getDate(COL_REGISTER_TIME));
    profile.setLastLoginTime(rs.getDate(COL_LAST_LOGIN));
    Map<String, String> properties = new HashMap<>();
    for (UserPropertyName prop : props) {
      String value = rs.getString(prop.getDbKey());
      if (!rs.wasNull()) {
        properties.put(prop.getName(), value);
      }
    }
    profile.setProperties(properties);
    return profile;
  }

  public UserProfile createAccount(String email, String password, Map<String, String> profileProperties) throws Exception {

    // make sure email is lowercase
    email = email.trim().toLowerCase();

    // assign user ID to the new user (unique, stable, sequential, primary key identifier)
    long userId = getNextUserId();

    // generate signature for this user (unique, stable, anonymous identifier)
    String signature = encryptPassword(userId + "_" + email);

    // generate stable ID for this user (unique, stable, human-readable identifier)
    String stableId = createStableId(email, userId);

    // save new user account to DB
    persistNewAccount(userId, email, password, signature, stableId, profileProperties, false);

    // read user profile back out of the database
    return getUserProfile(userId);
  }

  private void persistNewAccount(long userId, String email, String password, String signature,
      String stableId, Map<String, String> profileProperties, boolean assignLastLogin) throws Exception {
    // define SQL and params to insert user row
    Timestamp now = new Timestamp(new Date().getTime());
    final String insertUserSql = INSERT_USER_SQL.replace(ACCOUNT_SCHEMA_MACRO, _accountSchema);
    final Object[] params = { userId, email, encryptPassword(password), false,
        signature, stableId, now, assignLastLogin ? now : null };

    // define SQL and params to insert property rows
    final String insertPropSql = INSERT_PROPERTY_SQL.replace(ACCOUNT_SCHEMA_MACRO, _accountSchema);
    final ArgumentBatch propertyBatch = getUserPropertyBatch(userId, profileProperties);

    // perform all inserts in a transaction
    final Connection conn = _accountDb.getDataSource().getConnection();
    try {
      SqlUtils.performInTransaction(conn, new Procedure() {
        @Override public void perform() {
          // perform user row insert
          new SQLRunner(conn, insertUserSql, "insert-user-row").executeStatement(params, INSERT_USER_PARAM_TYPES);
          // perform property rows insert
          new SQLRunner(conn, insertPropSql, "insert-user-prop-rows").executeStatementBatch(propertyBatch);
        }
      });
    }
    finally {
      SqlUtils.closeQuietly(conn);
    }
  }

  private long getNextUserId() throws DBStateException, SQLException {
    return _accountDb.getPlatform().getNextId(_accountDb.getDataSource(), _accountSchema, TABLE_ACCOUNTS);
  }

  private ArgumentBatch getUserPropertyBatch(final long userId, Map<String, String> profileProperties) {
    // deal with null property map; this can sometimes be passed
    if (profileProperties == null) profileProperties = Collections.EMPTY_MAP;
    // first trim props to those allowed by the configuration of this account manager
    final Set<String> propKeys = _propertyNames.keySet();
    final Map<String,String> trimmedProps = Functions.pickKeys(profileProperties, new Predicate<String>(){
      @Override public boolean test(String propKey) {
        return propKeys.contains(propKey);
      }
    });
    return new ArgumentBatch() {

      @Override
      public Iterator<Object[]> iterator() {
        return IteratorUtil.transform(trimmedProps.entrySet().iterator(), new Function<Entry<String,String>,Object[]>() {
          @Override public Object[] apply(Entry<String, String> property) {
            return new Object[] { userId, property.getKey(), property.getValue() };
          }
        });
      }

      @Override
      public int getBatchSize() {
        return trimmedProps.size();
      }

      @Override
      public Integer[] getParameterTypes() {
        return INSERT_PROPERTY_PARAM_TYPES;
      }
    };
  }

  private static String createStableId(String email, long userId) {
    int atSignIndex = email.indexOf('@');
    if (atSignIndex == -1) atSignIndex = email.length();
    return email.substring(0, atSignIndex).toLowerCase() + "." + userId;
  }

  public UserProfile createGuestAccount(String emailPrefix) throws Exception {
    long userId = getNextUserId();
    String stableId = emailPrefix + userId;
    String email = stableId;
    String signature = encryptPassword(userId + "_" + email);
    persistNewAccount(userId, email, "", signature, stableId, null, true);
    return getUserProfile(userId);
  }

  private void updateColumn(String rawSql, Integer[] argTypes, String queryName, long userId, Object newValue) {
    String sql = rawSql.replace(ACCOUNT_SCHEMA_MACRO, _accountSchema);
    new SQLRunner(_accountDb.getDataSource(), sql, queryName)
      .executeUpdate(new Object[]{ newValue, userId }, argTypes);
  }

  public void updatePassword(long userId, String newPassword) {
    updateColumn(UPDATE_PASSWORD_SQL, UPDATE_PASSWORD_PARAM_TYPES,
        "update-user-password", userId, encryptPassword(newPassword));
  }

  public void updateLastLogin(long userId) {
    updateColumn(UPDATE_LAST_LOGIN_SQL, UPDATE_LAST_LOGIN_PARAM_TYPES,
        "update-user-last-login", userId, new Timestamp(new Date().getTime()));
  }

  public void saveUserProfile(final long userId, String email, Map<String, String> profileProperties) throws Exception {

    // first update email; this can be done independently of property updates
    updateColumn(UPDATE_EMAIL_SQL, UPDATE_EMAIL_PARAM_TYPES,
        "update-user-email", userId, email.trim().toLowerCase());

    // define SQL and params to remove existing property rows and replace with new via insert
    final String removePropsSql = REMOVE_PROPERTIES_SQL.replace(ACCOUNT_SCHEMA_MACRO, _accountSchema);
    final Object[] removePropsParams = { userId };
    final String insertPropSql = INSERT_PROPERTY_SQL.replace(ACCOUNT_SCHEMA_MACRO, _accountSchema);
    final ArgumentBatch propertyBatch = getUserPropertyBatch(userId, profileProperties);

    // perform all property-related operations in a transaction
    final Connection conn = _accountDb.getDataSource().getConnection();
    try {
      SqlUtils.performInTransaction(conn, new Procedure() {
        @Override public void perform() {
          // perform property rows delete
          new SQLRunner(conn, removePropsSql, "remove-user-prop-rows").executeStatement(removePropsParams, REMOVE_PROPERTIES_PARAM_TYPES);
          // perform property rows insert
          new SQLRunner(conn, insertPropSql, "insert-user-prop-rows").executeStatementBatch(propertyBatch);
        }
      });
    }
    finally {
      SqlUtils.closeQuietly(conn);
    }
  }

}
