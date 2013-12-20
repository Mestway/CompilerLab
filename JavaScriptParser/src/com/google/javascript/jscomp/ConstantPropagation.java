package com.google.javascript.jscomp;

import java.util.ArrayList;
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
	
	private HashMap<String, VarVal> final_map = new HashMap<String, VarVal>();
	
	private VarVal returnVal = new VarVal("ret");
	private Boolean Start_Flag;
	@SuppressWarnings("unused")
	private Boolean REWRITE = false;
	
	private Boolean R2 = true;
	
	public ControlFlowGraph<Node> getCFG()
	{
		return cfg;
	}
	
	public void process()
	{			
		DiGraphNode<Node,Branch> entry_node = cfg.getEntry();

		while(!NodeEx.map_equal(init_map, final_map))
		{
			init_map = copyMap(final_map);
			initNodeMap(entry_node);
			Start_Flag = true;
			computeMap(init_map,node_map.get(entry_node));
			for(Entry<String, VarVal> i : final_map.entrySet())
			{
				i.getValue().setValue(VarVal.UNDEF);
			}
			if(!entry_node.getValue().isFunction())
				break;
		}
		if(PrintCFG) {
			REWRITE = true;
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
			//init_map.put(i.getName(), new VarVal(i.getName()));
			final_map.put(i.getName(), new VarVal(i.getName()));
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
			NodeEx.merge_maps(final_map, new_input);
			return;
		}
		
		if(!node.getNode().getValue().isScript())
			node.merge_in_map(new_input);
		
		HashMap<String, VarVal> cpMap = new HashMap<String, VarVal>();
		for(Entry<String, VarVal> i : node.getInMap().entrySet())
		{
			cpMap.put(i.getKey(), new VarVal(i.getValue()));
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
		case WH_IF:
			IF_Handler(node, node.getInMap(), cpMap);
			break;
		case WH_WHILE:
			WHILE_Handler(node, node.getInMap(), cpMap);
			break;
		case WH_FOR:
			//FOR_Handler(node, node.getInMap(), cpMap);
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
		//node.DEBUG_MAP(false);
		
		if(!node_map.get(node.getNode()).getState().equals(STATE.STABLE) || Start_Flag)
		{
			for(DiGraphEdge<Node, Branch> i : node.getNode().getOutEdges())
			{
				DiGraphNode<Node, Branch> tempNode = i.getDestination();
				computeMap(node.getOutMap(),node_map.get(tempNode));
			}
		}
	}

	@SuppressWarnings("unused")
	private void FOR_Handler(NodeEx node, HashMap<String, VarVal> inMap,
			HashMap<String, VarVal> cpMap) {

		for(Node child : node.getNode().getValue().children())
		{
			if(child.getType() == Token.LT || child.getType() == Token.LE ||
				child.getType() == Token.GT || child.getType() == Token.GE ||
				child.getType() == Token.EQ || child.getType() == Token.NE)
			{
				node.bug_j.clear();
				VarVal output = dfs(child, inMap, cpMap, false);
				if(output.isConst())
				{
					node.bug_j.add("A not-taken branch at line " + node.getNode().getValue().getLineno());
				}
			}
		}
	}

	private void WHILE_Handler(NodeEx node, HashMap<String, VarVal> inMap,
			HashMap<String, VarVal> cpMap) 
	{
		Node child = getFirstChild(node.getNode().getValue());
		node.bug_j.clear();
		VarVal output = dfs(child, inMap, cpMap, false);
		if(output.isConst())
		{
			node.bug_j.add("A not-taken branch at line " + node.getNode().getValue().getLineno());
		}
		
	}

	private void IF_Handler(NodeEx node, HashMap<String, VarVal> inMap,
			HashMap<String, VarVal> cpMap) {
		Node child = getFirstChild(node.getNode().getValue());
		node.bug_j.clear();
		VarVal output = dfs(child, inMap, cpMap, false);
		if(output.isConst())
		{
			node.bug_j.add("A not-taken branch at line " + node.getNode().getValue().getLineno());
		}
	}

	private void FUNCTION_Handler(NodeEx node, HashMap<String, VarVal> inMap,
			HashMap<String, VarVal> cpMap) {
		
		process_function(node.getNode().getValue().getLastChild());
		
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
			VarVal output = dfs(getFirstChild(node.getNode().getValue()), inMap, cpMap, false);
			returnVal = new VarVal(output);
	}

	private HashMap<String, VarVal> copyMap(HashMap<String, VarVal> src)
	{
		HashMap<String, VarVal> dst = new HashMap<String, VarVal>();
		
		for(Entry<String, VarVal> i : src.entrySet())
		{
			dst.put(new String(i.getKey()),new VarVal(i.getValue()));
		}
		
		return dst;
	}
	
	@SuppressWarnings("unused")
	private void EMIT(Object str) {
		System.out.println(str);
	}
	
	private void VAR_Handler(NodeEx node, HashMap<String, VarVal> input, HashMap<String, VarVal> newMap) 
	{
		for(Node name : node.getNode().getValue().children())
		{
			VarVal var = dfs(name,input,newMap, false);
			if(!newMap.get(var.getName()).equals(var));
			{
				if(var.getType().equals(VarVal.UNDEF))
					var.setType(newMap.get(var.getName()).getType());
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
			var = dfs(child, input, newMap, false);
		}
		
		if(newMap.get(var.getName()) != null)
		{
			if(!newMap.get(var.getName()).equals(var));
			{
				newMap.put(var.getName(), var);
			}
		}
	}

	private VarVal dfs(Node node, HashMap<String, VarVal> input,HashMap<String, VarVal> cur_map, Boolean rewrite)
	{
		if(node.isName())
		{
			VarVal output = new VarVal(node.getString());
			output.setValue(input.get(node.getString()).getValue());
			
			for(Node i : node.children())
			{
				VarVal temp = dfs(i,input,cur_map, false);
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
			
			VarVal var = dfs(right,input,cur_map, false);
			
			if(cur_map.get(var.getName()) != null)
			{
				if(cur_map.get(var.getName()).getType().equals(VarVal.UNDEF))
					cur_map.get(var.getName()).setType(cur_map.get(left.getQualifiedName()).getType());
			}
			
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
			
			VarVal var = dfs(child, input, cur_map, false);
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
					var1 = dfs(it,input,cur_map, false);
				else if(count == 1)
					var2 = dfs(it,input,cur_map, false);
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
					var1 = dfs(it,input,cur_map, false);
				else if(count == 1)
					var2 = dfs(it,input,cur_map, false);
				
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
					var1 = dfs(it,input,cur_map, false);
				else if(count == 1)
					var2 = dfs(it,input,cur_map, false);
				
				count ++;
			}
			
			VarVal output = VarVal.merge(var1, var2, Token.SUB);
			
			return output;
		}
		else if(node.getType() == Token.LT || node.getType() == Token.GT ||
				node.getType() == Token.EQ || node.getType() == Token.NE ||
				node.getType() == Token.LE || node.getType() == Token.GE)
		{
			VarVal output = new VarVal("#$#$#$ret");
			output.setType("boolean");
			output.setValue(VarVal.NAC);
			Node left = getAnyChild(node, 1);
			Node right = getAnyChild(node, 2);
			
			VarVal lv = dfs(left, input, cur_map, false);
			VarVal rv = dfs(right, input, cur_map, false);
			
			if(lv.getValue().equals(VarVal.UNDEF) || rv.getValue().equals(VarVal.UNDEF))
			{
				ERROR("Compare value not initialized at line " + node.getLineno());
			}
			else if(lv.isConst() && rv.isConst())
			{
				Double lft ,rgt;
				try{
					lft = new Double(lv.getValue());
					rgt = new Double(rv.getValue());
					
					switch(node.getType())
					{
					case Token.LT:
						output.setValue(new Boolean(lft < rgt).toString());
						break;
					case Token.LE:
						output.setValue(new Boolean(lft <= rgt).toString());
						break;
					case Token.GT:
						output.setValue(new Boolean(lft > rgt).toString());
						break;
					case Token.GE:
						output.setValue(new Boolean(lft >= rgt).toString());
						break;
					case Token.EQ:
						output.setValue(new Boolean(lft == rgt).toString());
						break;
					case Token.NE:
						output.setValue(new Boolean(lft != rgt).toString());
						break;
					}
				}catch(Exception e){
					
				}
			}
			
			if(cur_map.get(lv.getName()) != null)
			{
				VarVal tv = new VarVal("tp");
				tv.setType("number");
				tv.setValue(VarVal.UNDEF);
				cur_map.get(lv.getName()).merge(tv);
			}
			if(cur_map.get(rv.getName()) != null)
			{
				VarVal tv = new VarVal("tp");
				tv.setType("number");
				tv.setValue(VarVal.UNDEF);
				cur_map.get(rv.getName()).merge(tv);
			}
			
			return output;
		}
		else if(node.isCall())
		{
			VarVal output = new VarVal("temp");
			
			String functionName = getFirstChild(node).getString();
			
			output.setType(functions.get(getFirstChild(node).getString()).getRetType());
			output.setValue(functions.get(getFirstChild(node).getString()).getRetValue());
			
			ArrayList<VarVal> argus = new ArrayList<VarVal>();
			
			int cnt = 0;
			for(Node vars : node.children())
			{
				cnt ++;
				if(cnt == 1)
					continue;
				
				switch(vars.getType())
				{
				case Token.NAME:
					if(input.get(vars.getQualifiedName()) != null)
					{
						argus.add(new VarVal(input.get(vars.getQualifiedName())));
					}
					else
					{
						ERROR("Argument not existed at line " + node.getLineno());
					}
					break;
				case Token.STRING:
					VarVal Stringv = new VarVal("");
					Stringv.setType("String");
					Stringv.setValue(vars.getString());
					argus.add(Stringv);
					break;
				case Token.NUMBER:
					VarVal Numberv = new VarVal("");
					Numberv.setType("number");
					Numberv.setValue(vars.getQualifiedName());
					argus.add(Numberv);
					break;
				default:		
				}	
			}
			if(argus.size() != functions.get(functionName).getArgus().size())
			{
				ERROR("Arguments size does not match at line " + node.getLineno());
			}
			else
			{
				for(int i = 0; i < argus.size(); i ++)
				{
					if(type_cmp(argus.get(i).getType(), functions.get(functionName).getArgus().get(i).getType()))
					{
						WARNING("Type may not match at line " + node.getLineno());
					}
				}
			}
			return output;
		}
		else if(node.isNew())
		{
			VarVal output = new VarVal("#newv");
			output.setType(VarVal.COMPLEX);
			output.setValue(VarVal.NAC);
			
			for(Node child : node.children())
			{
				dfs(child, input, cur_map, false);
			}
			return output;
		}
		else if(node.isGetElem())
		{
			VarVal output = new VarVal("#newv");
			output.setType(VarVal.UNDEF);
			output.setValue(VarVal.NAC);
			
			for(Node child : node.children())
			{
				dfs(child, input, cur_map, false);
			}
			return output;
		}
		else if(node.isGetProp())
		{
			VarVal output = new VarVal("#PROP");
			output.setType(VarVal.UNDEF);
			output.setValue(VarVal.NAC);
			
			VarVal name = dfs(node.getFirstChild(), input, cur_map, false);
			cur_map.get(name.getName()).setType(VarVal.COMPLEX);
			cur_map.get(name.getName()).setValue(VarVal.NAC);
			
			return output;
		}
		
		return null;
	}
	
	HashSet<DiGraphNode<Node,Branch>> visited_node = new HashSet<DiGraphNode<Node,Branch>>();
	
	private void traverse_cfg(DiGraphNode<Node,Branch> node)
	{
		if(node.getValue() == null)
			return;
		
		//EMIT(node.getValue());
		//EMIT(src_code.getLine(node.getValue().getLineno()));
		//node_map.get(node).DEBUG_MAP(true);
		//node_map.get(node).DEBUG_MAP(false);
		node_map.get(node).bug_j_report();
		node_map.get(node).bug_report();
		visited_node.add(node);
		
		dfsChange(node.getValue(), node_map.get(node).getInMap(), node_map.get(node).getOutMap(), false);
		
		for(DiGraphEdge<Node, Branch> i : node.getOutEdges())
		{
			DiGraphNode<Node, Branch> tempNode = i.getDestination();
			if(!visited_node.contains(tempNode))
			{
				traverse_cfg(tempNode);
			}
		}
	}
	
	private void dfsChange(Node node,
			HashMap<String, VarVal> inMap, HashMap<String, VarVal> outMap, boolean b) {
		
		if(node.isBlock() || node.isScript())
		{
			return;
		}
		
		if(node.isAssign())
		{
			dfsChange(node.getFirstChild(), inMap, outMap, false);
			if(R2)
			{
				if(node.getFirstChild().getType() == Token.NAME && outMap.get(node.getFirstChild().getString()).isConst())
				{
					if(!node.getFirstChild().equals(node.getLastChild()))
						node.removeChild(node.getLastChild());
					if(node.getFirstChild() != null) {
						
						Node nd;
						String st = outMap.get(node.getFirstChild().getString()).getValue();
						if(outMap.get(node.getFirstChild().getString()).getType().equals("String"))
							nd = Node.newString(st);
						else
						{
							nd = Node.newString("nb#" + st);
						}
						//nd.setString(outMap.get(node.getFirstChild().getString()).getValue());
						node.addChildToBack(nd);
					}
				}
				else 
					dfsChange(node.getLastChild(), inMap, outMap, true);
			}
			else 
				dfsChange(node.getLastChild(), inMap, outMap, true);
		}
		if(node.isName())
		{
			if(b)
			{
				VarVal v = inMap.get(node.getString());
				if(v != null && v.isConst())
				{
					if(v.getType().equals("String"))
						node.setString(v.getValue());
					else {
						node.setString("nb#" + v.getValue());
					}
				}
			}
		}
		else {
			for(Node i : node.children())
			{
				dfsChange(i, inMap,outMap, b);
			}
		}
	}

	private void process_function(NodeEx script_node)
	{
		for(Node i : script_node.getNode().getValue().children())
		{
			if(i.getType() == Token.FUNCTION)
			{
				PrintFlowGraph.dfs(i, 1);
				functions.put(getFirstChild(i).getQualifiedName(), new FuncVal(i,symbolTable,src_code));
			}
		}
	}
	
	/**
	 * Pre-process functions
	 * @param nd The start node of a Flow
	 */
	private void process_function(Node nd)
	{
		for(Node i : nd.children())
		{
			if(i.getType() == Token.FUNCTION)
			{
				PrintFlowGraph.dfs(i, 1);
				functions.put(getFirstChild(i).getQualifiedName(), new FuncVal(i,symbolTable,src_code));
			}
		}
	}
	
	/* return the first child of a node */
	private Node getFirstChild(Node node)
	{
		for(Node i : node.children())
		{
			return i;
		}
		return null;
	}
	
	/* start by 1, the last one is size()*/
	private Node getAnyChild(Node node, int n)
	{
		Node ret = null;
		int count = 1;
		for(Node i : node.children())
		{
			if(count == n)
			{
				ret = i;
				break;
			}
			count ++;
		}
		return ret;
	}
	
	/* If it is a function, getRetValue() to check the return type.
	 * Of no use if in a normal parse.
	 * */ 
	public VarVal getRetValue()
	{
		return returnVal;
	}
	
	/* generate Error */
	public void ERROR(Object st)
	{
		System.out.println("ERROR: " + st);
	}
	public void WARNING(Object st)
	{
		System.out.println("WARNING: " + st);
	}
	
	private static boolean type_cmp(String type1, String type2)
	{
		return (type_rank(type1) > type_rank(type2));
	}
	private static int type_rank(String type)
	{
		if(type.equals("String"))
			return 3;
		else if(type.equals("number"))
			return 2;
		else if(type.equals("boolean"))
			return 1;
		else if(type.equals(VarVal.UNDEF))
			return 0;
		
		return 0;
	}
}