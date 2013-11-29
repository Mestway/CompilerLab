package com.google.javascript.jscomp;

import java.util.HashMap;

import com.google.javascript.jscomp.ControlFlowGraph.Branch;
import com.google.javascript.jscomp.SymbolTable.Symbol;
import com.google.javascript.jscomp.graph.DiGraph.DiGraphEdge;
import com.google.javascript.jscomp.graph.DiGraph.DiGraphNode;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.head.Token;

public class ConstantPropagation {
	
	ControlFlowGraph<Node> cfg;
	
	HashMap<DiGraphNode<Node,Branch>, NodeCopy> node_map = new HashMap<DiGraphNode<Node,Branch>, NodeCopy>();
	
	HashMap<Symbol, VarVal> map = new HashMap<Symbol, VarVal>();
	
	SymbolTable symbolTable;
	
	public void init(ControlFlowGraph<Node> _cfg, SymbolTable _symbolTable)
	{
		cfg = _cfg;
		symbolTable = _symbolTable;
		
		for(Symbol i : symbolTable.getAllSymbols())
		{
			map.put(i, new VarVal());
		}
	}
	
	public HashMap<Symbol,VarVal> computeMap(HashMap<Symbol, VarVal> inMap, DiGraphNode<Node,Branch> diNode)
	{
		if(!node_map.containsKey(diNode))
			node_map.put(diNode, new NodeCopy());
		
		HashMap<Symbol, VarVal> outMap =  new HashMap<Symbol, VarVal>();
		
		switch(diNode.getValue().getType()) {
		case Token.VAR:
			//DefinitionHandler(diNode, inMap);
			System.out.println("Some Nodes: " + diNode.getValue());
			break;
		default:
			node_map.get(diNode).setMap(false, outMap);
		};
		
		return outMap;
	}
	
	public void process()
	{
		DiGraphNode<Node,Branch> node = cfg.getEntry();
		
		if(node.getValue() != null)
		{	
			int t = 0;
			for(Node i : node.getValue().siblings())
			{
				t ++;
			}
		}
		
		node_map.add(node);
		for(DiGraphEdge<Node, Branch> i : node.getOutEdges())
		{
			emit_num ++;
			DiGraphNode<Node, Branch> tempNode = i.getDestination();
			if(!node_map.contains(tempNode))
			{
				VisitPrint(tempNode);
			}
			emit_num --;
		}
	}
	
	
	
}
