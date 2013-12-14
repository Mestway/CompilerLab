package com.google.javascript.jscomp;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import com.google.javascript.jscomp.ControlFlowGraph.Branch;
import com.google.javascript.jscomp.NodeEx.STATE;
import com.google.javascript.jscomp.SymbolTable.Reference;
import com.google.javascript.jscomp.SymbolTable.Symbol;
import com.google.javascript.jscomp.graph.DiGraph.DiGraphEdge;
import com.google.javascript.jscomp.graph.DiGraph.DiGraphNode;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

public class ConstantPropagation {
	
	private ControlFlowGraph<Node> cfg;
	
	private HashMap<DiGraphNode<Node,Branch>, NodeEx> node_map = new HashMap<DiGraphNode<Node,Branch>, NodeEx>();
	
	private HashMap<String, VarVal> init_map = new HashMap<String, VarVal>();
	
	private SymbolTable symbolTable;
	
	public void init(ControlFlowGraph<Node> _cfg, SymbolTable _symbolTable)
	{
		cfg = _cfg;
		symbolTable = _symbolTable;
		
		for(Symbol i : symbolTable.getAllSymbols())
		{
			init_map.put(i.getName(), new VarVal(i.getName()));
		}
	}
	
	public void initNodeMap(DiGraphNode<Node,Branch> node)
	{
		if(!node_map.containsKey(node))
		{	
			node_map.put(node, new NodeEx(node, init_map, init_map));
		
			for(DiGraphEdge<Node, Branch> i : node.getOutEdges())
			{
				DiGraphNode<Node, Branch> tempNode = i.getDestination();
				initNodeMap(tempNode);
			}
		}
	}
	
	public void computeMap(HashMap<String, VarVal> new_input, NodeEx node)
	{	
		
		System.out.println("*************************************************************************");
		HashMap<String, VarVal> cpMap = copyMap(node.getOutMap());
		
		if(!node.getNode().getValue().isScript())
			node.merge_in_map(new_input);
		
		//System.out.println(node.getNode().getValue().getType());
	
		switch(node.getTreeNodeType()) {
		case WH_SCRIPT:
			ScriptInit(cpMap);
			break;
		case WH_VAR:
			Var_Handler(node, node.getInMap(),cpMap);
			//System.out.println("Some Nodes: " + node.getNode().getValue());
			break;
		default:
			;
		};
		
		if(node.map_equal(false,cpMap))
		{
			System.out.println("EQUAL");
			node.setState(STATE.STABLE);
		}
		else 
		{
			node.setOutMap(cpMap);
		}
		
		node.DEBUG_MAP(false);
		
		if(!node_map.get(node.getNode()).getState().equals(STATE.STABLE) || node.getNode().getValue().isScript())
		{
			
			for(DiGraphEdge<Node, Branch> i : node.getNode().getOutEdges())
			{
				DiGraphNode<Node, Branch> tempNode = i.getDestination();
				computeMap(node.getOutMap(),node_map.get(tempNode));
			}
		}
		
	}

	private void ScriptInit(HashMap<String, VarVal> newMap) 
	{
		for(Entry<String, VarVal> i : newMap.entrySet())
		{
			i.getValue().init();
			//System.err.println(i.getKey() + " " + i.getValue().getValue());
		}
	}

	public void process()
	{
		DiGraphNode<Node,Branch> entry_node = cfg.getEntry();
		initNodeMap(entry_node);
		computeMap(init_map,node_map.get(entry_node));
	}
	
	private HashMap<String, VarVal> copyMap(HashMap<String, VarVal> src )
	{
		HashMap<String, VarVal> dst = new HashMap<String, VarVal>();
		
		for(Entry<String, VarVal> i : src.entrySet())
		{
			dst.put(new String(i.getKey()),new VarVal(i.getValue()));
		}
		
		return dst;
	}
	
	private void EMIT(Object str) {
		System.out.println(str);
	}
	
	private void Var_Handler(NodeEx node, HashMap<String, VarVal> input, HashMap<String, VarVal> newMap) 
	{
		// TODO Auto-generated method stub
		for(Node name : node.getNode().getValue().children())
		{
			VarVal var = dfs(name,input,newMap);
			if(!newMap.get(var.getName()).equals(var));
			{
				newMap.put(var.getName(), var);
			}
		}
	}

	private VarVal dfs(Node node, HashMap<String, VarVal> input,HashMap<String, VarVal> cur_map)
	{
		EMIT(node.getType());
		if(node.isName())
		{
			VarVal output = new VarVal(node.getString());
			output.setValue(input.get(node.getString()).getValue());
			
			EMIT(output);
			
			for(Node i : node.children())
			{
				VarVal temp = dfs(i,input,cur_map);
				
				output.merge(temp);
				
				EMIT(output.getValue());
			}
			
			return output;
		}
		else if(node.isNumber())
		{
			Double temp = node.getDouble();
			
			VarVal output = new VarVal(temp.toString());
			
			output.setValue(temp.toString());
			EMIT("163: " + temp);
			output.setType("number");
			
			return output;
		}
		else if(node.isString())
		{
			String temp = node.getString();
			VarVal output = new VarVal(temp);
			output.setValue(temp);
			output.setType("String");
			EMIT("174: " + temp);
			return output;
		}
		else if(node.isTrue() || node.isFalse())
		{
			Boolean temp = node.isTrue();
			VarVal output = new VarVal(temp.toString());
			output.setValue(temp.toString());
			output.setType("Boolean");
			EMIT("183: " + temp);
			return output;
		}
		else if(node.isAdd())
		{
			int count = 0;
			VarVal var1;
			VarVal var2;
			
			for(Node it : node.children())
			{
				if(count == 0)
					var1 = dfs(it,input,cur_map);
				else if(count == 1)
					var2 = dfs(it,input,cur_map);
				
				count ++;
			}
		}
		else if(node.getType() == Token.MUL)
		{
			EMIT("MUL");
			int count = 0;
			VarVal var1;
			VarVal var2;
			
			for(Node it : node.children())
			{
				if(count == 0)
					var1 = dfs(it,input,cur_map);
				else if(count == 1)
					var2 = dfs(it,input,cur_map);
				
				count ++;
			}
		}
		return null;
	}
}
