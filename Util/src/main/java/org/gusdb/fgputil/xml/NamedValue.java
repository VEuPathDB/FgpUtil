package org.gusdb.fgputil.xml;

public class NamedValue {
    private String name;
    private String value;

    public NamedValue() {}
    public NamedValue(String name, String value) {
	this.name = name;
	this.value = value;
    }
    public void setName(String name) {this.name = name;}
    public String getName() { return name; }
    public void setValue(String value) {this.value = value;}
    public String getValue() {return value;}
}
