package com.google.javascript.jscomp;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import com.google.javascript.jscomp.ControlFlowGraph.Branch;
import com.google.javascript.jscomp.NodeEx.STATE;
import com.google.javascript.jscomp.SymbolTable.Symbol;
import com.google.javascript.jscomp.graph.DiGraph.DiGraphEdge;
import com.google.javascript.jscomp.graph.DiGraph.DiGraphNode;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

public class ConstantPropagation {
	
	private Boolean PrintCFG = true;
	private SourceFile src_code;
	private ControlFlowGraph<Node> cfg;
	private HashMap<DiGraphNode<Node,Branch>, NodeEx> node_map = new HashMap<DiGraphNode<Node,Branch>, NodeEx>();
	private HashMap<String, VarVal> init_map = new HashMap<String, VarVal>();
	private HashMap<String, FuncVal> functions = new HashMap<String, FuncVal>();
	private SymbolTable symbolTable;
	
	private VarVal returnVal = new VarVal("ret");
	private Boolean Start_Flag;
	
	public void process()
	{			
		DiGraphNode<Node,Branch> entry_node = cfg.getEntry();
		initNodeMap(entry_node);
		
		//while(NodeEx.map_equal(init_map, ))
		computeMap(init_map,node_map.get(entry_node));
		
		if(PrintCFG) {
			NodeEx.BOOLEAN_PRINT = true;
			traverse_cfg(cfg.getEntry());
		}
	}
	
	public void set_init_map(HashMap<String, VarVal> new_init)
	{
		init_map = copyMap(new_init);
	}
	
	public void init(ControlFlowGraph<Node> _cfg, SymbolTable _symbolTable, SourceFile src)
	{
		Start_Flag = true;
		cfg = _cfg;
		symbolTable = _symbolTable;
		src_code = src;
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
		if(node.getNode().getValue() == null)
		{	
			return;
		}
		
		if(!node.getNode().getValue().isScript())
			node.merge_in_map(new_input);
		
		HashMap<String, VarVal> cpMap = new HashMap<String, VarVal>();
		for(Entry<String, VarVal> i : node.getInMap().entrySet())
		{
			cpMap.put(i.getKey(), i.getValue());
		}
		
		switch(node.getTreeNodeType()) {
		case WH_SCRIPT:
			Process_Script_Node(cpMap, node);
			break;
		case WH_VAR:
			VAR_Handler(node, node.getInMap(),cpMap);
			break;
		case WH_EXPR_RESULT:
			EXPR_RESULT_Handler(node, node.getInMap(),cpMap);
			break;
		case WH_RETURN:
			RETURN_Handler(node, node.getInMap(), cpMap);
			break;
		case WH_FUNCTION:
			FUNCTION_Handler(node, node.getInMap(), cpMap);
			break;
		default:
			;
		};
		
		if(node.map_equal(false,cpMap))
		{
			node.setState(STATE.STABLE);
		}
		else 
		{
			Start_Flag = false;
			node.setOutMap(cpMap);
		}
		node.DEBUG_MAP(false);
		
		if(!node_map.get(node.getNode()).getState().equals(STATE.STABLE) || Start_Flag)
		{
			for(DiGraphEdge<Node, Branch> i : node.getNode().getOutEdges())
			{
				DiGraphNode<Node, Branch> tempNode = i.getDestination();
				computeMap(node.getOutMap(),node_map.get(tempNode));
			}
		}
	}

	private void FUNCTION_Handler(NodeEx node, HashMap<String, VarVal> inMap,
			HashMap<String, VarVal> cpMap) {
		// TODO Auto-generated method stub
		
		Node f_node = node.getNode().getValue();
		
		for(Node i : f_node.children())
		{
			if(i.isParamList())
			{
				for(Node j : i.children())
				{
					cpMap.get(j.getString()).setValue(VarVal.NAC);
				}
			}
		}
	}

	private void RETURN_Handler(NodeEx node, HashMap<String, VarVal> inMap,
			HashMap<String, VarVal> cpMap) {
			VarVal output = dfs(getFirstChild(node.getNode().getValue()), inMap, cpMap);
			System.out.println("T_T " + output);
			returnVal = output;
	}

	@SuppressWarnings("unused")
	private HashMap<String, VarVal> copyMap(HashMap<String, VarVal> src)
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
	
	private void VAR_Handler(NodeEx node, HashMap<String, VarVal> input, HashMap<String, VarVal> newMap) 
	{
		for(Node name : node.getNode().getValue().children())
		{
			VarVal var = dfs(name,input,newMap);
			if(!newMap.get(var.getName()).equals(var));
			{
				newMap.put(var.getName(), var);
			}
		}
	}
	
	private void Process_Script_Node(HashMap<String, VarVal> newMap, NodeEx script_node) 
	{
		for(Entry<String, VarVal> i : newMap.entrySet())
		{
			i.getValue().init();
		}
		process_function(script_node);
	}
	
	private void EXPR_RESULT_Handler(NodeEx node, HashMap<String, VarVal> input, HashMap<String, VarVal> newMap)
	{
		VarVal var = new VarVal("temp");
		for(Node child : node.getNode().getValue().children())
		{
			var = dfs(child, input, newMap);
		}
		
		if(!newMap.get(var.getName()).equals(var));
		{
			newMap.put(var.getName(), var);
		}
	}

	private VarVal dfs(Node node, HashMap<String, VarVal> input,HashMap<String, VarVal> cur_map)
	{
		if(node.isName())
		{
			VarVal output = new VarVal(node.getString());
			output.setValue(input.get(node.getString()).getValue());
			
			for(Node i : node.children())
			{
				VarVal temp = dfs(i,input,cur_map);
				output.setType(temp.getType());
				output.setValue(temp.getValue());
			}
		
			return output;
		}
		else if(node.isAssign())
		{
			int cnt = 0;
			Node left = null, right = null;
			for(Node i : node.children())
			{
				if(cnt == 0)
					left = i;
				else 
					right = i;
				cnt ++;
			}
			
			VarVal var = dfs(right,input,cur_map);
			var.setName(left.getQualifiedName());
			
			return var;
		}
		else if(node.isInc() || node.isDec())
		{
			Node child = null;
			for(Node i : node.children())
			{
				child = i;
			}
			
			VarVal var = dfs(child, input, cur_map);
			var = input.get(var.getName());
			
			VarVal tempv = new VarVal("1");
			tempv.setType("number");
			tempv.setValue("1");
			if(node.isDec())
				tempv.setValue("-1");
			
			VarVal newvar = VarVal.merge(var, tempv, Token.ADD);
			newvar.setName(var.getName());
			
			if(newvar.getType().equals("String"))
			{
				newvar.setType("String");
				newvar.setValue("NaN");
			}
			return newvar;
		}
		else if(node.isNumber())
		{
			Double temp = node.getDouble();
			VarVal output = new VarVal(temp.toString());
			output.setValue(temp.toString());
			output.setType("number");
			return output;
		}
		else if(node.isString())
		{
			String temp = node.getString();
			VarVal output = new VarVal(temp);
			output.setValue(temp);
			output.setType("String");
			return output;
		}
		else if(node.isTrue() || node.isFalse())
		{
			Boolean temp = node.isTrue();
			VarVal output = new VarVal(temp.toString());
			output.setValue(temp.toString());
			output.setType("boolean");
			return output;
		}
		else if(node.isAdd())
		{
			int count = 0;
			VarVal var1 = null;
			VarVal var2 = null;
			
			for(Node it : node.children())
			{
				if(count == 0)
					var1 = dfs(it,input,cur_map);
				else if(count == 1)
					var2 = dfs(it,input,cur_map);
				count ++;
			}
			
			VarVal output = VarVal.merge(var1, var2, Token.ADD);
			
			if(cur_map.get(var1.getName()) !=null)
				cur_map.get(var1.getName()).setType(var1.getType());
			if(cur_map.get(var2.getName()) != null)
				cur_map.get(var2.getName()).setType(var2.getType());
			
			return output;
		}
		else if(node.getType() == Token.MUL)
		{
			int count = 0;
			VarVal var1 = new VarVal("temp1");
			VarVal var2 = new VarVal("temp2");
			
			for(Node it : node.children())
			{
				if(count == 0)
					var1 = dfs(it,input,cur_map);
				else if(count == 1)
					var2 = dfs(it,input,cur_map);
				
				count ++;
			}
			
			VarVal output = VarVal.merge(var1, var2, Token.MUL);
			if(cur_map.get(var1.getName()) !=null)
				cur_map.get(var1.getName()).setType(var1.getType());
			if(cur_map.get(var2.getName()) != null)
				cur_map.get(var2.getName()).setType(var2.getType());
			return output;
		}
		else if(node.getType() == Token.SUB)
		{
			int count = 0;
			VarVal var1 = new VarVal("temp1");
			VarVal var2 = new VarVal("temp2");
			
			for(Node it : node.children())
			{
				if(count == 0)
					var1 = dfs(it,input,cur_map);
				else if(count == 1)
					var2 = dfs(it,input,cur_map);
				
				count ++;
			}
			
			VarVal output = VarVal.merge(var1, var2, Token.SUB);
			
			return output;
		}
		else if(node.isCall())
		{
			VarVal output = new VarVal("temp");
			output.setType("UNDEF");
			output.setValue("NAC");
			return output;
		}

		return null;
	}
	
	HashSet<DiGraphNode<Node,Branch>> visited_node = new HashSet<DiGraphNode<Node,Branch>>();
	
	private void traverse_cfg(DiGraphNode<Node,Branch> node)
	{
		if(node.getValue() == null)
			return;
		
		EMIT(node.getValue());
		EMIT(src_code.getLine(node.getValue().getLineno()));
		node_map.get(node).DEBUG_MAP(false);
		node_map.get(node).bug_report();
		visited_node.add(node);
		
		for(DiGraphEdge<Node, Branch> i : node.getOutEdges())
		{
			DiGraphNode<Node, Branch> tempNode = i.getDestination();
			if(!visited_node.contains(tempNode))
			{
				traverse_cfg(tempNode);
			}
		}
	}
	
	private void process_function(NodeEx script_node)
	{
		for(Node i : script_node.getNode().getValue().children())
		{
			if(i.getType() == Token.FUNCTION)
			{
				EMIT("HAHA! I got one " + getFirstChild(i).getQualifiedName());
				PrintFlowGraph.dfs(i, 1);
				functions.put(getFirstChild(i).getQualifiedName(), new FuncVal(i,symbolTable,src_code));
			
				System.out.println("WOW! " + functions.get(getFirstChild(i).getQualifiedName()));
			}
		}
	}
	
	private Node getFirstChild(Node node)
	{
		for(Node i : node.children())
		{
			return i;
		}
		return null;
	}
	
	public VarVal getRetValue()
	{
		return returnVal;
	}
	
}
