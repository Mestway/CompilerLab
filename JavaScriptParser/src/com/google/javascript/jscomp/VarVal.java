package com.google.javascript.jscomp;

public class VarVal {
	
	static final String UNDEF = "*UNDEF*";
	static final String NAC = "*NAC*";
	
	String value;
	String type;
	
	public VarVal()
	{
		value = UNDEF;
	}
	
	public String getValue() 
	{
		return value;
	}
	
	public void setValue(String _value) 
	{
		value = _value;
	}
	
	public void mergeValue(String _value) 
	{
		if(_value.equals(value))
			return;
		
		if(_value.equals(UNDEF))
			return;
		
		value = NAC;
		return;
	}
}
