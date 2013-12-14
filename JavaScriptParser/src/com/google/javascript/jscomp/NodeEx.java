package com.google.javascript.jscomp;

import java.util.HashMap;
import java.util.Map.Entry;

import com.google.javascript.jscomp.ControlFlowGraph.Branch;
import com.google.javascript.jscomp.SymbolTable.Symbol;
import com.google.javascript.jscomp.graph.DiGraph.DiGraphNode;
import com.google.javascript.rhino.Node;

public class NodeEx {
	public enum STATE { STABLE, UNSTABLE };
	
	public enum GraphNodeType {WH_SCRIPT, WH_ADD, WH_VAR, WH_IF, WH_BLOCK, 
								WH_FOR, WH_EXPR_RESULT, WH_INC, WH_PANIC, WH_MUL, WH_NUM}; 
	
	private DiGraphNode<Node,Branch> node;
	private HashMap<String, VarVal> out_map = new HashMap<String, VarVal>();
	private HashMap<String, VarVal> in_map = new HashMap<String, VarVal>();
	private STATE state;
	
	public NodeEx(DiGraphNode<Node, Branch> nd, HashMap<String, VarVal> in_mp, HashMap<String, VarVal> out_mp)
	{
		node = nd;
		in_map = in_mp;
		out_map = out_mp;
		state = STATE.UNSTABLE;
	}
	public DiGraphNode<Node,Branch> getNode()
	{
		return node;
	}
	public void setOutMap(HashMap<String, VarVal> map)
	{
		out_map = map;
	}
	public void setInMap(HashMap<String, VarVal> map)
	{
		in_map = map;
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
			if(!m2.get(i.getKey()).getValue().equals(i.getValue().getValue()))
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
			in_map.get(i.getKey()).merge(i.getValue());
		}
		return true;
	}
	
	public void DEBUG_MAP(boolean in)
	{
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
		System.out.println("##################### MAP DEBUG BEGIN ##################");
		
		System.out.println("Map Size: " + c_map.size());
		
		for(Entry<String, VarVal> i : c_map.entrySet())
		{
			System.out.println("Key: " + i.getKey() + "  ||  " + "Value: " + i.getValue());
		}
		
		System.out.println("##################### MAP DEBUG END ####################");
	}
}
