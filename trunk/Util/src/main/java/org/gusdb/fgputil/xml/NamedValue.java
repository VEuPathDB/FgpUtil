package org.gusdb.fgputil.xml;

import org.json.JSONObject;

public class NamedValue {
    private String name;
    private String value;
    
    public static final String NAME_KEY = "name";
    public static final String VALUE_KEY = "value";

    public NamedValue() {}
    public NamedValue(String name, String value) {
	this.name = name;
	this.value = value;
    }
    public void setName(String name) {this.name = name;}
    public String getName() { return name; }
    public void setValue(String value) {this.value = value;}
    public String getValue() {return value;}
    
    public JSONObject toJson() {
      JSONObject json = new JSONObject();
      json.put(NAME_KEY, name);
      json.put(VALUE_KEY, value);
      return json;
    }
}
