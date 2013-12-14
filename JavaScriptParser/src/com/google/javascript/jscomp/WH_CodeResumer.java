package com.google.javascript.jscomp;

import java.util.HashMap;

import com.google.javascript.jscomp.ControlFlowGraph.Branch;
import com.google.javascript.jscomp.SymbolTable.Symbol;
import com.google.javascript.jscomp.graph.DiGraph.DiGraphNode;
import com.google.javascript.rhino.Node;

/**
 * To resume JavaScript code from AST
 * @author steven
 * @version 0.01
 * 
 */
public class WH_CodeResumer {
	
	private DiGraphNode<Node, Branch> root;
	private HashMap<DiGraphNode<Node,Branch>, NodeEx> node_map;
	
	/**
	 * Constructor of a new WH_CodeResumer
	 * @param root the root of the AST
	 */
	public WH_CodeResumer(DiGraphNode<Node, Branch> _root, HashMap<DiGraphNode<Node,Branch>, NodeEx> _node_map) {
		root = _root;
		node_map = _node_map;
	}
	
	/**
	 * 
	 */
	private String generate(DiGraphNode<Node, Branch> root, HashMap<Symbol, VarVal> node_value_map) {
		if (node_map.get(root) != null) {
			NodeEx ne = node_map.get(root);
		}
		String ret = "";
		
		// Script
		if (root.getValue().isScript()) {
			
		}
		for (Node i : root.getValue().children()) {
			//ret += generate
		}
		return "";
	}
	/**
	 * Make resumer to resume the code from AST
	 * @return
	 */
	public String generateCode() {
		HashMap<Symbol, VarVal> node_value_map = new HashMap<Symbol, VarVal>();
		return generate(root, node_value_map);
	}
}
