package com.google.javascript.jscomp;

import com.google.javascript.rhino.head.Token;

public class VarVal {
	
	static final String UNDEF = "*UNDEF*";
	static final String NAC = "*NAC*";
	
	private String name;
	private String value;
	private String type ;
	
	public VarVal(String _name)
	{
		name = _name;
		value = UNDEF;
		type = UNDEF;
	}
	public VarVal(VarVal v)
	{
		value = new String(v.value);
		type = new String(v.type);
		name = new String(v.name);
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
	public void setName(String nm)
	{
		name = nm;
	}
	public void setValue(String _value) 
	{
		value = _value;
	}
	
	public void setType(String _type)
	{
		type = _type;
	}
	
	public String merge(VarVal _value) 
	{	
		String ret = "";
		
		if(this.type.equals(UNDEF) || _value.type.equals(UNDEF))
		{
			this.type = this.type.equals(UNDEF) ? _value.type : this.type;
		}
		else if(! this.type.equals(_value.type))
		{
			ret = "Type Conflict: " + this.type + " " + _value.type;
		}
		
		if(this.value.equals(UNDEF))
		{
			value = _value.getValue();
			type = _value.getType();
			return ret;
		}
		
		if(_value.value.equals(value))
			return ret;
		
		if(_value.value.equals(UNDEF))
			return ret;
		
		value = NAC;
		return ret;
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
	
	public static VarVal merge(VarVal v1, VarVal v2, int token)
	{
		VarVal ret = new VarVal("WH_temp");
		
		ret.type = type_cmp(v1.type, v2.type);
		
		if(v1.getValue().equals(NAC) || v2.getValue().equals(NAC))
		{	
			ret.value = NAC;
		}
		else
		{
			if(token == Token.ADD)
			{
				if(ret.type == "String")
					ret.value = v1.value + v2.value;
				else if(ret.type == "number")
				{	
					String t1, t2;
					t1 = (v1.type == "boolean") ? WH_parse_boolean(v1.value).toString() : v1.value;
					t2 = (v2.type == "boolean") ? WH_parse_boolean(v2.value).toString() : v2.value;
					
					Double tempDouble = (Double.parseDouble(t1) + Double.parseDouble(t2));
					ret.value = tempDouble.toString();
				}
				else if(ret.type == "boolean")
				{
					ret.type = "number";
					Integer tempDouble = WH_parse_boolean(v1.value) + WH_parse_boolean(v2.value);
					ret.value = tempDouble.toString();
				}
				
			}
			else if(token == Token.MUL)
			{
				if(ret.type == "number")
				{	
					String t1, t2;
					t1 = (v1.type == "boolean") ? WH_parse_boolean(v1.value).toString() : v1.value;
					t2 = (v2.type == "boolean") ? WH_parse_boolean(v2.value).toString() : v2.value;
					
					Double tempDouble = (Double.parseDouble(t1) * Double.parseDouble(t2));
					ret.value = tempDouble.toString();
				}
				else if(ret.type == "boolean")
				{
					ret.type = "number";
					Integer tempDouble = WH_parse_boolean(v1.value) * WH_parse_boolean(v2.value);
					ret.value = tempDouble.toString();
				}
				else 
				{
					ret.value = "NaN";
					ret.type = "String";
				}
				
			}
			else if(token == Token.SUB)
			{
				if(ret.type == "String")
					ret.value = v1.value + v2.value;
				else if(ret.type == "number")
				{	
					String t1, t2;
					t1 = (v1.type == "boolean") ? WH_parse_boolean(v1.value).toString() : v1.value;
					t2 = (v2.type == "boolean") ? WH_parse_boolean(v2.value).toString() : v2.value;
					
					Double tempDouble = (Double.parseDouble(t1) - Double.parseDouble(t2));
					ret.value = tempDouble.toString();
				}
				else if(ret.type == "boolean")
				{
					ret.type = "number";
					Integer tempDouble = WH_parse_boolean(v1.value) - WH_parse_boolean(v2.value);
					ret.value = tempDouble.toString();
				}
			}
		}
		
		if(! ret.type.equals(UNDEF))
		{
			if(v1.type.equals(UNDEF))
				v1.type = v2.type;
			if(v2.type.equals(UNDEF))
				v2.type = v1.type;
			
			ret.type = type_cmp(v1.type,v2.type);
		}
		else {
			v1.type = "number";
			v2.type = "number";
			ret.type = "number";
		}
		
		return ret;
	}
	private static String type_cmp(String type1, String type2)
	{
		if(type_rank(type1) > type_rank(type2))
			return type1;
		else return type2;
	}
	private static int type_rank(String type)
	{
		if(type.equals("String"))
			return 3;
		else if(type.equals("number"))
			return 2;
		else if(type.equals("boolean"))
			return 1;
		else if(type.equals(UNDEF))
			return 0;
		
		return 0;
	}
	private static Integer WH_parse_boolean(String s)
	{
		return s.equals("true") ? 1 : 0;
	}
}
