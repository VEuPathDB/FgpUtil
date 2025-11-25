package org.gusdb.fgputil.db.runner;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ParamBuilder {

  private final List<Object> _paramValues = new ArrayList<>();
  private final List<Integer> _paramTypes = new ArrayList<>();

  public ParamBuilder() { }

  private ParamBuilder add(Object value, int type) {
    _paramValues.add(value);
    _paramTypes.add(type);
    return this;
  }

  public ParamBuilder addLong(Long value)          { return add(value, Types.BIGINT); }
  public ParamBuilder addBlob(Object value)        { return add(value, Types.BLOB); }
  public ParamBuilder addBoolean(Boolean value)    { return add(value, Types.BOOLEAN); }
  public ParamBuilder addClob(Object value)        { return add(value, Types.CLOB); }
  public ParamBuilder addDate(Date value)          { return add(value, Types.DATE); }
  public ParamBuilder addDouble(Double value)      { return add(value, Types.DOUBLE); }
  public ParamBuilder addFloat(Float value)        { return add(value, Types.FLOAT); }
  public ParamBuilder addInteger(Integer value)    { return add(value, Types.INTEGER); }
  public ParamBuilder addLongVarChar(Object value) { return add(value, Types.LONGVARCHAR); }
  public ParamBuilder addNumeric(Object value)     { return add(value, Types.NUMERIC); }
  public ParamBuilder addShort(Short value)        { return add(value, Types.SMALLINT); }
  public ParamBuilder addTimestamp(Date value)     { return add(value, Types.TIMESTAMP); }
  public ParamBuilder addString(String value)      { return add(value, Types.VARCHAR); }

  Object[] getParamValues() {
    return _paramValues.toArray();
  }

  Integer[] getParamTypes() {
    return _paramTypes.toArray(new Integer[]{});
  }
}
