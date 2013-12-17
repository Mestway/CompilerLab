package com.google.javascript.jscomp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import com.google.javascript.jscomp.ControlFlowGraph.Branch;
import com.google.javascript.jscomp.graph.DiGraph.DiGraphNode;
import com.google.javascript.rhino.Node;

public class NodeEx {
	public static boolean BOOLEAN_PRINT = false;
	
	public enum STATE { STABLE, UNSTABLE };
	
	public enum GraphNodeType {WH_SCRIPT, WH_ADD, WH_VAR, WH_IF, WH_BLOCK, 
								WH_FOR, WH_EXPR_RESULT, WH_INC, WH_PANIC, WH_MUL, WH_NUM
								,WH_WHILE, WH_RETURN, WH_FUNCTION}; 
	
	private DiGraphNode<Node,Branch> node;
	private HashMap<String, VarVal> out_map = new HashMap<String, VarVal>();
	private HashMap<String, VarVal> in_map = new HashMap<String, VarVal>();
	private STATE state;
	
	private ArrayList<String> bug_report = new ArrayList<String>();
	
	public NodeEx(DiGraphNode<Node, Branch> nd, HashMap<String, VarVal> in_mp, HashMap<String, VarVal> out_mp)
	{
		node = nd;
		
		for(Entry<String, VarVal> i : in_mp.entrySet())
		{
			in_map.put(i.getKey(), new VarVal(i.getValue()));
		}
		for(Entry<String, VarVal> i : out_mp.entrySet())
		{
			out_map.put(i.getKey(), i.getValue());
		}

		state = STATE.UNSTABLE;
	}
	public DiGraphNode<Node,Branch> getNode()
	{
		return node;
	}
	public void setOutMap(HashMap<String, VarVal> out_mp)
	{
		out_map.clear();
		for(Entry<String, VarVal> i : out_mp.entrySet())
		{
			out_map.put(i.getKey(), i.getValue());
		}
	}
	public void setInMap(HashMap<String, VarVal> in_mp)
	{
		in_map.clear();;
		for(Entry<String, VarVal> i : in_mp.entrySet())
		{
			in_map.put(i.getKey(), new VarVal(i.getValue()));
		}
	}
	public void setState(STATE st)
	{
		state = st;
	}
	public STATE getState()
	{
		return state;
	}
	public HashMap<String, VarVal> getOutMap()
	{
		return out_map;
	}
	public HashMap<String, VarVal> getInMap()
	{
		return in_map;
	}
	public boolean map_equal(boolean in, HashMap<String, VarVal> m2)
	{
		HashMap<String, VarVal> c_map = in ? in_map : out_map;
		
		if(c_map.size() != m2.size())
		{	
			System.err.println("SIZE NOT EQUAL");
			return false;
		}
		
		for(Entry<String, VarVal> i : c_map.entrySet())
		{
			if(!(m2.get(i.getKey()).getValue().equals(i.getValue().getValue()) && 
					m2.get(i.getKey()).getType().equals(i.getValue().getType())) )
				return false;
		}
		
		return true;
	}
	
	public static boolean map_equal(HashMap<String, VarVal> m1, HashMap<String, VarVal> m2)
	{
		if(m1.size() != m2.size())
		{	
			System.err.println("SIZE NOT EQUAL");
			return false;
		}
		
		for(Entry<String, VarVal> i : m1.entrySet())
		{
			if(!(m2.get(i.getKey()).getValue().equals(i.getValue().getValue()) && 
					m2.get(i.getKey()).getType().equals(i.getValue().getType())) )
				return false;
		}
		
		return true;
	}
	
	public GraphNodeType getTreeNodeType(){
		if(this.getNode().getValue().isAdd())
		{
			return GraphNodeType.WH_ADD;
		}
		else if(this.getNode().getValue().isScript())
		{
			return GraphNodeType.WH_SCRIPT;
		}
		else if(this.getNode().getValue().isVar())
		{
			return GraphNodeType.WH_VAR;
		}
		else if(this.getNode().getValue().isExprResult())
		{
			return GraphNodeType.WH_EXPR_RESULT;
		}
		else if(this.getNode().getValue().isWhile())
		{
			return GraphNodeType.WH_WHILE;
		}
		else if(this.getNode().getValue().isFor())
		{
			return GraphNodeType.WH_FOR;
		}
		else if(this.getNode().getValue().isBlock())
		{
			return GraphNodeType.WH_BLOCK;
		}
		else if(this.getNode().getValue().isReturn())
		{
			return GraphNodeType.WH_RETURN;
		}
		else if(this.getNode().getValue().isFunction())
		{
			return GraphNodeType.WH_FUNCTION;
		}
		else return GraphNodeType.WH_PANIC;
	}
	
	public boolean merge_in_map(HashMap<String, VarVal> anothor_in_map)
	{
		if(anothor_in_map.size() != in_map.size())
		{	
			System.err.println("Severe ERROR! WARNING!");
			return false;
		}
		
		for(Entry<String, VarVal> i : anothor_in_map.entrySet())
		{
			String ifBugs = in_map.get(i.getKey()).merge(i.getValue());
			if(! ifBugs.equals(""))
			{
				bug_report.add(i.getKey() + " " + ifBugs + " " + "@Line#" + node.getValue().getLineno());
			}
		}
		
		bug_report();
		return true;
	}
	
	public void DEBUG_MAP(boolean in)
	{
		if(!BOOLEAN_PRINT)
			return;
			
		String str = in ? "IN " : "OUT ";
		System.out.println("##################### " + str + "MAP DEBUG BEGIN ##################");
		
		HashMap<String, VarVal> c_map = in ? in_map : out_map;
		System.out.println("Map Size: " + c_map.size());
		
		for(Entry<String, VarVal> i : c_map.entrySet())
		{
			System.out.println("Key: " + i.getKey() + "  ||  " + "Value: " + i.getValue());
		}
		
		System.out.println("##################### " + str + " MAP DEBUG END ####################");
	}
	
	public static void DEBUG_MAP(HashMap<String, VarVal> c_map)
	{
		if(!BOOLEAN_PRINT)
			return;
		
		System.out.println("##################### MAP DEBUG BEGIN ##################");
		
		System.out.println("Map Size: " + c_map.size());
		
		for(Entry<String, VarVal> i : c_map.entrySet())
		{
			System.out.println("Key: " + i.getKey() + "  ||  " + "Value: " + i.getValue());
		}
		
		System.out.println("##################### MAP DEBUG END ####################");
	}
	
	public void bug_report()
	{
		for(String i : bug_report)
		{
			System.out.println("WARNING: " + i);
		}
	}
}
