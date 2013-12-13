package com.google.javascript.jscomp;

import java.util.HashMap;
import java.util.Map.Entry;

import com.google.javascript.jscomp.ControlFlowGraph.Branch;
import com.google.javascript.jscomp.SymbolTable.Symbol;
import com.google.javascript.jscomp.graph.DiGraph.DiGraphNode;
import com.google.javascript.rhino.Node;

public class NodeEx {
	public enum STATE { STABLE, UNSTABLE };
	
	DiGraphNode<Node,Branch> node;
	HashMap<Symbol, VarVal> node_value_map = new HashMap<Symbol, VarVal>();
	STATE state;
	
	public NodeEx(DiGraphNode<Node, Branch> nd, HashMap<Symbol, VarVal> mp)
	{
		node = nd;
		node_value_map = mp;
	}
	public DiGraphNode<Node,Branch> getNode()
	{
		return node;
	}
	public void setMap(HashMap<Symbol, VarVal> map)
	{
		node_value_map = map;
	}
	public STATE getState()
	{
		return state;
	}
	public HashMap<Symbol, VarVal> getMap()
	{
		return node_value_map;
	}
	
	public boolean map_equal(HashMap<Symbol, VarVal> m2)
	{
		if(node_value_map.size() != m2.size())
		{	
			System.err.println("SIZE NOT EQUAL");
			return false;
		}
		
		for(Entry<Symbol, VarVal> i : node_value_map.entrySet())
		{
			if(!m2.get(i.getKey()).getValue().equals(i.getValue().getValue()))
				return false;
		}
		
		return true;
	}
}
