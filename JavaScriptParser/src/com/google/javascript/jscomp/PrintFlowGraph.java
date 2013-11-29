package com.google.javascript.jscomp;

import java.util.HashSet;

import com.google.javascript.jscomp.ControlFlowGraph.Branch;
import com.google.javascript.jscomp.graph.DiGraph;
import com.google.javascript.jscomp.graph.DiGraph.DiGraphEdge;
import com.google.javascript.jscomp.graph.DiGraph.DiGraphNode;
import com.google.javascript.jscomp.graph.GraphNode;
import com.google.javascript.rhino.Node;

public class PrintFlowGraph {
	ControlFlowGraph<Node> cfg;
	
	HashSet<DiGraphNode<Node,Branch>> node_set = new HashSet<DiGraphNode<Node,Branch>>();
	int emit_num = 0;
	
	public PrintFlowGraph(ControlFlowGraph<Node> _cfg) {
		cfg = _cfg;
	}
	
	public void emit(Object str)
	{
		String blank = " ";
		for(int i = 0; i < emit_num ; i ++)
		{
			blank += " ";
		}
		System.out.println(blank + str);
	}
	
	public void emit2(Object str, int depth)
	{
		String blank = " ";
		for(int i = 0; i < depth ; i ++)
		{
			blank += " ";
		}
		System.err.println(blank + str);
	}
	
	public void RecursivePrintGraph(ControlFlowGraph<Node> cfg) {
		VisitPrint(cfg.getEntry());
	}

	public void VisitPrint(DiGraphNode<Node,Branch> node) {

		emit(node.getValue());
		dfs(node.getValue(), emit_num);
		
		if(node.getValue() != null)
		{	
			int t = 0;
			for(Node i : node.getValue().siblings())
			{
				t ++;
			}
			emit("number of siblings: " + t);
		}
		
		node_set.add(node);
		for(DiGraphEdge<Node, Branch> i : node.getOutEdges())
		{
			emit_num ++;
			DiGraphNode<Node, Branch> tempNode = i.getDestination();
			if(!node_set.contains(tempNode))
			{
				VisitPrint(tempNode);
			}
			emit_num --;
		}
	}
	
	public void dfs(Node node, int depth)
	{
		if(node == null)
			return;
		
		emit2(node + " " + node.getClass(),depth);
		for(Node i : node.children())
		{
			dfs(i,depth + 1);
		}
	}
}
