package com.google.javascript.jscomp;

import java.util.HashMap;
import java.util.Map.Entry;

import com.google.javascript.jscomp.ControlFlowGraph.Branch;
import com.google.javascript.jscomp.NodeEx.STATE;
import com.google.javascript.jscomp.SymbolTable.Symbol;
import com.google.javascript.jscomp.graph.DiGraph.DiGraphEdge;
import com.google.javascript.jscomp.graph.DiGraph.DiGraphNode;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.head.Token;

public class ConstantPropagation {
	
	ControlFlowGraph<Node> cfg;
	
	HashMap<DiGraphNode<Node,Branch>, NodeEx> node_map = new HashMap<DiGraphNode<Node,Branch>, NodeEx>();
	
	HashMap<Symbol, VarVal> init_map = new HashMap<Symbol, VarVal>();
	
	SymbolTable symbolTable;
	
	public void init(ControlFlowGraph<Node> _cfg, SymbolTable _symbolTable)
	{
		cfg = _cfg;
		symbolTable = _symbolTable;
		
		for(Symbol i : symbolTable.getAllSymbols())
		{
			init_map.put(i, new VarVal());
		}
	}
	
	public void initNodeMap(DiGraphNode<Node,Branch> node)
	{
		if(!node_map.containsKey(node))
		{	
			node_map.put(node, new NodeEx(node, init_map));
		
			for(DiGraphEdge<Node, Branch> i : node.getOutEdges())
			{
				DiGraphNode<Node, Branch> tempNode = i.getDestination();
				initNodeMap(tempNode);
			}
		}
	}
	
	public void computeMap(HashMap<Symbol, VarVal> input, NodeEx node)
	{	
		HashMap<Symbol, VarVal> newMap = copyMap(node.getMap());
		
		//TODO: continue writing processing part
		switch(node.getNode().getValue().getType()) {
		case Token.VAR:
			//DefinitionHandler(diNode, inMap);
			System.out.println("Some Nodes: " + node.getNode().getValue());
			break;
		default:
			;
		};
		
		if(node.map_equal(newMap))
		{
			node.state = STATE.STABLE;
		}
		
		if(!node_map.get(node.getNode()).getState().equals(STATE.STABLE))
		{
			for(DiGraphEdge<Node, Branch> i : node.getNode().getOutEdges())
			{
				DiGraphNode<Node, Branch> tempNode = i.getDestination();
				computeMap(node.getMap(),node_map.get(tempNode));
			}
		}
		
	}
	
	public void process()
	{
		DiGraphNode<Node,Branch> entry_node = cfg.getEntry();
		initNodeMap(entry_node);
		computeMap(init_map,node_map.get(entry_node));
	}
	
	private HashMap<Symbol, VarVal> copyMap(HashMap<Symbol, VarVal> src )
	{
		HashMap<Symbol, VarVal> dst = new HashMap<Symbol, VarVal>();
		
		for(Entry<Symbol, VarVal> i : src.entrySet())
		{
			dst.put(i.getKey(),i.getValue());
		}
		
		return dst;
	}
}
