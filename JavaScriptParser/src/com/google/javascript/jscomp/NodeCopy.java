package com.google.javascript.jscomp;

import java.util.HashMap;

import com.google.javascript.jscomp.ControlFlowGraph.Branch;
import com.google.javascript.jscomp.SymbolTable.Symbol;
import com.google.javascript.jscomp.graph.DiGraph.DiGraphNode;
import com.google.javascript.rhino.Node;

public class NodeCopy {
	
	DiGraphNode<Node,Branch> diNode;
	
	HashMap<Symbol, VarVal> inMap = new HashMap<Symbol, VarVal>();
	HashMap<Symbol, VarVal> outMap = new HashMap<Symbol, VarVal>();
	
	public void setMap(boolean in, HashMap<Symbol, VarVal> map)
	{
		if(in)
			inMap = map;
		else 
			outMap = map;
	}
	
}
