package com.google.javascript.jscomp;

import java.util.HashMap;

public class VarVal {
	
	static final String UNDEF = "*UNDEF*";
	static final String NAC = "*NAC*";
	static final String NOTSTART = "*NOTSTART*";
	
	private String name;
	private String value;
	private String type ;
	
	public VarVal(String _name)
	{
		name = _name;
		value = UNDEF;
		type = new String();
	}
	
	public VarVal(VarVal v)
	{
		value = new String(v.value);
		type = new String(v.type);
	}
	
	public void init()
	{
		value = UNDEF;
		type = UNDEF;
	}
	
	public String getValue() 
	{
		return value;
	}
	
	public String getType()
	{
		return type;
	}
	public String getName()
	{
		return name;
	}
	public void setValue(String _value) 
	{
		value = _value;
	}
	
	public void setType(String _type)
	{
		type = _type;
	}
	
	public void merge(VarVal _value) 
	{
		if(value.equals(NOTSTART))
		{
			value = UNDEF;
		}
		
		if(_value.value.equals(value))
			return;
		
		if(_value.value.equals(UNDEF))
			return;
		
		value = NAC;
		return;
	}
	
	public final boolean equals(Object o)
	{
		if(o.getClass().equals(this.getClass()))
			return false;
		
		return (((VarVal)o).name.equals(this.name) && ((VarVal)o).value.equals(this.value));
	}
	
	public final String toString()
	{
		return value.toString() + " & " + type.toString();
	}
}
