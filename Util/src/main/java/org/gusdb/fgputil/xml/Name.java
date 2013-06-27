package org.gusdb.fgputil.xml;

// to make digester easy.  probably a better way, involving tinkering w/
// digester
public class Name implements Comparable<Name> {
    String name;
    public void setName(String n) { name = n; }
    public String getName() { return name; }
    @Override
	public int compareTo(Name arg0) {
        return name.compareTo(arg0.name);
    }
    @Override
	public String toString() { return name;}
}

